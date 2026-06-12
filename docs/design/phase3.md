# Phase 3 설계 — Controller · View 구현

> 기준: `docs/PLAN.md > Phase 3` / PRD 마일스톤: M3  
> 목표: 집계 로직을 Controller에, 대시보드 출력 포맷을 View에 구현하고 단위 테스트로 검증한다.

---

## 1. 현재 상태

| 파일 | 상태 |
|------|------|
| `model/entity/*`, `model/repository/*` | 완료 — Phase 2에서 구현 |
| `controller/MonitoringController.java` | 빈 클래스 — 이번 Phase에서 구현 |
| `view/MonitoringView.java` | 빈 클래스 — 이번 Phase에서 구현 |
| `MonitoringControllerTest.java` | placeholder — 이번 Phase에서 구현 |
| `MonitoringViewTest.java` | placeholder — 이번 Phase에서 구현 |

---

## 2. `MonitoringController` 설계

### 구조

```java
package org.example.controller;

public class MonitoringController {
    private final SampleRepository sampleRepo;
    private final OrderRepository  orderRepo;
    private final MonitoringView   view;

    public MonitoringController(SampleRepository sampleRepo,
                                OrderRepository orderRepo,
                                MonitoringView view) { ... }

    public void refresh() { ... }
}
```

### `refresh()` 흐름

```
1. orderRepo.findAll()  → List<Order> orders
2. REJECTED 제외 상태별 건수 집계
      Map<OrderStatus, Long> statusCounts
      = orders.stream()
              .filter(o -> o.getStatus() != OrderStatus.REJECTED)
              .collect(groupingBy(Order::getStatus, counting()))
3. sampleRepo.findAll() → List<Sample> samples
4. 시료별 SampleStatus 생성
   for each sample:
       demandSum = orders.stream()
                         .filter(o -> o.getSampleId() == sample.getSampleId()
                                   && (o.getStatus() == RESERVED || o.getStatus() == PRODUCING))
                         .mapToInt(Order::getQuantity).sum()
       stockLevel = calcStockLevel(sample.getStock(), demandSum)
       → new SampleStatus(sampleId, name, stock, stockLevel)
5. timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
6. view.render(statusCounts, sampleStatusList, timestamp)
```

### 재고 상태 판별 규칙 (`calcStockLevel`)

| 조건 | 반환값 |
|------|--------|
| `stock == 0` | `"고갈"` |
| `stock < demandSum` | `"부족"` |
| `stock >= demandSum` (demand==0 포함) | `"여유"` |

> `calcStockLevel`은 `private` 헬퍼 메서드로 분리한다. 테스트에서 `refresh()` 흐름으로 간접 검증한다.

### 제약
- `System.out` 직접 호출 금지
- `Scanner`, 콘솔 I/O 금지

---

## 3. `MonitoringView` 설계

### 구조

```java
package org.example.view;

public class MonitoringView {
    private static final String SEPARATOR  = "=".repeat(40);
    private static final String ANSI_RESET  = "\033[0m";
    private static final String ANSI_YELLOW = "\033[33m";
    private static final String ANSI_RED    = "\033[31m";

    public void render(Map<OrderStatus, Long> statusCounts,
                       List<SampleStatus> sampleStatuses,
                       String timestamp) { ... }

    public void clearScreen() { ... }
    public void printHeader(String timestamp) { ... }
    public void printOrderSummary(Map<OrderStatus, Long> statusCounts) { ... }
    public void printInventory(List<SampleStatus> sampleStatuses) { ... }
    public void printFooter(int intervalSeconds) { ... }
    public void printShutdownMessage() { ... }
}
```

### `render()` 흐름

```
clearScreen()
printHeader(timestamp)
printOrderSummary(statusCounts)
printInventory(sampleStatuses)
printFooter(3)   // PoC 범위에서 상수값 직접 전달 — app 패키지 참조 금지
```

> `render()`가 `MonitoringLoop.REFRESH_INTERVAL_SECONDS`를 참조하면 View → app 역방향 의존이 생긴다.  
> PoC 범위에서는 `render()` 내부에서 `3`을 직접 전달하여 레이어 분리를 유지한다.

### 출력 포맷

