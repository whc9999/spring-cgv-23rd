# spring-cgv-23rd
CEOS 23기 백엔드 스터디 - CGV 클론 코딩 프로젝트

<details>
<summary>리팩토링 내역 및 설계 의도</summary>

## 리팩토링 개요

기능 동작과 API 요청/응답 형식을 유지하면서, 서비스에 몰려 있던 유스케이스 흐름과 도메인 규칙을 점진적으로 분리하는 방향으로 진행했습니다. 큰 아키텍처 전환 대신 현재 프로젝트 규모에 맞춰 Entity factory, 도메인 메서드, 정책 클래스, 전역 예외 처리 정리를 적용했습니다.

## 주요 변경 사항

### 1. Service 책임 축소와 유스케이스 흐름 정리
- 변경 내용: `ReservationService`, `ConcessionService`, `InventoryService`, `PhotoService`, `MovieService`, `CinemaService`, `EventService`, `PersonService`, `UserService` 등에서 조회, 검증, 생성, 저장 흐름을 private 메서드로 분리했습니다.
- 리팩토링 이유: 하나의 Service 메서드에 권한 체크, 검증, 엔티티 생성, 저장, 계산이 함께 있으면 변경 지점이 불명확해지기 때문입니다.
- 기대 효과: Service는 유스케이스 흐름을 조율하고, 세부 규칙은 이름 있는 메서드나 도메인 객체로 이동해 읽기와 변경이 쉬워집니다.

### 2. 도메인 객체의 생성과 규칙 응집
- 변경 내용: `Reservation.create`, `FoodOrder.create`, `OrderItem.create`, `Inventory.create`, `Photo.create`, `Movie.create`, `User.create` 등 Entity 정적 factory를 추가했습니다. `Reservation.validateCancelableBy`, `Inventory.changeStockBy`, `Inventory.updateStock`처럼 상태 변경 규칙도 도메인 메서드로 모았습니다.
- 리팩토링 이유: Service에서 builder를 직접 조립하면 생성 기본값과 상태 규칙이 여러 곳으로 퍼질 수 있기 때문입니다.
- 기대 효과: 엔티티 생성 규칙과 상태 변경 조건이 한 곳에 모여, 이후 필드나 정책이 바뀌어도 수정 범위를 좁힐 수 있습니다.

### 3. 예외 코드 기반 응답 통일
- 변경 내용: 직접 `IllegalArgumentException`, `IllegalStateException`을 던지던 흐름을 `CustomException`과 `ErrorCode` 기반으로 바꿨습니다. 회원, 예약, 쿠폰, 사진 대상, 지역, 재고 수량 등에 필요한 에러 코드를 추가했습니다.
- 리팩토링 이유: 문자열 예외는 API 응답 형식과 상태 코드를 일관되게 유지하기 어렵고, 클라이언트가 에러를 안정적으로 분기하기 어렵기 때문입니다.
- 기대 효과: 예외 발생 시 도메인별 에러 코드와 HTTP status가 명확해지고, 응답 형식이 전역 핸들러를 통해 통일됩니다.

### 4. 역직렬화 예외와 전역 예외 처리 보강
- 변경 내용: `PhotoCreateRequest`, `Region.from`에서 발생하는 `CustomException`이 Jackson 역직렬화 중 `HttpMessageNotReadableException`으로 감싸질 수 있어, `GlobalExceptionHandler`에서 cause chain을 확인해 원래 `CustomException` 응답으로 변환하도록 했습니다. `IllegalArgumentException` 처리도 `INVALID_INPUT_VALUE` 기반 응답으로 통일했습니다.
- 리팩토링 이유: 요청 본문 변환 중 발생한 도메인 예외가 500으로 내려가면 실제 클라이언트 오류를 서버 오류처럼 전달할 수 있기 때문입니다.
- 기대 효과: 잘못된 사진 대상, 지원하지 않는 지역 값 등도 의도한 에러 코드(`PH002`, `RG001`)로 응답할 수 있습니다.

