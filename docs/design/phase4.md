# Phase 4 설계 — MonitoringLoop · Main 조립

> 기준: `docs/PLAN.md > Phase 4` / PRD 마일스톤: M4  
> 목표: 폴링 루프와 키 입력 감지를 조립하고 전체 흐름을 통합 검증한다.

---

## 1. 현재 상태

| 파일 | 상태 |
|------|------|
| `controller/MonitoringController.java` | 완료 — Phase 3에서 구현 |
| `view/MonitoringView.java` | 완료 — Phase 3에서 구현 |
| `app/MonitoringLoop.java` | 빈 클래스 — 이번 Phase에서 구현 |
| `Main.java` | 빈 main() — 이번 Phase에서 구현 |
| `MonitoringIntegrationTest.java` | placeholder — 이번 Phase에서 구현 |

---

## 2. `MonitoringLoop` 설계

### 구조

```java
package org.example.app;

public class MonitoringLoop {
    static final int REFRESH_INTERVAL_SECONDS = 3;

    private final MonitoringController controller;
    private volatile boolean            running;

    public MonitoringLoop(MonitoringController controller) { ... }

    public void start() { ... }  // 폴링 루프 실행 — 블로킹 (q 입력까지 반복)
    public void stop()  { ... }  // 루프 종료 플래그 설정
}
```

### 스레드 설계

| 스레드 | 역할 | 종류 |
|--------|------|------|
| 메인 스레드 | 폴링 루프 (`refresh → sleep → 반복`) | 일반 스레드 |
| 입력 감지 스레드 | `Scanner`로 표준 입력 대기, `"q"` 입력 시 `stop()` 호출 | **daemon thread** |

- `volatile boolean running` — 스레드 간 가시성 보장
- daemon thread로 설정 → JVM 종료 시 자동 정리, `System.in` 블로킹 해제 불필요

### `start()` 흐름

```
start() {
    running = true

    [입력 감지 스레드 — daemon]
        Scanner sc = new Scanner(System.in)
        while (running) {
            if (sc.hasNextLine() && sc.nextLine().trim().equals("q"))
                stop()
        }

    [메인 루프]
        while (running) {
            controller.refresh()
            try { Thread.sleep(REFRESH_INTERVAL_SECONDS * 1000L) }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); break }
        }
        controller.shutdown()   // View 의존 없이 Controller에 종료 위임
}
```

### `stop()` 구현

```java
public void stop() {
    running = false;
}
```

### 제약

- `start()` 내부에서 `System.out` 직접 호출 금지 — 출력은 Controller → View 경유
- 입력 감지 스레드는 반드시 `setDaemon(true)` 설정
- `MonitoringLoop`은 `view` 패키지를 import하지 않는다 — View 의존 금지

---

## 3. `Main` 설계

```java
package org.example;

public class Main {
    public static void main(String[] args) {
        SampleRepository     sampleRepo = new InMemorySampleRepository();
        OrderRepository      orderRepo  = new InMemoryOrderRepository();
        MonitoringView       view       = new MonitoringView();
        MonitoringController controller = new MonitoringController(sampleRepo, orderRepo, view);
        MonitoringLoop       loop       = new MonitoringLoop(controller);
        loop.start();
    }
}
```

- 객체 조립 순서: Repository → View → Controller → Loop
- 모든 의존성은 생성자 주입
- `MonitoringLoop`에 View를 전달하지 않는다 — PRD §8 아키텍처 준수

### `MonitoringController` 추가 메서드

Phase 3 구현체에 `shutdown()` 메서드를 추가한다.

```java
// controller/MonitoringController.java 에 추가
public void shutdown() {
    view.printShutdownMessage();
}
```

- Loop가 종료 시 `controller.shutdown()`을 호출 → Controller가 View에 위임
- Loop → View 직접 의존 제거

---

## 4. 통합 테스트 설계

### 4-1. 테스트 환경 구성

`MonitoringLoop`의 폴링 루프(`Thread.sleep` 기반)는 자동화 테스트에서 제외한다.  
통합 테스트는 `controller.refresh()` 1회 호출로 전체 렌더링 시나리오를 검증한다.

```java
class MonitoringIntegrationTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream           originalOut = System.out;

    private SampleRepository     sampleRepo;
    private OrderRepository      orderRepo;
    private MonitoringView       view;
    private MonitoringController controller;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        sampleRepo = new InMemorySampleRepository();
        orderRepo  = new InMemoryOrderRepository();
        view       = new MonitoringView();
        controller = new MonitoringController(sampleRepo, orderRepo, view);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        outContent.reset();
    }
}
```

### 4-2. 테스트 케이스

| 테스트 메서드 | 검증 내용 |
|--------------|-----------|
| `refreshRendersOnce` | `controller.refresh()` 1회 호출 시 `"DataMonitor"` 포함 출력 |
| `refreshStatusCountsMatchDummyData` | 출력에 `"RESERVED"`, `"PRODUCING"`, `"CONFIRMED"`, `"RELEASE"` 모두 포함 |
| `refreshRejectedExcluded` | 출력에 `"REJECTED"` 미포함 |
| `refreshAlphaChipYeoyu` | 출력에 `"AlphaChip"` + `"여유"` 포함 |
| `refreshBetaWaferGogal` | 출력에 `"BetaWafer"` + `"\033[31m"` (ANSI 적색) 포함 |
| `refreshGammaCorePartial` | 출력에 `"GammaCore"` + `"\033[33m"` (ANSI 황색) 포함 |
| `refreshTimestampInHeader` | 출력에 `HH:mm:ss` 형식 타임스탬프 포함 (`\\d{2}:\\d{2}:\\d{2}` 패턴 매칭) |

### 4-3. 시나리오별 더미 데이터 검증 기준 (PRD §9)

| 시료 | stock | RESERVED+PRODUCING demand | 예상 상태 |
|------|-------|--------------------------|-----------|
| AlphaChip (id=1) | 120 | 50 (orderId=1, RESERVED) | 여유 |
| BetaWafer (id=2) | 0 | 55 (orderId=2 qty=30 PRODUCING + orderId=5 qty=25 RESERVED) | 고갈 |
| GammaCore (id=3) | 15 | 40 (orderId=6, PRODUCING) | 부족 |

---

## 5. 완료 기준

- [ ] `MonitoringLoop` — `view` 패키지 import 없음, `System.out` 직접 호출 없음
- [ ] `MonitoringLoop` 입력 감지 스레드가 daemon thread로 설정됨
- [ ] `MonitoringController.shutdown()` 추가 — `view.printShutdownMessage()` 위임
- [ ] `Main` 객체 조립 순서 준수 (Repository → View → Controller → Loop)
- [ ] `MonitoringIntegrationTest` 7개 케이스 전부 통과
- [ ] `./gradlew test` 전 테스트 통과 (Phase 1·2·3 테스트 포함)
- [ ] `./gradlew run` 실행 시 대시보드 3초 간격 자동 갱신 확인 (수동 검증)
- [ ] `q` 입력 시 "모니터링을 종료합니다." 출력 후 정상 종료 (수동 검증)
