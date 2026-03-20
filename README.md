# spring-cgv-23rd
CEOS 23기 백엔드 스터디 - CGV 클론 코딩 프로젝트

## ❓EntityManager는 누가 생성하고, DB와의 연결은 어떻게 이루어질까요?

### EntityManager는 누가 생성할까?

직접 `new EntityManager()`로 만드는 게 아니라, **JPA 구현체(예: Hibernate)** 가 생성한다.
조금 더 정확히 말하면, **EntityManagerFactory**가 EntityManager를 생성한다.

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("unitName");
EntityManager em = emf.createEntityManager();
```

### 스프링에서는 누가 관리할까?

Spring 환경에서는**Spring Framework** 가 대신 해준다.

- `@PersistenceContext`
- 또는 `@Autowired`

```java
@PersistenceContext
private EntityManager em;
```

내부적으로는

- Spring이 **EntityManagerFactory를 생성**
- 요청마다 적절한 **EntityManager를 주입**
- 트랜잭션 범위에 맞게 관리

### DB 연결은 어떻게 이루어질까?

👇 전체 흐름

1. 애플리케이션 시작
2. Spring → DataSource 생성
3. DataSource 기반으로 EntityManagerFactory 생성
4. 요청 시 EntityManager 생성
5. 트랜잭션 시작 시 DB 커넥션 획득

이 때, EntityManager가 항상 DB와 연결되어 있는 건 아님.

실제 DB 연결은 **트랜잭션 시작 시점**에 이루어짐

```java
@Transactional
public void save() {
    em.persist(entity); // 이 시점에 커넥션 사용
}
```

### 정리

- EntityManager 생성 → **EntityManagerFactory**
- 관리 → **Spring**
- DB 연결 → **DataSource + 트랜잭션 시점**

## ❓flush의 발생하는 시점은 언제일까요?

### 트랜잭션 commit 시

- 트랜잭션이 커밋되기 직전에 자동으로 flush 발생
- 거의 모든 경우 기본 동작

```java
@Transactional
public void save() {
    em.persist(member);
} // commit 시 flush 발생
```

### `em.flush()`  직접 호출

- 개발자가 강제로 DB에 반영

```java
em.persist(member);
em.flush(); // 즉시 SQL 실행
```

#### 특징

- commit 안 해도 SQL 실행됨
- 하지만 rollback 되면 결국 반영 안 됨

### JPQL 쿼리 실행 직전

- 기본 flush 모드는 **AUTO**
- JPQL 실행 전에 flush 발생

```java
em.persist(member);

em.createQuery("select m from Member m")
  .getResultList(); // 여기서 flush 발생