### 5. 쿠폰 할인 정책 분리
- 변경 내용: `ReservationService` 안에 있던 `WELCOME_CGV`, `VIP_HALF_PRICE` 할인 규칙을 `CouponDiscountPolicy`로 분리했습니다.
- 리팩토링 이유: 쿠폰 할인은 예약 저장 흐름보다 가격 정책에 가까운 도메인 규칙이므로, Service가 직접 조건문을 계속 갖고 있으면 확장과 테스트가 어려워집니다.
- 기대 효과: 예약 Service는 가격 계산 흐름만 조율하고, 쿠폰 규칙은 작은 순수 정책 객체로 테스트하기 쉬워졌습니다.

### 6. 매점 주문과 재고 처리 의도 개선
- 변경 내용: `ConcessionService`에서 상품 Map 로딩, 필수 상품 조회, 재고 차감, 주문 항목 생성을 `loadProductMap`, `getRequiredProduct`, `decreaseInventoryStock`, `createOrderItem`으로 분리했습니다. `Inventory`에서는 재고 부족과 잘못된 재고 수량을 각각 `INVENTORY_SHORTAGE`, `INVALID_STOCK_QUANTITY`로 구분했습니다.
- 리팩토링 이유: 주문 생성은 조회, 검증, 재고 차감, 주문 항목 생성이 섞이기 쉬운 흐름이어서, 단계별 의도를 드러내는 이름이 필요했습니다.
- 기대 효과: 주문 처리 흐름을 따라가기 쉬워지고, 재고 관련 실패 사유도 더 정확히 표현됩니다.

### 7. Gradle wrapper 복구와 테스트 보정
- 변경 내용: 누락되어 있던 `gradle/wrapper/gradle-wrapper.jar`를 복구하고 `.gitignore`에서 wrapper jar가 추적되도록 예외를 추가했습니다. `ConcessionServiceTest`, `InventoryServiceTest`, `ReservationServiceTest`는 리팩토링된 서비스 흐름과 에러 코드에 맞게 보정했습니다.
- 리팩토링 이유: wrapper jar가 없으면 `./gradlew test` 실행 자체가 어려워지고, 리팩토링 후 테스트가 실제 서비스 의존성과 맞지 않으면 회귀 검증이 약해지기 때문입니다.
- 기대 효과: 로컬에서 Gradle wrapper 기반 테스트 실행이 가능해졌고, 변경된 도메인 규칙을 테스트가 따라가도록 정리되었습니다.

## 리팩토링 기준

- 기능 동작과 API 요청/응답 형식은 유지했습니다.
- 큰 구조 전환보다 현재 구조 안에서 Service, Entity, Policy, Exception의 책임을 조금씩 분리했습니다.
- 도메인 규칙은 가능하면 도메인 메서드나 정책 클래스로 모았습니다.
- 예외는 가능한 한 `ErrorCode`를 통해 응답 코드와 메시지를 일관되게 관리했습니다.

</details>

<details>
<summary>결제 시스템 연동 및 티켓팅/커머스 케이스 비교</summary>

## 결제 시스템 연동 개요

CGV 예매는 좌석이라는 한정 자원을 다룬다. 따라서 예매는 결제 완료 후 좌석을 차감하는 커머스 방식보다, 예매 시점에 좌석을 먼저 선점하고 결제 실패나 취소 시 좌석을 복구하는 티켓팅 방식이 적합하다.

반면 매점 상품 주문은 동일 상품의 수량 재고를 다룬다. 좌석처럼 특정 자원을 결제 전에 점유할 필요가 상대적으로 낮으므로, 결제 성공 후 재고를 차감하는 커머스 방식을 적용했다.

이번 연동은 CEOS 결제 서버 명세를 기준으로 `POST /payments/{paymentId}/instant` 즉시 결제와 `POST /payments/{paymentId}/cancel` 결제 취소를 사용한다. 외부 결제 API 호출은 DB 트랜잭션 밖에서 수행하고, 내부 예약 상태 변경은 별도 DB 트랜잭션으로 분리한다.

## 티켓팅 케이스와 커머스 케이스 비교

### 티켓팅 케이스
- 재고/좌석 차감 시점: 결제 전 예매 생성 또는 결제창 진입 시점에 좌석을 선점한다.
- 장점: 같은 좌석을 여러 사용자가 동시에 결제하는 상황을 줄일 수 있다.
- 단점: 결제 실패, 이탈, 취소 시 좌석 복구 로직이 필요하다.
- 적합한 상황: 영화 좌석, 공연 티켓처럼 특정 좌석이 한 번만 판매되어야 하는 경우.

