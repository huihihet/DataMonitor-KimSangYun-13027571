# Phase 2 설계 — Model 구현

> 기준: `docs/PLAN.md > Phase 2` / PRD 마일스톤: M2  
> 목표: 도메인 엔티티와 Repository 인터페이스·구현체를 완성하고 단위 테스트로 검증한다.

---

## 1. 현재 상태

| 파일 | 상태 |
|------|------|
| `model/entity/OrderStatus.java` | 완료 — Phase 1에서 5개 상수 완전 정의 |
| `model/entity/SampleStatus.java` | 완료 — Phase 1에서 record 완전 정의 |
| `model/entity/Sample.java` | 빈 클래스 — 이번 Phase에서 구현 |
| `model/entity/Order.java` | 빈 클래스 — 이번 Phase에서 구현 |
| `model/repository/SampleRepository.java` | 빈 인터페이스 — 이번 Phase에서 구현 |
| `model/repository/OrderRepository.java` | 빈 인터페이스 — 이번 Phase에서 구현 |
| `model/repository/InMemorySampleRepository.java` | 빈 구현체 — 이번 Phase에서 구현 |
| `model/repository/InMemoryOrderRepository.java` | 빈 구현체 — 이번 Phase에서 구현 |
| 테스트 파일 6개 | placeholder — 이번 Phase에서 구현 |

---

## 2. `Sample` 엔티티 설계

### 필드

| 필드 | 타입 | 제약 |
|------|------|------|
| `sampleId` | `Long` | 고유 식별자, null 불허 |
| `name` | `String` | null·공백 불허 |
| `avgProductionTime` | `int` | 1 이상 |
| `yield` | `double` | 0.0 초과 ~ 1.0 이하 |
| `stock` | `int` | 0 이상 |

### 구현 명세

```java
package org.example.model.entity;

public class Sample {
    private final Long   sampleId;
    private final String name;
    private final int    avgProductionTime;
    private final double yield;
    private final int    stock;

    public Sample(Long sampleId, String name, int avgProductionTime, double yield, int stock) {
        // 유효성 검증 후 필드 할당
    }

    // getter 5개 (setter 없음)
}
```

### 유효성 검증 규칙

| 조건 | 예외 메시지 |
|------|------------|
| `sampleId == null` | `"시료 ID는 null일 수 없습니다."` |
| `name == null \|\| name.isBlank()` | `"시료 이름은 공백일 수 없습니다."` |
| `avgProductionTime < 1` | `"평균 생산 시간은 1 이상이어야 합니다."` |
| `yield <= 0.0 \|\| yield > 1.0` | `"수율은 0.0 초과 1.0 이하이어야 합니다."` |
| `stock < 0` | `"재고 수량은 0 이상이어야 합니다."` |

위반 시 `IllegalArgumentException` throw.

---

## 3. `Order` 엔티티 설계

### 필드

| 필드 | 타입 | 제약 |
|------|------|------|
| `orderId` | `Long` | 고유 식별자, null 불허 |
| `sampleId` | `Long` | 연관 시료 ID, null 불허 |
| `customerName` | `String` | null·공백 불허 |
| `quantity` | `int` | 1 이상 |
| `status` | `OrderStatus` | null 불허 |

### 구현 명세

```java
package org.example.model.entity;

public class Order {
    private final Long        orderId;
    private final Long        sampleId;
    private final String      customerName;
    private final int         quantity;
    private final OrderStatus status;

    public Order(Long orderId, Long sampleId, String customerName, int quantity, OrderStatus status) {
        // 유효성 검증 후 필드 할당
    }

    // getter 5개 (setter 없음)
}
```

### 유효성 검증 규칙

| 조건 | 예외 메시지 |
|------|------------|
| `orderId == null` | `"주문 ID는 null일 수 없습니다."` |
| `sampleId == null` | `"시료 ID는 null일 수 없습니다."` |
| `customerName == null \|\| customerName.isBlank()` | `"고객명은 공백일 수 없습니다."` |
| `quantity < 1` | `"주문 수량은 1 이상이어야 합니다."` |
| `status == null` | `"주문 상태는 null일 수 없습니다."` |

