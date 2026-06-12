# 테스트 전략 검증 보고서

**일시**: 2026-06-12
**검증 대상**: `docs/design/phase3.md` (Phase 3 — Controller · View 구현)
**결과**: 미흡 5건 (CRITICAL: 2, WARNING: 3)

---

## 발견된 문제

### [CRITICAL-1] tearDown()의 System.setOut 복원 코드가 원본 스트림을 복원하지 못함

- **대상 기능**: `MonitoringViewTest` — `@AfterEach tearDown()`
- **문제**: 설계 문서의 `tearDown()` 코드가 `System.setOut(System.out)` 으로 작성되어 있다.
  `setUp()` 시점에 `System.out` 은 이미 `ByteArrayOutputStream` 을 감싼 `PrintStream` 으로 교체된
  상태이므로, tearDown 시점의 `System.out` 은 원본 스트림이 아니다.
  결과적으로 복원이 전혀 이루어지지 않아 이후 실행되는 모든 테스트(Controller, Integration 등)의
  표준 출력이 오염된다. JUnit 5 병렬 실행 환경에서는 더 심각한 부작용을 유발한다.
- **권장 테스트 및 수정 방향**:
  ```java
  // 필드 선언 시 원본 스트림 저장
  private final PrintStream originalOut = System.out;

  @BeforeEach
  void setUp() {
      System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void tearDown() {
      System.setOut(originalOut);  // 저장해 둔 원본으로 복원
      outContent.reset();           // 버퍼도 초기화하여 테스트 간 격리
  }
  ```

---

### [CRITICAL-2] capturedTimestamp의 HH:mm:ss 형식 검증 누락 — PRD F-05 및 PLAN.md와 불일치

- **대상 기능**: `MonitoringControllerTest` — `refreshTimestampNotNull`
- **문제**: 설계 문서의 테스트 케이스가 `capturedTimestamp != null && !capturedTimestamp.isBlank()`
  수준에서만 검증한다. PRD §5 F-05는 "마지막 갱신 시각을 `HH:mm:ss` 형식으로 헤더에 표시"를
  명시하고 있으며, PLAN.md Phase 3-3 역시 "타임스탬프가 `HH:mm:ss` 형식으로 헤더에 출력되는지
  확인"을 요구한다. `DateTimeFormatter.ofPattern("HH:mm:ss")` 패턴이 올바른지 여부는
  null/blank 검사로는 잡을 수 없다 (예: 잘못된 패턴 `"H:m:s"` 적용 시에도 통과).
  이유에 대한 설명이 설계 문서에 없으므로 의도적 생략인지 누락인지 불명확하다.
- **권장 추가 테스트**:
  ```java
  @Test
  void refreshTimestampMatchesHHmmssFormat() {
      controller.refresh();
      assertNotNull(spyView.capturedTimestamp);
      assertTrue(
          spyView.capturedTimestamp.matches("\\d{2}:\\d{2}:\\d{2}"),
          "타임스탬프가 HH:mm:ss 형식이어야 한다. 실제값: " + spyView.capturedTimestamp
      );
  }
  ```

---

### [WARNING-1] stock==0이고 demand==0인 경계 케이스 누락

- **대상 기능**: `MonitoringControllerTest` — 경계값 케이스 (`refreshDemandZeroIsYeoyu`)
- **문제**: 설계의 재고 판별 규칙은 `stock==0` 조건이 `stock < demandSum` 조건보다 우선한다.
  따라서 RESERVED·PRODUCING 주문이 없어 demand=0이더라도 stock=0이면 "고갈"이어야 한다.
  현재 경계값 케이스는 `stock>0, demand=0` → "여유" 만 커버하고,
  `stock==0, demand==0` → "고갈" 케이스가 명시되어 있지 않다.
  `calcStockLevel` 구현 시 조건 순서를 뒤바꾸면 이 케이스에서 "여유"를 반환하는 버그가 발생할 수 있다.
- **권장 추가 테스트**:
  ```java
  @Test
  void refreshStockZeroDemandZeroIsGogal() {
      // stock=0인 시료에 RESERVED·PRODUCING 주문이 없는 경우 → "고갈"
      // stock==0 조건이 demand 비교보다 우선함을 검증
      ...
      assertEquals("고갈", sampleStatus.stockLevel());
  }
  ```

---

### [WARNING-2] ANSI 이스케이프 코드 직접 비교 방식의 환경 의존성 미언급

