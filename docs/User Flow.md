### UF-01 - GUEST 사용자의 둘러보기

```mermaid
flowchart TD
  Start([시작]) --> List[질문_목록_S-02]
  List -->|페이지_이동_정렬| List
  List -->|상세_클릭| Detail[질문_상세_S-03]
  Detail -->|목록으로| List
  Detail -->|질문_작성_버튼| AuthCheck{로그인됨?}
  AuthCheck -->|아니오| LoginHint[로그인_화면_유도_S-01]
  AuthCheck -->|예| WriteQ[질문_작성_폼_S-04]
  LoginHint -->|로그인_성공| WriteQ
```

### UF-02 - 가입 후 질문, 답변, 채택

```mermaid
flowchart TD
  Start([시작]) --> Check{아이디_닉네임_중복?}
  Check -->|중복_409| Signup[회원가입_폼_S-01]
  Check -->|사용가능| Signup

  Signup -->|제출| VSignup{입력_유효?}
  VSignup -->|아니오_400| Signup
  VSignup -->|예_201| Login[로그인_폼_S-01]

  Login -->|제출| VLogin{인증_성공?}
  VLogin -->|실패_401| Login
  VLogin -->|성공_토큰발급| List[질문_목록_S-02]

  List -->|작성| WriteQ[질문_작성_폼_S-04]
  WriteQ -->|등록| VQ{제목_본문_유효?}
  VQ -->|아니오_400| WriteQ
  VQ -->|예_201| Detail[질문_상세_S-03]

  Detail -->|답변_작성| WriteA[답변_입력_S-04]
  WriteA -->|등록| VA{본문_유효?}
  VA -->|아니오_400| WriteA
  VA -->|예_201| Detail

  Detail -->|질문자_답변_채택| Accept{이미_해결됨?}
  Accept -->|예_409| Detail
  Accept -->|아니오| Resolved[질문_상태_해결됨_200]
  Resolved --> Detail
```

### UF-03 - 내 질문/답변 수정,삭제 (작성자 확인)

```mermaid
flowchart TD
  Start([로그인됨]) --> Item[질문_또는_답변]
  Item --> Own{내가_작성자?}
  Own -->|아니오| ReadOnly[조회만_가능]
  Own -->|예| Menu[수정_또는_삭제]
  Menu -->|수정| Edit[수정_폼_S-04]
  Edit -->|저장_성공_200| Item
  Edit -->|검증_실패_400| Edit
  Menu -->|삭제| Confirm{삭제_확인?}
  Confirm -->|취소| Item
  Confirm -->|확인_soft_delete_204| Deleted[목록_또는_상위로_이동]
```

### UF-04 - 목록 → 상세 → 역할별 권한 분기

```mermaid
flowchart TD
  Start([시작]) --> List[질문_목록_S-02]
  List -->|상세| Load{질문_존재_및_비삭제?}
  Load -->|아니오_404| E404[404_안내_S-06]
  Load -->|예| Detail[질문_상세_S-03]

  Detail --> Role{역할은?}
  Role -->|GUEST| GuestUI[조회만_작성_답변_버튼은_로그인_유도]
  Role -->|USER_타인글| OtherUI[조회_답변작성_가능_수정삭제_숨김]
  Role -->|USER_내글| OwnerUI[조회_수정_삭제_답변_채택버튼_노출]
  Role -->|ADMIN| AdminUI[조회_수정_삭제_답변]

  GuestUI -->|작성_시도| Login[로그인_유도_S-01]
  OtherUI -->|수정_삭제_시도| E403[403_안내_S-06]
  OwnerUI -->|수정| Edit[질문_수정_폼_S-04]
  OwnerUI -->|삭제| DeleteQ[soft_delete_후_목록]
  AdminUI -->|삭제| DeleteQ
  OtherUI -->|답변| Answer[답변_등록]
  OwnerUI -->|답변| Answer
  Answer --> Detail
  Edit --> Detail
```

### UF-05 - 검색,태그,상태 필터 + 추천(좋아요)

```mermaid
flowchart TD
  Start([질문_목록_S-02]) --> Filter[키워드_태그_상태_필터_입력]
  Filter -->|검색_적용| Search{조건에_맞는_질문_있음?}
  Search -->|없음| Empty[빈_목록_표시]
  Search -->|있음| Result[필터링된_질문_목록]

  Result -->|좋아요_클릭| LikeAuth{로그인됨?}
  LikeAuth -->|아니오_401| LoginHint[로그인_유도_S-01]
  LikeAuth -->|예| Toggle{이미_추천함?}
  Toggle -->|아니오| AddLike[추천_추가]
  Toggle -->|예| RemoveLike[추천_취소_토글]
  AddLike --> Result
  RemoveLike --> Result
```

