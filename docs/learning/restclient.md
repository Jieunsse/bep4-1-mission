# RestClient 체이닝

## 다룬 질문

- `.get()`, `.uri()`, `.retrieve()`, `.body()`는 각각 어떤 역할인가?
- `retrieve`의 영어 뜻은 무엇인가?
- URL과 URI의 차이는 무엇인가?

## 현재 이해한 내용

```java
return restClient.get()
    .uri("/posts/%d".formatted(id))
    .retrieve()
    .body(new ParameterizedTypeReference<>() {});
```

```text
.get()      → GET 요청 준비
.uri()      → 요청할 주소 지정
.retrieve() → 서버 응답 받기
.body()     → 응답 JSON을 Java 객체로 변환
```

- `retrieve`는 가져오다, 받아오다라는 뜻이다.
- URL은 전체 주소이고 URI는 자원 경로로 이해할 수 있다.

```text
URL: https://example.com/posts/3
URI: /posts/3
```

