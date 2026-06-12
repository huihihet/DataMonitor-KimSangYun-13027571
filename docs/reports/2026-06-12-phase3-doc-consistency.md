# 문서 정합성 검증 보고서

**일시**: 2026-06-12  
**검증 문서**:
- `docs/design/phase3.md`
- `docs/PLAN.md`
- `docs/PRD.md`
- `CLAUDE.md`

**결과**: 문제 2건 발견 (CRITICAL: 0, WARNING: 1, INFO: 1)

---

## 발견된 문제

### [WARNING] 테이블 컬럼 너비 불일치 — phase3.md vs PRD §6 / PLAN.md §3-2

- **위치**: `docs/design/phase3.md` — §3 "컬럼 포맷 규칙" 표
- **설명**:
  PRD §6 및 PLAN.md §3-2는 테이블 컬럼 너비를 `ID(4) / 이름(12) / 재고(7) / 상태(4)` 로 명시한다.
  phase3.md §3의 "컬럼 포맷 규칙" 표는 `이름 14 (%-14s)`, `재고 6 (%6d)` 로 기술하여 두 항목이 불일치한다.

  | 컬럼 | PRD §6 / PLAN.md §3-2 | phase3.md §3 |
  |------|----------------------|--------------|
  | 이름 | 12 | **14** |
  | 재고 | 7 | **6** |

  phase3.md의 출력 예시(§3) 헤더 행 `ID  이름            재고   상태` 또한 이름 너비 14에 맞춰져 있어 컬럼 포맷 규칙 표와 내부적으로는 일관되지만, PRD 및 PLAN.md 기준과 배치된다.
- **권장 조치**: 세 문서 중 하나로 통일한다. PRD §6이 설계 기준 문서이므로 phase3.md §3 "컬럼 포맷 규칙" 표를 `이름(12) / 재고(7)` 로 수정하고, 출력 예시의 포맷 문자열도 `%-12s` / `%7d` 로 맞춘다. 만약 한글 이름 정렬을 위해 너비를 의도적으로 조정한 것이라면 PRD §6과 PLAN.md §3-2를 함께 수정하고 변경 이유를 해당 섹션에 주석으로 남긴다.

---

### [INFO] Phase 3에서 ANSI 색상 상수 정의 — Phase 5 §5-1과의 중복 가능성

- **위치**: `docs/design/phase3.md` — §3 `MonitoringView` 구조 코드 블록 / `docs/PLAN.md` — Phase 5 §5-1
- **설명**:
  PLAN.md Phase 5 §5-1은 `ANSI_RESET`, `ANSI_YELLOW`, `ANSI_RED` 상수를 `MonitoringView` 내부에 정의하는 작업을 Phase 5 목표로 기술한다.
  phase3.md §3은 Phase 3 설계 단계에서 이미 동일한 세 상수를 `MonitoringView`에 포함하는 구조를 정의하고 있다.
  phase3.md 내에 "Phase 3에서 미리 정의하는 이유" 또는 "Phase 5에서 추가 정의 불필요" 에 대한 명시적 근거가 없어, Phase 5 구현 시 중복 작업이 발생하거나 Phase 5 §5-1 항목이 이미 완료된 항목을 다시 기술하는 혼선이 생길 수 있다.
  단, PLAN.md §3-2 출력 포맷 예시에도 Phase 3 단계에서 ANSI 코드(`\033[31m`, `\033[33m`)를 명시하고 있으므로 phase3.md의 의도 자체는 PLAN.md와 부합한다.
- **권장 조치**: PLAN.md Phase 5 §5-1 항목을 "Phase 3에서 MonitoringView에 정의 완료. Phase 5에서는 적용 여부 및 미지원 환경 처리를 재검증한다." 로 갱신하거나, phase3.md §3에 "ANSI 상수는 Phase 3에서 선행 정의하며 Phase 5 §5-1 작업 대상에서 제외한다." 는 노트를 추가하여 두 문서 간 관계를 명확히 한다.

---

## 통과 항목

| 체크 항목 | 결과 |
|-----------|------|
| [1] MonitoringController 생성자 시그니처 — phase3.md §2 vs PLAN.md §3-1 | 이상 없음 |
| [2] refresh() 흐름 (집계 순서, 재고 상태 판별 규칙) — phase3.md §2 vs PLAN.md §3-1 / PRD §4 | 이상 없음 |
| [3] MonitoringView 메서드 목록 — phase3.md §3 vs PLAN.md §3-2 | 이상 없음 |
| [4] 출력 포맷 (구분선·헤더) — phase3.md §3 vs PRD §6 | 구분선·헤더 형식 일치 / 컬럼 너비 불일치 → WARNING 등록 |
| [5] ANSI 색상 상수 정의 위치 — phase3.md §3 vs PLAN.md Phase 5 §5-1 | 중복 가능성 → INFO 등록 |
| [6] MonitoringControllerTest 케이스 — phase3.md §4-2 vs PLAN.md §3-3 | PLAN.md 명시 7개 케이스 전부 포함 (추가 2개 포함), 이상 없음 |
| [7] MonitoringViewTest 케이스 — phase3.md §4-1 vs PLAN.md §3-3 | PLAN.md 명세 전부 포함, 이상 없음 |
| [8] printFooter(int intervalSeconds) 시그니처 충돌 여부 — phase3.md §3 vs PLAN.md §3-2 | 이상 없음 |
| [9] phase3.md §1 현재 상태 표 vs Phase 2 완료 결과 | 이상 없음 |
