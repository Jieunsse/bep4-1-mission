# 아키텍처와 이벤트 흐름

## `app / domain / in / out` 패키지 흐름

```text
외부 요청 또는 이벤트
    ↓
in
    ↓
app
    ↓
domain 규칙 적용
    ↓
out
    ↓
DB 또는 외부 시스템
```

- `in`: 애플리케이션 안으로 들어오는 입구다. Controller, EventListener, 초기 데이터 생성 코드가 해당한다.
- `app`: 애플리케이션 동작을 조합한다. Facade, UseCase, Support가 위치하며, 업무 흐름과 조회·저장 호출을 담당한다.
- `domain`: 핵심 업무 객체와 규칙이다. Entity, Policy, Value Object 등이 위치한다.
- `out`: 애플리케이션 바깥으로 나가는 통로다. Repository나 외부 API Client 같은 인터페이스가 위치한다.

### `app` 내부의 역할 구분

현재 구조에서는 `app`을 다음처럼 나눈다.

```text
외부 요청 또는 이벤트
    ↓
Facade   ← 앱의 진입점
    ├─ 무거운 기능 → UseCase
    └─ 가벼운 기능 → Support
```

- **Facade**는 Controller나 EventListener 같은 외부 입구에서 들어온 요청을 받는 애플리케이션의 진입점이다. 적절한 UseCase 또는 Support로 연결한다.
- **UseCase**는 회원가입·동기화·지갑 생성처럼 여러 객체와 Repository, 이벤트를 조합하는 무거운 업무 기능을 처리한다.
- **Support**는 단순 조회나 재사용 가능한 가벼운 애플리케이션 기능을 처리한다.

따라서 Facade가 업무 로직을 직접 무겁게 처리하기보다, 기능의 복잡도에 따라 UseCase와 Support에 위임한다.

## `domain` 안에 Entity를 두는 이유

- Entity는 단순한 DB 테이블 모양이 아니라 서비스의 핵심 업무 대상을 표현한다.
- 회원, 게시글, 지갑처럼 비즈니스에서 의미 있는 객체를 한 영역 안에 모아 응집도를 높인다.
- Entity와 관련된 상태·행동·규칙을 가까이 두어 핵심 로직을 찾기 쉽게 한다.
- JPA 어노테이션이 붙어 있어 DB 테이블과 연결되지만, 역할의 중심은 DB보다 비즈니스 모델에 있다.
- `MemberPolicy`처럼 Entity와 관련된 업무 규칙도 domain에 둘 수 있다.

예를 들어 `Wallet`은 단순히 잔액 컬럼을 가진 테이블이 아니라, 잔액을 충전하고 차감하는 규칙을 가진 업무 객체다.

## Domain 로직과 UseCase 로직을 나누는 기준

둘 다 비즈니스 로직이므로, 단순히 "비즈니스 로직인가"로 구분하지 않는다. **그 규칙의 주인이 누구인지**와 **여러 객체·외부 시스템을 조정하는지**를 기준으로 나눈다.

```text
Entity 하나의 상태와 불변식을 지키는 로직
    → domain

여러 Entity, Repository, 이벤트, 외부 시스템을 순서대로 조정하는 로직
    → app의 UseCase
```

현재 `Wallet` 기준:

```java
wallet.credit(amount, eventType);
wallet.debit(amount, eventType);
wallet.hasBalance();
```

- 잔액을 변경하고 CashLog를 추가하는 것은 Wallet의 상태를 함께 관리하는 규칙이다.
- 따라서 Wallet이 직접 처리하는 것이 자연스럽다.
- UseCase가 `wallet.balance += amount`처럼 내부 상태를 직접 변경하면 Entity의 규칙이 흩어진다.

현재 `CashCreateWalletUseCase`와 `CashSyncMemberUseCase` 기준:

- Repository에서 회원을 조회한다.
- Entity를 생성하고 저장한다.
- 신규 회원인지 확인한다.
- 저장 후 이벤트를 발행한다.

이처럼 저장·조회·이벤트·여러 객체의 순서를 조정하는 작업은 Entity 하나의 책임이 아니므로 UseCase가 담당한다.