- **대상 기능**: `MonitoringViewTest` — `renderContainsBuchok`, `renderContainsGogal`
- **문제**: 설계 문서는 `ANSI_YELLOW + "부족"`, `ANSI_RED + "고갈"` 포함 여부를 직접 비교하는
  방식을 채택했다. PRD §6은 "ANSI 미지원 환경에서는 텍스트만 출력"을 허용하고 있으나,
  설계 문서는 이 테스트가 ANSI 미지원 환경(일부 CI, Windows cmd)에서 실패할 수 있다는
  점을 언급하지 않는다.
  두 방식의 트레이드오프 — 직접 비교(색상 누락 감지 가능, 환경 의존) vs 텍스트만 비교(환경 독립,
  색상 누락 감지 불가) — 중 어느 것을 선택할지 의사결정 근거가 없다.
- **권장 보완**: 설계 문서에 "이 테스트는 `System.out` 캡처 기반이므로 터미널 ANSI 지원 여부와
  무관하게 이스케이프 코드 문자열 자체를 검증한다"는 전제 조건을 명시해야 한다.
  또는 ANSI 코드 존재 여부를 별도 케이스로 분리하고 `@Disabled("ANSI 지원 환경에서만 실행")` 등의
  조건부 처리 방침을 기술한다.

---

### [WARNING-3] CONFIRMED·RELEASE 주문 quantity가 demandSum에서 제외되는지 검증 없음

- **대상 기능**: `MonitoringControllerTest` — 재고 상태 판별 케이스 전반
- **문제**: 설계의 demandSum 산출 로직은 RESERVED + PRODUCING 주문만 합산하며
  CONFIRMED, RELEASE 주문은 제외한다. 현재 더미 데이터에는 AlphaChip에 대해
  CONFIRMED(orderId=7, qty=15)와 RELEASE(orderId=4, qty=10) 주문이 있으나,
  이들이 demandSum 계산에서 실제로 제외된다는 것을 명시적으로 검증하는 케이스가 없다.
  우연히 올바른 집계 결과가 나오는 데이터 배치에 의존하고 있어, 필터 조건 실수를 감지하기 어렵다.
- **권장 추가 테스트**:
  ```java
  @Test
  void refreshConfirmedAndReleaseExcludedFromDemand() {
      // stock=100, CONFIRMED qty=200, RELEASING qty=200, RESERVED qty=0
      // → demandSum=0 이므로 "여유" 가 되어야 함
      // → 잘못 구현하면 CONFIRMED+RELEASE qty 합산 400 > stock 100 으로 "부족" 반환
      ...
      assertEquals("여유", sampleStatus.stockLevel());
  }
  ```

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 테스트 계획 존재 | 통과 | MonitoringViewTest 12케이스, ControllerTest 12케이스 명시 |
| [B] 엣지케이스 식별 | 미흡 | stock==0 & demand==0 케이스 누락, CONFIRMED/RELEASE 제외 검증 누락 |
| [C] 기존 테스트 충돌 | 통과 | Phase 2 Model 테스트와 충돌 없음, 새 클래스 추가만 발생 |
| [D] 테스트 구조 | 미흡 | tearDown() 복원 코드 결함, 타임스탬프 형식 검증 누락, ANSI 환경 의존성 미언급 |

---

## 체크 항목별 평가

| # | 항목 | 판정 | 근거 |
|---|------|------|------|
| 1 | @BeforeEach/@AfterEach 구조 정의 | 구조는 올바름, 복원 코드 결함 | setUp 패턴은 표준적이나 tearDown의 `System.setOut(System.out)` 이 원본 미복원 (CRITICAL-1) |
| 2 | ANSI 코드 직접 비교 vs 텍스트 비교 | 직접 비교 채택, 환경 의존성 미언급 | 색상 누락 감지 측면은 우수하나 PRD의 ANSI 미지원 허용과 충돌 가능성 설명 없음 (WARNING-2) |
| 3 | SpyMonitoringView 충분성 | 충분함 | 4개 캡처 필드 + clearScreen 오버라이드로 Mockito 없이 완전 검증 가능 |
| 4 | 익명 구현체 경계값 케이스 적절성 | 부분 누락 | stock==0 & demand==0 케이스 미명시 (WARNING-1) |
| 5 | REJECTED 제외 검증 충분성 | 부분 미흡 | statusCounts key 미포함은 검증하나, demandSum 계산에서도 제외되는지 별도 케이스 없음 |
| 6 | renderCallCount 검증 필요성 | 적절함 | render() 중복 호출 또는 미호출 버그를 감지하는 데 필수적 |
| 7 | tearDown() 복원 설계 | 결함 있음 | 원본 스트림 미저장으로 복원 불가 (CRITICAL-1) |
| 8 | capturedTimestamp 형식 검증 누락 | 부적절함 | PLAN.md 명시 요건 미충족, PRD F-05와 불일치 (CRITICAL-2) |