### 커머스 케이스
- 재고/재고 차감 시점: 결제 성공 후 상품 재고를 차감한다.
- 장점: 결제 실패 시 재고 복구가 단순하다.
- 단점: 결제 중 재고가 사라질 수 있고, 한정 좌석처럼 개별 자원이 중요한 경우 사용자 경험이 나빠질 수 있다.
- 적합한 상황: 일반 상품처럼 동일한 재고가 여러 개 있고 결제 후 차감해도 되는 경우.

## 우리 CGV 서비스에 선택한 방식

- 예매 선택 방식: 티켓팅 케이스
- 예매 선택 이유: 같은 상영 회차의 같은 좌석은 중복 예매되면 안 되므로, 결제 전에 좌석을 선점해야 한다.
- 좌석 선점/복구 흐름: `Screening` 비관적 락 조회 후 `Reservation(PENDING)`과 `ReservedSeat`를 저장한다. 결제 실패 또는 취소 시 `Reservation`을 `CANCELED`로 변경하고 `ReservedSeat`를 삭제해 좌석을 복구한다.
- 매점 선택 방식: 커머스 케이스
- 매점 선택 이유: 매점 상품은 동일 상품의 수량 재고를 다루므로, 결제 성공 후 재고를 차감하면 결제 실패 시 재고 복구가 필요 없어 흐름이 단순하다.

## HTTP Client 방식 비교

### Feign Client
- 장점: 인터페이스 기반 선언형 클라이언트라 외부 API 계약을 표현하기 쉽다.
- 단점: Spring Cloud OpenFeign 의존성과 설정이 추가된다.
- 적합한 상황: 외부 API가 많고 클라이언트 인터페이스를 명확히 분리해야 하는 경우.

### RestClient 또는 WebClient
- 장점: Spring에서 제공하는 HTTP Client를 직접 사용할 수 있다. `RestClient`는 동기 MVC 서비스에 잘 맞고, `WebClient`는 비동기/논블로킹에 강하다.
- 단점: Feign보다 선언형 인터페이스 느낌은 약하다. `WebClient`는 현재 MVC 구조에는 상대적으로 과하다.
- 적합한 상황: 현재처럼 Spring MVC 기반 서비스에서 외부 API 호출 수가 많지 않은 경우 `RestClient`가 적합하다.

### 이 프로젝트에서 선택한 방식
- 선택한 방식: `RestClient`
- 선택 이유: 현재 프로젝트는 `spring-boot-starter-webmvc` 기반이고 OpenFeign 의존성이 없다. 결제 API 수가 많지 않아 별도 의존성을 추가하지 않고 동기 흐름으로 명확히 처리하는 방식이 가장 작다.

## 결제 API 연동 흐름

1. 좌석 선점
   - DB 트랜잭션 1에서 `Screening`을 비관적 락으로 조회한다.
   - `Reservation`을 `PENDING` 상태로 저장하고 결제 요청 전에 생성한 `paymentId`를 함께 저장한다.
   - `ReservedSeat` 저장으로 좌석을 선점한다.
2. 결제 요청
   - DB 트랜잭션 밖에서 CEOS 결제 서버의 즉시 결제 API를 호출한다.
3. 결제 성공 처리
   - DB 트랜잭션 2에서 `paymentId`로 예약을 다시 조회하고 `PENDING` 상태를 검증한 뒤 `COMPLETED`로 변경한다.
4. 결제 실패 시 좌석 복구
   - DB 트랜잭션으로 예약 상태를 `CANCELED`로 변경하고 선점 좌석을 삭제한다.
5. 결제 취소 시 외부 결제 취소 + 좌석 복구
   - 외부 결제 취소 API가 성공한 뒤 내부 예약을 `CANCELED`로 변경하고 좌석을 삭제한다.

## 매점 결제 API 연동 흐름

1. 주문 생성
   - DB 트랜잭션 1에서 `FoodOrder(PENDING)`와 `OrderItem`을 저장하고 결제 요청 전에 생성한 `paymentId`를 함께 저장한다.
   - 이 시점에는 아직 `Inventory`를 차감하지 않는다.