판단 질문:

```text
"Wallet이 스스로 해야 하는 행동인가?"
    → domain Entity 메서드

"회원 동기화라는 시스템 업무를 실행하는가?"
    → app UseCase
```

## JPA 연관관계와 Java 컬렉션 복습

`Wallet`의 관계는 다음과 같다.

```text
CashMember 1명
    ↑
    │ @ManyToOne
Wallet 여러 개 가능

Wallet 1개
    ↓ @OneToMany
CashLog 여러 개
```

- `@ManyToOne`: 여러 Wallet이 하나의 CashMember를 바라볼 수 있도록 매핑한다.
- 다만 현재 코드에는 Wallet 종류가 따로 없고, `Wallet`의 id를 `holder.getId()`로 초기화하며 `findByHolder()`도 `Optional<Wallet>`을 반환한다. 실제 의도는 회원당 Wallet 하나에 가까워 보이며, 현재 `@ManyToOne`은 DB 관계를 느슨하게 표현한 것일 수 있다.
- 업무상으로는 1:1이어도 `@OneToOne(fetch = LAZY)`의 지연 로딩이 JPA 구현체에서 기대대로 동작하지 않을 수 있어 `@ManyToOne(fetch = LAZY)`로 지연 로딩을 얻는 우회가 있다.
- 이 경우 `@ManyToOne`은 여러 개를 허용하는 매핑이므로, 회원당 하나라는 규칙은 `unique` 제약이나 애플리케이션 규칙으로 별도 보장해야 한다.
- `@OneToMany(mappedBy = "wallet")`: 하나의 Wallet이 여러 CashLog를 가진다는 뜻이다.
- `mappedBy = "wallet"`은 관계의 주인이 `CashLog.wallet`이라는 뜻이다. 실제 외래키를 관리하는 쪽은 CashLog다.
- `fetch = LAZY`는 연관 객체를 처음부터 조회하지 않고 실제로 사용할 때 조회하도록 한다.
- `cascade = PERSIST`는 Wallet을 저장할 때 새 CashLog도 함께 저장한다.
- `cascade = REMOVE`는 Wallet을 삭제할 때 연결된 CashLog도 함께 삭제한다.
- `orphanRemoval = true`는 Wallet의 목록에서 CashLog를 제거했을 때 고아가 된 CashLog도 삭제한다.

## Cascade의 의미

Cascade는 Java 상속의 부모·자식이 아니라, JPA 연관관계에서 **주인 객체와 연관 객체의 생명주기 작업을 함께 처리하는 설정**이다.

```java
cascade = {
    CascadeType.PERSIST,
    CascadeType.REMOVE
}
```

- `PERSIST`: Wallet을 영속화할 때 새 CashLog도 함께 영속화한다.
- `REMOVE`: Wallet을 삭제할 때 연결된 CashLog도 함께 삭제한다.
- `MERGE`: 부모를 merge할 때 자식도 merge한다.
- `REFRESH`: 부모를 DB 기준으로 refresh할 때 자식도 refresh한다.
- `DETACH`: 부모를 영속성 컨텍스트에서 분리할 때 자식도 분리한다.
- `ALL`: 위 작업을 모두 전파한다.

현재 코드는 `PERSIST`와 `REMOVE`만 지정했으므로 모든 작업을 전파하는 것이 아니다. `orphanRemoval = true`는 Cascade와 별개의 설정으로, 컬렉션에서 제거된 자식 데이터를 DB에서도 삭제하는 기능이다.

`Repository.save()`와 `CascadeType.PERSIST`의 역할은 다르다.

```text
walletRepository.save(wallet)
    → Wallet 저장을 요청

cascade = PERSIST
    → Wallet 저장 작업을 연결된 새 CashLog에도 전파
```

따라서 `PERSIST`가 없으면 Wallet만 저장되고 CashLog는 자동으로 저장되지 않을 수 있다. 새 CashLog가 저장되지 않거나, 저장되지 않은 객체를 참조한다는 JPA 오류가 발생할 수 있다. `REMOVE`도 같은 원리로 Wallet 삭제 작업을 CashLog까지 전파한다.

