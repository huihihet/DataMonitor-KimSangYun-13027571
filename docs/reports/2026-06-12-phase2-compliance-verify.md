# 컴플라이언스 검증 보고서

**일시**: 2026-06-12  
**검증 대상**: `docs/design/phase2.md` (Phase 2 — Model 구현)  
**결과**: ❌ 위반 3건 (CRITICAL: 1, WARNING: 2)

---

## 발견된 위반

### [CRITICAL] 테스트 메서드명에 언더스코어(_) 사용 — camelCase 위반

- **위치**: `docs/design/phase2.md` — 섹션 7 (단위 테스트 설계), 전체 테스트 메서드 목록
- **위반 규칙**: 루트 CLAUDE.md 및 DataMonitor CLAUDE.md 공통 — "메서드·변수명: `camelCase`". 예외 규정 없음.
- **현재 설계**: 모든 테스트 메서드명이 `_`로 단어를 구분하는 스네이크케이스 변형 사용
  - `createSample_allFieldsStored`
  - `createSample_yieldAtMin`
  - `findAll_returnsThreeDummySamples`
  - `findByStatus_reserved_returnsTwoOrders`
  - `createRecord_allComponentsStored` 등
- **권장 수정**: `_` 없이 camelCase로 통일
  - `createSampleWithAllFieldsStored`
  - `createSampleYieldAtMinThrowsException`
  - `findAllReturnsThreeDummySamples`
  - `findByStatusReservedReturnsTwoOrders`
  - `createRecordWithAllComponentsStored`

---

### [WARNING] ID 필드 null 검증 누락 — 설계 내부 불일치

- **위치**: `docs/design/phase2.md` — 섹션 2 (Sample 유효성 검증 규칙), 섹션 3 (Order 유효성 검증 규칙)
- **위반 규칙**: 설계 문서 자체의 필드 제약 명세와 유효성 검증 규칙 테이블 간 불일치. 루트 CLAUDE.md — Model에서 "유효성 검사" 허용이 명시되어 있으므로 null ID는 생성자에서 방어해야 한다.
- **현재 설계**:
  - Sample 필드 제약: `sampleId — null 불허` 명시
  - Order 필드 제약: `orderId — null 불허`, `sampleId — null 불허` 명시
  - 그러나 유효성 검증 규칙 테이블에 `sampleId == null`, `orderId == null` 케이스가 없음
  - 테스트 설계(섹션 7-1, 7-2)에도 ID null 케이스가 없음
- **권장 수정**: 유효성 검증 규칙 테이블에 다음 항목 추가
  - Sample: `sampleId == null` → `"시료 ID는 null일 수 없습니다."` + `IllegalArgumentException`
  - Order: `orderId == null` → `"주문 ID는 null일 수 없습니다."`, `sampleId == null` → `"시료 ID는 null일 수 없습니다."` + `IllegalArgumentException`
  - 각 테스트 클래스에 대응하는 null ID 테스트 케이스 추가

---

### [WARNING] 미래 확장 가능성을 근거로 한 설계 결정 주석 — 불필요한 복잡성 유발 가능

- **위치**: `docs/design/phase2.md` — 섹션 5 (`InMemorySampleRepository` 설계 결정)
- **위반 규칙**: 루트 CLAUDE.md 검증 항목 [D] — "미래 요구 사항을 위한 불필요한 확장 포인트 금지". CLAUDE.md 주석 규칙 — "WHY가 비자명한 경우에만 한 줄 이내".
- **현재 설계**: 
  ```
  설계 결정:
  - store는 new ArrayList<>(...)로 생성 — 이후 Phase에서 데이터 추가 확장 가능성 고려
  ```
  PoC 범위 제한(DataMonitor CLAUDE.md)에 따르면 주문 접수·승인 등 데이터 변경 기능은 구현하지 않는다. 그럼에도 `ArrayList`를 선택한 이유를 "이후 Phase 확장 가능성"으로 정당화하는 것은 현재 Phase 요구 사항에 없는 기능을 위해 설계 결정을 유보하는 패턴이다.
- **권장 수정**: 설계 결정 주석을 제거하거나 현재 Phase 내 실제 이유(더미 데이터 가변 리스트 필요성)만 남긴다. 미래 확장 목적의 설계 결정 언급은 삭제한다.
  ```
  설계 결정:
  - findAll()은 Collections.unmodifiableList로 래핑하여 호출자의 외부 변경 방지
  ```

---

## 검증 결과 요약

| 항목 | 결과 | 비고 |
|------|------|------|
| [A] 아키텍처 제약 | ✅ | 패키지 구조, 레이어 분리, I 접두사 없음, InMemory 접두사 모두 준수 |
| [B] 코딩 컨벤션 | ❌ | 테스트 메서드명 camelCase 위반 (CRITICAL 1건) |
| [C] 보안 | ✅ | SQL/커맨드 인젝션 위험 없음 (인메모리), 민감 정보 없음 |
| [D] 불필요한 복잡성 | ❌ | 미래 확장 가능성 근거 설계 결정 (WARNING 1건), ID null 검증 누락 (WARNING 1건) |

---

## 통과 항목 확인

| 체크 항목 | 결과 |
|-----------|------|
| Sample·Order 모든 필드 `private final` + getter 전용(setter 없음) | ✅ |
| 유효성 검증 생성자에서만 수행, Model에 콘솔 I/O 없음 | ✅ |
| Repository 인터페이스명 I 접두사 없음 (`SampleRepository`, `OrderRepository`) | ✅ |
| 구현체 `InMemory` 접두사 사용 | ✅ |
| Model 클래스가 Controller·View를 import하지 않음 | ✅ |
| 클래스명 PascalCase, 패키지명 소문자 | ✅ |
| `findAll()` `Collections.unmodifiableList` 래핑 — 캡슐화 원칙 부합 | ✅ |
| 더미 데이터 생성자 초기화 — DataMonitor CLAUDE.md 허용 범위 내 | ✅ |
