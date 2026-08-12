# 🧑‍💻 dev_community

개발자를 위한 질문/답변(Q&A) 커뮤니티 플랫폼입니다. MVP1에서는 질문·답변 기반의 기본 커뮤니티 기능을 구현했으며, 이후 **유료 구독이 가능한 개발자 SNS 솔루션**으로 확장할 계획입니다.

## 📖 소개

회원가입/로그인부터 질문·답변 CRUD, 추천, 검색/필터링, 답변 채택까지 커뮤니티 운영에 필요한 핵심 기능을 갖추고 있으며, 이후 구독/결제, 소셜 로그인, 파일 첨부 등으로 확장할 예정입니다.

## ✨ 주요 기능

- **회원/인증**: 회원가입, JWT 로그인/로그아웃/토큰 재발급(Refresh Token Redis 저장), 정보 수정, 탈퇴(soft delete), 권한 분리(USER/ADMIN)
- **질문**: 작성/조회(페이지네이션, 정렬)/수정/삭제, 태그 연결, 검색·필터링, 삭제 시 하위 답변 비활성화
- **답변**: 작성/수정/삭제, 채택/채택 취소(질문 상태 연동), 내 답변 목록 조회
- **기타**: 추천(좋아요) 토글, XSS sanitize, 공통 응답 포맷(`ApiResponse<T>`) 및 예외 처리(`ErrorCode`) 컨벤션 통일

## 🛠 기술 스택

| 영역 | 스택 |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.0, Spring Security + JWT, Spring Data JPA, MySQL, Redis, JUnit/Mockito |
| Frontend | React (Vite) |
| Infra/DevOps | Docker, Docker Compose, Nginx, AWS EC2, GitHub Actions (CI/CD) |

## 🏗 아키텍처

```
[Client] → [Nginx (EC2, :80)]
                ├─ 정적 파일 서빙 (Frontend 빌드 결과물)
                └─ /api → [Backend (:8080)] → MySQL / Redis
```

로컬은 `docker-compose.yml`(MySQL, Redis), 운영은 `docker-compose.prod.yml`(MySQL, Redis, Backend, Frontend + Nginx)로 관리합니다.

## 👥 팀 구성

| 이름 | 역할 | 담당 |
| --- | --- | --- |
| 김재혁 | 부팀장 | 회원/인증/권한(F-01~F-05), Spring Security, CI/CD |
| 변재웅 | 팀원 | 질문 CRUD(F-06~F-11), 검색/필터링(F-17~F-19) |
| 조민규 | 팀원 | 답변 CRUD/채택(F-12~F-15), 추천(F-16), 인프라(Docker/EC2/Nginx) |

> MVP2 역할은 [로드맵](#-로드맵-mvp2) 참고


## 📦 배포

- **CI**: PR 생성 시 GitHub Actions가 MySQL/Redis 서비스 컨테이너 기반 `ci` 프로파일로 전체 테스트 실행 및 결과 게시
- **CD**: main 병합 시 GitHub Actions가 EC2에 SSH 접속해 자동 배포
- **운영**: EC2(Ubuntu)에서 `docker-compose.prod.yml`로 4개 컨테이너 실행, 컨테이너 내 Nginx가 정적 파일 서빙 + `/api` 프록시 담당
- 운영 환경변수: `DB_HOST/PORT/USER/PASSWORD`, `REDIS_HOST/PORT`, `JWT_SECRET/REFRESH`, `SPRING_PROFILES_ACTIVE=prod`

## 🌿 브랜치 전략

- `main`: 배포 가능한 안정 버전
- `feature/*`: 기능 단위 개발 (최소 단위로 분리해 PR)
- `fix/*`: 버그 수정
- `ci/*`: CI/CD 설정

여러 단계로 나뉘는 기능은 이전 단계 브랜치 위에서 다음 브랜치를 분기해 순차 병합합니다.

## 🗺 로드맵 (MVP2)

- 관리자 통계 대시보드, 마크다운 지원 및 코드 하이라이팅
- Mock PG 결제 / 구독 등급·이력 / 프리미엄 게시판
- GitHub·Google 소셜 로그인(OAuth2)
- 사용자 평판 시스템, 파일 첨부, 외부 API 연동
- *(최후순위)* AI 요약/태그 추천, 실시간 알림, 실시간 채팅