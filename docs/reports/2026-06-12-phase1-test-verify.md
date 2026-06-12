# 테스트 전략 검증 보고서

**일시**: 2026-06-12
**검증 대상**: docs/design/phase1.md (Phase 1 — 프로젝트 스켈레톤)
**결과**: 미흡 4건 (CRITICAL: 1, WARNING: 3)

---

## 발견된 문제

### [CRITICAL] PLAN.md Phase 1 완료 기준에 `./gradlew test` 항목 누락

- **대상 기능**: PLAN.md Phase 1 완료 기준 체크리스트
- **문제**: `phase1.md` 섹션 7 완료 기준에는 `./gradlew test` 통과 항목이 명시되어
  있으나, `PLAN.md` Phase 1 완료 기준에는 해당 항목이 없다.
  PLAN.md에는 `./gradlew build` 성공과 패키지 위치 확인만 있고, placeholder 테스트
  파일 생성 및 테스트 통과가 포함되어 있지 않다.
  Phase 1이 PLAN.md 기준으로 완료 처리될 경우 placeholder 테스트 파일이 생성되지 않은
  채로 Phase 2로 진입할 수 있으며, 이후 `./gradlew test`가 최초 실행될 때 빌드 구성 오류
  또는 누락 파일 문제가 발생할 수 있다.
- **권장 조치**: PLAN.md Phase 1 완료 기준에 다음 두 항목을 추가한다.
  - `[ ] 테스트 placeholder 파일 8개 생성 (SampleTest, OrderTest, SampleStatusTest, InMemorySampleRepositoryTest, InMemoryOrderRepositoryTest, MonitoringControllerTest, MonitoringViewTest, MonitoringIntegrationTest)`
  - `[ ] ./gradlew test 통과 (placeholder 테스트 포함)`

---

### [WARNING] PLAN.md Phase 1 작업 목록에 테스트 파일 생성 누락

- **대상 기능**: PLAN.md Phase 1 작업 목록 3번 항목
- **문제**: PLAN.md Phase 1 작업 목록 3번("빈 클래스 파일 생성")에 `src/test/` 하위
  placeholder 테스트 파일 생성이 명시되어 있지 않다. `phase1.md` 섹션 3과 섹션 5에는
  테스트 파일 구조와 공통 패턴이 상세히 기술되어 있으나, PLAN.md에서 실행 작업으로
  연결되지 않아 구현자가 PLAN.md만 참조할 경우 테스트 파일 생성을 누락할 수 있다.
- **권장 조치**: PLAN.md Phase 1 작업 목록에 다음 항목을 추가한다.
  - `5. 테스트 placeholder 파일 생성 (src/test/java/org/example/ 하위 8개 파일, 각각 @Test void placeholder() 포함)`

---

### [WARNING] JaCoCo 커버리지 임계값 강제 설정이 전체 설계에 없음

- **대상 기능**: build.gradle JaCoCo 설정 (phase1.md 섹션 2), Phase 5 최종 체크리스트
- **문제**: phase1.md의 build.gradle 설계는 `jacocoTestReport`로 HTML 리포트 생성만
  활성화하고, 커버리지 임계값을 강제하는 `jacocoTestCoverageVerification` 태스크가
  전체 설계(phase1~5) 어디에도 포함되어 있지 않다. Phase 5 완료 기준에 "커버리지 80%
  이상"이 명시되어 있으나 자동 차단 없이 수동 확인에만 의존하므로, 80% 미달 상태에서도
  `./gradlew build`가 통과된다.
- **권장 테스트**: Phase 5 설계(또는 Phase 1 build.gradle 설계)에 다음을 추가한다.
  ```groovy
  jacocoTestCoverageVerification {
      violationRules {
          rule {
              element = 'PACKAGE'
              includes = ['org.example.model.*', 'org.example.controller.*']
              limit {
                  minimum = 0.80
              }
          }
      }
  }
  check.dependsOn jacocoTestCoverageVerification
  ```

---

### [WARNING] OrderStatus 전용 테스트 파일이 설계에 없음

- **대상 기능**: `OrderStatus.java` — Phase 1에서 5개 상수 완전 정의
- **문제**: `OrderStatus`는 Phase 1에서 완전히 정의되는 유일한 열거형이지만,
  `phase1.md` 섹션 5 테스트 파일 목록과 PLAN.md 파일 생성 체크리스트 양쪽 모두에
  `OrderStatusTest.java`가 없다. `SampleStatus`는 `SampleStatusTest`가 있어 Phase 2에서
  검증되는 반면, `OrderStatus`의 5개 상수(`RESERVED, PRODUCING, CONFIRMED, RELEASE,
  REJECTED`)에 대한 명시적 검증 계획이 없다.
  `OrderStatus`는 단순 enum이어서 다른 테스트(`OrderTest`, `InMemoryOrderRepositoryTest`)
  에서 간접 검증이 가능하나, 설계 문서 수준에서 테스트 누락으로 보인다.
- **권장 조치**: `SampleStatusTest`와 동일하게 `OrderStatusTest.java` placeholder를
  Phase 1 테스트 파일 목록에 추가하고, Phase 2에서 5개 상수 열거 및 순서 검증 테스트
  케이스를 작성한다.
  ```java
  @Test
  void allConstantsDefined() {
      OrderStatus[] values = OrderStatus.values();
      assertThat(values).containsExactly(
          RESERVED, PRODUCING, CONFIRMED, RELEASE, REJECTED);
  }
  ```

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 테스트 계획 존재 | 부분 통과 | phase1.md에는 계획 있으나 PLAN.md 작업 목록·완료 기준에서 누락 |
| [B] 엣지케이스 식별 | 통과 | Phase 2 단위 테스트 케이스에 경계값·null·음수·공백 케이스 명시됨 |
| [C] 기존 테스트 충돌 | 통과 | 현재 테스트 파일 없음 (src/ 비어 있음), 충돌 위험 없음 |
| [D] 테스트 구조 | 부분 통과 | 단위/통합 분리는 적절하나 JaCoCo 임계값 강제 부재, OrderStatusTest 누락 |

- [A] 테스트 계획 존재: 부분 통과
- [B] 엣지케이스 식별: 통과
- [C] 기존 테스트 충돌: 통과
- [D] 테스트 구조: 부분 통과
