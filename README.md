# dev_com

> 질문하고, 답변하고, 도움이 됐다면 채택까지 - 개발 학습자를 위한 Q&A 게시판입니다.
> 멤버십(구독) 게시판, AI 요약/태그 추천, 코드리뷰, 커리어상담 1:1 채팅까지 지원합니다.

**Live**: https://dev-com.duckdns.org

**Demo**: [시연 영상](https://drive.google.com/file/d/1aDeFzo_2IxoHj9b9WAH6rkJaIcSQTPhL/view?usp=drive_link)

멋쟁이사자처럼 백엔드 24기 기초/응용 4조 팀 프로젝트입니다.

## 프로젝트 개요

| 항목 | 내용                                                                                             |
| ---- |------------------------------------------------------------------------------------------------|
| 개발 기간 | 2026.07.29 ~ 2026.08.26                                                                        |
| 팀 구성 | 백엔드 3명 (프론트엔드 겸임)                                                                              |
| 주요 사용자 | 개발을 학습하고 관심있는 모든 학생, 직장인                                                                       |
| 해결하려는 문제 | 질문·답변이 여러 채널에 파편화되어 있고, 채택 없이 추천수만 쌓이는 인기 위주 피드가 신뢰를 주지 못함.                                    |
| 핵심 가치 | 질문, 답변과 채택 하나의 플로우로 신뢰를 쌓고, 구독 하나로 전문가와 연결되는 Q&A 게시판으로서 사이트 내에서 한번에 코드 리뷰 및 채팅으로 상담을 받을 수 있게 함 |

##  핵심 사용자 흐름
`회원가입/로그인 → 질문 작성(태그 선택, 유형 지정) → 답변 수신 → 답변 채택 → 질문 상태 '해결됨'`

## 핵심 기능

### 회원가입 / 로그인
- 사용자: 아이디·비밀번호로 가입하거나 GitHub·Google 소셜 로그인으로 이용합니다.
- 구현: JWT Access/Refresh Token 발급, Refresh Token은 Redis에 저장해 로그아웃·탈퇴 시 즉시 무효화. 로그인 5회 연속 실패 시 15분 잠금(Redis).
- 검증: 로그아웃 후 이전 Refresh Token으로 재발급 시도 시 401 확인.

### 질문·답변과 채택
- 사용자: 질문을 올리고 답변을 받아, 마음에 드는 답변을 채택하면 질문 상태가 `해결됨`으로 바뀝니다.
- 구현: 질문 상태(OPEN/RESOLVED) 관리, 채택/채택취소 API, 태그 다대다 연결(신규 태그 자동 생성 및 정규화로 중복 방지).
- 검증: 질문자 본인이 아닌 사용자의 채택 시도 403 확인, 채택 취소 후 재채택 흐름 테스트.

### 멤버십 구독 결제
- 사용자: 구독하면 프리미엄(익명 작성 가능) 게시판과 코드리뷰 유형 글쓰기를 이용할 수 있습니다.
- 구현: PortOne 카드 정기결제(빌링키) 연동, 웹훅 수신 시 PortOne API로 결제 건을 재조회해 구독 상태 동기화, 7일 이내 취소/환불.
- 검증: 웹훅이 중복 수신돼도 재조회 결과가 같아 상태가 멱등하게 유지되는지 확인, 취소 기한 경과 후 요청 거부 확인.

### AI 본문 요약 / 태그 추천
- 사용자: 본문이 길때 AI 요약을, 코드/기술 글 작성 시 AI 태그 추천을 받을 수 있습니다.
- 구현: Gemini API 연동, 요약은 본문 300자 이상일 때 요약 버튼 노출, 태그 추천은 20자 이상일 때 태그 추천 버튼 노출.
- 검증: 기준 길이 미만일 때 버튼이 노출되지 않는지 프론트·백엔드 양쪽에서 확인.

### 커리어상담 실시간 채팅
- 사용자: 커리어상담 유형 질문에는 일반 답변 대신 1:1 채팅으로 상담을 받습니다.
- 구현: WebSocket(STOMP)로 실시간 메시지 송수신, 채팅을 채택하면 질문이 자동으로 `해결됨`으로 전환.
- 검증: 다중 클라이언트로 실시간 수신 확인, 미채택 상태에서 질문 상태가 유지되는지 확인.

### 신고 / 관리자
- 사용자: 부적절한 글·답변을 신고하고, 관리자는 신고를 검토해 회원을 정지시킬 수 있습니다.
- 구현: 신고 접수 → 관리자 처리 상태 관리, 회원별 누적 신고 횟수 집계, 정지/정지해제 API.
- 검증: 일반 사용자의 관리자 API 접근 403 확인, 정지된 계정 로그인 차단 확인.

## 기술 스택

**Backend**

| 기술                         | 선택 이유                                                    |
|----------------------------|----------------------------------------------------------|
| Java 21, Spring Boot 4.1   | REST API와 도메인 중심 계층 분리를 구현하기 위해 사용                       |
| Spring Security, JWT(jjwt) | 세션 없이 무상태 인증, Refresh Token만 Redis에서 관리해 강제 로그아웃/탈퇴 시 즉시 무효화 가능 |
| Spring OAuth2 Client       | GitHub/Google 소셜 로그인을 표준 플로우로 위임                         |
| Spring Data JPA, QueryDSL  | 태그+상태+검색어가 조합되는 동적 검색 조건을 타입 안전하게 작성                     |
| Spring WebSocket(STOMP)    | 실시간 채팅/알림에 요청-응답이 아닌 서버 푸시가 필요                           |
| MySQL 8                    | 질문-답변-채택 등 FK 관계가 많은 정형 데이터 저장                           |
| Redis 7                    | Refresh Token, 로그인 실패 카운트, 비밀번호 재설정 토큰 등 TTL이 필요한 휘발성 데이터 저장 |
| PortOne V2 서버 SDK          | 국내 카드 정기결제·웹훅 처리 구현 가능                                   |
| Springdoc OpenAPI(Swagger) | API 계약을 코드와 함께 유지                                        |

**Frontend**

| 기술 | 선택 이유 |
| ---- | ---- |
| React 19, Vite | 빠른 개발 서버와 HMR |
| React Router | SPA 라우팅 |
| Axios | 인터셉터로 Access Token 자동 첨부 및 만료 시 재발급 처리 |
| Vitest, @testing-library/react | 컴포넌트 단위 테스트 |

**Infra / CI**

| 기술 | 선택 이유 |
| ---- | ---- |
| Docker, Docker Compose | 로컬과 배포 환경을 동일하게 구성 |
| GitHub Actions | PR/main push마다 자동 테스트, main 배포 시 이미지 빌드 후 GHCR 푸시까지 CI에서 처리 |
| AWS EC2, Nginx | 프론트 정적 서빙, HTTPS 종료, `/api` `/ws` `/oauth2` API 프록시 |
| Let's Encrypt(certbot) | 인증서 자동 발급/갱신 구성 |

## 아키텍처

```
[Browser]
   | HTTPS
   ▼
----------------------------------
| Nginx (frontend Container)     |  ← Let's Encrypt(certbot) 인증서
|  - 정적 파일 서빙 (React build)  |
|  - /api, /ws, /oauth2 → 프록시  |
----------------------------------
                  |
                  ▼
----------------------------------
| Spring Boot App (:8080)        |
|  - REST API / WebSocket(STOMP) |
|  - Spring Security(JWT)        |
----------------------------------
          ▼              ▼
     ┌─────────┐   ┌───────────┐
     │ MySQL 8 │   │  Redis 7  │
     └─────────┘   └───────────┘
```

**배포 파이프라인**

```
GitHub push(main) → GitHub Actions CI(테스트, MySQL/Redis 서비스 컨테이너)
                  → 이미지 빌드 → GHCR push
                  → EC2: docker compose pull + up (CD)
```

## 문서
| 문서        | 위치                                                                                                                                                                                               | 용도                 |
|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------| 
| 요구사항 정의서  | [docs](./docs/요구사항정의서.md)                                                                                                                                                                        | 기능 범위와 결정 이력       |
| 기능명세서     | [docs](./docs/기능명세서.md)                                                                                                                                                                          | 기능별 요구사항과 동작 정의    |
| API       | [Swagger (local)](http://localhost:8080/swagger-ui/index.html) </br> [docs](./docs/API명세서.md)                                                                                                    | API 계약             |
| ERD       | [docs](./docs/ERD.md)                                                                                                                                                                            | 데이터 모델과 관계         |
| 시퀀스       | [docs](./docs/시퀀스.md)                                                                                                                                                                            | 주요 흐름의 컴포넌트 간 상호작용 |
| 화면 설계     | [Figma](https://www.figma.com/design/Hzd31NGLH85EqskfcWH43W/%EA%B0%9C%EB%B0%9C%EC%9E%90%EC%BB%A4%EB%AE%A4%EB%8B%88%ED%8B%B0-%ED%99%94%EB%A9%B4%EC%84%A4%EA%B3%84?node-id=0-1&t=ZNK7Br1YKZ7iWAT7-1) | 화면·컴포넌트 설계         |
| 아키텍쳐      | [docs](./docs/4조아키텍쳐.png)                                                                                                                                                                        | 시스템 아키텍쳐           |
| User Flow | [docs](./docs/UserFlow.md)                                                                                                                                                                       | 화면 간 이동 경로와 분기 조건  |
| 권한 매트릭스   | [docs](./docs/권한매트릭스.md)                                                                                                                                                                         | 역할별 기능 접근 권한       |
| 배포·운영      | [docs](./docs/배포_환경설정.md)                                                                                                                                                                        | 명세 대비 구현 완료 현황     |
| 기능완료 테스트 시트 | [Notion](https://app.notion.com/p/3b873873401a8002ab3ac14ab4aaf518?source=copy_link)                                                                                                             | 기능별 테스트 케이스 및 결과   |

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
├── docker-compose.prod.yml   # 배포용(MySQL + Redis + backend + frontend + certbot)
├── Dockerfile                 # 백엔드 이미지 빌드
└── .github/workflows/         # CI(테스트), 배포(CD)
```

## 실행 방법

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

| 변수                                                                                                                      | 설명                                   |
|-------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| `DB_HOST`, `DB_ROOT_PASSWORD`, `DB_USER`, `DB_PASSWORD`                                                                 | MySQL 접속 정보                          |
| `REDIS_HOST`                                                                                                            | Redis 접속 정보                          |
| `JWT_SECRET`, `JWT_REFRESH`                                                                                             | JWT 서명 키                             |
| `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`                                                                              | GitHub OAuth2 소셜 로그인                 |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`                                                                              | Google OAuth2 소셜 로그인                 |
| `FRONTEND_URL`                                                                                                          | 소셜 로그인 완료 후 리다이렉트할 프론트엔드 주소          |
| `PORTONE_API_SECRET`, `PORTONE_STORE_ID`, `PORTONE_CHANNEL_KEY`, `PORTONE_WEBHOOK_SECRET`, `PORTONE_WEBHOOK_NOTICE_URL` | PortOne 정기결제/웹훅 연동                   |
| `MAIL_USERNAME`, `MAIL_PASSWORD`                                                                                        | 비밀번호 재설정 메일 발송(Gmail이면 앱 비밀번호 발급 필요) |
| `GEMINI_API_KEY`                                                                                                        | AI 요약/태그 추천 (미설정 시 해당 기능만 비활성화)      |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `ADMIN_NICKNAME `                                                                   | 자동으로 생성될 관리자 계정 정보                 |

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

## 테스트

**Backend**

```bash
cd backend
./gradlew test
```

- `spring-boot-starter-{data-jpa,security,validation,webmvc}-test`로 레이어별 테스트를 작성했습니다.
- GitHub Actions CI가 PR과 main push마다 MySQL·Redis 서비스 컨테이너를 띄우고 `SPRING_PROFILES_ACTIVE=ci`로 전체 테스트를 자동 실행합니다.

**Frontend**

```bash
cd frontend
npm run test
```

- Vitest + @testing-library/react로 컴포넌트 단위 테스트를 작성했습니다.

## 주요 기술 의사결정

### Refresh Token을 Redis에 저장한 이유
- **상황**: JWT는 무상태라 서버가 발급 후 개입하지 않는 것이 원칙이지만, 로그아웃·탈퇴 시점에 토큰을 즉시 무효화할 방법이 없었습니다.
- **선택**: TTL로 자동 만료되고, 로그아웃/탈퇴 시 해당 키만 지우면 즉시 무효화됩니다.
- **한계**: Access Token 자체는 만료 전까지 강제로 무효화할 수 없다는 한계는 남아 있어, Access Token의 유효시간을 짧게 유지하는 것으로 절충했습니다.

### 결제 웹훅을 그대로 신뢰하지 않고 재조회하는 이유
- **상황**: 정기결제(매달 자동 결제)는 프론트엔드가 결제 시점에 붙어있지 않아, 웹훅이 상태 변경의 유일한 트리거입니다.
- **선택**: 웹훅 위변조나 중복 수신 위험을 줄이기 위해, 웹훅 수신 후 PortOne 서버에 실제 결제 상태를 다시 물어봐서 반영합니다.
- **한계**: 포트원이 웹훅 실패 시 최대 5회(exponential backoff, 0→1→4→16→64→256분, 총 약 5.7시간)까지 자동 재전송해주므로 일시적 장애는 상관없는데 다만 그 이상(6시간 넘게) 서버가 다운되는 경우까지 커버하는 배치성 재조회 폴백은 아직 존재하지 않습니다.

### 조회수 중복 집계를 DB가 아니라 Redis로 처리한 이유
- **상황**: 동일 사용자가 같은 질문을 짧은 시간에 여러 번 열어도 조회수가 매번 오르면 안 되고, 로그인 사용자는 userId, 비로그인 사용자는 IP(`X-Forwarded-For`) 기준으로 24시간 내 중복 조회를 1회로 쳐야 했습니다(F-08).
- **선택**: `view:{questionId}:{user 또는 ip}` 키로 Redis `SETNX`(`setIfAbsent`) + TTL 24시간을 사용했습니다. 키가 없을 때만 값을 쓰고 결과를 반환하는 원자적 연산이라 동시 요청에도 중복 증가가 없고, TTL이 지나면 자동으로 다시 카운트 가능해져 별도 배치 정리가 필요 없습니다.
- **한계**: Redis 조회가 실패하면 증가 안 함으로 처리해 조회수가 잠깐 덜 오를 수 있습니다. 또한 `X-Forwarded-For` 헤더를 그대로 신뢰하므로, Nginx가 이 헤더를 항상 올바르게 넘겨주는 것을 전제로 합니다.

## Troubleshooting

### Redis에 저장한 RefreshToken이 조회되지 않던 문제
- **재현**: 로그인은 정상이고 `redis-cli`로도 데이터가 확인되는데, 재발급(reissue) API 호출 시 매번 401 발생.
- **원인**: `@RedisHash` 엔티티에서 `@Id` 필드는 Redis 키(`refreshToken:7`)를 만드는 데만 쓰이고 해시의 필드-값 쌍에는 포함되지 않습니다. `findByUserId` 파생 쿼리는 "`userId` 필드 값"을 비교하려 하지만 해시 안에 그 필드가 없어 항상 조용히 빈 결과를 반환했습니다. 같은 구조의 `deleteByUserId`도 매번 0건 삭제로 끝나, 로그아웃해도 이전 토큰이 Redis에 남아있었습니다.
- **해결**: 파생 쿼리 대신 `CrudRepository`가 기본 제공하는 `findById`/`deleteById`로 교체했습니다. `@Id`가 이미 키에 녹아 있으므로 키로 직접 조회하는 이 방식은 문제없이 동작합니다.
- **검증**: 로그아웃 후 이전 Refresh Token으로 재발급 시도 시 401 확인.
- 전체 기록: [블로그](https://velog.io/@kbsjh8870/Redis%EC%97%90-%EC%A0%80%EC%9E%A5%ED%95%9C-RefreshToken-%EB%B6%84%EB%AA%85-%EC%9E%88%EB%8A%94%EB%8D%B0-%EC%99%9C-%EC%A1%B0%ED%9A%8C%EA%B0%80-%EC%95%88-%EB%90%A0%EA%B9%8C)

### 태그를 그대로 둔 채 질문을 수정해도 유니크 제약 위반이 나던 문제
- **재현**: 기존과 동일한 태그로 질문을 수정해도 `DataIntegrityViolationException`(unique 제약 위반) 발생.
- **원인**: `orphanRemoval=true`가 걸린 `@OneToMany` 컬렉션을 `clear()`로 비우고 같은 값으로 즉시 재생성했는데, Hibernate flush 시 `EntityInsertAction`(삽입)이 `EntityDeleteAction`(삭제)보다 먼저 실행되는 순서 때문에, 옛 로우가 지워지기 전에 같은 `(question_id, tag_id)`의 새 로우가 먼저 들어가 유니크 제약과 충돌했습니다.
- **해결**: 전체 삭제 후 재삽입 대신, 기존 태그와 요청 태그를 비교해 빠진 것만 삭제하고 새로 추가된 것만 삽입하는 방식으로 변경했습니다.
- **검증**: 동일 태그로 반복 수정하는 시나리오 테스트로 재현되지 않음을 확인.
- 전체 기록: [블로그](https://velog.io/@kbsjh8870/%EB%A9%8B%EC%9F%81%EC%9D%B4%EC%82%AC%EC%9E%90%EC%B2%98%EB%9F%BC-%EB%B0%B1%EC%97%94%EB%93%9C-%EB%B6%80%ED%8A%B8%EC%BA%A0%ED%94%84-24%EA%B8%B0-Hibernate-orphanRemoval%EA%B3%BC-%EC%9C%A0%EB%8B%88%ED%81%AC-%EC%A0%9C%EC%95%BD)

### 결제 검증 실패 시 실패 이력이 DB에 남지 않던 문제
- **재현**: PortOne 결제 완료 검증(F-30)에서 재검증을 실패시켜 테스트하면 응답은 실패로 오는데, `PaymentTransaction` 실패 이력이 DB에 하나도 남지 않음.
- **원인**: 검증 전체를 감싸는 `completePayment()`가 `@Transactional`이었고, 검증 실패 분기에서 `markFailed()` + 실패 이력 `save()`를 먼저 실행한 뒤 `CustomException`을 던지는 구조였습니다. `@Transactional`은 `RuntimeException`(과 그 하위 클래스인 `CustomException`)이 메서드 밖으로 던져지면 기본적으로 트랜잭션 전체를 롤백하는데, 코드 순서상 `save()`가 예외보다 먼저 실행되더라도 실제 커밋/롤백은 메서드 종료 시점에 한 번에 결정되기 때문에 순서와 무관하게 트랜잭션 안의 쓰기가 전부 같이 롤백됐습니다.
- **해결**: `@Transactional(noRollbackFor = CustomException.class)`로 해당 예외를 롤백 대상에서 제외했습니다. `markFailed()`와 실패 이력 저장은 검증 실패라는 같은 사건의 결과라 트랜잭션을 쪼갤 이유가 없다고 판단해, 실패 기록만 별도 트랜잭션에 커밋하는 `REQUIRES_NEW`(별도 `@Service` 빈 필요 - 같은 클래스 self-invocation은 AOP 프록시를 안 거쳐 새 트랜잭션이 안 열림) 대신 이 방식을 택했습니다.
- **검증**: 검증 실패 케이스를 재현해 `PaymentTransaction` 실패 이력이 정상적으로 커밋되는지 확인.
- 전체 기록: [블로그](https://velog.io/@kbsjh8870/%EB%A9%8B%EC%9F%81%EC%9D%B4%EC%82%AC%EC%9E%90%EC%B2%98%EB%9F%BC-%EB%B0%B1%EC%97%94%EB%93%9C-%EB%B6%80%ED%8A%B8%EC%BA%A0%ED%94%84-24%EA%B8%B0-JPA-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98-%EB%A1%A4%EB%B0%B1)

### 한글 태그 입력 시 마지막 글자가 별도 태그로 중복 생성되던 문제
- **재현**: 질문 작성 폼에서 한글 태그를 입력하고 Enter를 누르면, 의도한 태그 외에 마지막 글자 한 글자만 담긴 태그가 하나 더 생성됨(예: "태그" 입력 후 `["태그", "그"]`). macOS 사용자는 매번 재현됐지만 Windows+Chrome 조합에서는 전혀 재현되지 않음.
- **원인**: 한글 조합 완료 신호(`compositionend`)가 도착하는 시점이 `keydown`(Enter)보다 먼저인지 나중인지가 OS/브라우저에 따라 결정론적으로 달랐습니다. Windows+Chrome은 조합 완료가 Enter보다 먼저 도착해 문제가 드러나지 않았지만, macOS는 Enter가 먼저 처리되어 입력창을 비운 직후 뒤늦게 도착한 조합 완료 신호가 잔여 글자를 다시 채우고, 곧이어 인식되는 두 번째 Enter가 그 잔여 글자를 또 하나의 태그로 추가했습니다. `isComposing` 방어 로직이 없었던 것이 근본 원인.
- **해결**: `onCompositionStart`/`onCompositionEnd`로 조합 상태를 추적해 조합 중 Enter는 무시하고(`isComposing` 체크), 특정 브라우저 조합까지는 특정하지 못해 방어를 하나 더 두어 직전 태그 추가로부터 200ms 이내의 Enter는 잔여 글자로 간주해 무시하도록 했습니다(정상 사용 흐름에는 영향 없는 여유).
- **검증**: macOS 환경 없이 devtools 콘솔에서 `CompositionEvent`/`KeyboardEvent`를 실제와 같은 순서로 직접 디스패치해 재현 및 수정 확인. 조합 신호가 정상 감지되는 경우, 아예 감지되지 않는 경우, 정상적인 다중 태그 입력 3가지 시나리오 모두 의도대로 동작함을 확인 후 배포, 이후 재발하지 않음을 확인.
- 전체 기록: [Notion](https://app.notion.com/p/39373873401a82109d3d817cd5dff9e2?source=copy_link)

### 배포 환경에서만 소셜 로그인 redirect_uri가 불일치하던 문제
- **재현**: 로컬에서는 정상 동작하던 GitHub/Google OAuth2 로그인이 Nginx 리버스 프록시 뒤의 배포 서버에서만 실패. Google은 `400: redirect_uri_mismatch`, GitHub는 `The redirect_uri is not associated with this application` 발생. 콘솔에는 배포 도메인의 `https://` 콜백 URL을 정확히 등록해뒀는데도 계속 실패.
- **원인**: Nginx가 HTTPS를 종료하고 내부적으로는 HTTP로 백엔드에 요청을 전달하는 구조인데, Spring Security OAuth2 Client가 `redirect_uri`를 자동 생성할 때 백엔드가 실제로 받은 요청 프로토콜(HTTP)을 그대로 기준 삼아 `http://...`로 생성했습니다. 백엔드가 리버스 프록시 뒤에 있다는 걸 몰라 `X-Forwarded-Proto` 헤더를 신뢰하지 않고 있었던 것이 원인 — 콘솔엔 `https://...`를 등록했는데 실제 생성/전송되는 값은 `http://...`라 문자열이 달라 매칭에 실패했습니다.
- **해결**: `application.yml`(`docker-compose.prod.yml`의 `SERVER_FORWARD_HEADERS_STRATEGY=framework`)에 `server.forward-headers-strategy: framework`를 설정해 `X-Forwarded-*` 헤더를 신뢰하도록 하고, Nginx의 각 `location` 블록에 `proxy_set_header X-Forwarded-Proto $scheme;`이 실제로 들어가 있는지 재확인했습니다.
- **검증**: 설정 반영 후 백엔드가 생성하는 `redirect_uri`가 `https://`로 바뀌어 Google/GitHub 콘솔에 등록된 값과 정확히 일치하는지 확인, 배포 환경에서 소셜 로그인 재시도로 확인.
- 전체 기록: [Notion](https://app.notion.com/p/Nginx-OAuth2-redirect_uri-3c773873401a80748129cab5c17e115a?source=copy_link)

## 팀과 기여

| 이름 | GitHub | 역할 |
| ---- | ------ | ---- |
| 김재혁 | [@kbsjh8870](https://github.com/kbsjh8870) | 팀장 |
| 조민규 | [@Noisywhitecat-dev](https://github.com/Noisywhitecat-dev) | 팀원 |
| 변재웅 | [@woong1116](https://github.com/woong1116) | 팀원 |

### 김재혁
- 회원 인증 전체(회원가입/로그인/토큰 재발급/로그아웃, Refresh Token Redis 저장·무효화)와 권한 분리(USER/ADMIN), 회원 탈퇴(soft delete)
- 신고 접수/처리, 회원 목록·정지 등 관리자 기능
- 멤버십 구독과 PortOne 정기결제(빌링키) 연동, 웹훅 기반 결제 상태 동기화, 결제 취소
- 로그인/네브바 등 인증 관련 프론트엔드 화면

### 조민규
- 답변 작성/수정/삭제와 채택·채택취소, 조회수 중복 방지(Redis)
- AI 요약·태그 추천(Gemini), 질문/답변 파일 첨부, 실시간 알림·커리어상담 채팅(STOMP)
- 사용자 평판·전문가 등급, 관리자/개인 활동 대시보드
- EC2·Docker·Nginx·HTTPS 배포 인프라 구성, UI 테마 전반

### 변재웅
- 질문 작성/수정/삭제, 태그 연결, 키워드 검색과 태그·상태 필터링
- GitHub·Google 소셜 로그인 연동
- 질문 작성, 마이페이지 프론트엔드 화면

## 배포

**Live**: https://dev-com.duckdns.org (AWS EC2, DuckDNS + Let's Encrypt)

- GitHub Actions CI가 main push/PR마다 테스트를 자동 실행하고, main에 push되면 CD 워크플로우가 이어서 백엔드/프론트 이미지를 GHCR에 푸시합니다.
- EC2는 `docker compose -f docker-compose.prod.yml pull && up -d`로 이미지만 받아 기동합니다.
- Nginx가 정적 파일 서빙 + `/api`, `/ws`, `/oauth2`, `/swagger-ui` 프록시를 담당하고, certbot이 12시간마다 인증서 자동 갱신을 시도합니다.
- 배포 환경의 각종 시크릿 값, 외부 API 키 값, DB 정보 등은 .env와 Github Secrets로 관리합니다.

## 개선 계획

- 그라파나 같은 성능 분석 플랫폼 도입
