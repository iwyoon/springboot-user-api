# 프로젝트 개발 프롬프트 정리
- Java 17, Spring Boot 3.4.9, Spring Data JPA, Spring Data Redis, MySQL 8, Docker / Docker Compose로 프로젝트 개발할거야. docker-compose.yml 작성해줘

## 1. 회원가입 API
- 계정/암호/성명/주민등록번호/핸드폰번호/주소를 담는 User 엔티티 생성해줘
- UserRepository, UserService, UserController 작성해줘
- 계정(account)과 주민등록번호 unique 제약조건 설정 해서 schema.sql에 추가할 User 테이블 생성 sql 작성해줘
- 테스트 코드 작성해줘

## 2. 관리자 API (회원 조회/수정/삭제)
- 회원 목록 pagination 조회, 회원 정보(비밀번호, 주소) 수정, 회원 삭제 API를 담는 AdminService, AdminContorller 작성해줘
- 인증은 Spring Security + Basic Auth (username=admin, password=1212)로 구현하도록 SecurityConfig, CustomUserDetails, CustomUserDetailsService 작성해줘
- User 테이블 생성 시 관리자 계정(username=admin, password=1212)이 Insert 되도록 data.sql에 추가할 sql 작성해줘
- 테스트 코드 작성해줘

## 3. 사용자 로그인 & 본인 정보 조회
- 계정, 암호를 입력받아 로그인하도록 API를 UserService, UserController에 추가해줘
- 로그인한 사용자 본인 정보를 제공받는 API를 UserService, UserController에 추가해줘. 주소는 첫 번째 행정 구역까지만 잘라서 내려줘
- 테스트 코드 작성해줘

## 4. 관리자: 메시지 발송 API
- 연령대별 사용자 필터링하는 서비스를 AdminService에 추가해줘
- 호출 제한을 큐로 제어할 수 있도록 RedisConfig을 작성해줘
- 카카오톡 API 호출 (분당 100회 제한), 실패하면 SMS API 호출 (분당 500회 제한)하는 MessageService, RateLimiterService를 작성해줘
- 테스트 코드 작성해줘