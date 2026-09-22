# 학습 질문 및 이해 상태

학습 문서의 파사드다. 현재 상태는 [학습 개요](./overview.md)에서 확인하고, 상세 내용은 주제별 문서로 이동한다.

## 문서 인덱스

| 영역 | 문서 | 내용 |
| --- | --- | --- |
| 개요·학습 상태 | [overview.md](./overview.md) | 학습 단계, 이해 상태, 문서 관리 원칙, 다음 학습 큐 |
| 아키텍처·이벤트 | [architecture-and-events.md](./architecture-and-events.md) | `in / app / domain / out`, Entity·UseCase 책임, 오케스트레이션·코레오그래피·사가 패턴, JPA 연관관계, 이벤트 흐름 |
| Spring 프록시·트랜잭션 | [spring-proxy-and-transactions.md](./spring-proxy-and-transactions.md) | Proxy, `@Transactional`, `@TransactionalEventListener`, `AFTER_COMMIT`, `REQUIRES_NEW` |
| Spring 설정·환경변수 | [spring-config-and-env.md](./spring-config-and-env.md) | `spring.config.import`, `.env`, properties 형식, `${변수명}` 참조 |
| JSON·DTO | [json-and-dto.md](./json-and-dto.md) | Jackson, DTO 변환, `ParameterizedTypeReference` |
| RestClient | [restclient.md](./restclient.md) | 메서드 체이닝, HTTP 요청·응답, URI·URL |
| Java 문법 | [java-syntax.md](./java-syntax.md) | 향상된 for문, `Desc`, Lombok, `@Lazy` |
| Optional | [optional.md](./optional.md) | `Optional`, Spring Data JPA 조회 흐름 |

## 관리 방식

- 새 질문은 가장 가까운 주제 문서의 `다룬 질문`에 추가한다.
- 답변이 정리되면 같은 문서의 `현재 이해한 내용`을 갱신한다.
- 여러 영역에 걸친 내용은 주제 문서에 본문을 두고, 이 파일에는 링크만 추가한다.
- 학습 단계와 다음 질문은 [overview.md](./overview.md)에서만 관리한다.