위반 시 `IllegalArgumentException` throw.

---

## 4. Repository 인터페이스 설계

### `SampleRepository`

```java
package org.example.model.repository;

public interface SampleRepository {
    List<Sample> findAll();
    Optional<Sample> findById(Long id);
}
```

### `OrderRepository`

```java
package org.example.model.repository;

public interface OrderRepository {
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
    List<Order> findBySampleId(Long sampleId);
}
```

---

## 5. `InMemorySampleRepository` 구현 설계

```java
package org.example.model.repository;

public class InMemorySampleRepository implements SampleRepository {
    private final List<Sample> store;

    public InMemorySampleRepository() {
        this.store = new ArrayList<>(List.of(
            new Sample(1L, "AlphaChip", 30, 0.90, 120),
            new Sample(2L, "BetaWafer", 45, 0.75,   0),
            new Sample(3L, "GammaCore", 60, 0.85,  15)
        ));
    }

    @Override
    public List<Sample> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public Optional<Sample> findById(Long id) {
        return store.stream()
                    .filter(s -> s.getSampleId().equals(id))
                    .findFirst();
    }
}
```

**설계 결정**:
- `findAll()`은 `Collections.unmodifiableList`로 래핑하여 외부에서 `store` 직접 수정을 방지한다

---

## 6. `InMemoryOrderRepository` 구현 설계

```java
package org.example.model.repository;

public class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> store;

    public InMemoryOrderRepository() {
        this.store = new ArrayList<>(List.of(
            new Order(1L, 1L, "서울대 연구실",    50, OrderStatus.RESERVED),
            new Order(2L, 2L, "카이스트 팹리스",  30, OrderStatus.PRODUCING),
            new Order(3L, 3L, "삼성리서치",       20, OrderStatus.CONFIRMED),
            new Order(4L, 1L, "LG이노텍",         10, OrderStatus.RELEASE),
            new Order(5L, 2L, "포스텍",           25, OrderStatus.RESERVED),
            new Order(6L, 3L, "연세대 반도체",    40, OrderStatus.PRODUCING),
            new Order(7L, 1L, "SK하이닉스",       15, OrderStatus.CONFIRMED),
            new Order(8L, 2L, "한양대 연구소",    10, OrderStatus.REJECTED)
        ));
    }

    @Override
    public List<Order> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return store.stream()
                    .filter(o -> o.getStatus() == status)
                    .toList();
    }

    @Override
    public List<Order> findBySampleId(Long sampleId) {
        return store.stream()
                    .filter(o -> o.getSampleId().equals(sampleId))
                    .toList();
    }
}
```

---

## 7. 단위 테스트 설계

> 테스트 메서드명은 `camelCase`를 따른다 (CLAUDE.md 컨벤션).

### 7-1. `SampleTest`

```java
class SampleTest {
    Sample validSample = new Sample(1L, "AlphaChip", 30, 0.90, 120);
}
```

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `createSampleAllFieldsStored` | 모든 getter가 생성자 인수와 일치 |
| `createSampleNullId` | `sampleId=null` → `IllegalArgumentException` |
| `createSampleYieldAtMax` | `yield=1.0` 정상 생성 |
| `createSampleYieldAtMinBoundary` | `yield=0.0` → `IllegalArgumentException` |
| `createSampleYieldOverMax` | `yield=1.001` → `IllegalArgumentException` |
| `createSampleAvgProductionTimeAtMin` | `avgProductionTime=1` 정상 생성 |
| `createSampleAvgProductionTimeZero` | `avgProductionTime=0` → `IllegalArgumentException` |
| `createSampleStockAtMin` | `stock=0` 정상 생성 |
| `createSampleNegativeStock` | `stock=-1` → `IllegalArgumentException` |
| `createSampleNullName` | `name=null` → `IllegalArgumentException` |
| `createSampleBlankName` | `name="  "` → `IllegalArgumentException` |

