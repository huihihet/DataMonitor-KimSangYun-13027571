# PLAN — DataMonitor PoC

> 기준 문서: `docs/PRD.md`  
> 개발 순서: 스켈레톤 → Model → Controller·View → 루프 조립 → 최종 검증

---

## Phase 1. 프로젝트 스켈레톤 `> PRD 마일스톤: M1`

**목표**: 패키지 구조와 빈 클래스를 생성하고 빌드가 통과하는 상태를 만든다.  
더미 데이터 초기화를 Repository 구현체에 포함하여 이후 단계에서 바로 사용 가능하게 한다.

### 작업 목록

1. `build.gradle` 수정
   - `application` 플러그인 추가, `mainClass = 'org.example.Main'` 지정
   - JaCoCo 플러그인 추가 (`jacocoTestReport` 태스크 활성화)
2. 패키지 디렉터리 생성
   ```
   src/main/java/org/example/
   ├── Main.java
   ├── model/entity/
   ├── model/repository/
   ├── controller/
   ├── view/
   └── app/
   src/test/java/org/example/   (미러링)
   ```
3. 빈 클래스 파일 생성 (컴파일 가능한 상태)
   - `Sample.java`, `Order.java`
   - `SampleRepository.java`, `OrderRepository.java`
   - `InMemorySampleRepository.java`, `InMemoryOrderRepository.java`
   - `MonitoringController.java`, `MonitoringView.java`
   - `MonitoringLoop.java`, `Main.java`
4. 완전 정의 파일 생성 (Phase 1에서 확정)
   - `OrderStatus.java` — 5개 상수(`RESERVED, PRODUCING, CONFIRMED, RELEASE, REJECTED`) 완전 정의
   - `SampleStatus.java` — record 4개 컴포넌트(`sampleId, name, stock, stockLevel`) 완전 정의
5. 테스트 placeholder 파일 생성 (`src/test/` 하위 8개)
   - `SampleTest`, `OrderTest`, `OrderStatusTest`, `SampleStatusTest`
   - `InMemorySampleRepositoryTest`, `InMemoryOrderRepositoryTest`
   - `MonitoringControllerTest`, `MonitoringViewTest`, `MonitoringIntegrationTest`

### 완료 기준

- [ ] `./gradlew build` 성공
- [ ] `./gradlew test` 통과 (placeholder 테스트 포함)
- [ ] 모든 클래스 파일이 패키지 규칙에 맞게 위치

---

## Phase 2. Model 구현 `> PRD 마일스톤: M2`

**목표**: 도메인 엔티티, 리포지터리 인터페이스·구현체를 작성하고 단위 테스트로 검증한다.

### 2-1. `OrderStatus` 열거형

> Phase 1에서 완전 정의 완료. Phase 2에서 별도 수정 없이 그대로 사용한다.

### 2-2. `Sample` 엔티티

```java
// model/entity/Sample.java
class Sample {
    Long   sampleId           // 고유 식별자
    String name               // 공백 불허
    int    avgProductionTime  // 1 이상
    double yield              // 0.0 초과 ~ 1.0 이하
    int    stock              // 0 이상
}
```

- 생성자에서 유효성 검증 — 위반 시 `IllegalArgumentException`
- getter 제공 (setter 없음 — 불변 객체)

### 2-3. `Order` 엔티티

```java
// model/entity/Order.java
class Order {
    Long        orderId       // 고유 식별자
    Long        sampleId      // 연관 시료 ID
    String      customerName  // 고객명, 공백 불허
    int         quantity      // 1 이상
    OrderStatus status        // 주문 상태
}
```

- 생성자에서 유효성 검증 — 위반 시 `IllegalArgumentException`
- getter 제공 (setter 없음 — 불변 객체)

### 2-4. Repository 인터페이스

