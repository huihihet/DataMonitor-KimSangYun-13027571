# 문서 정합성 검증 보고서

**일시**: 2026-06-12  
**검증 문서**:
- `과제/CLAUDE.md` (루트 규칙)
- `DataMonitor/CLAUDE.md`
- `DataMonitor/docs/PRD.md`
- `DataMonitor/docs/PLAN.md`

**참고 문서**:
- `ConsoleMVC/CLAUDE.md`
- `ConsoleMVC/docs/PRD.md`
- `ConsoleMVC/docs/PLAN.md`

**결과**: 문제 4건 발견 (CRITICAL: 1, WARNING: 2, INFO: 1)

---

## 발견된 문제

### [CRITICAL] PLAN 3-3 Controller 테스트 케이스의 재고 수요량(demand) 계산 오류

- **위치**: `DataMonitor/docs/PLAN.md` — Phase 3 / 3-3 `MonitoringControllerTest` 테스트 케이스 표
- **설명**: PRD §4의 재고 상태 판별 규칙은 `RESERVED + PRODUCING 주문의 총 quantity`만 합산한다. 그러나 PLAN 3-3의 테스트 케이스 두 항목이 이 규칙과 불일치한다.

  | 시료 | PLAN 3-3 명시 demand | PRD §9 더미 데이터 기준 올바른 계산 | 오차 원인 |
  |------|----------------------|--------------------------------------|-----------|
  | AlphaChip (sampleId=1) | 75 | 50 (orderId=1, RESERVED, q=50만 해당) | CONFIRMED 주문 orderId=7(q=15)을 잘못 합산한 것으로 추정 (50+15=65 아닌 50+25=75이므로 불명확) |
  | GammaCore (sampleId=3) | 60 | 40 (orderId=6, PRODUCING, q=40만 해당) | CONFIRMED 주문 orderId=3(q=20)을 잘못 합산한 것으로 추정 (40+20=60) |

  구체적으로 PRD §9 더미 데이터 기준:
  - **AlphaChip**: RESERVED 주문 orderId=1(q=50), PRODUCING 없음 → demand=50. stock=120 >= 50 → 여유 (상태 판정은 맞으나 demand 수치 오류)
  - **GammaCore**: PRODUCING 주문 orderId=6(q=40), RESERVED 없음 → demand=40. stock=15 < 40 → 부족 (상태 판정은 맞으나 demand 수치 오류)

  CONFIRMED 상태 주문은 PRD §4에 따라 집계 대상이 아님에도 PLAN 테스트 케이스가 이를 포함한 수치를 사용하고 있다. 이 수치 그대로 테스트를 구현하면 PRD §4를 위반하는 집계 로직이 작성되거나, 올바른 로직으로 구현했을 때 테스트가 실패하는 사태가 발생한다.

- **권장 조치**: PLAN 3-3 `MonitoringControllerTest` 표에서 해당 두 테스트 케이스를 아래와 같이 수정한다.

  ```
  재고 상태 — 부족 | GammaCore(stock=15, demand=40) → "부족"
  재고 상태 — 여유 | AlphaChip(stock=120, demand=50) → "여유"
  ```

---

### [WARNING] 루트 CLAUDE.md의 리포지터리 명칭이 DataMonitor CLAUDE.md와 불일치

- **위치**: `과제/CLAUDE.md` — "리포지터리 구성" 표
- **설명**: 루트 CLAUDE.md는 해당 PoC 리포지터리를 `DataMonitoring`으로 표기하고 있으나, 실제 디렉터리 및 DataMonitor CLAUDE.md 프로젝트 목적 절에서는 `DataMonitor`를 사용한다.

  | 문서 | 표기 |
  |------|------|
  | `과제/CLAUDE.md` 리포지터리 구성 표 | `DataMonitoring` |
  | `DataMonitor/CLAUDE.md` 제목 및 본문 | `DataMonitor` |
  | 실제 디렉터리 경로 | `DataMonitor` |

- **권장 조치**: 루트 CLAUDE.md의 리포지터리 구성 표에서 `DataMonitoring`을 `DataMonitor`로 통일하거나, 실제 디렉터리·문서 모두를 `DataMonitoring`으로 통일한다. 실제 디렉터리명을 기준으로 루트 문서를 수정하는 것을 권장한다.

---

### [WARNING] DataMonitor CLAUDE.md의 PoC 범위 제한에 "생산 라인 큐 관리" Out-of-Scope 누락

