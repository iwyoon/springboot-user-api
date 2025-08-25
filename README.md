# User API - Spring Boot 3.4.9

간단한 사용자 관리 API 서버입니다.  
MySQL + Redis 기반으로 실행되며, 회원가입/로그인/관리자 기능 등을 제공합니다.

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
- Port: 3306
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

## 📦 주요 기술 스택
- Java 17
- Spring Boot 3.4.9
- Spring Data JPA
- Spring Data Redis
- MySQL 8
- Docker / Docker Compose