```java
public Wallet(CashMember holder) {
    super(holder.getId());
    this.holder = holder;
}
```

- `super(holder.getId())`는 부모 클래스인 `BaseIdAndTimeManual`의 생성자를 호출하고, 전달된 id를 부모의 id 필드에 대입한다.
- 부모 생성자를 호출한다고 id가 자동으로 정해지는 것은 아니며, 부모 생성자 안에 `this.id = id` 코드가 있기 때문에 초기화된다.
- 부모가 관리하는 `id`를 초기화하고, `this.holder = holder`는 현재 Wallet의 회원을 설정한다.
- `super(...)`는 자식 생성자의 첫 줄에서 호출해야 한다.
- Java는 자식 객체를 완성하기 전에 부모 객체 부분을 먼저 초기화하도록 보장하기 때문에 `super(...)`가 첫 줄이어야 한다.

## `holder.getId()`의 출처

```java
public Wallet(CashMember holder) {
    super(holder.getId());
    this.holder = holder;
}
```

- `holder`는 메서드가 아니라 `CashMember` 타입의 생성자 매개변수다.
- `.`은 holder 객체의 메서드나 필드에 접근한다는 뜻이다.
- `CashMember`는 `ReplicaMember`를 상속하고, `ReplicaMember`의 `id` 필드에 Lombok `@Getter`가 붙어 있어 `getId()`가 생성된다.
- 또한 `BaseEntity`에는 `getId()` 추상 메서드가 선언되어 있어 자식 클래스가 이를 구현하는 구조다.

```text
CashMember
  → ReplicaMember
      → BaseMember / BaseEntity
          → getId() 사용 가능
```

따라서 `holder.getId()`는 Wallet에 있는 메서드를 호출하는 것이 아니라, 전달받은 CashMember 객체에서 상속받은 getter를 호출하는 것이다.

```java
private List<CashLog> cashLogs = new ArrayList<>();
```

- `List<CashLog>`는 CashLog 목록을 다루기 위한 인터페이스다.
- `ArrayList`는 내부적으로 배열을 사용하지만, 필요할 때 더 큰 배열로 교체해 크기를 자동으로 늘리는 가변 배열 기반의 `List` 구현체다.
- 배열은 보통 크기가 고정되지만 ArrayList는 `add`, `remove` 등을 사용할 수 있다.
- `new ArrayList<>()`의 `<>`는 왼쪽의 `CashLog` 타입을 추론한다.
- 이 컬렉션은 `addCashLog()`에서 로그를 추가해야 하므로 처음부터 빈 목록으로 초기화한다.

## `CashLog`를 별도로 저장하는 이유

`CashLog`는 서버 콘솔에 남기는 기술 로그가 아니라, 잔액 변동 이력을 저장하는 도메인 기록이다.

```text
Wallet.balance
    → 현재 잔액을 빠르게 확인

CashLog
    → 언제, 어떤 사유로, 얼마가 변했는지 기록
```

- 현재 잔액과 거래 이력을 분리하면 잔액 조회와 이력 조회를 각각 쉽게 처리할 수 있다.
- 충전·차감 내역을 확인하고, 오류를 추적하고, 정산·감사·분쟁에 사용할 수 있다.
- `CashLog`에는 `eventType`, `amount`, `balance`, 관련 대상 정보가 저장된다.
- `CashLog`는 `@Entity`이고 `CASH_CASH_LOG` 테이블로 매핑되므로 DB에 저장된다.
- Wallet의 `cashLogs`에 추가된 새 로그는 `cascade = PERSIST` 설정에 따라 Wallet 저장 과정에서 함께 저장된다.
- 이는 `@Slf4j`로 남기는 서버 실행 로그와는 다른 종류의 기록이다.

## 원본 회원과 영역별 회원 복제본

```text
Member
  ├─ CashMember
  ├─ PostMember
  └─ MarketMember
```