### UF-06 - 마이페이지 (정보수정, 비번변경, 탈퇴, 내 답변)

```mermaid
flowchart TD
  Start([로그인됨]) --> My[마이페이지_S-05]
  My --> Info[내_정보_조회]
  Info -->|수정| EditInfo[닉네임_수정]
  EditInfo -->|저장_200| My

  My --> Pw[비밀번호_변경]
  Pw -->|현재_비번_확인| PwCheck{일치?}
  PwCheck -->|아니오_400| Pw
  PwCheck -->|예| PwDone[변경_완료_200]

  My --> MyA[내_답변_목록_조회]

  My --> Withdraw{탈퇴_확인?}
  Withdraw -->|취소| My
  Withdraw -->|확인| Deactivated[soft_delete_로그아웃_204]
```

### UF-07 - **게시글/답변 신고 접수 및 관리자 처리**

```mermaid
flowchart TD
  Start([로그인됨]) --> Item[질문_또는_답변_상세_S-03]
  Item -->|신고_버튼| SelfCheck{내가_작성자?}
  SelfCheck -->|예_403| Item
  SelfCheck -->|아니오| ReportForm[신고_사유_입력]
  ReportForm -->|제출| Submitted[신고_접수_201_PENDING]
  Submitted --> Item

  Submitted -.->|관리자| AdminList[신고_목록_조회_S-07]
  AdminList --> Review{검토_결과}
  Review -->|부당함| Rejected[신고_REJECTED]
  Review -->|정당함| Resolved[신고_RESOLVED]
  Resolved --> SuspendCheck{정지_필요?}
  SuspendCheck -->|아니오_보류| End1[처리_종료]
  SuspendCheck -->|예| Suspend[회원_정지_수동실행_SUSPENDED]
  Suspend --> End1
  Rejected --> End1
```

### UF-08 - 프리미엄 구독 결제 및 접근 제어

```mermaid
flowchart TD
    start([로그인됨]) --> access[프리미엄_게시판_접근_시도]
    access --> sub{구독중?}
    sub -->|예| use[프리미엄_게시판_이용]
    sub -->|아니오_403| guide[구독_안내_S-09]
    guide --> pay[결제_요청]
    pay --> portone[PortOne_승인]
    portone --> result{승인_결과}
    result -->|실패_400| guide
    result -->|성공| save[결제_이력_저장]
    save --> grant[구독_등급_부여]
    grant --> history[마이페이지_구독_이력_조회]
    history -->|재접근| access
```

### UF-09 - GitHub/Google 소셜 로그인

```mermaid
flowchart TD
    start([시작]) --> provider{프로바이더_선택}
    provider -->|GitHub| gh[GitHub_인증_화면]
    provider -->|Google| gg[Google_인증_화면]
    gh --> callback{콜백_수신}
    gg --> callback
    callback --> exist{기존_사용자?}
    exist -->|아니오| signup[자동_회원가입]
    exist -->|예| same[동일_사용자로_인식]
    signup --> jwt[로그인_처리_JWT_발급]
    same --> jwt
    jwt --> list[질문_목록_S-02]
```

### UF-10 — 사용자 평판 시스템

```mermaid
flowchart TD
    start([질문_또는_답변_작성]) --> event{추천_또는_채택_발생}
    event -->|추천받음| apply[평판_점수_자동_반영]
    event -->|채택됨| apply
    apply --> update[평판_점수_갱신]
    update --> view[마이페이지_평판_조회_S-05]
```

### UF-11 — 파일 첨부

```mermaid
flowchart TD
    start([질문_또는_답변_작성]) --> select[파일_첨부_선택]
    select --> validate{확장자_용량_검증}
    validate -->|통과| upload[업로드_처리]
    upload --> save[본문_저장_첨부파일_연결]
    save --> view[상세_정상_조회_S-03]
```

### UF-12 — 개인 활동 대시보드

```mermaid
flowchart TD
    start([로그인됨]) --> mypage[마이페이지_S-05]
    mypage --> tab[활동_대시보드_탭]
    tab --> stats[활동_통계_조회]
```

### UF-13 — 익명 질문 + 구독자 뱃지

```mermaid
flowchart TD
    start([질문_작성_폼_S-04]) --> sub{구독중?}
    sub -->|아니오| disabled[익명_옵션_비활성]
    sub -->|예| enabled[익명_옵션_선택_가능]
    enabled --> anon{익명_선택?}
    anon -->|예| regAnon[질문_등록_작성자_익명]
    anon -->|아니오| regNick[질문_등록_닉네임_노출]
    regAnon --> showAnon[목록_상세_익명_표시]
    regNick --> showNick[목록_상세_닉네임_표시]
```

