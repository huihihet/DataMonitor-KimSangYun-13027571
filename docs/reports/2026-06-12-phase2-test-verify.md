# 테스트 전략 검증 보고서

**일시**: 2026-06-12
**검증 대상**: `docs/design/phase2.md` (DataMonitor PoC Phase 2 — Model 구현)
**참조 문서**: `docs/PRD.md`, `docs/PLAN.md`
**결과**: 미흡 5건 (CRITICAL: 2, WARNING: 3)

---

## 발견된 문제

### [CRITICAL-1] 식별자(sampleId, orderId) null 검증 케이스 누락

- **대상 기능**: `Sample` 생성자, `Order` 생성자
- **문제**:
  - phase2.md 섹션 2 필드 표에 `sampleId`: "null 불허", 섹션 3 필드 표에 `orderId`: "null 불허", `sampleId(Order)`: "null 불허"로 각각 명시되어 있다.
  - 그러나 섹션 7-1 `SampleTest` 테스트 케이스 10개, 섹션 7-2 `OrderTest` 테스트 케이스 6개 어디에도 해당 필드가 null일 때 `IllegalArgumentException`을 발생시키는 케이스가 없다.
  - 유효성 검증 규칙 표(섹션 2, 3)에도 `sampleId == null` / `orderId == null` 조건이 누락되어, 구현 시 해당 검증 자체를 생략할 위험이 있다.
- **권장 테스트**:
  ```
  // SampleTest 추가
  createSample_nullSampleId → IllegalArgumentException 발생

  // OrderTest 추가
  createOrder_nullOrderId      → IllegalArgumentException 발생
  createOrder_nullSampleId     → IllegalArgumentException 발생
  ```
- **권장 설계 보완**: phase2.md 섹션 2·3 유효성 검증 규칙 표에 아래 행 추가 필요
  ```
  sampleId == null  →  "시료 ID는 null일 수 없습니다."
  orderId  == null  →  "주문 ID는 null일 수 없습니다."
  sampleId(Order) == null  →  "주문의 시료 ID는 null일 수 없습니다."
  ```

---

### [CRITICAL-2] findAll() 불변성 검증 케이스 누락 (설계 결정과 테스트 불일치)

- **대상 기능**: `InMemorySampleRepository.findAll()`, `InMemoryOrderRepository.findAll()`
- **문제**:
  - phase2.md 섹션 5 "설계 결정" 항목에 "`findAll()`은 `Collections.unmodifiableList`로 래핑하여 외부 변경 방지"라고 명시되어 있다.
  - 그러나 섹션 7-5 `InMemorySampleRepositoryTest`, 섹션 7-6 `InMemoryOrderRepositoryTest` 테스트 케이스 표에 해당 불변성을 검증하는 케이스가 존재하지 않는다.
  - 이 불일치는 설계 의도가 테스트로 보장되지 않음을 의미한다. `store`가 `ArrayList`로 생성되어 있어, 래핑을 누락하면 호출측이 반환 리스트를 직접 수정하여 내부 상태를 오염시킬 수 있다.
- **권장 테스트**:
  ```
  // InMemorySampleRepositoryTest 추가
  findAll_returnedList_isUnmodifiable
    → repo.findAll().add(anyElement) 호출 시 UnsupportedOperationException 발생 검증

  // InMemoryOrderRepositoryTest 추가
  findAll_returnedList_isUnmodifiable
    → repo.findAll().add(anyElement) 호출 시 UnsupportedOperationException 발생 검증
  ```

---

### [WARNING-1] findByStatus(PRODUCING), findByStatus(CONFIRMED) 케이스 누락

- **대상 기능**: `InMemoryOrderRepository.findByStatus()`
- **문제**:
  - 더미 데이터 8건 중 PRODUCING 2건, CONFIRMED 2건이 있으나, phase2.md 섹션 7-6 테스트 케이스 표에 `findByStatus(PRODUCING)` 및 `findByStatus(CONFIRMED)` 케이스가 없다.
  - 현재 명세된 케이스는 RESERVED(2건), REJECTED(1건), RELEASE(1건)뿐으로, 필터링 로직이 모든 상태에 대해 정확히 동작하는지 보장하지 못한다.
  - PLAN.md 섹션 2-7 표에서는 RELEASE 케이스마저 빠져 있어 PLAN.md와 phase2.md 간 커버 목록도 불일치한다.
