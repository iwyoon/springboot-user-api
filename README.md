# User API - Spring Boot 3.4.9

## 📦 주요 기술 스택
- Java 17
- Spring Boot 3.4.9
- Spring Data JPA
- Spring Data Redis
- MySQL 8
- Docker / Docker Compose

---

## 🚀 실행 방법

### 1. 저장소 클론
```bash
git clone https://github.com/iwyoon/springboot-user-api.git
```

### 2. MySQL & Redis 실행
```bash
docker-compose up -d
```

####  MySQL
- Host: localhost
- Port: 3307
- Database: userdb
- User: user
- Password: pass

####  Redis
- Host: localhost
- Port: 6379

### 3. 서버 실행
Gradle을 이용해 Spring Boot 애플리케이션을 실행합니다.
```bash
./gradlew bootRun
```
또는 jar 빌드 후 실행:
```bash
./gradlew clean build
java -jar build/libs/user-api-0.0.1-SNAPSHOT.jar
```

### 4. 확인
서버가 실행되면 다음 주소에서 확인 가능합니다:
```link
http://localhost:8080
```

---
## API Base URL
```
http://localhost:8080/api
```

## 사용자 API

### 1. 회원가입
새로운 사용자를 등록합니다.

**Endpoint**
```
POST /users/signup
```

**Request Body**
```json
{
  "account": "string",       // 사용자 계정
  "password": "string",      // 비밀번호
  "name": "string",          // 이름
  "ssn": "string",           // 주민등록번호
  "phone": "string",         // 전화번호
  "address": "string"        // 주소
}
```

**Response**
```json
{
  "id": 1,
  "account": "string",
  "name": "string",
  "ssn": "string",
  "phone": "string",
  "address": "string",
  "role": "USER"
}
```

---

### 2. 로그인
사용자의 계정과 비밀번호로 로그인합니다.

**Endpoint**
```
POST /users/login
```

**Request Body**
```json
{
  "account": "string",
  "password": "string"
}
```

**Response**
```
"로그인 성공"
```

---

### 3. 본인 정보 조회
인증된 사용자의 상세 정보를 조회합니다. 주소는 가장 큰 단위 행정구역만 반환합니다.

**Endpoint**
```
GET /users/me
```

**Headers (Basic Auth)**
```
Authorization: Basic base64(account:password)
```

**Response**
```json
{
  "account": "string",
  "name": "string",
  "ssn": "string",
  "phone": "string",
  "address": "지역 단위 문자열"
}
```

---

## 관리자 API

### 1. 회원 조회 (페이징)
페이지 단위로 모든 회원 목록을 조회합니다.

**Endpoint**
```
GET /admin/users?page=0&size=10
```

**Response**
```json
{
  "content": [
    {
      "id": 1,
      "account": "string",
      "name": "string",
      "ssn": "string",
      "phone": "string",
      "address": "string",
      "role": "USER"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": true,
      "sorted": false,
      "unsorted": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "last": false,
  "totalElements": 9002,
  "totalPages": 901,
  "first": true,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": false,
    "unsorted": true
  },
  "numberOfElements": 10,
  "empty": false
}
```

---

### 2. 회원 수정
회원의 비밀번호나 주소를 수정할 수 있습니다.

**Endpoint**
```
PUT /admin/users/{id}
```

**Request Body**
```json
{
  "password": "string", // 변경할 비밀번호 (선택)
  "address": "string"   // 변경할 주소 (선택)
}
```

**Response**
```json
{
  "id": 1,
  "account": "string",
  "name": "string",
  "ssn": "string",
  "phone": "string",
  "address": "string",
  "role": "USER"
}
```

---

### 3. 회원 삭제
지정한 회원을 삭제합니다.

**Endpoint**
```
DELETE /admin/users/{id}
```

**Response**
```
"삭제 완료"
```

---

### 4. 메시지 전송
특정 연령대 회원에게 카카오톡 또는 SMS 메시지를 전송합니다.

**Endpoint**
```
POST /admin/messages/send
```

**Request Body**
```json
{
  "ageGroup": "10s|20s|30s|40s|50s|60s|70s|80s|90s|all",
  "message": "string"
}
```

**Response**
```
"발송 시작"
```