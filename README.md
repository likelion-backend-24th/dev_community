# Dev_community

> 질문하고, 답변하고, 도움이 됐다면 채택까지 - 개발 학습자를 위한 Q&A 게시판입니다.
> 멤버십(구독) 게시판, AI 요약/태그 추천, 코드리뷰, 커리어상담 1:1 채팅까지 지원합니다.

멋쟁이사자처럼 백엔드 24기 기초/응용 4조 팀 프로젝트입니다.

## 팀원

| 이름   | GitHub                                                     | 역할 |
| ------ | ---------------------------------------------------------- | ---- |
| 김재혁 | [@kbsjh8870](https://github.com/kbsjh8870)                 | 팀장 |
| 변재웅 | [@woong1116](https://github.com/woong1116)                 | 팀원 |
| 조민규 | [@Noisywhitecat-dev](https://github.com/Noisywhitecat-dev) | 팀원 |

## 주요 기능

- **회원**: 회원가입 / 로그인 / 로그아웃, JWT 기반 인증(Access/Refresh Token), GitHub·Google 소셜 로그인, 마이페이지(내 질문·내 답변 조회, 개인 대시보드), 회원 탈퇴(soft delete), 전문가 인증 신청/승인
- **질문**: 질문 작성 / 수정 / 삭제, 태그를 통한 분류(AI 태그 추천 지원), 목록 조회(태그·상태·정렬·검색 필터), 조회수 집계, 유형별 작성(일반 / 코드리뷰 / 커리어상담), AI 본문 요약
- **답변**: 답변 작성 / 수정 / 삭제, **답변 채택**(질문 상태를 해결/미해결로 전환), 코드리뷰 유형 질문에 대한 라인별 코드 코멘트
- **좋아요**: 질문·답변에 대한 좋아요 토글
- **멤버십(구독)**: PortOne 카드 정기결제로 구독, 구독자 전용 프리미엄 게시판(익명 작성 가능), 결제 취소/환불(7일 이내), 웹훅 기반 결제 상태 동기화
- **커리어상담 채팅**: 커리어상담 유형 질문에 대한 1:1 채팅방 개설/수락/채택, WebSocket(STOMP) 기반 실시간 메시지
- **알림**: 새 답변/채택/채팅 등 실시간 알림, 읽음 처리
- **첨부파일**: 질문/답변에 이미지·코드 파일 첨부
- **신고 / 관리자**: 질문·답변 신고, 관리자의 신고 목록 확인 및 회원 정지 처리, 전문가 인증 승인/해제, 서비스 통계(일별 추이, 해결률, 방치된 질문, 인기 질문) 대시보드
- **약관**: 이용약관 / 개인정보처리방침 페이지
- **API 문서**: Swagger(OpenAPI)로 API 명세 제공

## 기술 스택

**Backend**

- Java 21, Spring Boot 4.1
- Spring Security, Spring OAuth2 Client(GitHub/Google 소셜 로그인), JWT (jjwt)
- Spring Data JPA, QueryDSL
- Spring WebSocket(STOMP) - 실시간 채팅/알림
- MySQL 8, Redis 7(Refresh Token 저장)
- PortOne 서버 SDK - 카드 정기결제/웹훅
- Gemini API - AI 질문 요약, 태그 추천
- Springdoc OpenAPI(Swagger)
- Gradle

**Frontend**

- React 19, Vite
- React Router
- Axios
- @stomp/stompjs - 실시간 채팅/알림 WebSocket 클라이언트
- react-markdown, remark-gfm, rehype-highlight, prismjs - 마크다운/코드 하이라이팅
- Recharts - 관리자 통계 차트

**Infra / CI**

- Docker, Docker Compose
- GitHub Actions (테스트 자동화 CI, 배포 CD)
- Nginx (프론트엔드 서빙 / API 프록시)

## 프로젝트 구조

```
dev_community/
├── backend/                 # Spring Boot 백엔드
│   ├── src/main/java/com/likelion/dev_community/
│   │   ├── domain/          # user, question, answer, like, report, attachment,
│   │   │                     chat, notification, subscription, payment,
│   │   │                     reputation, dashboard, admin
│   │   ├── security/        # JWT, OAuth2, WebSocket(STOMP) 인증
│   │   └── common/          # 공통 설정, 예외, 유틸(XSS, 조회수, Gemini 클라이언트 등)
│   ├── src/main/resources/  # application.yml (profile: local/prod/ci)
│   ├── docker-compose.yml   # 로컬 개발용 MySQL/Redis
│   └── build.gradle
├── frontend/                 # React (Vite) 프론트엔드
│   └── src/
│       ├── api/              # axios 기반 API 클라이언트, WebSocket 클라이언트
│       ├── pages/             # 라우트별 페이지 (auth, question, mypage, membership, chat, admin, legal ...)
│       ├── components/        # 공통/도메인 컴포넌트
│       ├── styles/            # 디자인 시스템 기반 CSS
│       └── context, hooks, utils
├── docker-compose.prod.yml   # 배포용(MySQL + Redis + backend + frontend)
├── Dockerfile                 # 백엔드 이미지 빌드
└── .github/workflows/         # CI(테스트), 배포(CD)
```

## API 개요

전체 명세는 서버 실행 후 Swagger UI(`/swagger-ui/index.html`)에서 확인할 수 있습니다. 주요 엔드포인트는 다음과 같습니다.

**인증** (`/api/auth`)

| Method | Endpoint                   | 설명                                     |
| ------ | --------------------------- | ---------------------------------------- |
| POST   | `/api/auth/signup`         | 회원가입                                 |
| POST   | `/api/auth/login`          | 로그인                                   |
| POST   | `/api/auth/reissue`        | Access Token 재발급                      |
| POST   | `/api/auth/logout`         | 로그아웃                                 |
| GET    | `/api/auth/check-username` | 아이디 중복 확인                         |
| GET    | `/api/auth/check-nickname` | 닉네임 중복 확인                         |
| POST   | `/api/auth/oauth/complete` | 소셜 로그인 최초 가입 시 닉네임 설정 완료 |

GitHub/Google 소셜 로그인은 `/oauth2/authorization/{github|google}`로 리다이렉트해 시작하며, 인증 완료 후 프론트엔드로 Access Token과 함께 리다이렉트됩니다.

**회원** (`/api/members`)

| Method | Endpoint                    | 설명                    |
| ------ | ---------------------------- | ----------------------- |
| GET    | `/api/members/me`           | 내 정보 조회             |
| PUT    | `/api/members/me`           | 내 정보 수정             |
| PUT    | `/api/members/me/password`  | 비밀번호 변경            |
| DELETE | `/api/members/me`           | 회원 탈퇴                |
| GET    | `/api/members/me/questions` | 내가 쓴 질문 목록        |
| GET    | `/api/members/me/answers`   | 내가 쓴 답변 목록        |
| POST   | `/api/members/me/expert-request` | 전문가 인증 신청     |
| GET    | `/api/members/me/subscription` | 내 구독 정보 조회      |
| GET    | `/api/members/me/dashboard/summary` | 개인 대시보드 요약 |
| GET    | `/api/members/me/dashboard/timeline` | 개인 활동 타임라인 |

**질문** (`/api/questions`)

| Method | Endpoint                    | 설명                                          |
| ------ | ---------------------------- | ---------------------------------------------- |
| POST   | `/api/questions`             | 질문 작성 (유형: 일반/코드리뷰/커리어상담)     |
| GET    | `/api/questions`             | 질문 목록 조회 (태그·상태·정렬·검색 필터)      |
| GET    | `/api/questions/premium`     | 멤버십 전용 게시판 질문 목록 조회              |
| GET    | `/api/questions/{id}`        | 질문 상세 조회                                 |
| PUT    | `/api/questions/{id}`        | 질문 수정                                      |
| DELETE | `/api/questions/{id}`        | 질문 삭제                                      |
| GET    | `/api/questions/{id}/summary` | AI 본문 요약 (구독자/관리자 전용)             |
| POST   | `/api/questions/tags/suggest` | AI 태그 추천 (구독자/관리자 전용)             |

**코드 코멘트** (`/api/questions/{questionId}/code-comments`, 코드리뷰 유형 질문 전용)

| Method | Endpoint                                          | 설명                |
| ------ | -------------------------------------------------- | ------------------- |
| POST   | `/api/questions/{questionId}/code-comments`        | 라인 코멘트 작성    |
| PUT    | `/api/questions/{questionId}/code-comments/{commentId}` | 라인 코멘트 수정 |
| DELETE | `/api/questions/{questionId}/code-comments/{commentId}` | 라인 코멘트 삭제 |

**답변** (`/api/questions/{questionId}/answers`, `/api/answers`)

| Method | Endpoint                              | 설명           |
| ------ | -------------------------------------- | -------------- |
| POST   | `/api/questions/{questionId}/answers` | 답변 작성      |
| GET    | `/api/questions/{questionId}/answers` | 답변 목록 조회 |
| GET    | `/api/answers/{answerId}`             | 답변 상세 조회 |
| PATCH  | `/api/answers/{answerId}`             | 답변 수정      |
| DELETE | `/api/answers/{answerId}`             | 답변 삭제      |
| POST   | `/api/answers/{answerId}/adopt`       | 답변 채택      |
| DELETE | `/api/answers/{answerId}/adopt`       | 답변 채택 취소 |

**좋아요**

| Method | Endpoint                   | 설명             |
| ------ | ---------------------------- | ---------------- |
| POST   | `/api/questions/{id}/like`  | 질문 좋아요 토글 |
| POST   | `/api/answers/{id}/like`    | 답변 좋아요 토글 |
| GET    | `/api/questions/{id}/like-status` | 좋아요 여부 조회 |

**첨부파일** (`/api/attachments`)

| Method | Endpoint                                     | 설명           |
| ------ | ---------------------------------------------- | -------------- |
| POST   | `/api/questions/{questionId}/attachments`     | 질문 첨부파일 업로드 |
| POST   | `/api/answers/{answerId}/attachments`         | 답변 첨부파일 업로드 |
| GET    | `/api/questions/{questionId}/attachments`     | 질문 첨부파일 목록   |
| GET    | `/api/answers/{answerId}/attachments`         | 답변 첨부파일 목록   |
| GET    | `/api/attachments/{attachmentId}`             | 첨부파일 다운로드    |
| DELETE | `/api/attachments/{attachmentId}`             | 첨부파일 삭제       |

**멤버십 구독 / 결제** (`/api/payments`)

| Method | Endpoint                          | 설명                            |
| ------ | ----------------------------------- | ------------------------------- |
| POST   | `/api/payments/billing/prepare`    | 정기결제 빌링키 발급 준비        |
| POST   | `/api/payments/billing/issue`      | 빌링키 발급 완료 및 첫 결제      |
| GET    | `/api/payments/me/latest`          | 내 최근 결제 조회                |
| POST   | `/api/payments/{paymentId}/cancel` | 결제 취소(환불, 7일 이내)        |
| POST   | `/api/payments/webhook`            | PortOne 결제 웹훅 수신           |

**커리어상담 채팅** (`/api/chat-rooms`, WebSocket `/ws`)

| Method | Endpoint                                  | 설명                    |
| ------ | -------------------------------------------- | ----------------------- |
| POST   | `/api/questions/{questionId}/chat-rooms`   | 채팅방 개설(첫 메시지 포함) |
| GET    | `/api/chat-rooms`                          | 내 채팅방 목록           |
| GET    | `/api/chat-rooms/unread-count`             | 안 읽은 채팅 수          |
| GET    | `/api/chat-rooms/{roomId}`                 | 채팅방 상세(메시지 포함) |
| PATCH  | `/api/chat-rooms/{roomId}/read`            | 채팅방 읽음 처리          |
| POST   | `/api/chat-rooms/{roomId}/messages`        | 메시지 전송               |
| PATCH  | `/api/chat-rooms/{roomId}/accept`          | 채팅 수락                 |
| PATCH  | `/api/chat-rooms/{roomId}/adopt`           | 채팅을 답변으로 채택(질문 해결 처리) |

새 메시지는 WebSocket(`/ws`, STOMP)으로 실시간 수신됩니다.

**알림** (`/api/notifications`)

| Method | Endpoint                       | 설명           |
| ------ | -------------------------------- | -------------- |
| GET    | `/api/notifications`            | 최근 알림 목록 |
| PATCH  | `/api/notifications/read-all`   | 전체 읽음 처리 |

**신고 / 관리자** (`/api/reports`, `/api/admin`)

| Method | Endpoint                                | 설명                          |
| ------ | ----------------------------------------- | ----------------------------- |
| POST   | `/api/reports`                           | 질문/답변 신고                |
| GET    | `/api/admin/reports`                     | 신고 목록 조회                |
| PATCH  | `/api/admin/reports/{id}`                | 신고 처리                     |
| GET    | `/api/admin/users`                       | 회원 목록 조회                |
| GET    | `/api/admin/users/{id}/reports`          | 특정 회원의 누적 신고 조회     |
| PATCH  | `/api/admin/users/{id}/suspend`          | 회원 정지                     |
| PATCH  | `/api/admin/users/{id}/unsuspend`        | 회원 정지 해제                |
| PATCH  | `/api/admin/users/{id}/expert`           | 전문가 인증 승인               |
| DELETE | `/api/admin/users/{id}/expert`           | 전문가 인증 해제               |
| POST   | `/api/admin/users/{id}/expert-request/reject` | 전문가 인증 신청 반려     |
| GET    | `/api/admin/users/{id}/dashboard/summary` | 특정 회원 대시보드 요약 조회 |
| GET    | `/api/admin/users/{id}/dashboard/timeline` | 특정 회원 활동 타임라인 조회 |
| GET    | `/api/admin/users/{id}/questions`        | 특정 회원의 질문 목록          |
| GET    | `/api/admin/users/{id}/answers`          | 특정 회원의 답변 목록          |
| GET    | `/api/admin/stats/daily-trend`           | 일별 질문/답변 추이            |
| GET    | `/api/admin/stats/resolution-rate`       | 질문 해결률                   |
| GET    | `/api/admin/stats/stale-questions`       | 오래 방치된 미해결 질문        |
| GET    | `/api/admin/stats/top-questions`         | 인기 질문 순위                |

## 로컬 실행 방법

### 1. 저장소 클론

```bash
git clone https://github.com/likelion-backend-24th/dev_community.git
cd dev_community
```

### 2. 백엔드 실행

```bash
cd backend
cp .env.example .env   # 값 채워넣기
```

`.env`에는 DB/Redis 접속 정보, JWT 시크릿 외에도 GitHub/Google OAuth 클라이언트, PortOne 결제 키가 필요합니다(자세한 항목은 `.env.example` 참고). AI 요약/태그 추천 기능을 쓰려면 `GEMINI_API_KEY`를 추가로 설정하세요(미설정 시 해당 기능만 비활성화됩니다).

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
