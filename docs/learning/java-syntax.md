# Java 문법과 보조 어노테이션

## 다룬 질문

- 향상된 `for`문은 어떻게 동작하는가?
- `Desc`는 무슨 뜻인가?
- `@Slf4j`는 무엇인가?
- `@Lazy`는 무엇인가?

## 현재 이해한 내용

## 향상된 `for`문

```java
for (Post post : posts) {
    result.add(new PostDto(post));
}
```

- `posts`에서 `Post`를 하나씩 꺼내 `post` 변수에 담는다.
- 인덱스가 필요하지 않을 때 사용하는 반복문이다.

## `Desc`

```java
findByOrderByIdDesc()
```

- `Descending`의 줄임말이다.
- 큰 값부터 작은 값, 즉 내림차순을 의미한다.
- `Asc`는 오름차순이다.

## `@Slf4j`

- Lombok이 로그 객체를 자동으로 만들어주는 어노테이션이다.
- `log.info()`, `log.warn()`, `log.error()` 등을 사용할 수 있다.

## `@Lazy`

- Spring Bean 생성을 필요한 시점까지 늦춘다.
- 자기 자신을 Spring Proxy를 통해 호출하거나 초기화 순서를 늦출 때 사용될 수 있다.

