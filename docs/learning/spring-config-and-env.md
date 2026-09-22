# Spring Boot 설정과 `.env` 연동

## 다룬 질문

- `application.yml`의 `spring.config.import`로 로컬 `.env`를 어떻게 읽는가?
- `optional:file:.env[.properties]`는 무슨 뜻인가?
- `.env`의 값을 Spring 설정에서 어떻게 사용하는가?

## 현재 이해한 내용

Spring Boot에게 `.env` 파일을 추가 설정 파일로 읽으라고 알려주면, 파일에 저장한 환경 설정값을 `application.yml`에서 사용할 수 있다.

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

### 설정 문법 해석

- `file:.env`: 실행할 때의 작업 디렉터리에서 `.env` 파일을 찾는다.
- `[.properties]`: `.env` 파일을 Java properties 형식으로 해석한다.
- `optional:`: 파일이 없어도 애플리케이션을 실행한다. 이 접두사가 없으면 파일이 없을 때 시작에 실패할 수 있다.

`.env`는 `key=value` 형식으로 작성한다.

```properties
TOSS_PAYMENTS_SECRET_KEY=test-key
DB_USERNAME=local-user
DB_PASSWORD=local-password
```

읽어온 값은 `${변수명}`으로 참조한다.

```yaml
custom:
  market:
    toss:
      payments:
        secretKey: ${TOSS_PAYMENTS_SECRET_KEY:}
```

위 설정은 `TOSS_PAYMENTS_SECRET_KEY` 값을 사용하고, 값이 없으면 콜론 뒤의 빈 문자열을 기본값으로 사용한다.

```text
.env
  → Spring Boot 설정 import
      → application.yml의 ${변수명} 치환
          → Bean이 설정값 사용
```

## 주의할 점

- `.env`는 YAML이 아니라 `key=value` 형식으로 작성한다.
- `file:.env`의 경로는 프로젝트 루트로 고정된 것이 아니라 애플리케이션을 실행한 작업 디렉터리 기준이다.
- `optional`은 값이 반드시 존재한다는 뜻이 아니다. 필수 값이라면 기본값을 두지 않거나 별도 검증이 필요하다.
- `.env`에는 비밀번호나 API 키가 들어갈 수 있으므로 Git에 커밋하지 않는다.
