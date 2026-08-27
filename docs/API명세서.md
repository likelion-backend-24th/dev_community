# API 명세서

## 공통 응답 포맷

### 성공 (data/meta 존재 시)

```json
{ "success": true, "data": { }, "message": "요청 성공", "meta": { } }
```

### 성공 (data없이 메시지만, 예) 중복확인, 로그아웃)

```json
{ "success": true, "message": "요청 성공" }
```

실패

```json
{ "success": false, "message": "메시지", "code": "ERROR_CODE" }
```

> 
`code` 값 목록은 구현 시 `@RestControllerAdvice` 에서 정의,

아래 모든 Response 예시는 `ApiResponse.data` 안에 들어가는 내용만 표기합니다
>

# MVP 1

---

# **인증/회원**

## 회원 가입

( POST `/api/auth/signup` )

Request

```json
{ "username": "testUser", "password": "testPW", "nickname": "테스트유저" }
```

- `username` 50자 이하, `password` 8~64자, `nickname` 30자 이하

Response

```json
{
  "success": true,
  "data": { "username": "testUser", "nickname": "테스트유저" },
  "message": "회원가입 성공"
}
```

**400** 형식 오류(`INVALID_INPUT`) / **409** 아이디·닉네임 중복(`DUPLICATE_RESOURCE`)

## 아이디/닉네임 중복 확인

( GET `/api/auth/check-username?username=testUser`
GET `/api/auth/check-nickname?nickname=테스트유저` )

```json
200 { "success": true, "message": "사용 가능한 아이디입니다" }
409 이미 존재 - `code: "DUPLICATE_RESOURCE"`
```

## 로그인

( POST `/api/auth/login` )

Request

```json
{ "username": "testUser", "password": "testPW" }
```

Response

```json
{
  "success": true,
  "data": { "accessToken": "eyJ...", "tokenType": "Bearer" },
  "message": "로그인 성공"
}
```

- Refresh Token은 응답 body에 없고 **`Set-Cookie: refreshToken=...; HttpOnly`** 로
  - **401**: 아이디/비밀번호 불일치(INVALID_CREDENTIALS) / 탈퇴 계정(WITHDRAWN_ACCOUNT) / 정지 계정(SUSPENDED_ACCOUNT)

## 토큰 재발급/로그아웃

( POST `/api/auth/reissue` , POST `/api/auth/logout` )

- 재발급 요청: 요청 body 없이 **쿠키에 담긴 refreshToken을 서버가 자동으로 읽음**. Redis에 저장된 값과 일치할 때만 발급.
- 로그아웃: 쿠키 삭제 + Redis에서 Refresh Token 삭제 → **200** (data 없음)

내 정보

정보 수정 Request

```json
{ "nickname": "새닉네임" }
```

비밀번호 변경 Request

```json
{ "currentPassword": "testPW", "newPassword": "newPw123!" }
```

400 - 현재 비밀번호 불일치

탈퇴 Request

```json
{ "currentPassword": "testPW" }
```

204 No content (body 없음, soft delete + refreshToken 쿠키·Redis 정리)

내 질/답 목록 - 응답은 `data` 는 배열, `meta` 에 `page/size/totalElemnets/totalPages` 담김

---

# **질문**

## 목록 (페이징, 검색, 필터)

( GET `/api/questions?page=0&size=10&sort=latest&keyword=&tag=&status=` )

> keyword, tag, status는 모두 선택 파라미터이며 동시 조합 가능, status는 요청 시 `RESOLVED` / `UNRESOLVED` 만 허용 ( 그 외 값 400 )

Response 200

```json
{
  "success": true,
  "message": "질문 목록 조회",
  "data": [
    {
      "id": 1,
      "title": "JPA N+1 질문",
      "authorNickname": "testUser",
      "status": "OPEN",
      "viewCount": 42,
      "likeCount": 3,
      "answerCount": 2,
      "tags": ["jpa", "spring-boot"],
      "createdAt": "2026-07-29T10:00:00"
    }
  ],
  "meta": { "page": 0, "size": 10, "totalElements": 1, "totalPages": 1 }
}
```

`authorNickname`은 작성자가 탈퇴한 경우 탈퇴한 사용자로 마스킹됨.

## 상세

(GET `/api/questions/{id}` )

Response 200

```json
{
  "success": true,
  "data": {
    "id": 1,
    "authorId": 7,
    "authorNickname": "testUser",
    "title": "JPA N+1 질문",
    "content": "목록 조회 시 연관 엔티티는 어떻게?",
    "status": "OPEN",
    "viewCount": 43,
    "likeCount": 3,
    "tags": ["jpa", "spring-boot"],
    "createdAt": "2026-07-29T10:00:00"
  }
}
```

답변 목록은 이 응답에 포함되지 않고 별도로 `GET /api/questions/{id}/answers`를 호출해서 받아옴.

404 : `{ code: "NOT_FOUND" }`

## 등록 (로그인 필요)

( POST `/api/questions` (header - `Authorization: Bearer <token>` ))