- 기본 회원 정보가 일부 중복 저장되는 것은 의도된 구조다.
- 각 bounded context가 회원 정보를 자기 영역에서 독립적으로 사용하고, 자기 기능과 함께 관리하기 위해 복제한다.
- Cash는 `CashMember`와 Wallet·CashLog를 함께 다루고, Post는 게시글 작성자·댓글 작성자를 다루며, Market은 상품·장바구니 등의 기능과 회원을 연결한다.
- 현재 예제는 하나의 DB 안에 여러 테이블을 사용하지만, 서비스가 분리되면 각 영역이 별도 DB를 가질 수도 있다.
- 이 방식은 영역 간 결합도를 낮추는 대신 데이터 중복, 동기화 지연, 이벤트 실패 재처리 같은 비용이 생긴다.
- 모든 영역이 항상 같은 회원 데이터만 필요하다면 원본 회원 테이블 하나를 직접 사용하는 편이 더 단순하다. 영역별 정책·확장·독립성이 필요할 때 복제 구조가 의미가 있다.

## Wallet과 CashLog의 일관성·동시성

- `Wallet.balance`와 `CashLog`는 한 번의 충전·차감 작업 안에서 함께 변경되어야 한다.
- 이것은 두 데이터가 서로 맞아야 한다는 **일관성**의 문제다.
- 보통 하나의 트랜잭션에서 잔액 변경과 로그 저장을 함께 처리해 하나만 성공하는 상황을 막는다.
- 여러 요청이 동시에 같은 Wallet을 수정하는 것은 **동시성**의 문제이며, Cascade만으로는 해결되지 않는다.
- 현재 코드에는 `@Version`이나 비관적 락이 보이지 않으므로, 동시에 충전·차감이 발생하면 별도의 동시성 제어가 필요할 수 있다.

```text
한 번의 작업
  → balance 변경
  → CashLog 추가
  → 함께 커밋
```

```text
동시에 여러 작업
  → 낙관적 락(@Version), 비관적 락, 원자적 DB 갱신 등을 검토
```

## Cascade와 이벤트의 차이

둘 다 한 작업이 다른 작업으로 이어질 수 있지만 같은 기능은 아니다.

```text
Cascade
  → JPA가 부모 Entity의 저장·삭제를 연관 Entity에 즉시 전파

Event
  → EventPublisher가 이벤트를 발행하고 Listener가 후속 작업을 수행
```

- Cascade는 JPA 연관관계 안에서 동작하며, 저장·삭제 전파가 목적이다.
- 이벤트는 서로 다른 기능이나 bounded context를 연결하고 후속 업무를 알리는 것이 목적이다.
- Cascade는 직접 연결된 부모·자식 관계이고, 이벤트는 발행자와 리스너를 느슨하게 연결한다.
- Cascade는 이벤트처럼 `AFTER_COMMIT`이나 별도 리스너를 거치지 않는다.

## 오케스트레이션과 코레오그래피

둘 다 여러 작업을 연결하는 방식이지만, **전체 순서를 누가 알고 지시하는지**가 다르다.

```text
오케스트레이션(Orchestration)
  → 중앙 조정자가 다음 작업과 순서를 직접 호출

코레오그래피(Choreography)
  → 각 참여자가 이벤트를 받고 자기 후속 작업을 수행
```

- `Facade → UseCase → Repository`처럼 Facade나 UseCase가 한 기능 내부의 실행 순서를 조정하는 것은 오케스트레이션이다.
- `MemberJoinedEvent`를 받은 Cash·Post·Market 영역이 각자 동기화하는 흐름은 이벤트 기반 코레오그래피다.
- `CashMemberCreatedEvent`를 받은 Cash 리스너가 지갑을 만드는 흐름도 발행자가 다음 처리자를 직접 지시하지 않으므로 코레오그래피다.
- 현재 구조는 기능 내부에서는 오케스트레이션을, 모듈 사이에서는 코레오그래피를 함께 사용한다.
- `MSA 지향 구조`는 독립 배포나 영역 분리를 향한 넓은 방향을 가리킬 뿐, 오케스트레이션이나 코레오그래피라는 구체적인 상호작용 방식을 대신하는 용어가 아니다.

코레오그래피는 모듈 간 직접 의존을 줄이지만 전체 흐름 파악, 순서 보장, 실패 추적과 재처리가 어려워질 수 있다. 중앙에서 순서와 실패 처리를 통제해야 한다면 오케스트레이션이 더 적합할 수 있다.