### **UF-X01, UF-X02, UF-X03, UF-X04, UF-X05 (예외·권한)**

```mermaid
flowchart TD
  subgraph auth_fail [미인증_쓰기_거부]
    Guest([GUEST_또는_토큰없음]) --> TryWrite[질문_답변_작성_또는_추천_시도]
    TryWrite --> E401[401_안내_S-06]
    E401 --> LoginHint[로그인_화면_유도_S-01]
  end

  subgraph forbid [본인_아닌_게시물_수정_삭제]
    User([USER]) --> Other[타인_질문_또는_답변]
    Other --> TryEdit[수정_또는_삭제_시도]
    TryEdit --> E403[403]
  end

  subgraph accept_fail [답변_채택_예외]
    Owner([질문_작성자_시도]) --> IsOwner{본인_질문?}
    IsOwner -->|아니오| E403b[403]
    IsOwner -->|예| StatusCheck{이미_해결됨?}
    StatusCheck -->|예| E409[409_중복채택]
    StatusCheck -->|아니오| Accepted[채택_완료]
  end

  subgraph report_fail [신고_예외]
    Reporter([로그인_사용자]) --> OwnPost{본인_게시물?}
    OwnPost -->|예| E403c[403_본인신고불가]
    OwnPost -->|아니오| ReportOk[신고_접수_가능]

    SuspendedUser([SUSPENDED_회원]) --> TryLogin[로그인_시도]
    TryLogin --> E401b[401_정지된_계정]
  end

  subgraph edge [기타_엣지]
    Anyone([누구든]) --> Missing[없는_질문_답변_ID]
    Missing --> E404[404]

    Logged([로그인_사용자]) --> Expired{Access_Token_유효?}
    Expired -->|만료| Reissue{Refresh_Token_유효?}
    Reissue -->|예| NewToken[Access_Token_재발급]
    Reissue -->|아니오_만료_위조| Relogin[재로그인_필요_401]
    NewToken --> OkPath[정상_API_재요청]
    Expired -->|유효| OkPath

    Logged --> BadInput[빈_제목_본문_제출]
    BadInput --> E400[400_검증_메시지]
  end
```

### **UF-X06, UF-X07 (예외·권한)**

```mermaid
flowchart TD
    payStart([결제_요청]) --> result{승인_결과}
    result -->|실패| payFail[결제_실패_처리]

    boardStart([게시판_접근]) --> sub{구독중?}
    sub -->|아니오_403| deny[접근_거부_구독_안내]
```

```mermaid
flowchart TD
    start([질문_작성_폼_S-04]) --> sub{구독중?}
    sub -->|아니오_403| disabled[익명_옵션_사용_불가]
    disabled --> guide[구독_안내_화면]
```

## 3. 화면 전환 요약

| 현재 화면             | 행동                    | 다음 화면           | 비고                       |
| --------------------- | ----------------------- | ------------------- | -------------------------- |
| 로그인 (S-01)         | 정지된 계정 로그인 시도 | 로그인 (S-01)       |                            |
| 질문\_목록 (S-02)     | 상세 클릭               | 질문\_상세 (S-03)   | 비로그인 OK                |
| 질문\_상세 (S-03)     | 작성 버튼 (GUEST)       | 로그인 (S-01)       | 유도                       |
| 질문\_목록 (S-02)     | 작성 (USER)             | 질문*작성*폼 (S-04) | 토큰 필요                  |
| 질문\_상세 (S-03)     | 타인 수정 시도          | 403 안내 (S-06)     | UF-X02                     |
| 질문\_상세 (S-03)     | 답변 채택 (질문자)      | 질문\_상세 (해결됨) | UF-X04                     |
| 질문\_목록 (S-02)     | 좋아요 클릭 (비로그인)  | 로그인 (S-01)       | UF-X01                     |
| 마이페이지 (S-05)     | 탈퇴 확인               | 로그아웃 상태       | soft delete                |
| 질문\_상세 (S-03)     | 신고 버튼 (본인 게시물) | 질문\_상세 (S-03)   | 403, UF-X05                |
| 질문\_상세 (S-03)     | 신고 버튼 (타인 게시물) | 신고*접수*완료      | 201 PENDING, UF-07         |
| 신고\_관리 (S-07)     | 신고 처리 (정당함)      | 회원\_관리 (S-08)   | 정지 필요 여부 판단, UF-07 |
| 회원\_관리 (S-08)     | 회원 정지 실행          | 회원\_관리 (S-08)   | SUSPENDED 전환             |
| 구독*안내*결제 (S-09) | 결제 요청 → 결제 승인   | 질문\_목록 (S-02)   | 구독 등급 부여             |
