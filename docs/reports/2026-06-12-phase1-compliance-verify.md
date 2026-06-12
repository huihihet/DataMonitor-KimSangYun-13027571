# 컴플라이언스 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: `docs/design/phase1.md` (Phase 1 — 프로젝트 스켈레톤 설계)  
**결과**: 위반 1건 (CRITICAL: 0, WARNING: 1)

---

## 발견된 위반

### [WARNING] 빈 클래스 주석이 WHAT 주석 금지 규칙 위반

- **위치**: `docs/design/phase1.md` — 4절 "빈 클래스 설계" 전체 / 5절 "테스트 빈 클래스 설계"
- **위반 규칙**: 루트 CLAUDE.md 및 DataMonitor CLAUDE.md 코딩 컨벤션 — "주석: WHY가 비자명한 경우에만 한 줄 이내로 작성" (WHAT 주석 금지)
- **현재 설계**:
  - `Sample.java`, `Order.java`, `MonitoringController.java`, `MonitoringView.java`, `MonitoringLoop.java` 등 빈 클래스 본문에 `// Phase 2에서 구현`, `// Phase 3에서 구현`, `// Phase 4에서 구현` 주석 포함
  - `SampleStatus.java` record 본문에 `// Phase 3에서 완성 — stockLevel: "여유" | "부족" | "고갈"` 주석 포함
  - 테스트 클래스 공통 패턴에 `// Phase {N}에서 구현` 주석 포함
  - 위 주석들은 모두 "언제 무엇을 할 것인가(WHAT/WHEN)"를 설명하며, 코드의 WHY(설계 이유·의도)를 설명하지 않음
- **권장 수정**: 위 주석을 모두 제거한다. 구현 예정 시점 정보는 코드 주석이 아니라 `docs/PLAN.md`의 Phase 작업 목록과 완료 기준으로 관리하는 것이 적절하다. 단, `Main.java`의 `// Phase 4에서 구현`처럼 의도적으로 비워둔 진입점임을 나타내야 한다면, 이를 PLAN 문서로 위임하고 코드에는 주석을 남기지 않는다.

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 아키텍처 제약 | OK | - |
| [B] 코딩 컨벤션 | WARNING | WHAT 주석 규칙 위반 (1건) |
| [C] 보안 | OK | - |
| [D] 불필요한 복잡성 | OK | - |

### 9개 체크 항목 세부 결과

| 번호 | 체크 항목 | 결과 | 근거 |
|------|-----------|------|------|
| 1 | 패키지 선언 소문자 + 클래스명 PascalCase | OK | `org.example.*` 전부 소문자, 모든 클래스명 PascalCase 준수 |
| 2 | 인터페이스 I 접두사 없음 | OK | `SampleRepository`, `OrderRepository` — I 접두사 없음 |
| 3 | 구현체 InMemory 접두사 | OK | `InMemorySampleRepository`, `InMemoryOrderRepository` 준수 |
| 4 | SampleStatus record의 model/entity 위치와 레이어 규칙 | OK | `model.entity` 패키지에 위치, Controller·View import 없음, 레이어 위반 없음 |
| 5 | 역방향 import 없음 | OK | Repository 인터페이스는 model.entity만 import, 나머지 클래스는 import 없음 |
| 6 | OrderStatus 상수명 UPPER_SNAKE_CASE | OK | `RESERVED, PRODUCING, CONFIRMED, RELEASE, REJECTED` 전부 준수 |
| 7 | Java 17, JUnit Jupiter 6.x 기술 스택 요건 | OK | 루트 CLAUDE.md `5.x (또는 6.x)` 허용 범위 내, `junit-bom:6.0.0` 사용, Java 17 설정 추가 명세 |
| 8 | application 플러그인 + mainClass 지정 | OK | `id 'application'`, `mainClass = 'org.example.Main'` 명세 준수 |
| 9 | JaCoCo finalizedBy 연결 | OK | `test { finalizedBy jacocoTestReport }` 로 test 완료 후 자동 실행 연결됨 |