- **권장 테스트**:
  ```
  findByStatus_producing_returnsTwoOrders   → PRODUCING 2건 반환 검증
  findByStatus_confirmed_returnsTwoOrders   → CONFIRMED 2건 반환 검증
  ```
  PLAN.md 섹션 2-7 표에도 `findByStatus(RELEASE) → 1건` 케이스를 추가하여 phase2.md와 일치시킬 것.

---

### [WARNING-2] customerName blankName 케이스의 검증 값이 isBlank() 의도를 충분히 검증하지 못함

- **대상 기능**: `Order` 생성자 — `customerName` 유효성 검증
- **문제**:
  - phase2.md 섹션 7-2 `createOrder_blankCustomerName` 케이스의 검증 내용 값이 `customerName=""` (빈 문자열)로만 명시되어 있다.
  - 구현 명세에는 `customerName.isBlank()`를 사용하도록 되어 있는데, `isBlank()`는 `""` 외에 `" "` (공백 문자열), `"\t"` (탭) 등도 true를 반환한다.
  - `""` 케이스만으로는 `isBlank()` 대신 실수로 `isEmpty()`를 사용해도 테스트가 통과하므로, 구현 오류를 탐지하지 못한다.
  - `SampleTest`의 `createSample_blankName`은 `name="  "` (공백 문자열)로 올바르게 명시되어 있어 Order와 불일치한다.
- **권장 테스트**:
  ```
  createOrder_blankCustomerName 의 검증 값을 "  " (공백 문자열) 또는
  두 케이스로 분리:
    createOrder_emptyCustomerName  → customerName="" → IllegalArgumentException
    createOrder_blankCustomerName  → customerName="  " → IllegalArgumentException
  ```

---

### [WARNING-3] findBySampleId(1) 반환 건수 검증 케이스 누락

- **대상 기능**: `InMemoryOrderRepository.findBySampleId()`
- **문제**:
  - 더미 데이터에서 sampleId=1 주문은 orderId 1(RESERVED), 4(RELEASE), 7(CONFIRMED) 총 3건으로, sampleId=2의 3건(PRODUCING·RESERVED·REJECTED)과 구성 상태가 다르다.
  - 현재 phase2.md에는 `findBySampleId(2) → 3건` 케이스만 있고 sampleId=1에 대한 케이스가 없다.
  - 필터링 구현이 `getSampleId().equals(sampleId)` 비교를 올바르게 수행하는지 단일 케이스만으로는 확인 범위가 제한된다. sampleId=1 케이스를 추가하면 equals 비교의 정확성을 교차 검증할 수 있다.
- **권장 테스트**:
  ```
  findBySampleId_one_returnsThreeOrders → sampleId=1 → 크기 3 (RESERVED·RELEASE·CONFIRMED 구성 확인)
  ```

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 테스트 계획 존재 | 부분 통과 | Sample·Order·Repository 각 테스트 클래스 명세 존재, 단 식별자 null 케이스 누락 |
| [B] 엣지케이스 식별 | 미흡 | sampleId/orderId null 미포함, blankName 값 불충분, findByStatus 미완성 |
| [C] 기존 테스트 충돌 | 통과 | 현재 모두 placeholder, 충돌 없음. Phase 3가 Phase 2 더미 데이터에 의존하는 구조적 주의사항은 문서에 미명시 |
| [D] 테스트 구조 | 통과 | 단위/통합 분리 적절, 픽스처 계획 명시됨 |

### 체크 항목별 결과 (요청 기준)

| 번호 | 항목 | 결과 |
|------|------|------|
| 1 | PRD §4 Sample 유효성 경계값 완전 포함 여부 | 미흡 — sampleId null 케이스 누락 |
| 2 | PRD §4 Order 유효성 규칙 완전 커버 여부 | 미흡 — orderId·sampleId null 누락, blankName 값 불충분 |
| 3 | 재고 상태 판별이 Phase 2 범위 밖임이 명확한지 | 통과 — Phase 3 MonitoringControllerTest로 명확히 분리됨 |
| 4 | InMemoryOrderRepository 더미 집계 검증 충분성 | 미흡 — PRODUCING·CONFIRMED 케이스 없음 |
| 5 | 누락된 엣지케이스 | 미흡 — findAll 불변성, findBySampleId(1) 건수 미명세 |
| 6 | SampleStatus record 테스트 Phase 2 적절성 | 조건부 통과 — record 단일 케이스는 구조상 적절하나, stockLevel 허용값 제약 미명시 |