2. 결제 요청
   - DB 트랜잭션 밖에서 CEOS 결제 서버의 즉시 결제 API를 호출한다.
3. 결제 성공 후 재고 차감
   - DB 트랜잭션 2에서 `paymentId`로 주문을 다시 조회한다.
   - 주문 상태가 `PENDING`인지 검증한 뒤, 상품별 `Inventory`를 비관적 락으로 조회해 재고를 차감하고 주문을 `COMPLETED`로 변경한다.
4. 결제 실패 처리
   - 외부 결제 요청이 실패하면 주문을 `CANCELED`로 변경한다. 재고는 아직 차감 전이므로 복구할 필요가 없다.
5. 결제 성공 후 재고 차감 실패
   - 결제는 성공했지만 재고 부족 또는 내부 DB 오류로 주문 완료에 실패하면 외부 결제 취소 API를 보상 호출하고 주문을 `CANCELED`로 변경한다.

## 예외 및 보상 처리

- 결제 실패: 외부 즉시 결제 요청이 실패하면 `PAYMENT_FAILED` 예외를 발생시키고, 내부 예약을 `CANCELED`로 바꾸며 좌석을 복구한다.
- 결제 취소 실패: 외부 결제 취소가 실패하면 내부 예약 취소와 좌석 복구는 수행하지 않는다.
- 내부 DB 저장 실패: 외부 결제 성공 후 내부 `COMPLETED` 처리 중 실패하면 외부 결제 취소 API를 호출하고 내부 예약/좌석 취소를 보상 처리한다.
- 매점 결제 실패: 주문만 `CANCELED`로 변경한다. 커머스 흐름에서는 결제 성공 전 재고를 차감하지 않으므로 재고 복구가 필요 없다.
- 매점 재고 부족: 결제 성공 후 재고 차감 단계에서 재고 부족이 확인되면 외부 결제를 취소하고 주문을 `CANCELED`로 변경한다.
- 중복 paymentId: UUID 기반 `reservation-{uuid}` 형식으로 생성한다. 외부 서버에서 중복이 발생하면 결제 실패로 처리한다.
- 중복 좌석 예매: 기존 `PESSIMISTIC_WRITE`와 `ReservedSeat` 유니크 제약을 유지한다.

## 현재 구조의 한계

- 외부 결제 API 호출과 내부 DB 상태 변경은 완전한 분산 트랜잭션이 아니다.
- 서버가 외부 결제 성공 직후 종료되면 내부 상태 보정이 필요할 수 있다.
- 운영 환경에서는 Outbox, 결제 이력 테이블, 재시도 큐, 스케줄러 기반 결제 상태 보정이 필요할 수 있다.

## 테스트/검증 결과

- 실행한 테스트:
  - `database=cgv password=dngur1213 PAYMENT_API_SECRET_KEY=placeholder ./gradlew test --tests com.ceos23.cgv.domain.reservation.service.ReservationServiceTest`
  - `database=cgv password=dngur1213 PAYMENT_API_SECRET_KEY=placeholder ./gradlew test --tests com.ceos23.cgv.domain.concession.service.ConcessionServiceTest`
- 외부 CEOS 결제 서버는 테스트에서 직접 호출하지 않고 `PaymentService`를 mock 처리했다.

</details>

<details>
<summary>보안 리뷰 반영 내용 보기</summary>

## JWT 설정

- `jwt.secret`은 설정 파일에 직접 두지 않고 `JWT_SECRET` 환경변수로 주입한다.
- 로컬 실행 시에는 충분히 긴 Base64 인코딩 secret을 `JWT_SECRET`으로 설정해야 한다.
- 실제 secret 값은 저장소와 README에 기록하지 않는다.

## 인증/인가 실패 응답

- 인증 실패는 `401`, 인가 실패는 `403`으로 분리한다.
- 응답 형식은 기존 전역 예외 응답과 맞춰 `status`, `code`, `message`, `errors` 구조를 사용한다.

## JWT 검증 로그

- JWT 검증 실패 시 `warn` 로그를 남긴다.
- 토큰 원문은 민감 정보이므로 로그에 남기지 않고, 예외 타입과 메시지만 기록한다.

## Authentication credential 처리

