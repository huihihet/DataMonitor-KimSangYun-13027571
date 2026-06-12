# PRD — DataMonitor PoC

## 1. 목적

콘솔 환경에서 **실시간 자동 갱신 모니터링 대시보드** 패턴을 검증한다.  
일정 간격으로 데이터를 재조회하고 화면을 다시 렌더링하는 폴링 구조를 확립하여  
SSemi 본 프로젝트의 모니터링 뷰 포맷·갱신 구조의 기반을 수립한다.

---

## 2. 배경 및 문제 정의

| 현황 | 문제 |
|------|------|
| 모니터링 화면이 정적 단발 출력 | 데이터 변경 시 사용자가 수동 재조회해야 함 |
| 집계 로직이 출력 코드와 혼재될 위험 | 포맷 변경과 집계 로직 변경이 얽혀 유지보수 곤란 |

**목표**: 폴링 기반 자동 갱신 + MVC 역할 분리가 함께 동작하는 구조를 PoC로 시연한다.

---

## 3. 범위 (Scope)

### In-Scope

- 주문 상태별(RESERVED / PRODUCING / CONFIRMED / RELEASE) 건수 집계 및 표시
- 시료별 재고 상태(여유 / 부족 / 고갈) 판별 및 표시
- N초 간격 자동 갱신 (폴링 루프)
- `q` 입력으로 모니터링 종료
- MVC 역할 분리 검증

### Out-of-Scope

- JSON 파일 영속성 (DataPersistence PoC 담당)
- 주문 접수·승인·거절·출고 기능 (SSemi 본 프로젝트 담당)
- 생산 라인 큐 관리 (SSemi 본 프로젝트 담당)
- 네트워크·API, 인증·권한

---

## 4. 도메인 모델

### 엔티티

```
Sample {
    sampleId         : Long    // 고유 식별자
    name             : String  // 시료 이름
    avgProductionTime: int     // 평균 생산 시간(분), 1 이상
    yield            : double  // 수율 (0.0 초과 ~ 1.0 이하)
    stock            : int     // 현재 재고 수량, 0 이상
}

Order {
    orderId      : Long         // 고유 식별자
    sampleId     : Long         // 주문 시료 ID
    customerName : String       // 고객명
    quantity     : int          // 주문 수량, 1 이상
    status       : OrderStatus  // 주문 상태
}

OrderStatus {
    RESERVED   // 주문 접수
    PRODUCING  // 생산 중
    CONFIRMED  // 출고 대기
    RELEASE    // 출고 완료
    REJECTED   // 거절 (모니터링 표시 제외)
}
```

### 재고 상태 판별 규칙

| 상태 | 조건 |
|------|------|
| **고갈** | `stock == 0` |
| **부족** | `0 < stock < RESERVED + PRODUCING 주문의 총 quantity` |
| **여유** | `stock >= RESERVED + PRODUCING 주문의 총 quantity` |

---

## 5. 기능 요구사항

| ID | 기능 | 설명 |
|----|------|------|
| F-01 | 주문 상태별 건수 표시 | RESERVED / PRODUCING / CONFIRMED / RELEASE 각 건수 표시 |
| F-02 | 시료별 재고 현황 표시 | sampleId·이름·재고·상태(여유/부족/고갈) 테이블 출력 |
| F-03 | 자동 갱신 | 기본 3초 간격으로 전체 화면 재렌더링 |
| F-04 | 종료 | `q` 키 입력 시 루프 종료, "모니터링을 종료합니다." 출력 |
| F-05 | 갱신 타임스탬프 표시 | 마지막 갱신 시각을 `HH:mm:ss` 형식으로 헤더에 표시 |

---

## 6. 출력 형식

### 대시보드 레이아웃

```
========================================
  DataMonitor — S-Semi 실시간 모니터링
  마지막 갱신: 14:32:05  (q: 종료)
========================================

[주문 현황]
  RESERVED  :   3건
  PRODUCING :   1건
  CONFIRMED :   2건
  RELEASE   :   5건

[시료별 재고 현황]
  ID  이름          재고   상태
  --  ----------  ------  ----
   1  AlphaChip      120   여유
   2  BetaWafer        0   고갈
   3  GammaCore       15   부족

========================================
  다음 갱신까지 3초...
========================================
```

### 출력 규칙

- 구분선은 `=` 40자
- 테이블 컬럼 너비: ID(4) / 이름(12) / 재고(7) / 상태(4)
- 재고 상태 색상: 여유(기본) / 부족(황색 `\033[33m`) / 고갈(적색 `\033[31m`) — ANSI 지원 환경에서만 적용, 미지원 시 텍스트만 출력
- REJECTED 상태 주문은 건수 집계에서 제외

---

## 7. 비기능 요구사항