```java
// model/repository/SampleRepository.java
interface SampleRepository {
    List<Sample> findAll();
    Optional<Sample> findById(Long id);
}

// model/repository/OrderRepository.java
interface OrderRepository {
    List<Order> findAll();
    List<Order> findByStatus(OrderStatus status);
    List<Order> findBySampleId(Long sampleId);
}
```

### 2-5. `InMemorySampleRepository` 구현체

- 생성자에서 PRD 9절 더미 데이터(시료 3개) 초기화
- `findAll()`: 전체 목록 반환
- `findById(Long)`: ID 일치 시료 반환

**더미 데이터 (PRD §9 기준)**

| sampleId | name | avgProductionTime | yield | stock |
|----------|------|-------------------|-------|-------|
| 1 | AlphaChip | 30 | 0.90 | 120 |
| 2 | BetaWafer | 45 | 0.75 | 0 |
| 3 | GammaCore | 60 | 0.85 | 15 |

### 2-6. `InMemoryOrderRepository` 구현체

- 생성자에서 PRD 9절 더미 데이터(주문 8개) 초기화
- `findAll()`: 전체 목록 반환
- `findByStatus(OrderStatus)`: 상태 일치 주문 필터링
- `findBySampleId(Long)`: sampleId 일치 주문 필터링

**더미 데이터 (PRD §9 기준)**

| orderId | sampleId | customerName | quantity | status |
|---------|----------|--------------|----------|--------|
| 1 | 1 | 서울대 연구실 | 50 | RESERVED |
| 2 | 2 | 카이스트 팹리스 | 30 | PRODUCING |
| 3 | 3 | 삼성리서치 | 20 | CONFIRMED |
| 4 | 1 | LG이노텍 | 10 | RELEASE |
| 5 | 2 | 포스텍 | 25 | RESERVED |
| 6 | 3 | 연세대 반도체 | 40 | PRODUCING |
| 7 | 1 | SK하이닉스 | 15 | CONFIRMED |
| 8 | 2 | 한양대 연구소 | 10 | REJECTED |

### 2-7. 단위 테스트

**`SampleTest`**

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| 정상 생성 | 모든 필드 저장 확인 |
| sampleId null | `IllegalArgumentException` 발생 |
| yield=1.0 (유효 최댓값) | 정상 생성 |
| yield=0.0 이하 | `IllegalArgumentException` 발생 |
| yield=1.0 초과 | `IllegalArgumentException` 발생 |
| avgProductionTime=1 (유효 최솟값) | 정상 생성 |
| avgProductionTime 0 이하 | `IllegalArgumentException` 발생 |
| stock=0 (유효 최솟값) | 정상 생성 |
| stock 음수 | `IllegalArgumentException` 발생 |
| name null | `IllegalArgumentException` 발생 |
| name 공백 문자열 (`"  "`) | `IllegalArgumentException` 발생 |

**`OrderTest`**

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| 정상 생성 | 모든 필드 저장 확인 |
| orderId null | `IllegalArgumentException` 발생 |
| sampleId null | `IllegalArgumentException` 발생 |
| quantity=1 (유효 최솟값) | 정상 생성 |
| quantity 0 이하 | `IllegalArgumentException` 발생 |
| customerName null | `IllegalArgumentException` 발생 |
| customerName 공백 (`"  "`) | `IllegalArgumentException` 발생 |
| status null | `IllegalArgumentException` 발생 |

**`OrderStatusTest`**

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| 상수 개수 확인 | `OrderStatus.values().length == 5` |
| 모든 상수 포함 여부 | RESERVED·PRODUCING·CONFIRMED·RELEASE·REJECTED 포함 확인 |

**`SampleStatusTest`**

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| record 컴포넌트 저장 확인 | 모든 accessor가 생성자 인수와 일치 |

**`InMemorySampleRepositoryTest`**

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| findAll | 더미 데이터 3건 반환 |
| findAll 불변성 | 반환 리스트 `.add()` 시 `UnsupportedOperationException` |
| findById (존재) | `id=2` → BetaWafer 반환, 모든 필드 일치 |
| findById (미존재) | `id=99` → `Optional.empty()` |

