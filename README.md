# Geulium-ieum (그리움-이음)

추모 및 가족 그룹 관리 서비스 서버 애플리케이션입니다.

## 개요
그리움-이음은 고인을 추모하는 공간을 생성하고, 가족 단위의 그룹을 통해 추억을 공유하며 소통할 수 있는 플랫폼을 제공합니다. 사용자는 온라인 추모관을 통해 헌화, 방명록, 앨범 등 다양한 방식으로 고인을 기릴 수 있습니다.

## 기술 스택
- **언어 및 프레임워크**: Java 21, Spring Boot 3.5.9
- **데이터베이스**: PostgreSQL, Redis (Cache/Session/Pub-Sub)
- **보안**: Spring Security, JWT, OAuth2 (Kakao, Naver)
- **인프라**: AWS S3 (Storage), Jib (Containerization)
- **모니터링**: Spring Boot Actuator, Micrometer (Prometheus), Loki
- **문서화**: Swagger/OpenAPI (SpringDoc)

## 프로젝트 구조
도메인 기반 패키지 구조
- `common`: 핵심 엔티티(`User`, `Memorial`, `FamilyGroup`), 공통 레포지토리, 공통 예외 처리
- `auth`: 소셜 로그인 및 JWT 기반 인증/인가 시스템
- `memorial`: 추모관 생성 및 관리, 상세 접근 정책(Family-Only 등) 제어
- `familygroup`: 가족 그룹 생성 및 멤버 초대/관리
- `tribute`, `guestbook`, `album`, `offering`: 추모 세부 기능 (헌화, 방명록, 사진첩 등)
- `notification`, `reminder`: Redis Pub/Sub 기반 실시간 SSE 알림 및 기일 리마인더
- `config`: Security, JPA, Redis, Async, OpenAPI 등 시스템 전반의 설정
- `admin`: 관리자 전용 대시보드 및 리소스 관리 기능

## 주요 기능 및 특징
1. **접근 제어**
   - 추모관별 공개 범위(전체 공개, 가족 공개, 비공개) 설정 기능을 제공합니다.

2. **실시간 알림 시스템 (SSE)**
   - **분산 환경 지원**: Redis Pub/Sub을 활용하여 다중 서버 환경에서도 끊김 없는 실시간 알림을 제공합니다.
   - **이벤트 기반 아키텍처**: Spring Event를 사용하여 도메인 로직과 알림 로직을 분리, 시스템 확장성을 높였습니다.

3. **성능 최적화**
   - **다각적 캐싱**: Redis를 통해 접근 권한, 프로필, 추모관 상세, 통계 데이터 등을 캐싱하여 DB 부하를 경감했습니다.
   - **N+1 문제 해결**: In 쿼리 및 데이터 구조 최적화를 통해 JPA 성능 저하 요인을 제거했습니다.

4. **운영 및 안정성**
   - **전역 예외 처리**: `GlobalExceptionHandler`를 통해 API 응답 형식을 일관되게 관리합니다.
   - **분산 트레이싱**: MDC TaskDecorator를 통해 비동기 작업에서도 Trace ID가 유지되도록 구성했습니다.
   - **자동화된 스케줄링**: 기일 리마인더 및 알림 데이터 정리 등 주기적인 관리 작업을 자동화했습니다.