- JWT는 서명 검증으로 신뢰성을 판단하므로 `UsernamePasswordAuthenticationToken`의 credential에는 토큰 원문 대신 `null`을 넣는다.
- 현재 코드에서는 credential 값을 직접 참조하지 않으므로 동작 영향 없이 토큰 노출 가능성을 줄일 수 있다.

## Refresh Token Cookie 전환

- HttpOnly Cookie 방식은 XSS로 인한 refresh token 탈취 위험을 줄일 수 있다.
- 로그인과 토큰 재발급 응답 body에는 access token만 포함한다.
- refresh token은 `Set-Cookie` 헤더의 `refreshToken` HttpOnly Cookie로 내려준다.
- 토큰 재발급 API는 request body 대신 `refreshToken` Cookie를 읽어 처리한다.
- 현재 로컬 개발 환경은 HTTPS가 아니므로 Cookie `secure` 옵션은 `false`로 둔다. 운영 HTTPS 환경에서는 `secure=true` 적용이 필요하다.

</details>

<details>
<summary>동시성 문제와 해결 방법</summary>

## 동시성 문제가 발생할 수 있는 상황

Spring은 일반적으로 요청마다 별도 스레드가 처리되는 Thread Per Request 방식으로 동작한다. 따라서 여러 사용자가 같은 상영 회차의 같은 좌석을 동시에 예매하면 같은 공유 자원에 동시에 접근할 수 있다.

기존 좌석 예매 흐름은 `Reservation` 생성과 `ReservedSeat` 저장이 분리되어 있었다. 이 경우 좌석 저장이 실패하면 좌석 없는 `Reservation`이 남을 수 있고, 동시에 같은 상영 회차 좌석 저장 요청이 들어오면 여러 트랜잭션이 같은 좌석 저장을 시도하는 Race Condition 상황이 발생할 수 있다.

현재는 `POST /api/reservations` 요청에서 예매 정보와 좌석 목록을 함께 받아, `Reservation` 생성과 `ReservedSeat` 저장을 하나의 트랜잭션으로 처리한다. 좌석 중복이 발생하면 `SEAT_ALREADY_RESERVED` 예외가 발생하고 전체 트랜잭션이 rollback되어 좌석 없는 예약이 남지 않는다.

## 해결 방법 비교

### 1. synchronized
- 개념: JVM 내부에서 특정 코드 블록을 한 번에 하나의 스레드만 실행하도록 막는다.
- 장점: 구현이 단순하고 별도 인프라가 필요 없다.
- 단점: 단일 서버/JVM 안에서만 동작하며, 서버가 여러 대면 중복 요청을 막지 못한다.
- 적합한 상황: 단일 JVM 내부의 짧고 단순한 임계 구역 보호.
- 우리 서비스에 적용하기 어려운 이유 또는 적합성: 예매 좌석은 DB에 저장되는 공유 자원이고, 멀티 서버 확장 가능성을 고려하면 부적합하다.

### 2. 비관적 락
- 개념: 트랜잭션이 데이터를 읽을 때 DB row에 락을 걸어 다른 트랜잭션의 동시 수정을 기다리게 한다.
- 장점: 충돌 가능성이 높은 자원을 DB 수준에서 직렬화할 수 있다.
- 단점: 락 대기 시간이 생기며, 락 순서가 꼬이면 Deadlock 위험이 있다.
- 적합한 상황: 같은 좌석, 같은 상영 회차처럼 동시에 접근하면 안 되는 자원이 명확한 경우.
- 우리 서비스에 적합한지: 같은 `Screening`의 좌석 저장 요청을 순서대로 처리하기에 적합하다.

### 3. 낙관적 락
- 개념: `@Version` 값으로 수정 충돌을 감지하고, 충돌 시 예외를 발생시킨다.
- 장점: 락 대기 비용이 적고 읽기 많은 환경에 유리하다.
- 단점: 충돌 후 재시도/예외 처리가 필요하고, insert 중복 문제에는 직접적이지 않다.
- 적합한 상황: 같은 엔티티를 자주 읽지만 동시에 수정하는 빈도는 낮은 경우.
- 우리 서비스에 적합한지: 좌석 중복 예매는 `ReservedSeat` 신규 insert 충돌이 핵심이라 `@Version`만으로는 직접 해결하기 어렵다.