**`InMemoryOrderRepositoryTest`**

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| findAll | 더미 데이터 8건 반환 |
| findAll 불변성 | 반환 리스트 `.add()` 시 `UnsupportedOperationException` |
| findByStatus(RESERVED) | 2건 반환 |
| findByStatus(PRODUCING) | 2건 반환 |
| findByStatus(CONFIRMED) | 2건 반환 |
| findByStatus(RELEASE) | 1건 반환 |
| findByStatus(REJECTED) | 1건 반환 |
| findBySampleId(1) | 3건 반환 |
| findBySampleId(2) | 3건 반환 |
| findBySampleId(미존재) | 빈 리스트 반환 |

### 완료 기준

- [ ] `Sample`, `Order` 유효성 검증 동작
- [ ] InMemory Repository 더미 데이터 초기화 및 조회 정상 동작
- [ ] 단위 테스트 전부 통과, Model 커버리지 80% 이상

---

## Phase 3. Controller · View 구현 `> PRD 마일스톤: M3`

**목표**: 집계 로직을 Controller에 구현하고, 대시보드 포맷을 View에 구현한다.

### 3-1. `MonitoringController`

Constructor Injection — `SampleRepository`, `OrderRepository` 주입

```java
class MonitoringController {
    MonitoringController(SampleRepository sampleRepo,
                         OrderRepository orderRepo,
                         MonitoringView view)

    void refresh();  // 집계 후 View 위임 (F-01, F-02, F-05)
}
```

**`refresh()` 내부 흐름**

1. `OrderRepository.findAll()`로 전체 주문 조회
2. 상태별 건수 집계 (REJECTED 제외)
   ```
   Map<OrderStatus, Long> statusCounts
   ```
3. `SampleRepository.findAll()`로 전체 시료 조회
4. 시료별 재고 상태 판별 (PRD §4 규칙 적용)
   - 해당 시료의 RESERVED + PRODUCING 주문 `quantity` 합산 → `demandSum`
   - `stock == 0` → 고갈
   - `stock < demandSum` → 부족
   - `stock >= demandSum` → 여유 (demand == 0 포함)
5. 타임스탬프 생성: `LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))`
6. `MonitoringView.render(statusCounts, sampleStatusList, timestamp)` 호출
7. `System.out` 직접 호출 금지

**전달 데이터 구조**

```java
// model/entity/SampleStatus.java — View에 넘길 시료별 현황 레코드
record SampleStatus(long sampleId, String name, int stock, String stockLevel) {}
// stockLevel: "여유" | "부족" | "고갈"
```

### 3-2. `MonitoringView`

출력 전용 — `System.out` 사용, 집계·계산 로직 없음

| 메서드 | 출력 내용 |
|--------|-----------|
| `render(Map<OrderStatus,Long>, List<SampleStatus>, String timestamp)` | 전체 대시보드 1회 출력 |
| `printHeader(String timestamp)` | 헤더 배너 + 타임스탬프 |
| `printOrderSummary(Map<OrderStatus,Long>)` | 상태별 건수 섹션 |
| `printInventory(List<SampleStatus>)` | 시료별 재고 테이블 |
| `printFooter(int intervalSeconds)` | "다음 갱신까지 N초..." |
| `printShutdownMessage()` | "모니터링을 종료합니다." |
| `clearScreen()` | `\033[H\033[2J` 출력으로 화면 클리어 |

**출력 포맷 (PRD §6 기준)**

```
========================================
  DataMonitor — S-Semi 실시간 모니터링
  마지막 갱신: HH:mm:ss  (q: 종료)
========================================

[주문 현황]
  RESERVED  :   N건
  PRODUCING :   N건
  CONFIRMED :   N건
  RELEASE   :   N건

[시료별 재고 현황]
  ID  이름          재고   상태
  --  ----------  ------  ----
   1  AlphaChip      120   여유
   2  BetaWafer        0   고갈  ← \033[31m 적용
   3  GammaCore       15   부족  ← \033[33m 적용

========================================
  다음 갱신까지 3초...
========================================
```