```

#### 이유

- JPQL은 DB 기준으로 조회하기 때문에
- 영속성 컨텍스트와 DB 결과를 맞추기 위해 flush

### 중요한 추가 포인트

#### flush ≠ commit

- flush → SQL 실행
- commit → 실제 DB 반영 확정

#### flush는 영속성 컨텍스트를 비우지 않는다

- 1차 캐시는 그대로 유지됨
- 단지 DB에 반영만 하는 것

#### flush 모드도 존재

- AUTO (기본)
- COMMIT

```java
em.setFlushMode(FlushModeType.COMMIT);
```

COMMIT 모드 특징

- JPQL 실행 시 flush 안 함
- 오직 commit 시에만 flush

### 정리

flush는

**"DB와 영속성 컨텍스트의 동기화를 맞추기 위해 필요한 시점에 발생"**

- commit 직전
- 직접 호출
- JPQL 실행 직전 (AUTO 모드)

## **JOIN을 사용할 때 SQL과 JPQL이 어떤 기준으로 조인을 수행하는지** 비교해보면 차이를 더 쉽게 이해할 수 있어요

### SQL JOIN 기준

테이블과 컬럼(FK)을 기준으로 조인

```java
SELECT *
FROM member m
JOIN team t ON m.team_id = t.id;
```

특징

- 개발자가 직접 `ON` 조건 작성
- **외래키(FK) 컬럼 기반으로 조인**
- DB 구조(테이블, 컬럼)에 의존

### JPQL JOIN 기준

엔티티와 연관관계를 기준으로 조인

```java
SELECT m FROM Member m JOIN m.team t
```

특징

- `ON` 조건을 직접 쓰지 않음 (기본적으로)
- **객체의 연관관계 필드 기준 (`m.team`)**
- 내부적으로 Hibernate 가 SQL로 변환

| 구분 | 기준 |
| --- | --- |
| SQL | 테이블 + 외래키 컬럼 |
| JPQL | 엔티티 + 연관관계 필드 |

👆사고방식 자체가 다르다

- SQL → **데이터 중심 (테이블)**
- JPQL → **객체 중심 (엔티티 그래프)**

### 정리

SQL은 외래키 컬럼 기준으로 조인하고,

JPQL은 엔티티 간 연관관계 필드를 기준으로 조인한다.

## ❓fetch join을 사용하면서 페이징을 적용할 때 발생하는 문제에 대해 알아보아요!

### 상황: fetch join + 페이징

```java
SELECT m FROM Member m
JOIN FETCH m.orders
```

여기서 페이징을 적용하면:

```java
query.setFirstResult(0);
query.setMaxResults(10);
```

### 문제 발생 원인

핵심은 **1:N 관계에서의 fetch join** 이다.

```java
Member A - Order 1, 2, 3
Member B - Order 4, 5
```

fetch join 결과:

```java
A, Order1
A, Order2
A, Order3
B, Order4
B, Order5
```

row가 뻥튀기됨 (중복 발생)

### 페이징이 깨지는 이유

DB는 이렇게 생각함

→ "row 기준으로 10개 잘라야지”

하지만 우리는

→ "Member 10명"을 기대

### 실제 문제

- DB 페이징 → row 기준
- JPA 결과 → 중복 제거 후 엔티티 기준

결과적으로:

- 데이터 누락 발생
- 페이지 크기 깨짐
- 심하면 메모리 페이징 발생

### 특히 위험한 경우

컬렉션 fetch join (`@OneToMany`)

- JPA 구현체(예: Hibernate)는

  → 경고 로그 발생


```java
firstResult/maxResults specified with collection fetch; applying in memory!
```

의미:

- DB에서 페이징 안 함
- 전부 가져온 뒤 메모리에서 자름 (🔥 성능 최악)

### 해결 방법

#### 1. 컬렉션 fetch join + 페이징 ❌

→ 가장 중요한 원칙

#### 2. ToOne 관계만 fetch join + 페이징 ✅

```java
SELECT m FROM Member m
JOIN FETCH m.team
```

이유

- row 증가 없음

#### 3. 컬렉션은 별도 조회 (지연로딩 활용)

```java
SELECT m FROM Member m
```

이후

```java
m.getOrders() // 필요할 때 조회
```

#### 4. DTO 조회 방식 (실무에서 많이 사용)

```java
SELECT new com.example.dto.MemberDto(m.id, o.name)
FROM Member m
JOIN m.orders o
```

#### 5. 배치 사이즈 활용

```java
@BatchSize(size = 100)
```

→ N+1 문제 완화

### 정리

컬렉션 fetch join을 사용하면 row가 증가하기 때문에

DB 페이징이 깨지고, 경우에 따라 메모리 페이징이 발생한다.

## data jpa를 찾다보면 SimpleJpaRepository에서  entity manager를 생성자 주입을 통해서 주입 받는다. 근데 싱글톤 객체는 한번만 할당을  받는데, 한번 연결 때 마다 생성이 되는 entity manager를 생성자 주입을 통해서 받는 것은 수상하지 않는가? 어떻게 되는 것일까? 한번 알아보자

### SimpleJpaRepository는 싱글톤인데 EntityManager는 왜 괜찮을까?

**주입되는 건 진짜 EntityManager가 아니라 프록시(EntityManager Proxy)이다.**

Spring Data JPA의 `SimpleJpaRepository`는 싱글톤이다.

```java
public class SimpleJpaRepository<T, ID> {
    private final EntityManager em;
}
```

여기서 주입되는 `em`은 실제 객체가 아니라

**→ Spring Framework 가 만든 프록시**

### 핵심 동작 방식

- 주입 시점 → 프록시 객체 1개 (싱글톤처럼 보임)
- 실제 사용 시 → 트랜잭션마다 진짜 EntityManager 할당

### 내부 흐름

```java
클라이언트 요청
 → 트랜잭션 시작
 → 실제 EntityManager 생성
 → 프록시가 실제 EM을 찾아 위임
