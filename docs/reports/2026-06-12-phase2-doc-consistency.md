# 문서 정합성 검증 보고서

**일시**: 2026-06-12
**검증 문서**:
- `docs/design/phase2.md`
- `docs/PLAN.md`
- `docs/PRD.md`
- `CLAUDE.md`

**결과**: 문제 3건 발견 (CRITICAL: 0, WARNING: 2, INFO: 1)

---

## 발견된 문제

### [WARNING] PLAN.md §2-7 OrderTest 케이스 표에 `status == null` 검증 누락

- **위치**: `docs/PLAN.md` — §2-7 `OrderTest` 테스트 케이스 표
- **설명**: `docs/design/phase2.md` §7-2에는 `createOrder_nullStatus` (`status=null` → `IllegalArgumentException`) 테스트 케이스가 정의되어 있다. 그러나 `docs/PLAN.md` §2-7의 `OrderTest` 케이스 표에는 이 항목이 없다. phase2.md의 `Order` 유효성 검증 규칙(§3)에는 `status == null` 조건이 명시되어 있어 테스트 구현은 올바르나, PLAN 문서와 역방향 불일치가 발생한다.
- **권장 조치**: `docs/PLAN.md` §2-7 `OrderTest` 케이스 표에 `status null → IllegalArgumentException 발생` 항목을 추가하여 phase2.md와 동기화한다.

---

### [WARNING] PLAN.md §2-7에 `OrderStatusTest` · `SampleStatusTest` 케이스 표 미정의

- **위치**: `docs/PLAN.md` — §2-7 단위 테스트 절
- **설명**: `docs/design/phase2.md` §7-3에는 `OrderStatusTest` 2개 케이스, §7-4에는 `SampleStatusTest` 1개 케이스가 정의되어 있다. 그러나 `docs/PLAN.md` §2-7에는 이 두 테스트 클래스에 대한 케이스 표가 아예 존재하지 않는다. Phase 1에서 파일이 완전 정의된 이후 Phase 2에서 placeholder 테스트를 구현하는 설계 근거는 타당하나, PLAN 문서에 케이스 수준의 명세가 빠져 있어 PLAN만 보면 두 테스트의 검증 내용을 파악할 수 없다.
- **권장 조치**: `docs/PLAN.md` §2-7에 `OrderStatusTest`와 `SampleStatusTest` 케이스 표를 추가한다.

---

### [INFO] phase2.md §7-6 `InMemoryOrderRepositoryTest`에 PLAN 미명시 케이스 추가

- **위치**: `docs/design/phase2.md` — §7-6 `InMemoryOrderRepositoryTest` 케이스 표
- **설명**: `docs/PLAN.md` §2-7 `InMemoryOrderRepositoryTest` 케이스 표에는 `findByStatus(RELEASE) → 1건` 케이스가 없다. phase2.md에는 `findByStatus_release_returnsOneOrder`가 추가되어 있다. 이는 테스트를 보강하는 방향이므로 결함은 아니나, PLAN 문서와의 불일치 항목으로 기록한다.
- **권장 조치**: `docs/PLAN.md` §2-7 `InMemoryOrderRepositoryTest` 표에 `findByStatus(RELEASE) → 1건 반환` 항목을 추가하거나, 해당 케이스가 의도적 추가임을 phase2.md에 주석으로 명시한다.

---

## 통과 항목

### [A] 교차 참조 일관성
- PLAN.md에 나열된 Phase 2 대상 파일과 phase2.md 내 구현 대상 일치 — 이상 없음
- phase2.md 내 기준 문서 링크(`docs/PLAN.md > Phase 2`, `PRD 마일스톤: M2`) 경로 일치 — 이상 없음

### [B] 기술 스택 일관성
- phase2.md 패키지 선언 `org.example` — CLAUDE.md 및 PLAN.md 구조와 일치
- InMemory 구현체 접두사 규칙(`InMemorySampleRepository`, `InMemoryOrderRepository`) — CLAUDE.md 코딩 컨벤션 준수
- 외부 의존성 추가 없음 — 이상 없음

### [C] 설계 제약 반영
- Model 엔티티: setter 없음, 생성자 주입, 불변 객체 설계 — CLAUDE.md 규칙 준수
- Controller import 금지, 콘솔 I/O 금지 제약 — phase2 설계 범위(Model·Repository)에서 위반 없음
- `static` 출력 메서드 금지 — phase2 설계 범위 내 위반 없음

### [D] 완료 기준 존재
- phase2.md §8에 완료 기준 6개 항목 명시 — 이상 없음
- PLAN.md Phase 2 완료 기준 3개 항목 모두 포함 — 이상 없음

### [E] 내부 모순 검사
- Sample 필드·유효성 규칙: phase2.md ↔ PLAN.md §2-2 ↔ PRD §4 완전 일치
- Order 필드·유효성 규칙: phase2.md ↔ PLAN.md §2-3 ↔ PRD §4 완전 일치 (status null 유효성은 phase2에서 보강)
- Repository 인터페이스 시그니처: phase2.md §4 ↔ PLAN.md §2-4 완전 일치
- 더미 데이터 (시료 3개·주문 8개): phase2.md §5·§6 ↔ PLAN.md §2-5·§2-6 ↔ PRD §9 완전 일치
- Phase 1 완료 상태 표: phase2.md §1 ↔ PLAN.md Phase 1 §4 완전 일치
- SampleStatus 테스트(§7-4) 포함 근거: PLAN.md Phase 1에서 placeholder 생성, Phase 2에서 구현 — 타당

### [F] 더미 데이터 정합성 (수치 검증)
- `findByStatus(RESERVED)` → 주문 1(RESERVED), 주문 5(RESERVED) = 2건 — PLAN §2-7 일치
- `findByStatus(REJECTED)` → 주문 8(REJECTED) = 1건 — PLAN §2-7 일치
- `findBySampleId(2)` → 주문 2(sampleId=2), 주문 5(sampleId=2), 주문 8(sampleId=2) = 3건 — PLAN §2-7 일치
- `findByStatus(RELEASE)` → 주문 4(RELEASE) = 1건 — phase2.md 추가 케이스, 수치 정확
