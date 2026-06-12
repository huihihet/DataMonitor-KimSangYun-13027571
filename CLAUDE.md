# DataMonitor — CLAUDE.md

> 부모 규칙: `과제/CLAUDE.md` 상속. 이 파일은 DataMonitor PoC 전용 추가 규칙만 기술한다.

## 프로젝트 목적

**콘솔 실시간 데이터 모니터링** 패턴을 검증하는 PoC.  
주문 상태별 현황, 시료별 재고 상태를 일정 간격으로 자동 갱신하여 표시하는  
모니터링 대시보드 구조를 확립한다.  
이 PoC의 뷰 포맷·갱신 구조는 SSemi 본 프로젝트의 `MonitoringController` / `MonitoringView` 기반이 된다.

---

## 기술 스택

- **언어**: Java 17+
- **빌드**: Gradle 8.x
- **테스트**: JUnit Jupiter 6.x
- **런타임**: 콘솔 (표준 입출력)
- **외부 의존성**: 없음 (순수 Java, 인메모리 데이터)

---

## 패키지 구조

```
src/main/java/org/example/
├── Main.java                        # 진입점 — 객체 조립, 모니터링 루프 실행
├── model/
│   ├── entity/
│   │   ├── Sample.java              # 시료 도메인 객체
│   │   ├── Order.java               # 주문 도메인 객체
│   │   └── OrderStatus.java         # 주문 상태 열거형
│   └── repository/
│       ├── SampleRepository.java    # 시료 조회 인터페이스
│       ├── OrderRepository.java     # 주문 조회 인터페이스
│       ├── InMemorySampleRepository.java
│       └── InMemoryOrderRepository.java
├── controller/
│   └── MonitoringController.java    # 모니터링 흐름 제어
├── view/
│   └── MonitoringView.java          # 대시보드 출력 포맷팅
└── app/
    └── MonitoringLoop.java          # 자동 갱신 루프 (폴링)

src/test/java/org/example/          # main과 미러링된 패키지 구조
```

---

## 역할 분리 규칙 (엄격 준수)

부모 CLAUDE.md의 MVC 레이어 규칙을 그대로 적용한다.

| 레이어 | 허용 | 금지 |
|--------|------|------|
| **Model** | 도메인 데이터 보유, 유효성 검사 | View·Controller import, 콘솔 I/O, 집계 계산 |
| **Controller** | Repository 호출, **데이터 집계·상태 판별**, View 위임, 갱신 주기 관리 | `System.out` 직접 출력, 도메인 유효성 로직 |
| **View** | `System.out` 출력, 테이블·배너 포맷팅 | Model 직접 수정, 집계·계산 비즈니스 로직 |

---

## 모니터링 전용 규칙

- **자동 갱신**: `MonitoringLoop`가 N초 간격으로 Controller를 반복 호출한다
- **화면 지우기**: 갱신 시마다 콘솔을 클리어(`\033[H\033[2J` 또는 `cls`/`clear`)한 뒤 재출력
- **갱신 주기**: 기본 3초, `Main`에서 상수로 정의
- **종료 조건**: `q` 입력 시 루프 종료 — 별도 입력 스레드로 감지
- **집계 로직**: Repository에서 조회한 데이터를 Controller가 집계하여 View에 전달 (View는 집계 금지)

---

## 코딩 컨벤션

부모 CLAUDE.md 컨벤션을 그대로 따른다.

- 클래스명: `PascalCase` / 메서드·변수명: `camelCase` / 상수: `UPPER_SNAKE_CASE`
- 패키지명: 전부 소문자
- Controller: 반드시 Constructor Injection으로 의존성 수신
- View: 인스턴스 메서드로 렌더링 — `static` 출력 메서드 금지
- InMemory 구현체: `InMemory` 접두사 사용
- 주석: WHY가 비자명한 경우에만 한 줄 이내

---

## 테스트 전략

- **Model**: 상태 판별 로직(`여유` / `부족` / `고갈`), 집계 계산 단위 테스트
- **Controller**: Spy View + 실제 InMemory Repository로 흐름 검증
- **View**: `System.out` 캡처로 출력 포맷 검증 (헤더·행·구분선)
- **통합**: 더미 데이터 주입 후 모니터링 1회 렌더링 시나리오 검증
- 목표 커버리지: Model·Controller 핵심 로직 **80% 이상**

---

## 빌드 & 실행

```bash
./gradlew build   # 빌드
./gradlew run     # 실행 (모니터링 루프 시작)
./gradlew test    # 테스트
```

---

## PoC 범위 제한

- 파일 영속성·JSON I/O는 **구현하지 않는다** (DataPersistence PoC 담당)
- 모든 데이터는 인메모리 더미 데이터로 초기화
- 주문 접수·승인·출고 기능은 **구현하지 않는다** (SSemi 본 프로젝트 담당)
- 설계 변경 필요 시 `docs/PRD.md` 먼저 수정 후 코드 변경