### 4. Redis 분산 락
- 개념: Redis에 특정 key를 락으로 저장해 여러 서버 간 동시 접근을 제어한다.
- 장점: 멀티 서버 환경에서 좌석 단위 락을 정교하게 걸 수 있다.
- 단점: Redis 인프라, 락 만료, 장애 상황 처리가 필요해 복잡도가 높다.
- 적합한 상황: 트래픽이 크고 애플리케이션 서버가 여러 대인 운영 환경.
- 우리 서비스에 적합한지: 현재 프로젝트 규모에서는 과하다. 추후 멀티 서버와 대규모 트래픽을 고려할 때 후보가 될 수 있다.

### 5. 유니크 제약 조건
- 개념: DB에서 특정 컬럼 조합의 중복 저장을 금지한다.
- 장점: 멀티 서버 환경에서도 DB가 최종적으로 중복 데이터를 막는다.
- 단점: 충돌을 사전에 막기보다 저장 시점에 예외로 감지한다.
- 적합한 상황: 같은 상영 회차의 같은 좌석처럼 절대 중복되면 안 되는 데이터.
- 우리 서비스에 적합한지: 이미 `ReservedSeat`에 적용되어 있으며, 반드시 유지해야 하는 최종 방어선이다.

## CGV 서비스에 선택한 해결 방법

선택한 방법은 **DB 유니크 제약 조건 유지 + 상영 회차 row에 비관적 락 적용**이다.

선택 근거는 다음과 같다.
- 현재 프로젝트는 Spring/JPA 기반이므로 `@Lock(LockModeType.PESSIMISTIC_WRITE)`를 Repository에 적용하는 방식이 가장 작다.
- 같은 상영 회차의 같은 좌석 중복 예매를 막는 것이 핵심이므로, 좌석 저장 전에 해당 `Screening` row를 잠그면 같은 회차 좌석 저장 흐름을 직렬화할 수 있다.
- 기존 `(screening_id, seat_row, seat_col)` 유니크 제약은 유지해 DB 최종 방어선을 남긴다.
- Redis 분산 락은 현재 규모에는 복잡하고, `synchronized`는 멀티 서버에서 동작하지 않는다.
- 낙관적 락은 기존 row 업데이트 충돌 감지에는 좋지만, 현재 문제처럼 좌석 row insert 중복을 직접 막는 데는 유니크 제약과 비관적 락 조합보다 덜 적합하다.

## 적용 내용

- `ScreeningRepository.findByIdForUpdate()`를 추가하고 `PESSIMISTIC_WRITE` 락을 적용했다.
- `ReservationService.createReservation()`에서 `Screening`을 비관적 락으로 조회하고, `Reservation` 생성과 `ReservedSeat` 저장을 하나의 트랜잭션으로 묶었다.
- `ReservationCreateRequest`에 좌석 목록을 추가해 예매 확정 API에서 좌석까지 함께 받도록 변경했다.
- `ReservedSeat`의 유니크 제약과 `DataIntegrityViolationException`을 `SEAT_ALREADY_RESERVED`로 변환하는 기존 방어 로직은 유지했다.

예매 생성 요청 예시:

```json
{
  "screeningId": 1,
  "peopleCount": 2,
  "payment": "APP_CARD",
  "couponCode": "WELCOME_CGV",
  "seats": [
    { "row": "G", "col": 4 },
    { "row": "G", "col": 5 }
  ]
}
```

## 검증 내용

- `database=cgv password=dngur1213 ./gradlew test --tests com.ceos23.cgv.domain.reservation.service.ReservationServiceTest`
- `database=cgv password=dngur1213 ./gradlew test --tests com.ceos23.cgv.domain.reservation.service.ReservedSeatServiceTest`
- `database=cgv password=dngur1213 ./gradlew test`

동시성 통합 테스트는 별도 테스트 DB가 분리되어 있지 않고 현재 테스트가 로컬 MySQL 환경변수에 의존하므로, 이번 변경에서는 서비스 단위 테스트와 전체 테스트로 회귀 여부를 확인했다.

</details>

<details>
<summary>이전 README 내용</summary>

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

