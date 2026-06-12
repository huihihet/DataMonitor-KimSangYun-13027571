# 문서 정합성 검증 보고서

**일시**: 2026-06-12  
**검증 문서**:
- `docs/design/phase1.md`
- `docs/PLAN.md`
- `docs/PRD.md`
- `DataMonitor/CLAUDE.md`
- `과제/CLAUDE.md` (루트)

**결과**: 문제 3건 발견 (CRITICAL: 1, WARNING: 1, INFO: 1)

---

## 발견된 문제

### [CRITICAL] OrderStatus 완전 정의 타이밍이 PLAN.md Phase 2와 충돌

- **위치**: `docs/design/phase1.md` — §4 빈 클래스 설계 > `model/entity/OrderStatus.java` 및 §7 완료 기준 5번째 항목
- **설명**:
  - PLAN.md Phase 2-1에는 `OrderStatus` 열거형 완전 정의(`RESERVED, PRODUCING, CONFIRMED, RELEASE, REJECTED`)가 Phase 2 작업 항목으로 명시되어 있다.
  - phase1.md는 `OrderStatus`를 Phase 1에서 5개 상수 모두 완전하게 정의하는 설계를 포함하고, 완료 기준에 "OrderStatus 열거형 5개 상수 완전 정의"를 명시한다.
  - 두 문서가 동일 클래스의 구현 완료 시점을 서로 다른 Phase로 지정하고 있어 직접 충돌한다.
  - Phase 1에서 완전 정의를 완료하면 PLAN.md Phase 2-1 항목은 이미 완료된 작업을 다시 다루는 중복 항목이 되어 Phase 2 작업 범위가 불명확해진다.
- **권장 조치**: 두 문서 중 하나를 수정하여 타이밍을 통일해야 한다.
  - 옵션 A — phase1.md 채택: PLAN.md Phase 2-1의 `OrderStatus` 항목을 삭제하고, Phase 1 작업 목록에 "OrderStatus 열거형 완전 정의" 항목을 추가한다.
  - 옵션 B — PLAN.md 채택: phase1.md의 `OrderStatus.java` 설계를 빈 열거형(`public enum OrderStatus {}`) 상태로 변경하고, 완료 기준에서 "5개 상수 완전 정의" 항목을 제거한다.

---

### [WARNING] SampleStatus.java가 PLAN.md Phase 1 빈 클래스 목록 및 CLAUDE.md 패키지 구조에 미등재

- **위치**: `docs/design/phase1.md` — §3 패키지 구조 설계 및 §4 빈 클래스 설계 > `model/entity/SampleStatus.java`
- **설명**:
  - phase1.md는 `model/entity/SampleStatus.java`를 Phase 1에서 생성하는 파일로 설계하고, Phase 1 패키지 구조 다이어그램과 빈 클래스 설계 목록에 포함한다.
  - PLAN.md Phase 1 작업 목록 3번(빈 클래스 파일 생성 열거)에는 `SampleStatus.java`가 없다. PLAN.md 하단 "파일 생성 체크리스트(전체)"에는 포함되어 있으므로 최종 산출물 기준으로는 일치하나, Phase 1 작업 범위 서술과는 불일치한다.
  - DataMonitor CLAUDE.md 패키지 구조에도 `SampleStatus.java`가 없어 이 파일이 패키지 설계에서 공식적으로 관리되지 않는 상태다.
- **권장 조치**:
  - PLAN.md Phase 1 작업 목록 3번의 빈 클래스 목록에 `SampleStatus.java`를 추가한다.
  - DataMonitor CLAUDE.md 패키지 구조에 `SampleStatus.java` 항목(`# Controller → View 전달용 record`)을 추가한다.

---

### [INFO] PLAN.md Phase 1 완료 기준이 phase1.md 대비 과소 명세

- **위치**: `docs/PLAN.md` — Phase 1 완료 기준 섹션
- **설명**:
  - PLAN.md Phase 1 완료 기준은 2개 항목(`./gradlew build` 성공, 패키지 규칙 준수)만 명시한다.
  - phase1.md 완료 기준은 8개 항목(`./gradlew run`, `./gradlew test`, OrderStatus 완전 정의, SampleStatus record 선언, 레이어 역방향 import 없음, `.gitignore` 항목 포함)을 추가로 열거하며 Phase 1의 완료 판정 기준을 훨씬 구체화한다.
  - phase1.md의 추가 기준들은 PLAN.md와 모순되지 않으나, PLAN.md만 참조할 경우 Phase 1 완료 판정이 불완전하게 이루어질 수 있다.
- **권장 조치**: PLAN.md Phase 1 완료 기준에 phase1.md의 추가 항목들을 반영하거나, phase1.md가 PLAN.md 완료 기준의 세부 명세 역할을 한다는 점을 PLAN.md에 주석으로 명시한다.

---

## 통과 항목

- **[A] 교차 참조**: 문제 1건 (WARNING — SampleStatus.java Phase 1 목록 미등재)
- **[B] 기술 스택**: 이상 없음 — Java 17, Gradle 8.x, JUnit Jupiter 6.x, 외부 의존성 없음 모두 일치
- **[C] 설계 제약 반영**: 이상 없음 — MVC 레이어 역방향 금지 규칙 다이어그램에 명시, Constructor Injection 규칙 준수
- **[D] 완료 기준 누락**: 이상 없음 — phase1.md §7에 완료 기준 8개 항목 명시 (PLAN.md 기준 포함)
- **[E] 내부 모순**: 문제 1건 (CRITICAL — OrderStatus 구현 Phase 충돌), 문제 1건 (INFO — 완료 기준 과소 명세)
