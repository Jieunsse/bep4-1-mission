# Java `Optional`

## 다룬 질문

- `Optional<MarketMember>`는 무엇을 의미하는가?

## 현재 이해한 내용

```java
public Optional<MarketMember> findMemberByUsername(String username)
```

- username에 해당하는 회원이 있을 수도 있고 없을 수도 있다는 것을 반환 타입으로 표현한다.
- 값이 있으면 `MarketMember`를 담고, 없으면 빈 `Optional`을 반환한다.
- `null`을 직접 반환하는 대신 값의 존재 여부를 명시적으로 다루기 위한 도구다.
- 회원이 없는 이유는 서비스 최초 실행뿐 아니라 잘못된 조회 조건, 미가입, 삭제, 복제·동기화 지연 등 여러 가지일 수 있다.

```java
Optional<MarketMember> result = ...;

result.ifPresent(member -> ...);
MarketMember member = result.orElse(defaultMember);
MarketMember member = result.orElseThrow();
```

- `get()`은 값이 없을 때 예외가 발생하므로 존재가 확실할 때만 사용한다.

## Spring Data JPA 조회 흐름

```text
MarketFacade
  → MarketSupport
      → MarketMemberRepository.findByUsername()
          → Spring Data JPA가 메서드 이름을 해석
              → DB 조회
                  → Optional<MarketMember> 반환
```

- `Optional` 자체는 Java 기능이다.
- `MarketMemberRepository`는 `JpaRepository`를 상속한 Spring Data JPA Repository다.
- `findByUsername()`은 Repository가 DB에서 회원을 조회하는 메서드다.
- 조회 결과가 있으면 `Optional.of(member)`, 없으면 `Optional.empty()`가 반환된다.

