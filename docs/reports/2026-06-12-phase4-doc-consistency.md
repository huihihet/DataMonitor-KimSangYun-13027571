# 문서 정합성 검증 보고서

**일시**: 2026-06-12
**검증 문서**:
- `docs/design/phase4.md` (검증 대상)
- `docs/PLAN.md` (Phase 4 섹션)
- `docs/PRD.md`
- `CLAUDE.md` (DataMonitor PoC)
- `docs/design/phase3.md` (Phase 3 완료 상태)

**결과**: ❌ 문제 3건 발견 (CRITICAL: 1, WARNING: 1, INFO: 1)

---

## 발견된 문제

### [CRITICAL] MonitoringLoop → MonitoringView 직접 의존이 PRD §8 아키텍처 다이어그램과 불일치

- **위치**: `docs/design/phase4.md` — §2 MonitoringLoop 설계, §3 Main 설계
- **설명**:
  PRD §8 레이어 다이어그램에서 `MonitoringLoop`는 `MonitoringController`에만 의존 화살표가 연결되어 있고,
  `MonitoringView`와의 직접 의존 경로는 표현되어 있지 않다.

  그러나 phase4.md §2에서 `MonitoringLoop`의 생성자 시그니처를
  `MonitoringLoop(MonitoringController controller, MonitoringView view)` 로 정의하여
  `app` 패키지가 `view` 패키지를 직접 참조하도록 설계했다.
  이는 `start()` 종료 시 `view.printShutdownMessage()`를 `MonitoringLoop`가 직접 호출하기 위한 것이다.

  PRD §8 다이어그램의 의도에 따르면 종료 메시지 출력 책임은 `MonitoringController`를 통해 위임하거나,
  `MonitoringLoop`가 출력 대신 콜백·플래그를 사용하는 방식으로 처리해야 아키텍처가 일치한다.

- **권장 조치**:
  아래 두 방법 중 하나를 선택하여 PRD §8 다이어그램과 일치시킨다.

  *방법 A (권장)*: `MonitoringController`에 `printShutdown()` 또는 `notifyShutdown()` 메서드를 추가하여
  `MonitoringLoop`는 `controller`만 참조하도록 변경한다.
  ```java
  // MonitoringController에 추가
  public void notifyShutdown() {
      view.printShutdownMessage();
  }
  // MonitoringLoop.start() 종료부
  controller.notifyShutdown();
  ```

  *방법 B*: PRD §8 다이어그램을 수정하여 `MonitoringLoop → MonitoringView` 직접 의존을 명시한다.
  다이어그램 변경 전 PRD.md를 먼저 수정하고 재검증한다.

---

### [WARNING] PLAN.md Phase 4 통합 테스트와 phase4.md §4-2 테스트 케이스 수 불일치

- **위치**: `docs/PLAN.md` — Phase 4 §4-3 통합 시나리오, `docs/design/phase4.md` — §4-2
- **설명**:
  PLAN.md Phase 4는 통합 시나리오를 3개 항목으로 요약 기술하고 있고,
  phase4.md §4-2는 이를 7개 테스트 메서드로 세분화하여 기술하고 있다.
  7개 항목이 3개 시나리오를 포함하므로 내용상 모순은 없다.

  그러나 PLAN.md에는 완료 기준 항목에 `"통합 시나리오 테스트 전부 통과"` 만 기재되어 있어,
  기준 문서(PLAN.md)와 상세 설계(phase4.md) 간 테스트 수에 명시적 불일치가 존재한다.
  검수자가 PLAN.md 기준으로 3개만 작성해도 된다고 오해할 여지가 있다.

- **권장 조치**:
  PLAN.md Phase 4 완료 기준의 "통합 시나리오 테스트 전부 통과" 항목에
  `(7개 케이스, phase4.md §4-2 참고)` 를 부기하여 추적 가능성을 높인다.

---

### [INFO] 상위 CLAUDE.md와 DataMonitor CLAUDE.md 간 JUnit 버전 표기 불일치

- **위치**: `C:\reviewer\workspace\과제\CLAUDE.md` — 기술 스택 섹션 / `CLAUDE.md` (DataMonitor)
- **설명**:
  상위 과제 레벨 CLAUDE.md에는 `JUnit Jupiter 5.x (또는 6.x)` 로 기재되어 있고,
  DataMonitor CLAUDE.md에는 `JUnit Jupiter 6.x` 로 단독 명시되어 있다.
  phase4.md 자체에는 JUnit 버전이 명시되어 있지 않으므로 phase4.md에서 비롯된 문제는 아니나,
  교차 참조 문서 간 버전 표기가 상충하여 혼란을 줄 수 있다.

  phase4.md 테스트 환경(§4-1)에서도 JUnit 버전이 명시되지 않았으므로,
  DataMonitor CLAUDE.md의 6.x 기준을 따른다고 간주한다.

- **권장 조치**:
  상위 CLAUDE.md의 `JUnit Jupiter 5.x (또는 6.x)` 표기를 DataMonitor PoC에 맞게
  `JUnit Jupiter 5.x (DataMonitor PoC는 6.x 사용)` 으로 구체화하거나,
  DataMonitor CLAUDE.md에 `상위 문서 기준에서 6.x로 확정` 사유를 주석으로 명시한다.

---

## 통과 항목

- **[A] 교차 참조**: 이상 없음
  - PLAN.md Phase 4에 명시된 `MonitoringLoop`, `Main` 클래스가 phase4.md에 동일하게 설계됨
  - PLAN.md Phase 4 완료 기준 3개 항목이 phase4.md §5에 전부 포함됨
  - phase4.md 참조 기준 주석(`docs/PLAN.md > Phase 4`, `PRD 마일스톤: M4`)이 실제 파일 경로와 일치

- **[B] 기술 스택 일관성**: 이상 없음 (INFO 1건 별도 기재)
  - Java 17+, Gradle 8.x, 외부 의존성 없음 기준 일관 적용
  - phase4.md 외부 라이브러리 추가 없음

- **[C] 설계 제약 반영**: 이상 없음
  - `MonitoringLoop.start()` 내부 `System.out` 직접 호출 금지 제약이 phase4.md §2 제약란에 명시됨
  - 입력 감지 스레드 daemon 설정 제약 준수
  - View `static` 메서드 금지 컨벤션 위반 없음

- **[D] 완료 기준 누락**: 이상 없음
  - phase4.md §5에 7개 완료 기준 항목이 체크리스트 형식으로 명시됨

- **[E] 내부 모순**: 이상 없음 (CRITICAL 항목과 별개로 phase4.md 자체 내부 모순은 없음)
  - §4-3 더미 데이터 표가 PRD §9 및 PLAN.md Phase 2 §2-6 더미 데이터와 완전히 일치
    - AlphaChip(id=1): stock=120, RESERVED demand=50(orderId=1) → 여유
    - BetaWafer(id=2): stock=0, demand=55(orderId=2 qty=30 PRODUCING + orderId=5 qty=25 RESERVED) → 고갈
    - GammaCore(id=3): stock=15, PRODUCING demand=40(orderId=6) → 부족
  - Phase3 `MonitoringView.printShutdownMessage()` 시그니처가 phase4.md에서 동일하게 참조됨
  - Phase3 `MonitoringController(SampleRepository, OrderRepository, MonitoringView)` 생성자가 phase4.md §3에서 동일하게 조립됨
  - `volatile boolean running` 플래그 스레드 가시성 설계가 PLAN.md 및 DataMonitor CLAUDE.md 규칙과 일치
