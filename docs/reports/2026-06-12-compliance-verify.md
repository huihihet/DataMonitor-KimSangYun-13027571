# 컴플라이언스 검증 보고서

**일시**: 2026-06-12
**검증 대상**: `DataMonitor/CLAUDE.md`, `docs/PRD.md`, `docs/PLAN.md`
**결과**: X 위반 4건 (CRITICAL: 2, WARNING: 2)

---

## 발견된 위반

### [CRITICAL-1] MonitoringLoop.start()에서 System.out 직접 출력 설계

- **위치**: `docs/PLAN.md` — Phase 4-1 MonitoringLoop 동작 흐름
- **위반 규칙**: 루트 CLAUDE.md 아키텍처 레이어 규칙 — "Controller: `System.out` 직접 출력 금지" / DataMonitor CLAUDE.md 역할 분리 규칙 — "View: `System.out` 출력, 포맷팅 / Model·Controller 이외 레이어도 출력은 View를 통해야 하는 원칙"
- **현재 설계**: PLAN Phase 4-1 동작 흐름 마지막 단계에서 `stop 플래그 true → "모니터링을 종료합니다." 출력 후 종료`를 `MonitoringLoop.start()` 내부에서 직접 처리하도록 명세함. `MonitoringLoop`는 `app` 패키지 소속으로 View를 거치지 않고 `System.out`에 직접 출력하는 설계임.
- **권장 수정**: 종료 메시지 출력을 `MonitoringView.printShutdownMessage()` 인스턴스 메서드로 분리하고, `MonitoringLoop`가 `MonitoringController`를 통해 또는 직접 `MonitoringView`에 위임하도록 설계를 변경한다. `MonitoringLoop`의 생성자에 `MonitoringView`를 추가 주입하거나, Controller에 `shutdown()` 메서드를 두어 View 위임을 일관되게 유지한다.

---

### [CRITICAL-2] DataMonitor CLAUDE.md 내부 규칙 충돌 — 집계 책임 귀속 불일치

- **위치**: `DataMonitor/CLAUDE.md` — 역할 분리 규칙 표 vs 모니터링 전용 규칙 섹션
- **위반 규칙**: DataMonitor CLAUDE.md 자체 내에서 두 규칙이 상충함.
  - 역할 분리 규칙 표 Model 항목: "도메인 로직, **집계 계산, 상태 판별**" 을 Model 허용으로 명시
  - 모니터링 전용 규칙: "Repository에서 조회한 데이터를 **Controller가 집계**하여 View에 전달 (View는 집계 금지)"로 명시
- **현재 설계 영향**: 이 충돌이 PLAN Phase 3-1에 그대로 반영되어 `MonitoringController.refresh()`가 상태별 건수 집계 및 시료별 재고 상태 판별을 직접 수행하는 설계로 이어짐. 역할 분리 규칙 표 기준으로는 이 집계 로직이 Model에 있어야 하나, PLAN은 Controller에 배치함.
- **권장 수정**: DataMonitor CLAUDE.md의 역할 분리 규칙 표에서 Model 허용 항목 "집계 계산, 상태 판별"을 삭제하거나, 모니터링 전용 규칙의 "Controller가 집계" 표현을 "Repository 조회 결과를 Controller가 조율하여 View에 전달"로 수정하여 집계 책임 귀속을 일관되게 단일화한다. 이후 PLAN도 일관된 규칙에 맞게 수정한다.

---

### [WARNING-1] SampleStatus record 패키지 위치 미명시 및 파일 체크리스트 누락

