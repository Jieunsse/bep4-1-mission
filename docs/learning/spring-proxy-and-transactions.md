# Spring Proxy와 트랜잭션

## 다룬 질문

- 프록시란 무엇인가?
- 왜 대리 객체가 필요한가?
- 프록시가 실제 호출 전에 하는 작업은 무엇인가?
- 프록시는 상수와 같은 역할인가?
- 역할에 따라 여러 종류의 프록시가 있는가?
- 현재 코드베이스에서 실제로 작동하는 프록시는 무엇인가?
- 면접에서 프록시를 어떻게 설명하면 되는가?
- `Proxy`라는 영단어의 뜻과 어원은 무엇인가?

## 현재 이해한 내용

```text
호출자
  → 프록시
      → 실제 객체
```

- 프록시는 실제 객체 앞에서 호출을 대신 받아주는 대리 객체다.
- 트랜잭션, 권한, 로그, 캐시, 비동기 처리처럼 여러 곳에서 반복되는 공통 기능을 대신 처리한다.
- 프록시는 상수가 아니라, 메서드 호출을 가로채 공통 작업을 적용하는 객체다.
- Spring에서는 `@Transactional`이 대표적인 프록시 적용 사례다.
- 현재 코드베이스에는 `@Transactional` 프록시, Spring Data Repository 프록시, `@Lazy`와 관련된 지연 프록시가 있다.
- `@TransactionalEventListener`는 이벤트 수신 등록 기능이고, 함께 붙은 `@Transactional`이 트랜잭션 프록시를 적용한다.

## 특히 주의할 부분

```java
this.someMethod();
```

- 객체 내부에서 `this`로 호출하면 Spring 프록시를 거치지 않을 수 있다.
- 외부에서 Spring Bean을 호출하거나 프록시가 주입된 `self`를 통해 호출해야 `@Transactional` 같은 기능이 적용된다.

## `@Transactional`과 `@TransactionalEventListener`

## 가장 쉬운 비교

```text
@Transactional
→ 이 작업을 하나의 거래로 처리해

@TransactionalEventListener
→ 특정 이벤트가 발생하면, 거래의 특정 시점에 이 작업을 실행해
```

- `@Transactional`은 메서드 실행의 트랜잭션 범위를 설정한다.
- `@TransactionalEventListener`는 이벤트를 받는 시점을 트랜잭션과 연결한다.
- `@TransactionalEventListener` 자체는 이벤트를 발행하지 않고, 이벤트를 받아 실행할 타이밍을 정한다.
- 어떤 이벤트를 받을지는 `handle(MemberJoinedEvent event)`의 매개변수 타입으로 판단한다.
- 어떤 트랜잭션을 기다릴지는 별도로 지정하는 것이 아니라, 해당 이벤트가 발행된 당시의 현재 트랜잭션에 연결된다.
- 현재 코드의 `AFTER_COMMIT`은 원래 트랜잭션이 성공적으로 커밋된 뒤 리스너를 실행한다는 뜻이다.
- 리스너에 함께 붙은 `@Transactional(propagation = REQUIRES_NEW)`는 리스너 작업을 별도의 새 트랜잭션으로 실행한다는 뜻이다.
- 따라서 현재 코드에서는 이벤트가 커밋 후 전달된 다음, `handle()` 메서드 본문이 새 트랜잭션 안에서 실행된다.
- 두 어노테이션은 같은 일을 중복하는 것이 아니다. 하나는 이벤트 처리 시점, 다른 하나는 리스너 내부 DB 작업의 트랜잭션 범위를 담당한다.

```text
회원 저장 트랜잭션
  → 커밋 성공
      → MemberJoinedEvent 리스너 실행
    → 새 트랜잭션에서 복제 회원 저장
```

비유하면 다음과 같다.

```text
@TransactionalEventListener
→ 원본 거래가 성공했다는 연락이 오면 그때 처리해

@Transactional
→ 네가 처리할 후속 작업도 하나의 거래로 묶어
```

둘 중 하나만 있으면 역할이 부족하다.

```text
@Transactional만 있음
→ 트랜잭션은 있지만 이벤트가 왔을 때 자동 실행할 리스너로 등록되지 않음

@TransactionalEventListener만 있음
→ 이벤트 시점은 정하지만 리스너 내부 저장 작업을 명확한 새 트랜잭션으로 묶지 않음
```

```java
handle(MemberJoinedEvent event)
```

위 메서드는 `MemberJoinedEvent`를 받는 리스너로 등록된다. `MemberJoinUseCase`가 회원가입 트랜잭션 안에서 이 이벤트를 발행하면, Spring은 그 이벤트를 해당 트랜잭션에 연결해 보관했다가 커밋 후 리스너를 호출한다. 이벤트가 트랜잭션 밖에서 발행되면 기본적으로 처리할 트랜잭션 시점이 없으므로 실행되지 않는다.