| 항목 | 기준 |
|------|------|
| 빌드 | `./gradlew build` 경고 없이 성공 |
| 테스트 커버리지 | Model·Controller 핵심 로직 80% 이상 |
| 의존 방향 | Controller → Model, Controller → View (역방향 금지) |
| 외부 의존성 | JUnit 외 추가 라이브러리 없음 |
| 갱신 주기 | 상수(`REFRESH_INTERVAL_SECONDS`)로 정의, 기본값 3 |

---

## 8. 아키텍처 설계

### 레이어 다이어그램

```
┌──────────────────────────────────────┐
│               Main                   │  객체 조립 · MonitoringLoop 실행
└──────────────────┬───────────────────┘
                   │ Constructor Injection
                   ▼
┌──────────────────────────────────────┐
│           MonitoringLoop             │  폴링 루프 · 키 입력 감지 스레드 관리
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│        MonitoringController          │  Repository 호출 · 집계 · View 위임
└──────────┬───────────────────────────┘
           │                    │
           ▼                    ▼
┌──────────────────┐   ┌──────────────────┐
│  SampleRepository│   │  MonitoringView  │
│  OrderRepository │   │  (출력 포맷팅)    │
│  (InMemory impls)│   └──────────────────┘
└──────────────────┘
```

### 클래스 책임 요약

| 클래스 | 패키지 | 책임 |
|--------|--------|------|
| `Sample` | `model.entity` | 시료 도메인 데이터 보유 |
| `Order` | `model.entity` | 주문 도메인 데이터 보유 |
| `OrderStatus` | `model.entity` | 주문 상태 열거형 |
| `SampleRepository` | `model.repository` | 시료 조회 계약 (인터페이스) |
| `OrderRepository` | `model.repository` | 주문 조회 계약 (인터페이스) |
| `InMemorySampleRepository` | `model.repository` | 리스트 기반 인메모리 구현, 더미 데이터 초기화 포함 |
| `InMemoryOrderRepository` | `model.repository` | 리스트 기반 인메모리 구현, 더미 데이터 초기화 포함 |
| `MonitoringController` | `controller` | 데이터 집계(상태별 건수, 재고 상태 판별), View 위임 |
| `MonitoringView` | `view` | 대시보드 포맷팅 및 `System.out` 렌더링 |
| `MonitoringLoop` | `app` | N초 폴링 루프, `q` 입력 감지 스레드 관리 |
| `Main` | (루트) | 객체 조립 및 루프 실행 |

---

## 9. 더미 데이터 명세

### 시료 (3개)

| sampleId | name | avgProductionTime | yield | stock |
|----------|------|-------------------|-------|-------|
| 1 | AlphaChip | 30 | 0.90 | 120 |
| 2 | BetaWafer | 45 | 0.75 | 0 |
| 3 | GammaCore | 60 | 0.85 | 15 |

### 주문 (8개)

| orderId | sampleId | customerName | quantity | status |
|---------|----------|--------------|----------|--------|
| 1 | 1 | 서울대 연구실 | 50 | RESERVED |
| 2 | 2 | 카이스트 팹리스 | 30 | PRODUCING |
| 3 | 3 | 삼성리서치 | 20 | CONFIRMED |
| 4 | 1 | LG이노텍 | 10 | RELEASE |
| 5 | 2 | 포스텍 | 25 | RESERVED |
| 6 | 3 | 연세대 반도체 | 40 | PRODUCING |
| 7 | 1 | SK하이닉스 | 15 | CONFIRMED |
| 8 | 2 | 한양대 연구소 | 10 | REJECTED |

---

## 10. 수용 기준 (Acceptance Criteria)

- [ ] `./gradlew build`가 경고 없이 성공한다
- [ ] F-01~F-05 기능이 콘솔에서 정상 동작한다
- [ ] 3초 간격으로 화면이 자동 갱신된다
- [ ] `q` 입력 시 루프가 종료된다
- [ ] Controller 클래스에 `System.out` 직접 호출이 없다
- [ ] Model 클래스에 콘솔 I/O 코드가 없다
- [ ] View 클래스에 집계·계산 로직이 없다
- [ ] REJECTED 주문이 현황 건수에 포함되지 않는다
- [ ] 재고 상태(여유/부족/고갈)가 규칙에 맞게 판별된다
- [ ] `./gradlew test`에서 모든 테스트가 통과한다
- [ ] Model·Controller 커버리지 80% 이상

---

## 11. 마일스톤

| 단계 | 내용 |
|------|------|
| M1 | 패키지 스켈레톤 + 빈 클래스 생성, 더미 데이터 초기화 |
| M2 | Model(entity + Repository) 구현 + 단위 테스트 |
| M3 | Controller(집계 로직) + View(출력 포맷) 구현 + 단위 테스트 |
| M4 | MonitoringLoop(폴링·키 입력) + Main 조립 + 통합 검증 |
| M5 | ANSI 색상 적용, 커버리지 측정, PoC 검증 완료 |
