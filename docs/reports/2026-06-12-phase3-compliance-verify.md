# 컴플라이언스 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: `docs/design/phase3.md` — Phase 3 설계 (Controller · View 구현)  
**결과**: ❌ 위반 2건 (CRITICAL: 1, WARNING: 1)

---

## 발견된 위반

### [CRITICAL] render() 흐름 코드 블록이 app 레이어 상수를 직접 참조

- **위치**: `docs/design/phase3.md` — 3절 `MonitoringView` 설계, `render()` 흐름 코드 블록
- **위반 규칙**: 루트 CLAUDE.md 및 DataMonitor CLAUDE.md 레이어 분리 원칙 — View 레이어는 다른 레이어(특히 `app` 패키지)를 직접 참조해서는 안 된다.
- **현재 설계**:
  ```
  clearScreen()
  printHeader(timestamp)
  printOrderSummary(statusCounts)
  printInventory(sampleStatuses)
  printFooter(MonitoringLoop.REFRESH_INTERVAL_SECONDS)
  ```
  `render()` 흐름 코드 블록이 `app.MonitoringLoop.REFRESH_INTERVAL_SECONDS`를 참조한다.  
  설계 문서 내 주석("render()가 MonitoringLoop.REFRESH_INTERVAL_SECONDS 상수를 참조하면 레이어 위반이다")이 위반을 인지하고 있으나, 실제 흐름 코드 블록은 수정 없이 위반 형태 그대로 남아 있다.  
  이 코드 블록대로 구현하면 View가 `app` 패키지 클래스에 의존하여 레이어 경계가 무너진다.
- **권장 수정**: 설계 문서에 이미 서술된 의도대로 흐름 코드 블록을 수정한다.
  ```
  clearScreen()
  printHeader(timestamp)
  printOrderSummary(statusCounts)
  printInventory(sampleStatuses)
  printFooter(3)   // Phase 3: 하드코딩. Phase 4에서 호출자(MonitoringLoop)가 인수로 전달
  ```
  또는 `render()` 시그니처에 `int intervalSeconds`를 추가하여 호출자가 주입하도록 설계한다.

---

### [WARNING] 경계값 테스트에서 익명 Repository 구현체 사용이 CLAUDE.md 테스트 전략 지침과 정합성 불명확

- **위치**: `docs/design/phase3.md` — 4-2절 `MonitoringControllerTest`, 경계값 케이스
- **위반 규칙**: DataMonitor CLAUDE.md 테스트 전략 — "Controller: Spy View + **실제 InMemory Repository**로 흐름 검증". 루트 CLAUDE.md Repository 구현체 명명 규칙 — "저장 방식 접두사 사용 (e.g., `InMemorySampleRepository`)".
- **현재 설계**: `stock==demand`, `demand==0` 경계값 케이스에서 `OrderRepository`와 `SampleRepository`의 익명 구현체를 테스트 클래스 내부에서 직접 선언한다. 익명 클래스는 이름이 없으므로 명명 규칙을 준수할 수 없으며, "실제 InMemory Repository"가 아닌 임시 구현체이다.
- **권장 수정**: 경계값 픽스처를 지원하는 `StubSampleRepository`, `StubOrderRepository` 등 명명된 내부 정적 클래스로 선언하거나, 기존 `InMemorySampleRepository` / `InMemoryOrderRepository`에 데이터를 직접 삽입(`save()` 등)하는 방식으로 교체한다. 이미 4절 서두에 "테스트 더블은 내부 클래스로 직접 작성한다"는 지침이 있으므로, 익명 클래스 대신 명명된 `private static class`를 사용하는 것이 일관성 있는 접근이다.

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 아키텍처 제약 | ❌ | render() 흐름에서 View → app 레이어 참조 (CRITICAL) |
| [B] 코딩 컨벤션 | ✅ | PascalCase / camelCase / UPPER_SNAKE_CASE 모두 준수 |
| [C] 보안 | ✅ | 해당 없음 (콘솔 읽기 전용, 사용자 입력 없음) |
| [D] 불필요한 복잡성 | ✅ | 오버엔지니어링 없음 |

### 항목별 세부 결과 (요청된 9개 체크 항목)

| # | 체크 항목 | 결과 |
|---|-----------|------|
| 1 | MonitoringController Constructor Injection만 사용 | ✅ |
| 2 | MonitoringController에 System.out 직접 호출 없음 | ✅ |
| 3 | MonitoringView에 집계·판별 비즈니스 로직 없음 | ✅ |
| 4 | ANSI 색상 상수가 private static final로 View 내부 정의 | ✅ |
| 5 | render() 위임 순서 준수 및 직접 계산 없음 | ❌ (흐름 코드 블록에 레이어 위반 참조 포함) |
| 6 | SpyMonitoringView 상속 기반 설계 — 테스트 격리 문제 없음 | ✅ |
| 7 | 익명 Repository 구현체 사용 — CLAUDE.md 정합성 | ⚠️ (WARNING) |
| 8 | 클래스명·메서드명·변수명 컨벤션 준수 | ✅ |
| 9 | calcStockLevel private 헬퍼 — Controller 내 위치 적합 | ✅ |
