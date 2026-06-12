# Phase 1 설계 — 프로젝트 스켈레톤

> 기준: `docs/PLAN.md > Phase 1` / PRD 마일스톤: M1  
> 목표: 패키지 구조와 빈 클래스를 생성하고 `./gradlew build`가 통과하는 상태를 만든다.

---

## 1. 현재 상태

| 항목 | 상태 |
|------|------|
| Gradle 프로젝트 | 완료 (`build.gradle`, `settings.gradle`, Gradle 래퍼 존재) |
| Java 17 설정 | 미설정 — `build.gradle`에 `sourceCompatibility` 없음 |
| `application` 플러그인 | 미설정 — `mainClass` 미지정으로 `./gradlew run` 불가 |
| JaCoCo 플러그인 | 미설정 — `jacocoTestReport` 태스크 없음 |
| JUnit 6.x 의존성 | 완료 (`junit-bom:6.0.0`) |
| 패키지 디렉터리 | 미생성 — `src/` 하위 전체 비어 있음 |
| 빈 클래스 파일 | 미생성 |

---

## 2. `build.gradle` 수정 설계

```groovy
plugins {
    id 'java'
    id 'application'          // gradlew run 지원
    id 'jacoco'               // 커버리지 리포트 지원
}

group   = 'org.example'
version = '1.0-SNAPSHOT'

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass = 'org.example.Main'
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation platform('org.junit:junit-bom:6.0.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly    'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required  = false
        html.required = true
    }
}
```

---

## 3. 패키지 구조 설계

```
src/main/java/org/example/
├── Main.java
├── model/
│   ├── entity/
│   │   ├── Sample.java
│   │   ├── Order.java
│   │   ├── OrderStatus.java
│   │   └── SampleStatus.java        # Controller → View 전달용 record
│   └── repository/
│       ├── SampleRepository.java    # 인터페이스
│       ├── OrderRepository.java     # 인터페이스
│       ├── InMemorySampleRepository.java
│       └── InMemoryOrderRepository.java
├── controller/
│   └── MonitoringController.java
├── view/
│   └── MonitoringView.java
└── app/
    └── MonitoringLoop.java

src/test/java/org/example/
├── model/
│   ├── entity/
│   │   ├── SampleTest.java
│   │   ├── OrderTest.java
│   │   └── SampleStatusTest.java
│   └── repository/
│       ├── InMemorySampleRepositoryTest.java
│       └── InMemoryOrderRepositoryTest.java
├── controller/
│   └── MonitoringControllerTest.java
├── view/
│   └── MonitoringViewTest.java
└── integration/
    └── MonitoringIntegrationTest.java
```

---

## 4. 빈 클래스 설계

각 클래스는 컴파일 가능한 최소 상태로 생성한다. 필드·메서드는 Phase 2~4에서 채운다.

### `Main.java`

```java
package org.example;

public class Main {
    public static void main(String[] args) {}
}
```

### `model/entity/OrderStatus.java`

```java
package org.example.model.entity;

public enum OrderStatus {
    RESERVED, PRODUCING, CONFIRMED, RELEASE, REJECTED
}
```

> 열거형은 Phase 2에서 확장 없이 그대로 사용하므로 Phase 1에서 완전하게 정의한다.

### `model/entity/Sample.java`

```java
package org.example.model.entity;

public class Sample {}
```

### `model/entity/Order.java`

```java
package org.example.model.entity;

public class Order {}
```

### `model/entity/SampleStatus.java`

```java
package org.example.model.entity;

public record SampleStatus(long sampleId, String name, int stock, String stockLevel) {}
```

> `record`는 컴파일러가 생성자·getter·equals·hashCode를 자동 생성하므로  
> 필드 선언만으로 컴파일이 통과한다. stockLevel 허용값: "여유" | "부족" | "고갈"

### `model/repository/SampleRepository.java`

```java
package org.example.model.repository;

import org.example.model.entity.Sample;
import java.util.List;
import java.util.Optional;

public interface SampleRepository {}
```

### `model/repository/OrderRepository.java`

```java
package org.example.model.repository;

import org.example.model.entity.Order;
import org.example.model.entity.OrderStatus;
import java.util.List;

public interface OrderRepository {}
```

### `model/repository/InMemorySampleRepository.java`

```java
package org.example.model.repository;

public class InMemorySampleRepository implements SampleRepository {}
```

### `model/repository/InMemoryOrderRepository.java`

```java
package org.example.model.repository;

public class InMemoryOrderRepository implements OrderRepository {}
```

### `controller/MonitoringController.java`

```java
package org.example.controller;

public class MonitoringController {}
```

### `view/MonitoringView.java`

```java
package org.example.view;

public class MonitoringView {}
```

### `app/MonitoringLoop.java`

```java
package org.example.app;

public class MonitoringLoop {}
```

---

## 5. 테스트 빈 클래스 설계

빌드 시 컴파일 오류 없이 통과할 수 있도록 최소 구조만 작성한다.

```java
// 공통 패턴 — 각 테스트 클래스 동일 구조
package org.example.{패키지};

import org.junit.jupiter.api.Test;

class {ClassName} {
    @Test
    void placeholder() {}
}
```

| 테스트 파일 | 실제 구현 Phase |
|------------|----------------|
| `SampleTest` | Phase 2 |
| `OrderTest` | Phase 2 |
| `OrderStatusTest` | Phase 2 |
| `SampleStatusTest` | Phase 2 |
| `InMemorySampleRepositoryTest` | Phase 2 |
| `InMemoryOrderRepositoryTest` | Phase 2 |
| `MonitoringControllerTest` | Phase 3 |
| `MonitoringViewTest` | Phase 3 |
| `MonitoringIntegrationTest` | Phase 4 |

---

## 6. 의존 관계 다이어그램

Phase 1에서 인터페이스와 클래스 골격을 배치하면 아래 의존 방향이 성립한다.  
화살표 방향 위반(역방향 import)이 없는지 Phase 1 완료 시점에 확인한다.

```
Main
 └─► MonitoringLoop          (app)
          └─► MonitoringController  (controller)
                   ├─► SampleRepository    (model/repository)
                   │        └─► InMemorySampleRepository
                   ├─► OrderRepository     (model/repository)
                   │        └─► InMemoryOrderRepository
                   └─► MonitoringView      (view)

model/entity: Sample, Order, OrderStatus, SampleStatus
  └─ Controller·Repository·View 에서 import 가능
  └─ Controller·View·Repository 를 역방향 import 금지
```

---

## 7. 완료 기준

- [ ] `./gradlew build` 경고 없이 성공
- [ ] `./gradlew run` 실행 시 Main 진입점 호출 확인 (빈 main 메서드 실행)
- [ ] `./gradlew test` 통과 (placeholder 테스트 포함)
- [ ] 모든 클래스가 CLAUDE.md 패키지 규칙에 맞게 위치
- [ ] `OrderStatus` 열거형 5개 상수 완전 정의
- [ ] `SampleStatus` record 4개 컴포넌트 완전 정의
- [ ] 테스트 placeholder 9개 파일 생성 (`OrderStatusTest` 포함)
- [ ] 레이어 역방향 import 없음 (Model이 Controller·View를 import하지 않음)
- [ ] `.idea/` 등 IDE 파일이 `.gitignore`에 포함되어 추적되지 않음