- **위치**: `docs/PLAN.md` — Phase 3-1 전달 데이터 구조 및 Phase 5 파일 생성 체크리스트
- **위반 규칙**: 루트 CLAUDE.md 패키지 구조 규칙 — 신규 타입은 명확한 패키지에 배치해야 함. DataMonitor CLAUDE.md 패키지 구조 명세.
- **현재 설계**: `record SampleStatus(long sampleId, String name, int stock, String stockLevel)` 가 Phase 3-1에 정의되나 어느 패키지(`model.entity`? `controller`? 별도?)에 위치할지 명시되지 않음. Phase 5 파일 생성 체크리스트에도 `SampleStatus.java`가 없어 구현 단계에서 패키지 규칙 위반이 발생할 수 있음.
- **권장 수정**: `SampleStatus`를 `model.entity` 패키지에 배치하도록 PLAN에 명시하고, 파일 생성 체크리스트에 `src/main/java/org/example/model/entity/SampleStatus.java` 항목을 추가한다. 단, `SampleStatus`가 Controller 전용 집계 결과 DTO라면 `controller` 패키지에 내부 클래스 또는 별도 파일로 배치하는 근거를 PLAN에 기술한다.

---

### [WARNING-2] MonitoringController.refresh()의 타임스탬프 생성 책임 미명세

- **위치**: `docs/PLAN.md` — Phase 3-1 refresh() 내부 흐름
- **위반 규칙**: 루트 CLAUDE.md 코딩 컨벤션 — Controller는 "Model 호출, View 위임, 입력 파싱" 허용이며 "도메인 로직" 금지. 현재 시각 획득(`LocalTime.now()` 등)은 Controller 책임인지 MonitoringLoop 책임인지 모호하여 구현 단계에서 역할 오염 위험이 있음.
- **현재 설계**: `view.render(statusCounts, sampleStatusList, timestamp)` 에서 `timestamp`를 전달하나, 이 `timestamp`를 누가 언제 생성하는지 PLAN 어디에도 명시되지 않음. F-05(갱신 타임스탬프 표시) 요구사항의 구현 책임이 불분명함.
- **권장 수정**: PLAN Phase 3-1 `refresh()` 흐름에 "5번 이전에 `LocalTime.now()`로 현재 시각 문자열 생성 (`HH:mm:ss` 포맷, Controller 책임)"을 명시한다. 또는 `MonitoringLoop`가 루프 진입 시 타임스탬프를 생성하여 `controller.refresh(String timestamp)`로 전달하는 방식으로 명세를 구체화한다.

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 아키텍처 제약 | X | CRITICAL-1: MonitoringLoop System.out 직접 출력 / CRITICAL-2: 집계 책임 규칙 충돌 |
| [B] 코딩 컨벤션 | X | WARNING-1: SampleStatus 패키지 미명시 / WARNING-2: 타임스탬프 생성 책임 미명세 |
| [C] 보안 | OK | 사용자 입력은 단순 q 문자 비교, 민감 정보 없음, 입력 경계 검증 적절 |
| [D] 불필요한 복잡성 | OK | PoC 범위에 맞는 최소 구성, 오버엔지니어링 없음 |

### 네이밍 규칙 세부 검토

| 체크 항목 | 결과 |
|-----------|------|
| 클래스명 PascalCase | OK |
| 메서드·변수명 camelCase | OK |
| 상수 UPPER_SNAKE_CASE | OK |
| 패키지명 전부 소문자 | OK |
| Constructor Injection (Controller) | OK |
| View static 출력 메서드 금지 | OK |
| 인터페이스 I 접두사 없음 | OK |
| Repository 구현체 InMemory 접두사 | OK |
| MVC 의존 방향 (역방향 금지) | OK (MonitoringLoop 제외한 MVC 흐름 정상) |
| Model에 콘솔 I/O 없음 | OK |
| Controller에 System.out 없음 | OK (MonitoringLoop의 직접 출력은 CRITICAL-1로 별도 지적) |
| View에 비즈니스 로직 없음 | OK |
| MonitoringLoop volatile + daemon thread 명세 | OK |
| 기술 스택 Java 17+, Gradle 8.x 준수 | OK |
| JUnit Jupiter 6.x (루트 허용 범위 내) | OK |