Request (작성자는 토큰에서 결정 - body에 넣지 않음)

```json
{ "title": "제목", "content": "본문", "tags": ["jpa", "spring-boot"] }
```

- title 100자 이하, content 필수, tags 최대 5개, 태그당 30자 이하

**201** 생성 / **400** title·content 비어 있음 / **401** 토큰 없음

## 수정/삭제

(PUT `/api/questions/{id}` , DELETE `/api/questions/{id}` )

- 수정 — **작성자 본인,admin만**
- 삭제 — 작성자 본인 **또는** ADMIN (soft delete, 하위 답변도 함께 비활성화 soft delete)

403: 남의 질문을 USER가 수정/삭제 시도, 또는 ADMIN이 타인 질문 PUT 시도.

---

# **답변**

## 등록

(POST `/api/questions/{questionId}/answers` - (header - `Authorization: Bearer <token>` ))

Response 201

```json
{
  "success": true,
  "message": "답변 등록 완료",
  "data": {
    "id": 1,
    "questionId": 1,
    "authorId": 3,
    "authorNickname": "김멋사",
    "content": "fetch join을 쓰세요",
    "isAdopted": false,
    "likeCount": 0,
    "createdAt": "2026-07-29T10:05:00"
  }
}
```

**401** 토큰 없음 / **403** 본인이 작성한 질문에 본인이 답변 등록 시도(SELF_ANSWER_NOT_ALLOWED) / **404** 질문 없음(삭제됨 포함)

## 수정/삭제

(PUT `/api/answers/{id}` , DELETE `/api/answers/{id}` )

- 수정 — 작성자 본인, admin만
- 삭제 — 작성자 본인, admin만 (soft delete, 채택된 답변 삭제 불가 → `ADOPTED_ANSWER_DELETE_FORBIDDEN`, 먼저 채택 취소 필요)

403: 남의 답변 수정/삭제 시도

## 채택 / 채택 취소

(POST `/api/answers/{id}/adopt` ,
DELETE `/api/answers/{id}/adopt`)

- 응답은 두 동작 모두 위 답변 등록과 같은 형태의 답변 객체(`data`)를 반환 (`isAdopted`가 `true`/`false`로 바뀜). 질문의 `status`도 채택 시 `RESOLVED`, 취소 시 다시 `OPEN`으로 바뀜
- 403: 질문 작성자가 아님 / 409: 이미 해결된 질문 재채택(QUESTION_ALREADY_RESOLVED), 채택 안 된 답변 취소 시도(ANSWER_NOT_ADOPTED)

---

# **추천**

## 질문 추천 토글

(POST `/api/questions/{id}/likes` )

- **200** `{ "liked": true, "likeCount": 4 }` (누른 직후 상태와 개수를 함께 반환, 재요청 시 취소)
- **401** 비로그인 / **404** 대상 없음

## 답변 추천 토글

(POST `/api/answers/{id}/likes` )

- **200** `{ "liked": true, "likeCount": 4 }` (누른 직후 상태와 개수를 함께 반환, 재요청 시 취소)
- **401** 비로그인 / **404** 대상 없음

---

# 신고

## (POST `/api/reports` )

**Request**

```json
{ "targetType": "QUESTION", "targetId": 1, "reason": "광고성 도배글입니다" }
```

**Response 201**

```json
{
  "id": 1,
  "reporterId": 7,
  "reporterNickname": "테스트유저",
  "targetType": "QUESTION",
  "targetId": 1,
  "targetUserId": 3,
  "targetUserNickname": "김멋사",
  "reason": "광고성 도배글입니다",
  "status": "PENDING",
  "createdAt": "2026-08-06T10:00:00"
}
```

**401** 토큰 없음 / **403** 본인 게시물 신고 시도 / **404** 대상(질문/답변) 없음

---

# 관리자

| 동작                          | Method | URI                                                |
| ----------------------------- | ------ | -------------------------------------------------- |
| 신고 목록 조회                | GET    | `/api/admin/reports?status=PENDING&page=0&size=10` |
| 신고 처리                     | PATCH  | `/api/admin/reports/{id}`                          |
| 회원 목록 조회                | GET    | `/api/admin/users?page=0&size=10`                  |
| 특정 회원의 누적 신고 수 조회 | GET    | `/api/admin/users/{id}/reports`                    |
| 회원 정지                     | PATCH  | `/api/admin/users/{id}/suspend`                    |
| 회원 정지 해제                | PATCH  | `/api/admin/users/{id}/unsuspend`                  |

## 신고 목록 조회

(GET `/api/admin/reports?status=PENDING&page=0&size=10` )

Response 200