```
========================================
  DataMonitor — S-Semi 실시간 모니터링
  마지막 갱신: 14:32:05  (q: 종료)
========================================

[주문 현황]
  RESERVED  :   2건
  PRODUCING :   2건
  CONFIRMED :   2건
  RELEASE   :   1건

[시료별 재고 현황]
  ID  이름            재고  상태
  --  ----------  -------  ----
   1  AlphaChip       120   여유
   2  BetaWafer         0   고갈
   3  GammaCore        15   부족

========================================
  다음 갱신까지 3초...
========================================
```

### 컬럼 포맷 규칙

| 컬럼 | 너비 | 정렬 |
|------|------|------|
| ID | 4 | 우측 (`%4d`) |
| 이름 | 12 | 좌측 (`%-12s`) |
| 재고 | 7 | 우측 (`%7d`) |
| 상태 | — | 좌측 |

> 한글 이름은 문자 너비가 2바이트이므로 `%-12s`는 실제 터미널 출력 시 정렬이 맞지 않을 수 있다.  
> PoC 범위에서는 Java 표준 포맷 문자열을 사용하고 정밀 정렬은 SSemi 본 프로젝트에서 처리한다.

### ANSI 색상 적용 규칙

| stockLevel | 적용 코드 |
|------------|----------|
| `"부족"` | `ANSI_YELLOW + "부족" + ANSI_RESET` |
| `"고갈"` | `ANSI_RED + "고갈" + ANSI_RESET` |
| `"여유"` | 색상 없음 |

---

## 4. 테스트 전략

외부 Mock 라이브러리(Mockito 등)를 사용하지 않는다. 테스트 더블은 내부 클래스로 직접 작성한다.

### 4-1. `MonitoringViewTest`

`ByteArrayOutputStream`으로 `System.out`을 교체하여 출력 결과를 캡처한다.

```java
class MonitoringViewTest {
    private final PrintStream           originalOut = System.out;   // 원본 저장
    private final ByteArrayOutputStream outContent  = new ByteArrayOutputStream();
    private final MonitoringView        view        = new MonitoringView();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);   // 원본 복원 — 다른 테스트 오염 방지
        outContent.reset();
    }
}
```

**픽스처 데이터**

```java
Map<OrderStatus, Long> statusCounts = Map.of(
    OrderStatus.RESERVED,  2L,
    OrderStatus.PRODUCING, 2L,
    OrderStatus.CONFIRMED, 2L,
    OrderStatus.RELEASE,   1L
);

List<SampleStatus> sampleStatuses = List.of(
    new SampleStatus(1L, "AlphaChip", 120, "여유"),
    new SampleStatus(2L, "BetaWafer",   0, "고갈"),
    new SampleStatus(3L, "GammaCore",  15, "부족")
);
```

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `renderContainsSeparator` | 출력에 `"========"` 포함 |
| `renderContainsTitle` | 출력에 `"DataMonitor"` 포함 |
| `renderContainsTimestamp` | `printHeader("14:32:05")` 호출 시 `"14:32:05"` 포함 |
| `renderContainsQuitHint` | 출력에 `"(q: 종료)"` 포함 |
| `renderContainsOrderSummaryHeader` | 출력에 `"[주문 현황]"` 포함 |
| `renderContainsReservedCount` | 출력에 `"RESERVED"` 및 `"2"` 포함 |
| `renderContainsInventoryHeader` | 출력에 `"[시료별 재고 현황]"` 포함 |
| `renderContainsYeoyu` | 여유 행에 `"여유"` 포함 |
| `renderContainsBuchok` | 부족 행에 `"\033[33m"` + `"부족"` 포함 (ANSI 코드 직접 비교) |
| `renderContainsGogal` | 고갈 행에 `"\033[31m"` + `"고갈"` 포함 (ANSI 코드 직접 비교) |
| `renderContainsFooter` | 출력에 `"다음 갱신까지"` 포함 |
| `printShutdownMessageContainsText` | `"모니터링을 종료합니다."` 포함 |

### 4-2. `MonitoringControllerTest`

실제 InMemory Repository + 수동 Spy View를 사용한다.

**SpyMonitoringView (내부 클래스)**