- 컬럼 너비: ID(4) / 이름(12) / 재고(7) / 상태(4)
- ANSI 색상: 부족 `\033[33m`, 고갈 `\033[31m`, 리셋 `\033[0m`

### 3-3. 단위 테스트

**`MonitoringViewTest`**

- `ByteArrayOutputStream`으로 `System.out` 캡처
- 헤더·구분선·테이블 행 포맷 검증
- 각 재고 상태 행에 올바른 레이블 포함 여부 확인
- 타임스탬프가 `HH:mm:ss` 형식으로 헤더에 출력되는지 확인
- `printShutdownMessage()` 호출 시 "모니터링을 종료합니다." 출력 확인

**`MonitoringControllerTest`**

- `InMemorySampleRepository` + `InMemoryOrderRepository` 실제 구현체 주입
- Spy / Mock `MonitoringView` 주입
- `refresh()` 호출 시 `view.render()` 가 올바른 집계 결과로 호출되는지 검증

| 테스트 케이스 | 검증 내용 |
|--------------|-----------|
| 상태별 건수 집계 | RESERVED 2, PRODUCING 2, CONFIRMED 2, RELEASE 1 |
| REJECTED 제외 | 건수 집계에 REJECTED 미포함 |
| 재고 상태 — 고갈 | BetaWafer(stock=0) → "고갈" |
| 재고 상태 — 부족 | GammaCore(stock=15, demand=40) → "부족" (PRODUCING orderId=6 qty=40만 합산) |
| 재고 상태 — 여유 | AlphaChip(stock=120, demand=50) → "여유" (RESERVED orderId=1 qty=50만 합산) |
| 재고 상태 — 경계(stock==demand) | stock=50, demand=50 → "여유" (>= 조건 등호 경계) |
| 재고 상태 — demand=0 | RESERVED·PRODUCING 주문 없는 시료 → stock>0이면 "여유" |

### 완료 기준

- [ ] Controller에 `System.out` 직접 호출 없음
- [ ] View에 집계·조건 분기 비즈니스 로직 없음
- [ ] `MonitoringControllerTest` 집계 결과 전부 통과
- [ ] Controller 커버리지 80% 이상

---

## Phase 4. MonitoringLoop · Main 조립 `> PRD 마일스톤: M4`

**목표**: 폴링 루프와 키 입력 감지를 조립하고 전체 흐름을 통합 검증한다.

### 4-1. `MonitoringLoop`

```java
class MonitoringLoop {
    static final int REFRESH_INTERVAL_SECONDS = 3;

    MonitoringLoop(MonitoringController controller)

    void start();  // 폴링 루프 실행 — 블로킹 (q 입력까지 반복)
    void stop();   // 루프 종료 플래그 설정
}
```

**동작 흐름**

```
start() 진입
  ├─ [별도 스레드] Scanner로 표준 입력 대기
  │       └─ "q" 입력 시 stop() 호출
  └─ [메인 루프]
        1. controller.refresh() 호출
        2. Thread.sleep(REFRESH_INTERVAL_SECONDS * 1000)
        3. stop 플래그 확인 → false면 1로 반복
        4. stop 플래그 true → view.printShutdownMessage() 호출 후 종료
```

- `volatile boolean running` 플래그로 스레드 간 가시성 보장
- 키 입력 스레드는 `daemon thread`로 설정 (JVM 종료 시 자동 정리)

### 4-2. `Main`

