# 테스트 전략 검증 보고서

**일시**: 2026-06-12
**검증 대상**: docs/PLAN.md (DataMonitor PoC)
**결과**: 미흡 11건 (CRITICAL: 4, WARNING: 7)

---

## 발견된 문제

### [CRITICAL-1] F-05 타임스탬프 포맷 검증 테스트 누락

- **대상 기능**: F-05 (갱신 타임스탬프 표시) / `MonitoringView.printHeader()`
- **문제**: `MonitoringViewTest`에 헤더·구분선·테이블 행 포맷 검증이 언급되어 있으나, `HH:mm:ss` 형식 타임스탬프 문자열이 헤더 출력에 포함되는지 명시적으로 검증하는 테스트 케이스가 PLAN에 존재하지 않는다. PRD F-05는 독립 요구사항임에도 대응 테스트가 없다.
- **권장 테스트**:
  - `printHeader("14:32:05")` 호출 시 출력 문자열에 `"14:32:05"` 포함 여부 검증
  - `render()` 호출 시 전달된 timestamp 값이 헤더에 반영되는지 `MonitoringControllerTest`에서 Spy로 캡처 검증

---

### [CRITICAL-2] 재고 상태 판별 경계값 stock == demand 케이스 누락

- **대상 기능**: PRD §4 재고 상태 판별 규칙 / `MonitoringController.refresh()`
- **문제**: PRD의 "여유" 조건은 `stock >= demand`(>=, 이상)이다. 현재 PLAN의 테스트 케이스는 stock=120/demand=50(여유), stock=15/demand=40(부족), stock=0(고갈)만 검증하며, 경계값인 `stock == demand` 케이스(예: stock=50, demand=50)가 없다. 구현 시 `>` 와 `>=` 혼동으로 발생하는 버그를 검출하지 못한다.
- **권장 테스트**:
  - stock=50, RESERVED quantity=50인 커스텀 픽스처 → 재고 상태 "여유" 검증
  - stock=49, RESERVED quantity=50인 커스텀 픽스처 → 재고 상태 "부족" 검증

---

### [CRITICAL-3] demand == 0 (주문 없는 시료) 재고 상태 케이스 누락

- **대상 기능**: PRD §4 재고 상태 판별 규칙 / `MonitoringController.refresh()`
- **문제**: RESERVED + PRODUCING 주문이 0건인 시료(demand=0)일 때 stock > 0이면 "여유"가 되어야 한다. 더미 데이터에는 이 케이스가 없으며 PLAN 테스트에도 명시되지 않았다. 집계 로직이 `demand=0`을 올바르게 처리하는지(NullPointer, 0 나누기 등) 검증이 없다.
- **권장 테스트**:
  - RESERVED + PRODUCING 주문이 없는 시료(stock=10, demand=0) → 재고 상태 "여유" 검증
  - stock=0, demand=0인 시료 → 재고 상태 "고갈" 검증(stock==0 규칙 우선)

---

### [CRITICAL-4] Sample.name 및 null 입력에 대한 유효성 검증 테스트 누락

- **대상 기능**: PRD §4 Sample 엔티티 유효성 / `Sample` 생성자
- **문제**: `SampleTest`에 name 관련 유효성 케이스가 전혀 없다. PLAN 2-2에서 "공백 불허" 조건을 명시했으나 대응 테스트 케이스가 없다. `OrderTest`에는 customerName 공백 케이스가 있지만 null 케이스는 없다. 또한 sampleId/orderId null 입력 케이스도 없다.
- **권장 테스트**:
  - `new Sample(...)` 호출 시 name에 `null` 전달 → `IllegalArgumentException` 검증
  - `new Sample(...)` 호출 시 name에 `""` 또는 `"  "` 전달 → `IllegalArgumentException` 검증
  - `new Order(...)` 호출 시 customerName에 `null` 전달 → `IllegalArgumentException` 검증

---

### [WARNING-1] yield/avgProductionTime 유효 경계값(1.0, 1) 명시 테스트 누락

- **대상 기능**: PRD §4 Sample 엔티티 유효성 / `SampleTest`
- **문제**: yield=0.0(예외)과 yield=1.0 초과(예외) 케이스는 있으나, 유효 경계값인 `yield=1.0`(정상), `yield=0.0` 초과 최솟값(`yield=0.001` 등), `avgProductionTime=1`(정상 최솟값)을 명시적으로 검증하는 케이스가 없다. 경계값 테스트의 양면(예외/정상 경계 모두)이 필요하다.
- **권장 테스트**:
  - `new Sample(..., yield=1.0, ...)` → 정상 생성 검증
  - `new Sample(..., avgProductionTime=1, ...)` → 정상 생성 검증
  - `new Order(..., quantity=1, ...)` → 정상 생성 검증

---

### [WARNING-2] Order quantity 최솟값(1) 경계값 테스트 누락