```java
private static class SpyMonitoringView extends MonitoringView {
    Map<OrderStatus, Long> capturedStatusCounts;
    List<SampleStatus>     capturedSampleStatuses;
    String                 capturedTimestamp;
    int                    renderCallCount = 0;

    @Override
    public void render(Map<OrderStatus, Long> statusCounts,
                       List<SampleStatus> sampleStatuses,
                       String timestamp) {
        this.capturedStatusCounts   = statusCounts;
        this.capturedSampleStatuses = sampleStatuses;
        this.capturedTimestamp      = timestamp;
        this.renderCallCount++;
    }

    @Override
    public void clearScreen() {}  // 테스트 중 화면 클리어 방지
}
```

**픽스처**

```java
class MonitoringControllerTest {
    SpyMonitoringView        spyView    = new SpyMonitoringView();
    SampleRepository         sampleRepo = new InMemorySampleRepository();
    OrderRepository          orderRepo  = new InMemoryOrderRepository();
    MonitoringController     controller = new MonitoringController(sampleRepo, orderRepo, spyView);
}
```

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `refreshCallsViewRenderOnce` | `refresh()` 호출 시 `renderCallCount == 1` |
| `refreshStatusCountsReservedTwo` | `statusCounts.get(RESERVED) == 2` |
| `refreshStatusCountsProducingTwo` | `statusCounts.get(PRODUCING) == 2` |
| `refreshStatusCountsConfirmedTwo` | `statusCounts.get(CONFIRMED) == 2` |
| `refreshStatusCountsReleaseOne` | `statusCounts.get(RELEASE) == 1` |
| `refreshRejectedExcludedFromCounts` | `statusCounts.containsKey(REJECTED) == false` |
| `refreshAlphaChipStockLevelYeoyu` | sampleId=1(AlphaChip, stock=120, demand=50) → `"여유"` |
| `refreshBetaWaferStockLevelGogal` | sampleId=2(BetaWafer, stock=0) → `"고갈"` |
| `refreshGammaCoreStockLevelBuchok` | sampleId=3(GammaCore, stock=15, demand=40) → `"부족"` |
| `refreshTimestampFormat` | `capturedTimestamp.matches("\\d{2}:\\d{2}:\\d{2}")` — HH:mm:ss 형식 검증 |

**경계값 케이스** (명명된 Stub Repository 사용)

`stock == demand`, `demand == 0`, `stock==0 & demand==0` 케이스는 실제 더미 데이터로 검증하기 어려우므로,  
테스트 클래스 내 `private static` 명명 클래스로 특정 데이터를 반환하는 Stub Repository를 작성하여 주입한다.

```java
private static class StubSampleRepository implements SampleRepository {
    private final List<Sample> samples;
    StubSampleRepository(Sample... samples) { this.samples = List.of(samples); }
    @Override public List<Sample> findAll() { return samples; }
    @Override public Optional<Sample> findById(Long id) { return Optional.empty(); }
}

private static class StubOrderRepository implements OrderRepository {
    private final List<Order> orders;
    StubOrderRepository(Order... orders) { this.orders = List.of(orders); }
    @Override public List<Order> findAll() { return orders; }
    @Override public List<Order> findByStatus(OrderStatus s) { return List.of(); }
    @Override public List<Order> findBySampleId(Long id) { return List.of(); }
}
```

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `refreshStockEqualsDemandIsYeoyu` | `stock=50, demand=50` (RESERVED qty=50) → `"여유"` (`>=` 등호 경계) |
| `refreshDemandZeroStockPositiveIsYeoyu` | RESERVED·PRODUCING 주문 없는 시료, `stock=10` → `"여유"` |
| `refreshStockZeroDemandZeroIsGogal` | `stock=0, demand=0` → `"고갈"` (stock==0 우선 판별) |
| `refreshConfirmedReleasedExcludedFromDemand` | CONFIRMED·RELEASE 주문만 있는 시료, `stock=5` → `"여유"` (demand==0) |

---

## 5. 완료 기준

- [ ] `MonitoringController.refresh()` — `System.out` 직접 호출 없음
- [ ] `MonitoringView` — 집계·판별 비즈니스 로직 없음
- [ ] `MonitoringControllerTest` 14개 케이스 전부 통과 (경계값 4개 포함)
- [ ] `MonitoringViewTest` 12개 케이스 전부 통과
- [ ] `./gradlew test` 전 테스트 통과
- [ ] `./gradlew jacocoTestReport` — Controller 커버리지 80% 이상
