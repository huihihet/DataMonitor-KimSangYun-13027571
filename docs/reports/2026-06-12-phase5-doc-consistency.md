# 문서 정합성 검증 보고서

**일시**: 2026-06-12  
**검증 문서**:
- `docs/design/phase5.md`
- `docs/PLAN.md` (Phase 5 섹션)
- `docs/PRD.md` (§10 수용 기준)
- `DataMonitor/CLAUDE.md`
- `docs/design/phase3.md` (ANSI 색상 정의)
- `docs/design/phase4.md`

**결과**: 문제 2건 발견 (CRITICAL: 1, WARNING: 1, INFO: 0)

---

## 발견된 문제

### [CRITICAL] CLAUDE.md "갱신 주기 상수 위치" 규칙을 PLAN.md·phase4.md·phase5.md가 일제히 위반

- **위치**: `DataMonitor/CLAUDE.md` — 모니터링 전용 규칙 섹션  
  `docs/PLAN.md` — Phase 4 §4-1 `MonitoringLoop` 구조  
  `docs/design/phase4.md` — §2 `MonitoringLoop` 구조  
  `docs/design/phase5.md` — §4 커버리지 측정 (간접 참조)
- **설명**:  
  CLAUDE.md는 **"갱신 주기: 기본 3초, `Main`에서 상수로 정의"** 라고 명시한다.  
  그러나 PLAN.md Phase 4 §4-1, phase4.md §2는 `REFRESH_INTERVAL_SECONDS = 3` 상수를  
  `MonitoringLoop` 클래스 내부(`static final`)에 정의하는 설계를 채택하고 있다.  
  phase5.md §6 완료 기준도 이 설계를 전제로 검증을 기술한다.  
  `Main`이 아닌 `MonitoringLoop`에 상수가 위치하므로 CLAUDE.md 규칙과 직접 상충한다.
- **권장 조치**:  
  (A) CLAUDE.md의 규칙을 현행 설계에 맞게 수정하거나 ("갱신 주기 상수는 `MonitoringLoop`에 정의"),  
  (B) PLAN.md·phase4.md의 설계를 수정하여 `Main`이 `REFRESH_INTERVAL_SECONDS`를 정의하고  
      `MonitoringLoop` 생성자에 인수로 전달하는 방식으로 변경한다.  
  phase5.md는 확정된 방향에 따라 완료 기준을 갱신한다.

---

### [WARNING] PLAN.md Phase 4 §4-1과 phase4.md §2의 `MonitoringLoop` 생성자 시그니처 불일치

- **위치**:  
  `docs/PLAN.md` — Phase 4, §4-1 `MonitoringLoop` 구조 코드 블록  
  `docs/design/phase4.md` — §2 `MonitoringLoop` 구조 코드 블록  
  `docs/design/phase4.md` — §3 `Main` 설계 코드 블록
- **설명**:  
  PLAN.md Phase 4 §4-1은 생성자를 다음과 같이 정의한다.  
  ```
  MonitoringLoop(MonitoringController controller, MonitoringView view)
  ```  
  반면 phase4.md §2(상세 설계)와 §3(Main 조립)은 `MonitoringView`를 받지 않는 단일 인수 형태로 정의한다.  
  ```
  MonitoringLoop(MonitoringController controller)
  ```  
  phase4.md §3 Main 코드 역시 `new MonitoringLoop(controller)`로 view를 전달하지 않는다.  
  PLAN.md는 상위 계획 문서, phase4.md는 구현 상세 문서로서, 두 문서의 API 명세가 달라  
  구현 시 혼란이 발생할 수 있다.
- **권장 조치**:  
  phase4.md §2의 설계(view 미전달, Controller에 shutdown() 위임)가 역할 분리 규칙에 더 부합하므로,  
  PLAN.md Phase 4 §4-1의 생성자 시그니처를 `MonitoringLoop(MonitoringController controller)`로  
  수정하여 phase4.md와 일치시킨다.

---

## 통과 항목

- **[A] 교차 참조 — 이상 없음**  
  PLAN.md의 Phase 1~5 목록과 실제 phase1~5.md 파일이 존재한다고 전제할 때 일치.  
  phase5.md의 기준 참조(`docs/PLAN.md > Phase 5`, PRD 마일스톤 M5) 경로 표기 이상 없음.

- **[B-1] ANSI 색상 상수 일관성 — 이상 없음**  
  PLAN.md Phase 5 §5-1, phase3.md §3, phase5.md §2-1 세 문서 모두  
  `ANSI_RESET="\033[0m"`, `ANSI_YELLOW="\033[33m"`, `ANSI_RED="\033[31m"` 로 동일하게 정의.  
  적용 규칙(부족→황색, 고갈→적색, 여유→색상 없음)도 세 문서 간 일치.

- **[B-2] 기술 스택 일관성 — 이상 없음**  
  CLAUDE.md(Java 17+, Gradle 8.x, JUnit Jupiter 6.x, 외부 의존성 없음)와  
  설계 문서들의 기술 내용 간 신규 의존성 추가 없음.

- **[C] 설계 제약 반영 — 해당 항목 이상 없음**  
  "수정 금지" 등의 제약은 DataMonitor CLAUDE.md에 명시되지 않음.  
  역할 분리 규칙(Controller System.out 금지, View 집계 금지, Model 콘솔 I/O 금지)은  
  phase5.md §3, §5에서 grep 검증 및 완료 기준으로 반영됨.

- **[D] 완료 기준 — 이상 없음**  
  phase5.md §6에 6개의 완료 기준 항목이 체크리스트 형식으로 명시됨.

- **[E-1] PRD §10 수용 기준 11개 항목 대응 — 이상 없음**  
  PRD §10의 11개 항목과 phase5.md §5 표의 11개 행이 번호 순서대로 1:1 대응.  
  각 항목의 검증 방법도 PRD 내용과 모순 없이 기술됨.

- **[E-2] 커버리지 목표 일관성 — 이상 없음**  
  CLAUDE.md, PRD §7, phase5.md §4 모두 "Model·Controller 80% 이상"으로 일치.  
  phase5.md는 `model/entity/`, `model/repository/`, `controller/`를 개별 항목으로 세분화하여  
  상위 기준과 모순 없이 구체화함.

- **[E-3] grep 경로 일치 — 이상 없음**  
  phase5.md §3에 명시된 grep 대상 경로  
  (`src/main/java/org/example/controller/`, `src/main/java/org/example/model/`,  
  `src/main/java/org/example/app/MonitoringLoop.java`)가  
  CLAUDE.md의 패키지 구조 명세와 모두 일치.