## 사가 패턴

**사가 패턴(Saga Pattern)**은 여러 서비스의 로컬 트랜잭션을 하나의 업무 흐름으로 연결하고, 중간 단계가 실패하면 앞선 작업을 되돌리는 보상 트랜잭션을 실행하는 분산 트랜잭션 패턴이다.

```text
로컬 트랜잭션 A
  → 로컬 트랜잭션 B
      ├─ 성공 → 다음 단계
      └─ 실패 → 보상 트랜잭션으로 A 취소
```

- 오케스트레이션 사가(Orchestration-based Saga)는 중앙 오케스트레이터가 다음 단계와 보상 작업을 지시한다.
- 코레오그래피 사가(Choreography-based Saga)는 각 서비스가 이벤트를 발행하고 다음 서비스가 반응하며 흐름을 이어 간다.
- 따라서 오케스트레이션과 코레오그래피는 사가 패턴을 구현하는 두 가지 방식이 될 수 있지만, 이벤트를 사용한다고 모두 사가 패턴인 것은 아니다. 실패 시 보상 작업과 전체 업무 흐름이 정의돼 있어야 한다.
- 현재 회원 저장 → CashMember·Wallet 생성 흐름은 이벤트 기반 코레오그래피에 가깝지만, 실패 시 보상 트랜잭션이 정의돼 있지 않으므로 완전한 사가 패턴으로 보기는 어렵다.

## 다른 예시: `Post`와 `PostComment`

```java
@OneToMany(
    mappedBy = "post",
    cascade = {PERSIST, REMOVE},
    orphanRemoval = true
)
private List<PostComment> comments = new ArrayList<>();
```

```text
Post 1개
  └─ PostComment 여러 개
```

- 새 Post와 댓글을 함께 저장하면 `PERSIST`에 따라 댓글도 저장된다.
- Post를 삭제하면 `REMOVE`에 따라 해당 PostComment들도 삭제된다.
- Post의 `comments` 목록에서 댓글을 제거하면 `orphanRemoval`에 따라 DB의 댓글도 삭제된다.
- `PostComment.post`에는 Cascade가 없으므로 댓글에서 Post로 저장·삭제가 전파되지는 않는다.
- `Post.addComment()`가 `PostCommentCreatedEvent`를 발행하는 것은 Cascade가 아니라, 댓글 생성 후 다른 기능에 알리는 이벤트 처리다.

HTTP 요청 흐름:

```text
Controller(in)
  → Facade(app)
      → UseCase 또는 Support(app)
          → Domain(domain)
          → Repository(out)
              → DB
```

이벤트 흐름:

```text
EventListener(in)
  → Facade(app)
      → UseCase(app)
          → Repository(out)
              → DB
```

`app`이 중심에서 `in`, `domain`, `out`을 연결하지만, `in`이나 `out`이 서로 직접 비즈니스 로직을 처리하지 않는 것이 기본 방향이다.

## Cash 회원 동기화를 패키지별로 보기

```text
MemberJoinUseCase(app)
  → MemberRepository(out): 원본 회원 저장
  → MemberJoinedEvent 발행

CashEventListener(in)
  → CashFacade(app)
      → CashSyncMemberUseCase(app)
          → CashMember(domain) 생성
          → CashMemberRepository(out): 복제 회원 저장
          → CashMemberCreatedEvent 발행

CashEventListener(in)
  → CashFacade(app)
      → CashCreateWalletUseCase(app)
          → Wallet(domain) 생성
          → WalletRepository(out): 지갑 저장
```