```json
{
  "content": [
    {
      "id": 1,
      "reporterId": 7,
      "reporterNickname": "테스트유저",
      "targetType": "QUESTION",
      "targetId": 1,
      "targetUserId": 3,
      "targetUserNickname": "김멋사",
      "reason": "광고성 도배글입니다",
      "status": "PENDING",
      "createdAt": "2026-08-06T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

- 401 토큰 없음 / 403 비관리자 접근

## 신고 처리

(PATCH `/api/admin/reports/{id}` )

Request

```json
{ "status": "RESOLVED" }
```

Response

```json
{
  "id": 1,
  "reporterId": 7,
  "reporterNickname": "테스트유저",
  "targetType": "QUESTION",
  "targetId": 1,
  "targetUserId": 3,
  "targetUserNickname": "김멋사",
  "reason": "광고성 도배글입니다",
  "status": "RESOLVED",
  "createdAt": "2026-08-06T10:00:00"
}
```

`status`는 `RESOLVED`(정당함) 또는 `REJECTED`(부당함)만 허용. `RESOLVED` 처리만으로 회원이 자동 정지되지는 않음.
관리자가 신고 누적 현황을 보고 `회원 정지` API를 별도로 호출해야 실제 제재가 적용.

- 401 토큰 없음 / 403 비관리자 접근 / 404 존재하지 않는 신고 id /
  400 status가 RESOLVED·REJECTED 외 값 / 409 이미 처리된 신고

## 회원 목록 조회

(GET `/api/admin/users?page=0&size=10` )

Response 200

```json
{
  "content": [
    {
      "id": 3,
      "username": "kimMeosa",
      "nickname": "김멋사",
      "role": "USER",
      "status": "ACTIVE",
      "createdAt": "2026-07-01T09:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

- 401 토큰 없음 / 403 비관리자 접근

## 누적 신고 수 조회

(GET `/api/admin/users/{id}/reports` )

Response 200

```json
{ "success": true, "data": 3 }
```

## 회원 정지 / 정지 해제

(PATCH `/api/admin/users/{id}/suspend` ,
PATCH `/api/admin/users/{id}/unsuspend`)

- 정지, 해제된 회원의 `UserInfoResponse`(`userId/username/nickname/role/status/createdAt`)를 `data`로 반환. 정지 시 해당 회원의 Refresh Token도 Redis에서 삭제되어 즉시 로그아웃되지만, 해제는 세션을 강제 종료한 적이 없으므로 별도 처리 없음.
- **403** 비관리자 접근 / **404** 대상 없음 / **409** 이미 정지된 회원 재정지(ALREADY_SUSPENDED_USER), 정지 상태가 아닌 회원 정지 해제(NOT_SUSPENDED_USER)

---

# MVP 2

# 결제/구독

---

## **PortOne V2 결제 연동**

### 1) 빌링키 발급 준비

| 항목   | 내용                            |
| ------ | ------------------------------- |
| Method | POST                            |
| URI    | `/api/payments/billing/prepare` |
| 헤더   | `Authorization: Bearer <token>` |

Response 200

```json
{
  "success": true,
  "message": "빌링키 발급 준비 완료",
  "data": {
    "storeId": "store-xxxx",
    "channelKey": "channel-key-xxxx",
    "issueId": "issue-xxxx"
  }
}
```

> 프론트는 이 값으로
> `PortOne.requestIssueBillingKey({ storeId, channelKey, issueId, ... })`
> 를 호출해 결제창을 띄움.

401 토큰 없음 / 400 planType 값 오류

### 2) 빌링키 발급 + 첫 결제

| 항목   | 내용                            |
| ------ | ------------------------------- |
| Method | POST                            |
| URI    | `/api/payments/billing/issue`   |
| 헤더   | `Authorization: Bearer <token>` |

Request

```sql
{ "billingKey": "billing-key-xxxx", "planType": "PREMIUM" }
```

Response 200

```json
{
  "success": true,
  "message": "정기결제 등록 완료",
  "data": {
    "paymentId": "...",
    "planType": "PREMIUM",
    "status": "PAID",
    "paidAt": "2026-08-12T10:00:00"
  }
}
```

**401** 토큰 없음 / **400** `planType` 값 오류 / PortOne 결제 승인 실패 시 `Payment`가 `FAILED`로 기록

### 3) 최근 결제 조회

| 항목   | 내용                            |
| ------ | ------------------------------- |
| Method | GET                             |
| URI    | `/api/payments/me/latest`       |
| 헤더   | `Authorization: Bearer <token>` |

Response 200 2)와 동일한 `PaymentCompleteResponse` 형태로 본인의 가장 최근 `PAID` 결제 1건 반환.

### 4) 웹훅 수신

| 항목   | 내용                                                                                                                                                                                           |
| ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Method | POST                                                                                                                                                                                           |
| URI    | `/api/payments/webhook`                                                                                                                                                                        |
| 헤더   | PortOne 서명 헤더(`webhook-id`, `webhook-signature`, `webhook-timestamp`) - 테스트 환경에서 헤더가 없으면 서명 검증을 건너뛰고 `processUnverified`로 처리, 있으면 SDK로 검증 후 `process` 호출 |

서명 검증(또는 미서명) 통과 시 결제 상태를 PortOne 재조회 기반으로 동기화. 완료 API 호출 없이 사용자가 결제창에서 이탈한 경우를 복구하는 방어선 역할.서명 검증 실패 시 `400`, 그 외에는 `200 OK`

400 웹훅 서명 검증 실패

### 5) 결제 취소

| 항목   | 내용                               |
| ------ | ---------------------------------- |
| Method | POST                               |
| URI    | `/api/payments/{paymentId}/cancel` |
| 헤더   | `Authorization: Bearer <token>`    |

Request

```json
{ "reason": "사용자 요청" }
```

Response 200

```json
{
  "success": true,
  "message": "결제 취소 완료",
  "data": {
    "paymentId": "...",
    "status": "CANCELLED",
    "cancelledAt": "2026-08-12T11:00:00",
    "reason": "사용자 요청"
  }
}
```

서버는 PortOne 취소 API 호출 후 성공 응답을 받으면 Payment를 `CANCELLED`로, 연결된 Subscription을 즉시 `cancel()` 처리한다(환불이므로 잔여 구독 기간을 유지하지 않음). PortOne 관리자 콘솔에서 직접 취소된 경우(`Transaction.Cancelled` 웹훅)도 동일한 동기화 로직을 재사용해 로컬 상태를 맞춤.

401 토큰 없음 / 403 본인 결제가 아님 / 409 Payment가 `PAID` 상태가 아님(READY, FAILED, 이미 CANCELLED)

---

## 구독 등급 관리

| 항목   | 내용                            |
| ------ | ------------------------------- |
| Method | GET                             |
| URI    | `/api/members/me/subscription`  |
| 헤더   | `Authorization: Bearer <token>` |

Response 200 (구독중)

```json
{
  "success": true,
  "data": {
    "planType": "PREMIUM",
    "startedAt": "2026-08-11T10:00:00",
    "expiresAt": "2026-09-11T10:00:00",
    "status": "ACTIVE"
  }
}
```

Response 200 (구독x)

```json
{ "success": true, "data": null, "message": "구독 이력 없음" }
```

401 토큰 없음

## 프리미엄 게시판

| 동작      | Method | URI                      | 인증                                               |
| --------- | ------ | ------------------------ | -------------------------------------------------- |
| 목록 조회 | GET    | `/api/questions/premium` | 구독자(또는 ADMIN)만                               |
| 상세 조회 | GET    | `/api/questions/{id}`    | 구독자(또는 ADMIN)만, `isPremium=true`인 글에 한함 |
| 작성      | POST   | `/api/questions`         | 구독자(또는 ADMIN)만 `isPremium: true`로 요청 가능 |

작성 Request

```json
{ "title": "제목", "content": "본문", "tags": ["jpa"], "isPremium": true }
```

목록/상세 Response는 F-07/F-08과 동일 구조에 `isPremium` 필드가 추가된 형태. 일반 게시판 목록(`GET /api/questions`)에는 `isPremium=true`인 글이 섞이지 않음.

403 비구독자가 프리미엄 게시판 목록, 상세 접근, 또는 `isPremium: true`로 작성 시도(ADMIN은 구독 여부와 무관하게 허용)

---

# 인증/계정

## Github / Google 로그인

| 동작                                | Method | URI                            |
| ----------------------------------- | ------ | ------------------------------ |
| GitHub 인가 시작                    | GET    | `/oauth2/authorization/github` |
| Google 인가 시작                    | GET    | `/oauth2/authorization/google` |
| 가입 완료(신규 사용자, 닉네임 확정) | POST   | `/api/auth/oauth/complete`     |

Spring Security OAuth2 Client의 표준 진입점으로, 인가 코드 교환, 콜백 처리는 프레임워크가 담당.
콜백 완료 후:

- **기존 사용자** (동일 provider+providerId로 이미 가입됨): JWT 발급 후 `302 Redirect` →
  `{frontendUrl}/oauth/callback?accessToken=<accessToken>` (accessToken은 URL 인코딩된 쿼리 파라미터로 전달, JSON body 없음). Refresh Token은 `Set-Cookie: refreshToken=...; HttpOnly`로만 전달.
- **신규 사용자**: 계정을 즉시 만들지 않고 1회용 `signupToken`을 발급해 `OAuthSignupInfo`(provider, providerId, email)로 임시 저장한 뒤 `302 Redirect` →
  `{frontendUrl}/oauth/nickname?signupToken=<signupToken>&nickname=<제안닉네임>`
  (제안 닉네임은 GitHub `login` 또는 Google `name` 클레임에서 추출한 값이며, 그대로 써도 되고 프론트에서 수정 가능).
      이메일은 GitHub의 경우 프로필에 공개돼 있으면 그대로, 비공개면 `user:email` 스코프로 `/user/emails`를 추가 호출해 대표 이메일을 가져와 `OAuthSignupInfo`에 채운다(`CustomOAuth2UserService`). Google은 보통 프로필 클레임에 이메일이 포함된다.
      프론트가 닉네임을 확정해 `POST /api/auth/oauth/complete`로 `{ signupToken, nickname, email }`을 보내면(`email`은 provider가 이메일을 못 채워준 경우에만 사용자가 2단계 화면에서 직접 입력해 채우는 값, provider가 이미 이메일을 제공했다면 이 값은 무시하고 `OAuthSignupInfo`에 저장된 이메일을 그대로 사용),
      서버가 이때 비로소 `User`를 생성(`username`은 `github_{providerId}`/`google_{providerId}` 형태로 자동 생성, 비밀번호는 랜덤 값, email)하고 로그인 처리한다.
      이 마지막 단계의 응답만 기존 로그인(F-02)과 동일한 JSON 포맷.
- **정지·탈퇴 계정**: 리다이렉트 플로우라 실제 `401` 응답 바디를 줄 수 없어, `302 Redirect` →
  `{frontendUrl}/login?error=SUSPENDED_ACCOUNT` 또는 `?error=WITHDRAWN_ACCOUNT`로 프론트가 에러 메시지를 표시하게 한다.

가입 완료 Request

```json
{
  "signupToken": "1f2e3d4c-...",
  "nickname": "새싹개발자",
  "email": "dev_user01@example.com"
}
```

> `email` 은 provider가 이메일을 제공하지 못했을 때만 사용됨 (선택 입력이지만 provider, 요청 양쪽 다 없으면 400)

가입 완료 Response 200

```json
{
  "success": true,
  "message": "회원가입 및 로그인 성공",
  "data": { "accessToken": "eyJ...", "tokenType": "Bearer" }
}
```

Refresh Token은 body에 없고 `Set-Cookie: refreshToken=...; HttpOnly`로만 전달

username 생성 규칙 - `github_{providerId}` / `google_{providerId}` 형태로 자동 생성해 기존 회원(자체가입 포함)과 충돌 방지 - 닉네임은 자동 생성이 아닌 사용자가 2단계 화면에서 직접 확정(중복 시 `DUPLICATE_RESOURCE`)

이메일: `existsByEmail`로 자체 가입, 다른 소셜 계정과 통합 중복 검사하며, 중복이면 닉네임과 동일하게 `409 DUPLICATE_RESOURCE`. 별도로 `GET /api/auth/check-email`(자체가입용 중복 확인 API)과 동일한 검사 로직을 재사용.

404 (`OAUTH_SIGNUP_EXPIRED`) signupToken 만료/존재하지 않음 / 409 (`DUPLICATE_RESOURCE`) 닉네임 또는 이메일 중복 / 400 provider, 요청 모두 이메일 없음 / 소셜 인증 실패, 정지, 탈퇴 계정은 위와 같이 `401` 대신 프론트 리다이렉트 쿼리로 처리.

---

# 커뮤니티 확장

## 사용자 평판

기존 `GET /api/members/me` 응답에 `reputation` 필드 추가(별도 엔드포인트 없음). 이벤트 발생 시 서버 내부에서 자동 갱신되며, 가중치는 `ReputationEvent` enum에 정의.

| 이벤트                                 | 점수 |
| -------------------------------------- | ---- |
| 질문 추천받음 (QUESTION_LIKED)         | +2   |
| 답변 추천받음 (ANSWER_LIKED)           | +5   |
| 답변 채택됨 (ANSWER_ADOPTED)           | +15  |
| 커리어상담 채팅 수락됨 (CHAT_ACCEPTED) | +5   |
| 커리어상담 채팅 채택됨 (CHAT_ADOPTED)  | +20  |

## 외부 API 연동

연동 대상 미정 — 확정 시 별도 섹션으로 추가.

## 파일 첨부

| 동작               | Method | URI                                       |
| ------------------ | ------ | ----------------------------------------- |
| 질문에 첨부 업로드 | POST   | `/api/questions/{questionId}/attachments` |
| 답변에 첨부 업로드 | POST   | `/api/answers/{answerId}/attachments`     |
| 질문 첨부 목록     | GET    | `/api/questions/{questionId}/attachments` |
| 답변 첨부 목록     | GET    | `/api/answers/{answerId}/attachments`     |
| 첨부 다운로드/조회 | GET    | `/api/attachments/{attachmentId}`         |
| 첨부 삭제          | DELETE | `/api/attachments/{attachmentId}`         |

업로드는 `multipart/form-data`의 `files` 파트로 받으며, 업로드 권한은 해당 질문/답변의 작성자 본인만 있음.

Response 200 (업로드)

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "originalFilename": "Solution.java",
      "contentType": "text/x-java",
      "fileSize": 1024,
      "createdAt": "2026-08-12T10:00:00"
    }
  ]
}
```

> 첨부 `id`로 응답하며, 실제 다운로드는 `GET /api/attachments/{id}`를 별도 호출.

다운로드 응답은 이미지(`image/*`)면 `Content-Disposition: inline`, 그 외 코드 파일이면 `attachment`로 내려 브라우저 동작을 파일 종류별로 다르게 함.

400 (`UNSUPPORTED_FILE_TYPE`) 허용 확장자 외 / 400 (`FILE_TOO_LARGE`) 2MB 초과 / 400 (`ATTACHMENT_LIMIT_EXCEEDED`) 대상당 5개 초과 / 403 작성자 본인이 아님(삭제는 ADMIN도 가능) / 401 토큰 없음

## 개인 활동 대시보드

### **1) 요약**

| 항목   | 내용                                |
| ------ | ----------------------------------- |
| Method | GET                                 |
| URI    | `/api/members/me/dashboard/summary` |
| 헤더   | `Authorization: Bearer <token>`     |

**Response 200**

```json
{
  "success": true,
  "message": "개인 활동 요약 조회 성공",
  "data": {
    "questionCount": 12,
    "answerCount": 8,
    "adoptedAnswerCount": 3,
    "unresolvedQuestionCount": 4,
    "reputation": 57
  }
}
```

### **2) 최근 활동 타임라인**

| 항목   | 내용                                 |
| ------ | ------------------------------------ |
| Method | GET                                  |
| URI    | `/api/members/me/dashboard/timeline` |
| 헤더   | `Authorization: Bearer <token>`      |

**Response 200**

```json
{
  "success": true,
  "message": "최근 활동 타임라인 조회 성공",
  "data": [
    {
      "type": "ANSWER",
      "questionId": 10,
      "title": "답변 본문 일부...",
      "isAdopted": true,
      "createdAt": "2026-08-12T10:00:00"
    },
    {
      "type": "QUESTION",
      "questionId": 9,
      "title": "질문 제목",
      "isAdopted": false,
      "createdAt": "2026-08-11T09:00:00"
    }
  ]
}
```

401 토큰 없음

---

# 콘텐츠 작성

## 마크다운 지원 / 코드 하이라이팅

기존 질문/답변 작성·수정·조회 API(`POST/PUT /api/questions`, `POST/PATCH /api/answers/{id}` 등)를 그대로 재사용 - `title`/`content`에 마크다운 문법이 포함된 문자열을 그대로 저장·반환.

렌더링(마크다운 → HTML 변환, 코드 하이라이팅)은 프론트엔드에서 처리.

사용 라이브러리: `react-markdown` + `remark-gfm`(GFM 문법) + `rehype-highlight`(코드 하이라이팅, `prismjs` 기반).

---

# 구독자 혜택

## 익명 질문/ 답변

기존 질문 작성/수정 API(`POST /api/questions`, `PUT /api/questions/{id}`)에 `isAnonymous` 필드 추가.

Request

```json
{ "title": "제목", "content": "본문", "isAnonymous": true }
```

Response

```json
{
  "id": 1,
  "authorNickname": "익명",
  "isAnonymous": true,
  "title": "제목",
  "...": "..."
}
```

실제 `author` FK는 DB에 그대로 저장되어 신고 처리(F-27)·관리자 조회 시에는 원 작성자를 확인할 수 있음. 화면에 노출되는 `authorNickname`만 익명으로 대체.

401 토큰 없음 / 403 비구독자가 `isAnonymous: true`로 요청(ADMIN은 구독 여부와 무관하게 허용)

## 게시글 유형 세분화

기존 질문 작성/수정 API(`POST /api/questions`, `PUT /api/questions/{id}`)에 `type` 필드 추가. 일반 게시판 글은 항상 `GENERAL`로 고정되며, 프리미엄 게시판(F-32) 글만 `CODE_REVIEW`/`CAREER_CONSULT` 선택 가능.

Request

```json
{ "title": "제목", "content": "본문", "isPremium": true, "type": "CODE_REVIEW" }
```

Response (목록/상세 공통)

```json
{ "id": 1, "type": "CODE_REVIEW", "title": "제목", "...": "..." }
```

401 토큰 없음 / 403 비구독자가 `type: CODE_REVIEW` 또는 `CAREER_CONSULT`로 요청(ADMIN은 구독 여부와 무관하게 허용)

`CODE_REVIEW` 유형은 답변 목록 조회 시 전문가가 작성한 답변이 상단에 우선 정렬. (별도 엔드포인트 없음, 기존 답변 목록 조회 응답의 정렬 순서에 반영). `CAREER_CONSULT` 유형은 답변(Answer) 기능 사용하지 않음. 응답은 F-44 채팅으로만 이루어지며, `POST /api/answers`를 `CAREER_CONSULT` 질문 대상으로 호출하면 400.

## **코드 라인 코멘트** (`type: CODE_REVIEW` 질문용)

| 항목   | 내용                                |
| ------ | ----------------------------------- |
| Method | POST                                |
| URI    | `/api/questions/{id}/code-comments` |
| 헤더   | `Authorization: Bearer <token>`     |

**Request**

```json
{ "lineNumber": 12, "content": "이 부분은 null 체크가 필요해 보입니다." }
```

**Response 201**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "lineNumber": 12,
    "content": "...",
    "authorNickname": "...",
    "createdAt": "..."
  }
}
```

`GET /api/questions/{id}/code-comments`로 특정 질문의 라인 코멘트 목록 조회.

**401** 토큰 없음 / **403** 비구독자(ADMIN은 구독 여부와 무관하게 허용) / **400** 대상 질문이 `CODE_REVIEW` 유형이 아님

## AI 기반 게시글 요약 / 태그 추천

### **1) 태그 추천** (작성 중, 질문 생성 전)

| 항목   | 내용                            |
| ------ | ------------------------------- |
| Method | POST                            |
| URI    | `/api/questions/tags/suggest`   |
| 헤더   | `Authorization: Bearer <token>` |

**Request**

```json
{ "title": "제목", "content": "본문 내용..." }
```

**Response 200**

```json
{ "success": true, "data": ["spring", "jpa", "hibernate"] }
```

### **2) 질문 AI 요약**

| 항목   | 내용                            |
| ------ | ------------------------------- |
| Method | GET                             |
| URI    | `/api/questions/{id}/summary`   |
| 헤더   | `Authorization: Bearer <token>` |

**Response 200**

```json
{ "success": true, "data": "이 질문은 ... 문제를 다룬다." }
```

`Question.summary` 컬럼에 결과를 캐시해 최초 요청 시에만 Gemini를 호출, 이후 재요청은 캐시된 값을 그대로 반환(질문 수정 시 캐시 무효화).

401 토큰 없음 / 403 비구독자(ADMIN은 구독 여부와 무관하게 허용) /
외부 LLM 호출 실패 시 예외 처리(재시도/폴백 정책은 `GeminiClient` 구현 참고)

## 사용자 채팅 (커리어 상담글 한정)

상태: `PENDING`(개설 직후, 첫 메시지만 보낸 상태) → `ACTIVE`(질문자가 수락, 자유 대화) → `ADOPTED`(질문자가 채택, 질문 자동 해결) 또는 `CLOSED`(같은 질문의 다른 채팅방이 채택되며 자동 종료)

| 동작                      | Method | URI                                      | 인증                                           |
| ------------------------- | ------ | ---------------------------------------- | ---------------------------------------------- |
| 채팅방 개설(+ 첫 메시지)  | POST   | `/api/questions/{questionId}/chat-rooms` | 구독자만(또는 ADMIN) - 개설자가 곧 답변자 역할 |
| 내 채팅방 목록            | GET    | `/api/chat-rooms`                        | 로그인 필요, 질문자·답변자 겸용                |
| 안읽은 채팅방 개수        | GET    | `/api/chat-rooms/unread-count`           | 로그인 필요                                    |
| 채팅방 상세(+메시지 이력) | GET    | `/api/chat-rooms/{roomId}`               | 로그인 필요, 참여자만                          |
| 읽음 처리                 | PATCH  | `/api/chat-rooms/{roomId}/read`          | 로그인 필요, 참여자만                          |
| 메시지 전송               | POST   | `/api/chat-rooms/{roomId}/messages`      | 로그인 필요, 참여자만                          |
| 채팅 수락                 | PATCH  | `/api/chat-rooms/{roomId}/accept`        | 질문 작성자만, `PENDING`일 때만                |
| 채팅 채택                 | PATCH  | `/api/chat-rooms/{roomId}/adopt`         | 질문 작성자만, `ACTIVE`일 때만                 |

채팅방은 임의의 사용자와 자유롭게 개설할 수 없고, F-42 커리어상담(`type: CAREER_CONSULT`) 질문을 대상으로만 개설 가능. 개설하는 쪽이 구독자(답변자 역할)이고, 상대방(질문 작성자)은 서버가 경로의 `questionId`로부터 자동 조회 - 질문 작성자 본인은 자기 글에 개설 불가. 동일 질문, 동일 답변자 조합으로 중복 개설 시 기존 방을 그대로 반환. 채팅방이 한 번이라도 열리면 해당 질문의 `type`이 잠김(`Question.lockType()`, 이후 게시글 유형 변경 불가).

채팅방 개설 Request (`POST /api/questions/{questionId}/chat-rooms`)

```json
{ "content": "안녕하세요, 이 부분 상담 도와드릴게요." }
```

채팅방 개설 Response 201: 아래 채팅방 상세 Response와 동일한 `ChatRoomDetailResponse` 형태(방 정보 + 메시지 목록).

채팅방 상세 Response

```json
{
  "id": 1,
  "questionId": 10,
  "questionTitle": "이직 준비 관련 상담 부탁드립니다",
  "questionStatus": "OPEN",
  "role": "ANSWERER",
  "questionerId": 5,
  "questionerNickname": "질문자닉네임",
  "answererId": 8,
  "answererNickname": "답변자닉네임",
  "answererReputation": 42,
  "answererIsExpert": true,
  "status": "PENDING",
  "messages": [
    {
      "id": 1,
      "senderId": 8,
      "senderNickname": "답변자닉네임",
      "content": "...",
      "createdAt": "2026-08-12T10:00:00"
    }
  ]
}
```

> `role`은 조회하는 사용자 기준 `QUESTIONER`/`ANSWERER`.

메시지 전송 규칙: `PENDING` 상태에서는 답변자(개설자)만, 그것도 최초 1건만 가능 - 이미 1건 보냈다면 `CHAT_FIRST_MESSAGE_LIMIT`. `ACTIVE`가 되면 양쪽 모두 자유롭게 전송 가능. `ADOPTED`/`CLOSED` 상태에서는 전송 불가(`CHAT_NOT_ACTIVE`).

채팅 수락: 질문 작성자가 `PENDING` 채팅방을 `ACTIVE`로 전환. 답변자에게 평판 +5(`CHAT_ACCEPTED`) 지급.

채팅 채택: 질문 작성자가 `ACTIVE` 채팅방을 `ADOPTED`로 전환, 질문을 `RESOLVED`로 자동 변경, 답변자에게 평판 +20(`CHAT_ADOPTED`) 지급, 같은 질문에 열려 있던 다른 채팅방들은 모두 `CLOSED`로 자동 종료.

403(`CHAT_NOT_ALLOWED`) 비구독자가 채팅방 개설 시도 / 403(`CHAT_SELF_NOT_ALLOWED`) 본인 질문에 개설 시도 / 403(`CHAT_FORBIDDEN`) 참여자가 아닌 사용자의 접근 / 401 토큰 없음 / 400(`CHAT_NOT_ALLOWED`) 대상 질문이 `CAREER_CONSULT`가 아님 / 409(`QUESTION_ALREADY_RESOLVED`) 이미 해결된 질문에 개설·채택 시도 / 409(`CHAT_ALREADY_ACCEPTED`, `CHAT_NOT_ACCEPTED`) 상태 불일치

실시간 메시지 송수신은 WebSocket(STOMP)으로 처리하되, 토픽 구독이 아니라 사용자별 개인 큐(`SimpMessagingTemplate.convertAndSendToUser`) 방식.

| 구분                                  | 경로                                                  |
| ------------------------------------- | ----------------------------------------------------- |
| 연결                                  | STOMP 엔드포인트(SecurityConfig/WebSocketConfig 참고) |
| 새 메시지 수신                        | `/user/queue/chat-messages` (수신자 개인 큐)          |
| 채팅방 상태 변경 알림(수락/채택/종료) | `/user/queue/chat-room-updates` (참여자 개인 큐)      |

> 채팅방 개설은 구독자만 가능하지만, 일단 개설된 채팅방에 초대된 상대방(질문 작성자)은 구독 여부와 무관하게 메시지 송수신에 참여할 수 있음.

---

# 추가 (알림, 관리자통계)

## 알림

| 동작                | Method | URI                           | 인증        |
| ------------------- | ------ | ----------------------------- | ----------- |
| 최근 알림 목록 조회 | GET    | `/api/notifications`          | 로그인 필요 |
| 전체 알림 읽음 처리 | PATCH  | `/api/notifications/read-all` | 로그인 필요 |

**목록 조회 Response 200**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "NEW_ANSWER",
      "message": "회원님의 질문에 새 답변이 달렸습니다.",
      "link": "/questions/10",
      "isRead": false,
      "createdAt": "2026-08-23T10:00:00"
    }
  ]
}
```

> `type`은 `NEW_ANSWER`/`ANSWER_ADOPTED`/`NEW_CHAT_ROOM`/`CHAT_ACCEPTED`/`CHAT_ADOPTED` 중 하나. `AnswerServiceImpl`(새 답변, 채택)·`ChatService`(채팅 개설/수락/채택)에서 이벤트 발생 시 서버가 자동 생성.

401 토큰 없음

## 관리자 통계

| 동작                  | Method | URI                                |
| --------------------- | ------ | ---------------------------------- |
| 일별 가입자/질문 추이 | GET    | `/api/admin/stats/daily-trend`     |
| 질문 해결률           | GET    | `/api/admin/stats/resolution-rate` |
| 방치된 질문           | GET    | `/api/admin/stats/stale-questions` |
| 인기 질문 Top 5       | GET    | `/api/admin/stats/top-questions`   |

일별 추이 Response 200 (`daily-trend`, 최근 30일)

```json
{
  "success": true,
  "data": [{ "date": "2026-08-23", "signupCount": 12, "questionCount": 34 }]
}
```

해결률 Response 200 (`resolution-rate`)

```json
{
  "success": true,
  "data": {
    "totalQuestions": 120,
    "resolvedQuestions": 84,
    "resolutionRate": 70.0
  }
}
```

> `resolutionRate`는 `resolvedQuestions / totalQuestions * 100`을 소수점 첫째 자리까지 반올림.

방치된 질문 Response 200 (`stale-questions`, 7일 이상 미답변)

```json
{
  "success": true,
  "data": {
    "count": 5,
    "questions": [
      {
        "id": 1,
        "title": "Spring Security 인증 관련 질문입니다",
        "createdAt": "2026-08-23T10:00:00"
      }
    ]
  }
}
```

인기 질문 Response 200 (`top-questions`, 조회수 기준 Top 5)

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Spring Security 인증 관련 질문입니다",
      "viewCount": 152,
      "likeCount": 24
    }
  ]
}
```

401 토큰 없음 / 403 ADMIN 아님
