# Phase 5 설계 — ANSI 색상 · 최종 검증

> 기준: `docs/PLAN.md > Phase 5` / PRD 마일스톤: M5  
> 목표: ANSI 색상 적용 여부를 재검증하고, 역할 분리 규칙 준수 및 커버리지를 최종 확인한다.

---

## 1. 현재 상태

Phase 1~4 구현 완료. Phase 5는 신규 코드 추가 없이 검증·확인 단계이다.

| 항목 | 현재 상태 | Phase 5 작업 |
|------|-----------|-------------|
| ANSI 색상 상수 | `MonitoringView`에 `private static final` 3개 정의됨 | 적용 정확성 재검증 |
| 역할 분리 | Controller `System.out` 없음, View 집계 없음 | `grep` 자동 확인 |
| 전체 테스트 | 69개 통과 | `./gradlew test` 재실행 |
| 커버리지 | 미측정 | `./gradlew jacocoTestReport` 실행 |
| PRD 수용 기준 | 미대조 | 전 항목 체크 |

---

## 2. ANSI 색상 재검증

### 2-1. 현재 정의 (MonitoringView.java)

```java
private static final String ANSI_RESET   = "\033[0m";
private static final String ANSI_YELLOW  = "\033[33m";
private static final String ANSI_RED     = "\033[31m";
```

### 2-2. 적용 규칙 확인

| stockLevel | 기대 출력 | 적용 위치 |
|------------|----------|-----------|
| `"부족"` | `\033[33m부족\033[0m` | `colorize()` switch |
| `"고갈"` | `\033[31m고갈\033[0m` | `colorize()` switch |
| `"여유"` | `여유` (색상 없음) | `colorize()` default |

### 2-3. 검증 방법

기존 `MonitoringViewTest`·`MonitoringIntegrationTest`에서 이미 ANSI 코드를 직접 비교하고 있으므로,  
`./gradlew test` 통과로 색상 적용이 검증된다. 별도 신규 테스트 추가 불필요.

---

## 3. 역할 분리 최종 체크리스트

### 3-1. Controller에 `System.out` 없음

```bash
grep -r "System.out" src/main/java/org/example/controller/
# 결과: 없음이어야 함
```

### 3-2. Model에 `Scanner` 또는 `System` 코드 없음

```bash
grep -r "System\|Scanner" src/main/java/org/example/model/
# 결과: 없음이어야 함
```

### 3-3. View에 집계·판별 비즈니스 로직 없음

- `MonitoringView`에 `stream()`, `filter()`, `if.*stock` 등 집계 패턴 없음을 코드 리뷰로 확인

### 3-4. MonitoringLoop에 `view` 패키지 import 없음

```bash
grep "import org.example.view" src/main/java/org/example/app/MonitoringLoop.java
# 결과: 없음이어야 함
```

---

## 4. 커버리지 측정

```bash
./gradlew jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html
```

### 목표 기준 (CLAUDE.md)

| 대상 | 목표 |
|------|------|
| `model/entity/` | 80% 이상 |
| `model/repository/` | 80% 이상 |
| `controller/` | 80% 이상 |

### 커버리지 미달 시 대응

- 미달 패키지가 있으면 누락 케이스를 파악하여 테스트 추가
- `app/MonitoringLoop`의 폴링 루프(`Thread.sleep` 경로)는 측정 제외 대상이므로 낮아도 무방

---

## 5. PRD 수용 기준 최종 대조

| # | 수용 기준 (PRD §10) | 검증 방법 |
|---|---------------------|-----------|
| 1 | `./gradlew build` 경고 없이 성공 | `./gradlew build` 실행 |
| 2 | F-01~F-05 기능 콘솔 정상 동작 | `./gradlew run` 수동 확인 |
| 3 | 3초 간격 자동 갱신 | `./gradlew run` 수동 확인 |
| 4 | `q` 입력 시 루프 종료 | `./gradlew run` 수동 확인 |
| 5 | Controller에 `System.out` 없음 | `grep` 자동 확인 |
| 6 | Model에 콘솔 I/O 없음 | `grep` 자동 확인 |
| 7 | View에 집계·계산 로직 없음 | 코드 리뷰 |
| 8 | REJECTED 주문 건수 미포함 | `MonitoringControllerTest` 통과로 검증 |
| 9 | 재고 상태 판별 정확성 | `MonitoringControllerTest` 통과로 검증 |
| 10 | `./gradlew test` 전 통과 | 자동화 |
| 11 | Model·Controller 커버리지 80% 이상 | JaCoCo 리포트 |

---

## 6. 완료 기준

- [ ] `./gradlew build` — 경고 없이 성공
- [ ] `./gradlew test` — 전체 테스트 통과
- [ ] `./gradlew jacocoTestReport` — Model·Controller 커버리지 80% 이상
- [ ] `grep` 확인 — Controller `System.out` 없음, Model `Scanner`·`System` 없음, Loop `view` import 없음
- [ ] PRD §10 수용 기준 11개 항목 전부 충족
- [ ] `./gradlew run` 수동 실행으로 대시보드 동작 확인 (F-01~F-05)
