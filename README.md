# Dev_community

> 질문하고, 답변하고, 도움이 됐다면 채택까지 - 개발 학습자를 위한 Q&A 게시판입니다.

멋쟁이사자처럼 백엔드 24기 기초/응용 4조 팀 프로젝트입니다.

## 팀원

| 이름   | GitHub                                                     | 역할 |
| ------ | ---------------------------------------------------------- | ---- |
| 김재혁 | [@kbsjh8870](https://github.com/kbsjh8870)                 | 팀장 |
| 변재웅 | [@woong1116](https://github.com/woong1116)                 | 팀원 |
| 조민규 | [@Noisywhitecat-dev](https://github.com/Noisywhitecat-dev) | 팀원 |

## 주요 기능

- **회원**: 회원가입 / 로그인 / 로그아웃, JWT 기반 인증(Access/Refresh Token), 마이페이지(내 질문·내 답변 조회), 회원 탈퇴(soft delete)
- **질문**: 질문 작성 / 수정 / 삭제, 태그를 통한 분류, 목록 조회(태그·상태·정렬 필터), 조회수 집계
- **답변**: 답변 작성 / 수정 / 삭제, **답변 채택**(질문 상태를 해결/미해결로 전환)
- **좋아요**: 질문·답변에 대한 좋아요 토글
- **신고 / 관리자**: 질문·답변 신고, 관리자의 신고 목록 확인 및 회원 정지 처리
- **API 문서**: Swagger(OpenAPI)로 API 명세 제공

## 기술 스택

**Backend**

- Java 21, Spring Boot 4.1
- Spring Security, JWT (jjwt)
- Spring Data JPA, QueryDSL
- MySQL 8, Redis 7
- Springdoc OpenAPI(Swagger)
- Gradle

**Frontend**

- React 19, Vite
- React Router
- Axios

**Infra / CI**

- Docker, Docker Compose
- GitHub Actions (테스트 자동화 CI)
- Nginx (프론트엔드 서빙 / API 프록시)

## 프로젝트 구조

```
dev_community/
├── backend/                 # Spring Boot 백엔드
│   ├── src/main/java/com/likelion/dev_community/
│   │   ├── domain/          # user, question, answer, like, report, admin
│   │   └── common/          # 공통 설정, 예외, 유틸(XSS, 조회수 등)
│   ├── src/main/resources/  # application.yml (profile: local/prod/ci)
│   ├── docker-compose.yml   # 로컬 개발용 MySQL/Redis
│   └── build.gradle
├── frontend/                 # React (Vite) 프론트엔드
│   └── src/
│       ├── api/              # axios 기반 API 클라이언트
│       ├── pages/             # 라우트별 페이지 (auth, question, mypage, admin ...)
│       ├── components/        # 공통/도메인 컴포넌트
│       └── context, hooks, utils
├── docker-compose.prod.yml   # 배포용(MySQL + Redis + backend + frontend)
├── Dockerfile                 # 백엔드 이미지 빌드
└── .github/workflows/         # CI(테스트), 배포
```

## API 개요

전체 명세는 서버 실행 후 Swagger UI(`/swagger-ui/index.html`)에서 확인할 수 있습니다. 주요 엔드포인트는 다음과 같습니다.

**인증** (`/api/auth`)

| Method | Endpoint                   | 설명                |
| ------ | -------------------------- | ------------------- |
| POST   | `/api/auth/signup`         | 회원가입            |
| POST   | `/api/auth/login`          | 로그인              |
| POST   | `/api/auth/reissue`        | Access Token 재발급 |
| POST   | `/api/auth/logout`         | 로그아웃            |
| GET    | `/api/auth/check-username` | 아이디 중복 확인    |
| GET    | `/api/auth/check-nickname` | 닉네임 중복 확인    |

**회원** (`/api/members`)

| Method | Endpoint                    | 설명              |
| ------ | --------------------------- | ----------------- |
| GET    | `/api/members/me`           | 내 정보 조회      |
| PUT    | `/api/members/me`           | 내 정보 수정      |
| PUT    | `/api/members/me/password`  | 비밀번호 변경     |
| DELETE | `/api/members/me`           | 회원 탈퇴         |
| GET    | `/api/members/me/questions` | 내가 쓴 질문 목록 |
| GET    | `/api/members/me/answers`   | 내가 쓴 답변 목록 |

**질문** (`/api/questions`)

| Method | Endpoint              | 설명                                 |
| ------ | --------------------- | ------------------------------------ |
| POST   | `/api/questions`      | 질문 작성                            |
| GET    | `/api/questions`      | 질문 목록 조회 (태그·상태·정렬 필터) |
| GET    | `/api/questions/{id}` | 질문 상세 조회                       |
| PUT    | `/api/questions/{id}` | 질문 수정                            |
| DELETE | `/api/questions/{id}` | 질문 삭제                            |

**답변** (`/api/questions/{questionId}/answers`, `/api/answers`)

| Method | Endpoint                              | 설명           |
| ------ | ------------------------------------- | -------------- |
| POST   | `/api/questions/{questionId}/answers` | 답변 작성      |
| GET    | `/api/questions/{questionId}/answers` | 답변 목록 조회 |
| GET    | `/api/answers/{answerId}`             | 답변 상세 조회 |
| PATCH  | `/api/answers/{answerId}`             | 답변 수정      |
| DELETE | `/api/answers/{answerId}`             | 답변 삭제      |
| POST   | `/api/answers/{answerId}/adopt`       | 답변 채택      |
| DELETE | `/api/answers/{answerId}/adopt`       | 답변 채택 취소 |

**좋아요**

| Method | Endpoint                   | 설명             |
| ------ | -------------------------- | ---------------- |
| POST   | `/api/questions/{id}/like` | 질문 좋아요 토글 |
| POST   | `/api/answers/{id}/like`   | 답변 좋아요 토글 |

**신고 / 관리자** (`/api/reports`, `/api/admin`)

| Method | Endpoint                          | 설명                       |
| ------ | --------------------------------- | -------------------------- |
| POST   | `/api/reports`                    | 질문/답변 신고             |
| GET    | `/api/admin/reports`              | 신고 목록 조회             |
| PATCH  | `/api/admin/reports/{id}`         | 신고 처리                  |
| GET    | `/api/admin/users`                | 회원 목록 조회             |
| GET    | `/api/admin/users/{id}/reports`   | 특정 회원의 누적 신고 조회 |
| PATCH  | `/api/admin/users/{id}/suspend`   | 회원 정지                  |
| PATCH  | `/api/admin/users/{id}/unsuspend` | 회원 정지 해제             |

## 로컬 실행 방법

### 1. 저장소 클론

```bash
git clone https://github.com/likelion-backend-24th/dev_community.git
cd dev_community
```

### 2. 백엔드 실행

```bash
cd backend
cp .env.example .env   # 값 채워넣기 (DB_USER, DB_PASSWORD, JWT_SECRET 등)
```

MySQL / Redis를 Docker로 띄웁니다.

```bash
docker compose up -d
```

Gradle로 백엔드 서버를 실행합니다. (기본 profile: `local`, `http://localhost:8080`)

```bash
./gradlew bootRun
```

Swagger API 문서는 `http://localhost:8080/swagger-ui/index.html` 에서 확인할 수 있습니다.

### 3. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

기본적으로 `http://localhost:5173` 에서 실행되며, `.env.development`의 `VITE_API_BASE_URL`(기본값 `http://localhost:8080`)을 통해 백엔드 API를 호출합니다.

### 4. 한 번에 실행 (Docker Compose)

배포 환경과 동일하게 MySQL, Redis, 백엔드, 프론트엔드를 한 번에 띄우려면 저장소 루트에서:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```