<details>
<summary><h2>1️⃣ JWT 인증(Authentication) 방법에 대해서 알아보기</h2></summary>
<div markdown="1">

<br>

<details>
<summary><b>JWT를 이용한 인증 방식(액세스토큰, 리프레쉬 토큰)에 대해서 조사해보아요</b></summary>
<div markdown="1">

- **개념**: 사용자를 인증하고 식별하기 위한 정보를 암호화시킨 토큰입니다. 별도의 세션 저장소 없이 토큰 자체로 검증이 가능하여 Stateless한 현대 웹에서 널리 쓰입니다.
- **구조**: `Header`(타입 및 알고리즘) + `Payload`(클레임 정보, 유저 ID, 만료일시 등) + `Signature`(위변조 검증용 서명)

<img width="1050" height="207" alt="Image" src="https://github.com/user-attachments/assets/8073950e-0be3-4370-9dbf-cce1e1123a68" />

- **토큰의 종류**:
    - **Access Token**: 실제 서버 자원을 요청할 때 헤더에 실어 보내는 수명이 짧은 토큰입니다.
    - **Refresh Token**: Access Token이 만료되었을 때, 새 Access Token을 발급받기 위한 수명이 긴 토큰입니다. (보안을 위해 주로 DB에 저장)

**🔄 JWT 인증 흐름**
1. **로그인**: 사용자가 로그인을 요청하면 서버가 회원 DB를 대조하여 확인합니다.
2. **토큰 발급**: 유효기간이 짧은 `Access Token`과 긴 `Refresh Token`을 함께 생성하여 응답합니다.
3. **API 요청**: 클라이언트는 매 API 요청 시 헤더에 `Access Token`을 담아 보냅니다.
4. **만료 및 재발급**: `Access Token`이 만료되어 401(Unauthorized) 에러를 받으면, 보관해둔 `Refresh Token`을 서버로 보내 유효성 검증 후 새로운 `Access Token`을 발급받아 통신을 재개합니다.

<img width="820" height="382" alt="Image" src="https://github.com/user-attachments/assets/9a7759d1-b631-46f3-bb10-f6870cf0b4dc" />

**✅ 장단점**
- **장점**: 세션 저장소가 필요 없어 서버 자원을 절약하고 확장에 유리합니다. 짧은 수명의 Access Token과 긴 수명의 Refresh Token을 조합해 보안과 사용자 편의성(자동 로그인 유지)을 모두 챙길 수 있습니다.
- **단점**: 한 번 발급된 토큰은 임의로 강제 만료시키기 어렵고, Payload 자체는 누구나 디코딩할 수 있어 민감한 정보를 담을 수 없습니다.

</div>
</details>

<br>

<details>
<summary><b>추가로 세션, 쿠키, OAuth 등 다른 방식도 조사해보아요</b></summary>
<div markdown="1">

**🍪 쿠키(Cookie) 인증**
- **특징**: 브라우저에 저장되는 Key-Value 형태의 문자열입니다. 한 번 설정되면 이후 매 요청마다 브라우저가 자동으로 헤더에 담아 보냅니다.
- **한계**: 용량이 4KB로 제한적이며, 네트워크 상에 값이 그대로 노출되어 보안에 매우 취약합니다.

**🗄️ 세션(Session) 인증**
- **특징**: 비밀번호 같은 민감한 정보는 서버(메모리/DB)에 저장하고, 클라이언트에게는 출입증 역할의 무의미한 고유 식별자(`Session ID`)만 쿠키에 담아 발급합니다.
- **장/단점**: 쿠키 자체에 유의미한 개인정보가 없어 훨씬 안전하지만, 동시 접속자가 많아질수록 서버의 저장 공간과 메모리 부하가 심해집니다. 또한 '세션 하이재킹' 공격의 위험이 있습니다.

**🔐 OAuth 2.0 인증**
- **특징**: 사용자가 비밀번호를 우리 서비스에 직접 제공하지 않고, 구글/카카오 같은 외부의 신뢰할 수 있는 서비스의 인증 및 권한을 위임받아 사용하는 범용 프로토콜입니다.
- **장점**: 사용자는 일일이 회원가입을 할 필요 없이 안전하게 서비스를 이용할 수 있으며, 서비스 개발자는 복잡한 보안 처리를 거대 플랫폼에 위임할 수 있어 더욱 안전합니다.