- `in`은 이벤트를 받는 입구다.
- `app`은 이벤트를 어떤 업무로 처리할지 조정한다.
- `domain`은 `CashMember`, `Wallet`처럼 저장될 업무 객체와 상태 규칙을 표현한다.
- `out`은 Repository를 통해 실제 DB 저장을 수행한다.
- `@Transactional`과 `AFTER_COMMIT`은 이 패키지 구분과 별개로 Spring이 실행 시점과 트랜잭션을 관리하는 기능이다.
- `Repository.save()` 호출이 끝난 것과 DB 트랜잭션이 실제로 커밋된 것은 다르다. 현재 흐름에서는 UseCase가 저장을 요청하고 이벤트를 발행한 뒤, 바깥 트랜잭션이 커밋되면 `AFTER_COMMIT` 리스너가 실행된다.
- `CashEventListener`는 이벤트를 주기적으로 조회하는 것이 아니다. Spring이 애플리케이션 시작 시 `@TransactionalEventListener` 메서드를 이벤트 타입별로 등록하고, `EventPublisher`가 이벤트를 발행할 때 해당 리스너에 전달한다.
- `CashMemberCreatedEvent`는 발행 즉시 리스너 메서드가 실행되는 것이 아니라, `AFTER_COMMIT` 설정에 따라 CashMember 저장 트랜잭션이 커밋된 뒤 `handle(CashMemberCreatedEvent)`가 실행된다.

## 저장 실패와 이벤트 발행 순서

```java
CashMember cashMember = cashMemberRepository.save(...);
eventPublisher.publish(new CashMemberCreatedEvent(...));
```

- `save()` 호출 자체에서 예외가 발생하면 다음 줄로 이동하지 않으므로 이벤트도 발행되지 않는다.
- `save()`가 성공해도 실제 SQL 반영은 flush·commit 시점까지 늦춰질 수 있다.
- 이벤트가 발행된 뒤 커밋이 실패하면 `AFTER_COMMIT` 리스너는 실행되지 않는다.
- 따라서 현재 구조에서는 CashMember 저장이 최종 커밋된 뒤에만 지갑 생성 리스너가 실행된다.

## 다룬 질문

- Facade, UseCase, Support, Repository는 각각 어떤 역할을 하는가?
- Repository 호출은 Facade, Support, UseCase 중 어디에서 하는가?
- Repository 호출을 위해 별도의 이벤트를 만들어야 하는가?
- UseCase가 DB 작업을 끝낸 뒤 이벤트를 발행하고, 리스너가 다시 UseCase를 호출하는 흐름이 맞는가?
- `MemberJoinUseCase`가 이벤트를 생성하고, 리스너가 동기화 UseCase를 호출하는 흐름은 어떻게 되는가?
- 이벤트 퍼블리셔와 리스너 사이에서 이벤트가 실제로 어떻게 전달되는가?
- 이벤트 리스너는 이벤트 객체에서 DTO를 꺼내 다시 UseCase를 호출하는가?
- 이벤트 처리에서 Facade를 거치는 이유는 무엇인가?

## 현재 이해한 내용

```text
Facade
  → UseCase 또는 Support
      → Repository
```

- Facade는 외부 요청이나 이벤트를 받아 적절한 기능으로 연결하는 조정자다.
- UseCase는 회원가입, 회원 동기화, 지갑 생성처럼 하나의 업무 행동과 비즈니스 로직을 담당한다.
- Support는 현재 구조에서 주로 조회 기능을 담당한다.
- Repository는 실제 데이터베이스 접근을 담당한다.
- 이벤트는 Repository 호출을 대신하는 것이 아니라, 한 작업이 끝난 뒤 다른 작업을 연결하기 위해 사용한다.

## 이벤트 흐름

```text
MemberJoinUseCase
  → MemberRepository에 회원 저장
  → MemberJoinedEvent 발행
  → EventPublisher
  → Spring 이벤트 시스템
  → PostEventListener / CashEventListener / MarketEventListener
  → 각 Facade
  → 각 동기화 UseCase
  → 각 Repository
```

Cash 영역은 추가 흐름이 있다.

```text
CashSyncMemberUseCase
  → CashMember 저장
  → CashMemberCreatedEvent 발행
  → CashEventListener
  → CashCreateWalletUseCase
  → Wallet 저장
```

## 이벤트 처리와 멱등성

이벤트는 네트워크 오류나 재시도 때문에 같은 이벤트가 여러 번 전달될 수 있다. 따라서 이벤트를 처리하는 쪽은 중복 수신을 전제로 설계해야 한다. 같은 작업을 여러 번 실행해도 한 번 실행한 것과 같은 결과가 나오도록 만드는 성질을 **멱등성**이라고 한다.