- **대상 기능**: PRD §4 Order 엔티티 유효성 / `OrderTest`
- **문제**: `quantity 0 이하` 예외 케이스는 있으나 유효 최솟값 `quantity=1` 정상 케이스가 없다. `SampleTest`의 정상 생성 케이스와 달리 `OrderTest` 정상 생성 케이스가 quantity=1 경계를 커버하는지 불명확하다.
- **권장 테스트**:
  - `new Order(..., quantity=1, ...)` → 정상 생성 및 quantity 필드값 1 검증

---

### [WARNING-3] MonitoringView 빈 데이터 렌더링 케이스 누락

- **대상 기능**: F-01, F-02 / `MonitoringView.render()`
- **문제**: 주문이 0건이거나 시료가 0건일 때 View가 어떻게 렌더링되는지 테스트가 없다. 빈 Map 또는 빈 List를 전달했을 때 NPE, 포맷 오류 없이 출력되는지 검증이 필요하다.
- **권장 테스트**:
  - `render(emptyMap, emptyList, "00:00:00")` 호출 시 예외 없이 실행 완료 검증
  - 빈 주문 현황 시 각 상태가 0건으로 출력되는지 확인

---

### [WARNING-4] InMemoryOrderRepository.findBySampleId() 미존재 ID 케이스 누락

- **대상 기능**: `InMemoryOrderRepository.findBySampleId()` / `InMemoryOrderRepositoryTest`
- **문제**: `findBySampleId(2)` → 3건 반환 케이스만 있고, 존재하지 않는 sampleId(예: 999L) 조회 시 빈 리스트 반환을 검증하는 케이스가 없다. Controller의 demand 계산이 빈 리스트를 전달받아도 정상 동작하는지 경로가 테스트되지 않는다.
- **권장 테스트**:
  - `findBySampleId(999L)` → 빈 리스트 반환 검증
  - `findByStatus(CONFIRMED)` 반환 건수(더미 기준 2건) 케이스 추가

---

### [WARNING-5] REJECTED 집계 제외 검증 방법 미명시

- **대상 기능**: PRD §4 REJECTED 제외 규칙 / `MonitoringControllerTest`
- **문제**: "건수 집계에 REJECTED 미포함" 케이스는 명시되어 있으나, 구체적으로 무엇을 assertion하는지 기술되지 않았다. `statusCounts.containsKey(REJECTED) == false`인지, 또는 전체 건수 합계가 7(=8-1)인지 등 검증 방법이 불명확하다. 구현자가 다르게 해석할 여지가 있다.
- **권장 테스트**:
  - `statusCounts.containsKey(OrderStatus.REJECTED)` → false 검증
  - 또는 statusCounts의 모든 값의 합 → 7 검증 (REJECTED 1건 제외)

---

### [WARNING-6] MonitoringLoop.stop() 단위 테스트 부재

- **대상 기능**: F-04 (종료) / `MonitoringLoop`
- **문제**: PLAN에서 MonitoringLoop 폴링 루프 전체를 자동화 제외로 처리했다. 그러나 `stop()` 메서드의 `running` 플래그 설정 자체는 Thread.sleep 없이 단위 테스트가 가능하다. stop() 호출 후 running이 false가 되는지 정도의 상태 검증은 자동화가 타당하다.
- **권장 테스트**:
  - `MonitoringLoop` 인스턴스 생성 후 `stop()` 호출 → `isRunning()` 또는 리플렉션으로 `running == false` 검증
  - F-04 종료 메시지("모니터링을 종료합니다.") 출력 검증은 `MonitoringViewTest` 또는 별도 케이스로 분리

---

### [WARNING-7] ANSI 미지원 환경 폴백 테스트 미계획

- **대상 기능**: PRD §6 ANSI 색상 조건부 적용 / `MonitoringView`
- **문제**: Phase 4 통합 테스트에서 ANSI 코드 포함 여부(`\033[31m`, `\033[33m`)를 검증하지만, PRD §6에서 "ANSI 미지원 시 텍스트만 출력"을 요구한다. 이 폴백 경로를 검증하는 테스트나 전략이 PLAN 어디에도 없다. ANSI 적용 여부 분기 자체가 구현될 경우 해당 분기의 테스트가 없는 것이다.
- **권장 테스트**:
  - ANSI 색상 분기 여부를 플래그/전략 패턴으로 구현하는 경우 플래그=false일 때 텍스트만 출력됨 검증
  - 또는 출력 문자열에서 ANSI 코드 제거 후 "부족", "고갈" 텍스트 포함 여부를 별도 검증

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 테스트 계획 존재 | 미흡 | F-04 종료, F-05 타임스탬프 대응 케이스 누락 |
| [B] 엣지케이스 식별 | 미흡 | stock==demand, demand==0, 빈 데이터, null 입력 미포함 |
| [C] 기존 테스트 충돌 | 통과 | ConsoleMVC 테스트 전부 placeholder 상태, 실질 충돌 없음 |
| [D] 테스트 구조 | 통과 | 단위/통합 계층 분리, 픽스처 구조 적절. MonitoringLoop 제외 사유 명시됨 |