### 7-2. `OrderTest`

```java
class OrderTest {
    Order validOrder = new Order(1L, 1L, "서울대 연구실", 50, OrderStatus.RESERVED);
}
```

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `createOrderAllFieldsStored` | 모든 getter가 생성자 인수와 일치 |
| `createOrderNullOrderId` | `orderId=null` → `IllegalArgumentException` |
| `createOrderNullSampleId` | `sampleId=null` → `IllegalArgumentException` |
| `createOrderQuantityAtMin` | `quantity=1` 정상 생성 |
| `createOrderQuantityZero` | `quantity=0` → `IllegalArgumentException` |
| `createOrderNullCustomerName` | `customerName=null` → `IllegalArgumentException` |
| `createOrderBlankCustomerName` | `customerName="  "` → `IllegalArgumentException` |
| `createOrderNullStatus` | `status=null` → `IllegalArgumentException` |

### 7-3. `OrderStatusTest`

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `hasAllFiveValues` | `OrderStatus.values().length == 5` |
| `containsExpectedConstants` | RESERVED·PRODUCING·CONFIRMED·RELEASE·REJECTED 포함 확인 |

### 7-4. `SampleStatusTest`

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `createRecordAllComponentsStored` | 모든 컴포넌트 accessor가 생성자 인수와 일치 |

### 7-5. `InMemorySampleRepositoryTest`

```java
class InMemorySampleRepositoryTest {
    SampleRepository repo = new InMemorySampleRepository();
}
```

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `findAllReturnsThreeDummySamples` | 크기 3 반환 |
| `findAllIsUnmodifiable` | 반환 리스트에 `.add()` 시 `UnsupportedOperationException` |
| `findByIdExistingReturnsCorrectSample` | `id=2` → BetaWafer 반환, 모든 필드 일치 |
| `findByIdNonExistingReturnsEmpty` | `id=99` → `Optional.empty()` |

### 7-6. `InMemoryOrderRepositoryTest`

```java
class InMemoryOrderRepositoryTest {
    OrderRepository repo = new InMemoryOrderRepository();
}
```

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `findAllReturnsEightDummyOrders` | 크기 8 반환 |
| `findAllIsUnmodifiable` | 반환 리스트에 `.add()` 시 `UnsupportedOperationException` |
| `findByStatusReservedReturnsTwoOrders` | `RESERVED` → 크기 2 |
| `findByStatusProducingReturnsTwoOrders` | `PRODUCING` → 크기 2 |
| `findByStatusConfirmedReturnsTwoOrders` | `CONFIRMED` → 크기 2 |
| `findByStatusReleaseReturnsOneOrder` | `RELEASE` → 크기 1 |
| `findByStatusRejectedReturnsOneOrder` | `REJECTED` → 크기 1 |
| `findBySampleIdOneReturnsThreeOrders` | `sampleId=1` → 크기 3 (RESERVED·RELEASE·CONFIRMED) |
| `findBySampleIdTwoReturnsThreeOrders` | `sampleId=2` → 크기 3 (PRODUCING·RESERVED·REJECTED) |
| `findBySampleIdNonExistingReturnsEmpty` | `sampleId=99` → 빈 리스트 |

---

## 8. 완료 기준

- [ ] `Sample` 생성자 유효성 검증 11개 케이스 통과 (ID null 포함)
- [ ] `Order` 생성자 유효성 검증 8개 케이스 통과 (ID null 포함)
- [ ] `InMemorySampleRepository` 더미 데이터 3개 초기화 및 조회·불변성 검증 통과
- [ ] `InMemoryOrderRepository` 더미 데이터 8개 초기화 및 조회·불변성 검증 통과
- [ ] `./gradlew test` 전 테스트 통과
- [ ] `./gradlew jacocoTestReport` — Model 커버리지 80% 이상