```java
public class Main {
    public static void main(String[] args) {
        SampleRepository      sampleRepo  = new InMemorySampleRepository();
        OrderRepository       orderRepo   = new InMemoryOrderRepository();
        MonitoringView        view        = new MonitoringView();
        MonitoringController  controller  = new MonitoringController(sampleRepo, orderRepo, view);
        MonitoringLoop        loop        = new MonitoringLoop(controller, view);
        loop.start();
    }
}
```

### 4-3. 통합 시나리오 테스트

| 시나리오 | 검증 내용 |
|----------|-----------|
| 1회 렌더링 정상 동작 | `controller.refresh()` 호출 시 View가 1회 출력 완료 |
| 더미 데이터 기준 집계 정확도 | 주문 현황 건수 및 시료별 상태가 PRD §9 더미 데이터와 일치 |
| ANSI 색상 포함 여부 | 고갈 행에 `\033[31m`, 부족 행에 `\033[33m` 포함 |

> `MonitoringLoop`의 폴링 루프 자체는 자동화 테스트에서 제외한다.  
> `Thread.sleep` 의존 코드의 테스트는 실용적이지 않으므로 수동 실행으로 검증한다.

### 완료 기준

- [ ] `./gradlew run` 실행 후 대시보드가 3초 간격으로 자동 갱신
- [ ] `q` 입력 시 "모니터링을 종료합니다." 출력 후 정상 종료
- [ ] 통합 시나리오 테스트 전부 통과

---

## Phase 5. ANSI 색상 · 최종 검증 `> PRD 마일스톤: M5`

**목표**: ANSI 색상을 적용하고, 역할 분리 규칙 준수 및 커버리지를 최종 확인한다.

### 5-1. ANSI 색상 상수 정의

> Phase 3 구현 시 `MonitoringView`에 선행 정의된다. Phase 5에서는 적용 여부와 동작을 재검증한다.

`MonitoringView` 내부 `private static final` 상수로 정의한다.

```java
private static final String ANSI_RESET  = "\033[0m";
private static final String ANSI_YELLOW = "\033[33m";
private static final String ANSI_RED    = "\033[31m";
```

- "부족" → `ANSI_YELLOW` 적용
- "고갈" → `ANSI_RED` 적용
- ANSI 미지원 환경에서도 텍스트 자체는 정상 출력

### 5-2. 최종 체크리스트

- [ ] Controller 클래스에 `System.out` 직접 호출 없음 (`grep` 확인)
- [ ] Model 클래스에 `Scanner` 또는 `System` 코드 없음 (`grep` 확인)
- [ ] View 클래스에 집계·판별 비즈니스 로직 없음 (코드 리뷰)
- [ ] `./gradlew test` 전 테스트 통과
- [ ] `./gradlew jacocoTestReport` — Model·Controller 커버리지 80% 이상
- [ ] PRD 수용 기준(Acceptance Criteria) 전 항목 충족

---

## 파일 생성 체크리스트 (전체)

```
src/main/java/org/example/
├── Main.java
├── model/
│   ├── entity/
│   │   ├── Sample.java
│   │   ├── Order.java
│   │   ├── OrderStatus.java
│   │   └── SampleStatus.java        # Controller → View 전달용 record
│   └── repository/
│       ├── SampleRepository.java
│       ├── OrderRepository.java
│       ├── InMemorySampleRepository.java
│       └── InMemoryOrderRepository.java
├── controller/
│   └── MonitoringController.java
├── view/
│   └── MonitoringView.java
└── app/
    └── MonitoringLoop.java

src/test/java/org/example/
├── model/
│   ├── entity/
│   │   ├── SampleTest.java
│   │   ├── OrderTest.java
│   │   ├── OrderStatusTest.java
│   │   └── SampleStatusTest.java    # record 필드 검증
│   └── repository/
│       ├── InMemorySampleRepositoryTest.java
│       └── InMemoryOrderRepositoryTest.java
├── controller/
│   └── MonitoringControllerTest.java
├── view/
│   └── MonitoringViewTest.java
└── integration/
    └── MonitoringIntegrationTest.java
```