```text
이미 처리한 이벤트인가?
    ├─ 예  → 다시 처리하지 않고 종료
    └─ 아니오 → 업무 처리 후 처리 이력 저장
```

예를 들어 결제 성공 이벤트를 처리할 때마다 지갑을 차감하면 중복 이벤트로 금액이 여러 번 빠질 수 있다. 주문 ID나 이벤트 ID로 처리 여부를 확인한 뒤, 아직 처리하지 않은 경우에만 지갑 차감과 holding 입금을 실행해야 한다.

```text
결제 성공 이벤트 수신
  → 주문이 이미 결제 완료인가?
      ├─ 예  → 중복 이벤트로 보고 무시
      └─ 아니오 → 지갑 처리 후 결제 완료 기록
```

- 주문 상태를 `PAID`로 변경하는 것처럼 같은 값을 다시 설정하는 작업은 멱등하게 만들기 쉽다.
- 지갑 차감, 포인트 적립, 재고 차감처럼 실행할 때마다 수량이 변하는 작업은 처리 여부 기록이 필요하다.
- 이벤트 ID 또는 주문 ID를 중복 방지 키로 사용할 수 있다.
- 처리 여부 확인과 업무 처리는 같은 트랜잭션에서 수행해야, 확인 직후 동시에 들어온 중복 이벤트가 함께 처리되는 문제를 줄일 수 있다.
- Outbox를 사용해도 이벤트 중복 발행 가능성은 남아 있으므로, Outbox 소비자도 멱등해야 한다.

## 모노프로젝트에서 MSA 지향 구조로 모듈 분리

모노프로젝트에서 모듈을 나누는 것은 패키지를 분리하는 것만으로 끝나지 않는다. 다른 모듈의 클래스를 직접 `import`하지 않게 되어야 모듈 간 결합이 끊겼다고 볼 수 있다.

```text
모듈 간 직접 참조 제거
  ├─ 사건을 알림       → Spring Event, 추후 Kafka
  ├─ 값을 받아야 함    → API 호출
  └─ 다른 Entity 필요  → 복제 테이블(Replica Table)
```

- **이벤트 처리(Event-driven communication)**는 한 모듈의 작업이 끝났음을 알리고 다른 모듈이 후속 작업을 수행하게 한다.
- **API 호출**은 다른 모듈의 현재 값을 즉시 조회하거나 요청해야 할 때 사용한다.
- **복제 테이블(Replica Table)**은 다른 모듈의 Entity를 직접 공유하지 않고, 필요한 데이터를 자기 모듈에 복제해 사용한다.

현재 MPM처럼 여러 모듈이 하나의 JVM에 있을 때는 Spring Event로 모듈 간 결합을 낮출 수 있다. 모듈이 별도 프로젝트·프로세스로 분리되면 Spring Event를 프로젝트 경계를 넘는 Kafka 이벤트로 교체할 수 있다. Kafka는 이벤트 보관과 consumer offset 관리로 장애 후 재처리를 지원하지만, 중복 전달 가능성이 있으므로 소비자는 멱등성을 가져야 한다.

이 구조는 **MSA 지향 구조**이지, 아직 완전히 분리된 MSA 자체는 아니다. 모듈을 별도 서비스로 옮길 수 있는 결합 구조를 먼저 만드는 단계다.

## 헷갈렸던 부분과 정리

- 리스너가 이벤트를 직접 조회하는 것이 아니라, Spring이 이벤트 타입에 맞는 리스너를 자동으로 호출한다.
- `EventPublisher`가 각 리스너를 직접 호출하는 것이 아니라 Spring 이벤트 시스템에 이벤트를 발행한다.
- 리스너가 직접 Repository를 호출하는 것이 아니라 현재 구조에서는 Facade를 거쳐 UseCase를 호출한다.
- 모든 UseCase가 이벤트를 발행하는 것은 아니다. 다른 기능에 후속 처리를 알려야 할 때만 발행한다.
- 이벤트를 발행한 뒤 같은 이벤트가 돌아오는 것이 아니라, 후속 작업이 필요하면 새로운 이벤트를 다시 발행한다.