```

### 정리

싱글톤에 주입되는 건 프록시이고,

실제 EntityManager는 트랜잭션마다 따로 생성된다.

## fetch join 할 때 distinct를 안하면 생길 수 있는 문제

### 문제 상황

```java
SELECT m FROM Member m
JOIN FETCH m.orders
```

결과:

```java
Member A
Member A
Member A
Member B
```

### 문제

- 동일 엔티티 중복 반환
- 컬렉션 크기 이상하게 보임
- 로직 오류 발생 가능

### 해결

```java
SELECT DISTINCT m FROM Member m
JOIN FETCH m.orders
```

### 디테일

JPQL의 DISTINCT는 2가지 역할

1. SQL에 DISTINCT 추가
2. **JPA가 엔티티 중복 제거 (핵심)**

### 정리

fetch join 시 DISTINCT를 사용하지 않으면

중복 엔티티가 반환된다.

## fetch join 을 할 때 생기는 에러가 생기는 3가지 에러 메시지의 원인과 해결 방안

### `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!`

#### 원인

- 컬렉션 fetch join + 페이징

#### 결과

- DB 페이징 불가
- 메모리 페이징 발생 (성능 최악)

#### 해결

- 컬렉션 fetch join 제거
- ToOne만 fetch join
- 또는 DTO 조회

### `query specified join fetching, but the owner of the fetched association was not present in the select list`

#### 원인

```java
SELECT o FROM Order o
JOIN FETCH o.member m
```

여기서 m을 fetch 했는데 select에 없음 → 문제 발생

#### 해결

반드시 fetch 대상의 owner 포함

```java
SELECT o FROM Order o
JOIN FETCH o.member
```

또는

```java
SELECT m FROM Member m
JOIN FETCH m.orders
```

### `org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags`

#### 원인

```java
JOIN FETCH m.orders
JOIN FETCH m.coupons
```

둘 다 List (bag)일 때 발생

#### 왜?

DB 결과가 카테시안 곱으로 폭발

```java
orders 3개 * coupons 4개 = 12 rows
```

Hibernate가 감당 못함 (Hibernate 정책적으로 막음)

#### 해결 방법

방법 1: Set으로 변경

```java
Set<Order> orders;
```

방법 2: 하나만 fetch join

방법 3: 나머지는 지연로딩 + BatchSize

### 정리

| 문제 | 원인 | 해결 |
| --- | --- | --- |
| 페이징 에러 | 컬렉션 fetch join | fetch join 제거 |
| owner 없음 | select 대상 누락 | owner 포함 |
| multiple bag | List 2개 fetch | Set / 분리 조회 |

## 1️⃣ DB를 모델링해봐요!

### CGV 서비스 개요

CGV는 영화 관람을 중심으로 다양한 기능을 제공하는 복합 플랫폼입니다.

핵심 기능은 다음과 같이 크게 5가지 도메인으로 나눌 수 있습니다.

- **영화 도메인**: 영화 정보 조회, 배우 정보, 리뷰
- **상영 도메인**: 영화 상영 스케줄 및 상영관 관리
- **예매 도메인**: 좌석 선택 및 영화 예매
- **커머스 도메인**: 매점 상품 주문 및 재고 관리
- **커뮤니티 도메인**: 리뷰, 시네톡, 좋아요 기능

이러한 기능들을 기반으로 실제 서비스 흐름을 분석한 뒤, 데이터 간 관계를 중심으로 ERD를 설계하였습니다.

### ERD 설계 핵심 방향

본 ERD는 단순 데이터 저장이 아니라 **실제 서비스 동작 방식**을 반영하는 것을 목표로 설계되었습니다.

핵심 설계 기준은 다음과 같습니다:

- 다대다 관계는 반드시 중간 테이블로 분리
- 조회 성능과 확장성을 고려한 테이블 분리
- 실제 사용자 행동 흐름(예매, 주문 등)을 기준으로 모델링
- 불필요한 데이터 생성 최소화 (ex. 좌석 테이블 제거)

### 모델링 설명

1. 좌석 테이블을 만들지 않기
    - “통로가 없고 빈 곳이 없는 직사각형”, “동일한 타입이면 좌석 형태가 같다”
    - 즉, 일반관(10x10), 특별관(15x15) 처럼 규격이 고정되어 있으므로, 굳이 DB에 모든 좌석 데이터를 100개, 200개씩 미리 만들어둘 필요가 없다.
    - 예매 된 좌석만 DB에 저장하고, 화면에 보여줄 때는 전체 좌석 화면에서 예매 된 좌석만 색깔을 칠하는 방식이 효율적.
2. 매점 상품과 지점별 재고를 분리 (다대다 관계 해결)
    - 메뉴는 전국 공통이므로 하나만 만든다.
    - 각 지점의 재고는 ‘영화관’과 ‘상품’ 사이의 중간 테이블로 만들어 관리한다.
3. 중복 예매 방지를 위해 예매된 좌석 상세 테이블 만들기
    - 상영관 ID, 좌석 행, 좌석 열 3개를 묶어서 유니크 키 제약조건 설정하기
4. 매점 구매 시 환불 X
    - 매점 구매 내역 테이블에서 상태 컬럼 생략 또는 무조건 ‘COMPLETED’
5. 영화 - 배우 관계를 다대다로 분리
    - 한 배우는 여러 영화에 출연할 수 있고, 하나의 영화에도 여러 배우가 출연한다.
    - 또한 단순 출연이 아니라 **역할(주연, 조연, 감독 등)** 이 존재한다.

   따라서 `movies` ↔ `persons` 를 직접 연결하지 않고 중간 테이블 `work_participations`로 분리한다.

    - `movie_id`, `actor_id`를 FK로 가지며
    - `role` 컬럼으로 역할 정보까지 함께 관리
6. 영화 좋아요 / 영화관 좋아요 분리
    - `movie_likes`, `cinema_likes` 테이블을 별도로 둔다.

   이유:

    - 좋아요 대상이 서로 다름 (영화 vs 영화관)
    - 확장성 고려 (추후 리뷰 좋아요 등 추가 가능)
7. 상영 정보와 영화 분리
    - 영화 자체 정보(`movies`)와

      실제 상영 정보(`screenings`)는 완전히 다른 개념


    `movies`
    
    → 영화 메타데이터 (제목, 러닝타임 등)
    
    `screenings`
    
    → 실제 상영 스케줄 (시간, 상영관)

8. 상영관(theater)과 영화관(cinema) 구조 분리
    - 하나의 영화관(`cinemas`)에는 여러 상영관(`theaters`)이 존재

   `cinemas (1) : theaters (N)`

    - 좌석 크기, 타입(일반관, 특별관)은 상영관 기준으로 관리

   → 이 구조 덕분에

   같은 영화관인데 IMAX관, 일반관 다르게 운영 가능

9. 예매(reservations)와 좌석(reserved_seats) 분리
    - 하나의 예매에는 여러 좌석이 포함될 수 있음

   `reservations (1) : reserved_seats (N)`

    - 좌석은 따로 테이블을 만들지 않고

      **예매된 좌석만 저장**


    추가 핵심:
    
    - `(screening_id, seat_row, seat_col)` → UNIQUE
    
    이걸로 **중복 좌석 예매 방지**

10. 주문(food_orders) - 주문상품(order_items) 구조
    - 하나의 주문에는 여러 상품이 포함될 수 있음

    `food_orders (1) : order_items (N)`

    - `order_items`에서 수량(`quantity`) 관리

    장점:

    - 같은 상품 여러 개 주문 가능
    - 주문 단위와 상품 단위 책임 분리
11. 유저 활동 데이터 분리 (user_statics)
    - 유저의 활동 통계는 별도 테이블로 분리

    이유:

    - 조회 성능 최적화 (count 쿼리 최소화)
    - 랭킹 / 프로필 빠른 조회 가능

    예:

    - cinetalk_count
    - follower_count

    → 실시간 계산이 아니라 **집계 데이터 캐싱 개념**

12. 리뷰 / 시네톡 분리
    - `reviews`: 영화에 대한 평가 중심 콘텐츠
    - `cinetalks`: 커뮤니티 성격 (영화 + 영화관 기반)

    둘 다 user, movie를 참조하지만 목적이 다름

    → 하나로 합치지 않고 분리한 이유:

    - 기능 확장 시 충돌 방지
    - 정책 다르게 적용 가능 (ex. 신고, 노출 방식)
13. 이벤트와 영화 연결 (movie_events)
    - 하나의 이벤트는 여러 영화에 적용될 수 있음
    - 하나의 영화도 여러 이벤트에 포함될 수 있음

    `events ↔ movies` = N:M 관계

    → `movie_events`로 분리

14. 사진(photo) 테이블의 유연한 구조
    - `actor_id`, `movie_id` 둘 다 nullable

    하나의 테이블로:

    - 배우 사진
    - 영화 스틸컷

    을 모두 관리

    → 테이블 분리 대신 **유연한 단일 테이블 전략**

15. ENUM 적극 활용

    여러 테이블에서 ENUM 사용:

    - 영화 장르 (`genre`)
    - 관 타입 (`type`)
    - 결제 방식 (`payment`)
    - 리뷰 타입 (`type`) 등

    👉 장점:

    - 데이터 정합성 보장
    - 잘못된 값 입력 방지

    👉 단점:

    - 확장 시 마이그레이션 필요 (트레이드오프)

<img width="2860" height="1892" alt="image" src="https://github.com/user-attachments/assets/e3baf6d9-ca74-43c1-be06-7ed91c85bb58" />


https://www.erdcloud.com/d/PhXPysc9AfrTJbSYq