</div>
</details>

</div>
</details>

<details>
<summary><h2>2️⃣ 액세스 토큰 발급 및 검증 로직 구현하기</h2></summary>
<div markdown="1">

<br>

- **`TokenProvider` 구현**: `io.jsonwebtoken` 라이브러리를 활용하여 Access 및 Refresh Token 생성, 서명(Signature) 검증 로직을 구현했습니다.
- **DB 조회 최소화 (최적화)**: `getAuthentication` 호출 시 매번 DB를 찌르지 않고, **토큰의 Payload에 담긴 유저 식별자(ID)와 권한 정보(Role)만을 이용해 Authentication 객체를 생성**하도록 최적화하여 Stateless한 JWT의 장점을 극대화했습니다.
- **`JwtAuthenticationFilter` 적용**: 헤더(`Authorization`)에서 토큰을 추출해 유효성을 검증한 뒤, 정상 토큰인 경우 `SecurityContextHolder`에 인증 정보를 저장하는 커스텀 필터를 구현했습니다.

</div>
</details>

<details>
<summary><h2>3️⃣ 회원가입 및 로그인 API 구현하고 테스트하기</h2></summary>
<div markdown="1">

<br>

- **비밀번호 암호화**: 회원가입 API 호출 시 `BCryptPasswordEncoder`를 통해 평문 비밀번호를 단방향 암호화하여 DB에 안전하게 저장합니다.
- **로그인 및 토큰 발급**: 로그인 시 `AuthenticationManager`를 통해 계정을 검증하고, 성공 시 `TokenProvider`를 거쳐 Access Token과 Refresh Token을 동시 발급합니다.
  <img width="670" height="569" alt="Image" src="https://github.com/user-attachments/assets/84876572-179f-4ec3-9e12-4927a20b4c00" />

</div>
</details>

<details>
<summary><h2>4️⃣ 토큰이 필요한 API 1개 이상 구현하고 테스트하기</h2></summary>
<div markdown="1">

<br>

- **URL 접근 권한 제어**: 관리자(ADMIN)와 일반 사용자(USER)의 권한(`Role`)을 Enum으로 분리하고, `SecurityConfig`를 통해 `/api/admin/**` 등 경로별 접근 권한을 제어했습니다.
  <img width="814" height="465" alt="Image" src="https://github.com/user-attachments/assets/12242bf1-b371-489d-981d-94222a943c04" />
- **API 보안 개선**: 기존에 Request 파라미터나 바디로 직접 유저 ID를 입력받던 취약한 구조를 개선했습니다. `ReservationController`(예매, 예매 취소) 등에서 `@AuthenticationPrincipal`을 사용해 **검증된 JWT 토큰에서 안전하게 유저 식별자를 추출**해 비즈니스 로직을 처리하도록 리팩토링했습니다.
  <img width="665" height="683" alt="Image" src="https://github.com/user-attachments/assets/477eb54b-da7c-4840-929a-48483d845b30" />

</div>
</details>

<details>
<summary><h2>5️⃣(도전 미션~!) 리프레쉬 토큰 발급 로직 구현하고 테스트하기</h2></summary>
<div markdown="1">

<br>

보안과 사용자 편의성을 모두 잡기 위해 Refresh Token 시스템을 도입했습니다.
- **DB 저장**: 로그인 시 발급된 긴 수명의 Refresh Token을 DB(`User` 엔티티)에 저장합니다.
- **재발급(Reissue) API 구현**: 토큰이 만료되었을 때 클라이언트가 Refresh Token을 보내면, DB에 저장된 토큰과 대조하여 일치할 경우에만 새로운 토큰을 발급합니다.
- **RTR (Refresh Token Rotation) 기법 적용**: 토큰 재발급 시 Access Token뿐만 아니라 **Refresh Token도 함께 새로 발급하고 DB를 갱신**합니다. 이를 통해 Refresh Token이 탈취되더라도 한 번 사용되면 폐기되도록 보안을 한층 강화했습니다.
  <img width="673" height="552" alt="Image" src="https://github.com/user-attachments/assets/ad9cbc21-fbb3-4c9f-9783-73f240caf68d" />

</div>
</details>

</details>
