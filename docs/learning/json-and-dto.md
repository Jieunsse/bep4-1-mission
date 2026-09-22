# JSON, Jackson, DTO 변환과 타입 참조

## 다룬 질문

- API 응답 JSON은 어떻게 Java 객체로 변환되는가?
- `@JsonCreator`는 무엇인가?
- 코드에서 Jackson을 직접 호출하지 않는데 어떻게 작동하는가?
- `@JsonCreator`가 객체 변환 전체를 담당하는가?
- JSON을 Java 타입과 값으로 바꿀 때 DTO가 기준이 되는가?
- 반환 타입에 맞춰 JSON이 DTO로 변환되는가?

## 현재 이해한 내용

```text
RestClient
  → API 호출
  → JSON 응답 수신
  → Spring 내부 변환기
  → Jackson
  → PostDto 객체
```

- Spring Boot가 Jackson을 내부적으로 연결해주므로 코드에서 Jackson을 직접 호출하지 않아도 된다.
- DTO의 필드와 생성자 정보가 JSON을 Java 객체로 바꾸는 기준이 된다.
- `@JsonCreator`는 Jackson에게 JSON을 객체로 만들 때 사용할 생성자를 알려준다.
- `@JsonCreator` 자체가 API를 호출하거나 변환 전체를 수행하는 것은 아니다.
- `@Getter`는 Java 객체를 JSON으로 내보낼 때 사용될 getter를 제공한다.

## `ParameterizedTypeReference` 문법

### 다룬 질문

- 다음 문법에서 `<>`, `()`, `{}`는 각각 무엇을 의미하는가?

```java
new ParameterizedTypeReference<>() {}
```

- 매개변수가 없는데 JSON 데이터는 어디에 들어가는가?
- 빈 중괄호는 빈 객체라는 뜻인가?
- `ParameterizedTypeReference` 객체와 실제 `PostDto` 객체는 같은 것인가?

### 현재 이해한 내용

```text
ParameterizedTypeReference
  → 응답 타입을 알려주는 타입 안내서

PostDto
  → JSON 데이터가 실제로 들어가는 객체
```

- `<>`는 제네릭 타입을 지정하거나 컴파일러가 추론하게 한다.
- `()`는 타입 안내 객체의 매개변수 없는 생성자를 호출한다.
- `{}`는 이름 없는 익명 자식 클래스를 정의한다.
- `new ParameterizedTypeReference<>() {}`는 실제 `PostDto` 데이터를 담는 객체가 아니다.
- Spring/Jackson이 나중에 JSON을 읽고 `PostDto` 객체를 별도로 만든다.
- 익명 자식 클래스는 한 번만 사용하는 타입 안내 객체이므로 별도 클래스 이름을 만들지 않은 것이다.