- **위치**: `DataMonitor/CLAUDE.md` — "PoC 범위 제한" 절
- **설명**: PRD §3 Out-of-Scope에는 "생산 라인 큐 관리 (SSemi 본 프로젝트 담당)"이 명시되어 있으나, DataMonitor CLAUDE.md의 PoC 범위 제한 절에는 해당 항목이 누락되어 있다.

  | 문서 | 생산 라인 큐 관리 Out-of-Scope |
  |------|-------------------------------|
  | `DataMonitor/docs/PRD.md` §3 | 명시됨 |
  | `DataMonitor/CLAUDE.md` PoC 범위 제한 | 누락 |

- **권장 조치**: DataMonitor CLAUDE.md의 "PoC 범위 제한" 절에 아래 항목을 추가한다.
  ```
  - 생산 라인 큐 관리는 **구현하지 않는다** (SSemi 본 프로젝트 담당)
  ```

---

### [INFO] PLAN의 PRD 기준 문서 링크가 상대 경로 텍스트로만 표기 (실제 하이퍼링크 미설정)

- **위치**: `DataMonitor/docs/PLAN.md` — 1행 (`> 기준 문서: \`docs/PRD.md\``)
- **설명**: PLAN.md 상단에 기준 문서로 `docs/PRD.md`가 텍스트로 언급되어 있으나, 마크다운 하이퍼링크(`[PRD.md](PRD.md)`)로 설정되어 있지 않다. 기능적 문제는 없으나 ConsoleMVC PLAN.md와 동일한 패턴이므로 의도적 스타일로 볼 수 있다. 참고용으로 기록한다.
- **권장 조치**: 선택 사항. 다른 PoC 문서와의 일관성을 위해 현행 유지도 무방하다.

---

## 통과 항목

- **[A] 교차 참조**: 문제 1건 (INFO — PLAN 기준 문서 링크 미설정)
- **[B] 기술 스택**: 이상 없음 — Java 17+, Gradle 8.x, JUnit Jupiter 6.x 모두 루트 CLAUDE.md 범위 내 일치
- **[C] 설계 제약 반영**: 이상 없음 — MVC 레이어 규칙, Controller System.out 금지, View 집계 금지 모두 PLAN에 반영됨
- **[D] 완료 기준**: 이상 없음 — Phase 1~5 전 단계에 완료 기준(acceptance criteria) 명시됨
- **[E] 내부 모순 (PRD §9 더미 데이터 vs PLAN 2-5·2-6)**: 이상 없음 — 완전 일치
- **[E] 내부 모순 (PRD §6 출력 포맷 vs PLAN 3-2 포맷 명세)**: 이상 없음 — 구분선 40자, 컬럼 너비, ANSI 색상 코드 모두 일치
- **[E] 내부 모순 (PRD F-01~F-05 vs PLAN Phase 반영)**: 이상 없음 — 5개 기능 요구사항 모두 해당 Phase에 반영됨
- **[E] 내부 모순 (PRD §8 클래스 목록 vs PLAN 파일 체크리스트)**: 이상 없음 — 11개 클래스 모두 일치
- **[E] 내부 모순 (PRD §10 수용 기준 vs PLAN Phase 완료 기준)**: 이상 없음 — 11개 수용 기준 모두 PLAN에 반영됨
- **[F] PoC 범위 제한 일치 (DataMonitor CLAUDE.md vs PRD Out-of-Scope)**: 문제 1건 (WARNING — 생산 라인 큐 관리 누락)
- **[G] 리포지터리 명칭 일관성 (루트 CLAUDE.md vs DataMonitor CLAUDE.md)**: 문제 1건 (WARNING)
- **[H] PLAN 3-3 테스트 케이스 수치 정확성**: 문제 1건 (CRITICAL — demand 수치 오류)

---

## 수정 필요 파일 요약

| 우선순위 | 파일 | 수정 내용 |
|----------|------|-----------|
| CRITICAL | `DataMonitor/docs/PLAN.md` | Phase 3-3 MonitoringControllerTest 테스트 케이스의 AlphaChip demand 수치를 75→50, GammaCore demand 수치를 60→40으로 수정 |
| WARNING | `과제/CLAUDE.md` | 리포지터리 구성 표에서 `DataMonitoring` → `DataMonitor`로 수정 |
| WARNING | `DataMonitor/CLAUDE.md` | PoC 범위 제한 절에 "생산 라인 큐 관리 (SSemi 본 프로젝트 담당)" 항목 추가 |
