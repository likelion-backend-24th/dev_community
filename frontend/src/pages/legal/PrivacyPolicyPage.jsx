import "../../styles/legal.css";

const EFFECTIVE_DATE = "2026-08-22";

function PrivacyPolicyPage() {
  return (
    <div className="page legal-page">
      <div className="page__header">
        <h1 className="page__title">개인정보처리방침</h1>
      </div>

      <p className="legal-meta">시행일자: {EFFECTIVE_DATE}</p>

      <article className="legal-content">
        <section>
          <h2>제1조 (수집하는 개인정보 항목)</h2>
          <p>
            Dev_Community(이하 "서비스")는 회원가입 및 서비스 제공을 위해 다음과
            같은 개인정보를 수집합니다.
          </p>
          <ol>
            <li>
              <strong>일반 회원가입 시</strong>: 아이디, 비밀번호(암호화 저장),
              닉네임
            </li>
            <li>
              <strong>소셜 로그인(GitHub, Google) 이용 시</strong>: 각 소셜
              로그인 제공자가 발급하는 고유 식별자(providerId)만
              수집·저장합니다. 이메일 등 그 외 프로필 정보는 저장하지 않습니다.
            </li>
            <li>
              <strong>멤버십 구독(결제) 시</strong>: 결제대행사 PortOne을 통해
              발급되는 정기결제용 빌링키, 결제 금액, 결제 상태 및 일시. 카드번호
              등 카드 정보 자체는 서비스가 직접 수집·저장하지 않으며 PortOne 및
              카드사가 처리·보관합니다.
            </li>
            <li>
              <strong>서비스 이용 과정에서 생성되는 정보</strong>: 작성한
              질문·답변·댓글, 첨부파일, 추천·신고 내역, 평판 점수, 로그인 세션
              유지를 위한 토큰
            </li>
          </ol>
        </section>

        <section>
          <h2>제2조 (개인정보의 수집 및 이용 목적)</h2>
          <ol>
            <li>회원 식별 및 본인 확인, 로그인 유지 등 회원 관리</li>
            <li>질문·답변 게시물의 등록·열람 등 서비스 제공</li>
            <li>멤버십 구독 결제 처리 및 결제 내역 관리</li>
            <li>신고 처리, 부정 이용 방지 등 서비스 운영 및 이용자 보호</li>
          </ol>
        </section>

        <section>
          <h2>제3조 (개인정보의 보유 및 이용 기간)</h2>
          <ol>
            <li>
              회원 탈퇴 시 회원의 개인정보는 지체 없이 파기합니다. 다만 회원이
              작성한 질문·답변 등 게시물은 이용약관 제5조에 따라 별도로
              처리됩니다.
            </li>
            <li>
              관계 법령에 따라 보존할 필요가 있는 경우, 서비스는 해당 법령에서
              정한 기간 동안 결제·거래 관련 정보를 보관합니다.
              <ol className="legal-sublist">
                <li>
                  계약 또는 청약철회 등에 관한 기록: 5년(전자상거래 등에서의
                  소비자보호에 관한 법률)
                </li>
                <li>
                  대금결제 및 재화 등의 공급에 관한 기록: 5년(전자상거래
                  등에서의 소비자보호에 관한 법률)
                </li>
                <li>
                  소비자의 불만 또는 분쟁처리에 관한 기록: 3년(전자상거래
                  등에서의 소비자보호에 관한 법률)
                </li>
              </ol>
            </li>
          </ol>
        </section>

        <section>
          <h2>제4조 (개인정보의 제3자 제공 및 처리위탁)</h2>
          <p>
            서비스는 원칙적으로 회원의 개인정보를 외부에 제공하지 않으며, 다음의
            경우에 한해 서비스 제공에 필요한 범위 내에서만 개인정보 처리를
            위탁하거나 제공합니다.
          </p>
          <ol>
            <li>
              <strong>PortOne(결제대행)</strong>: 멤버십 정기결제 처리를 위해
              빌링키 발급 및 결제 승인 정보를 위탁합니다.
            </li>
            <li>
              <strong>GitHub, Google(소셜 로그인)</strong>: 회원이 소셜 로그인을
              선택한 경우, 해당 제공자가 발급하는 고유 식별자를 통해 본인 인증을
              처리합니다.
            </li>
          </ol>
        </section>

        <section>
          <h2>제5조 (이용자의 권리와 행사 방법)</h2>
          <p>
            회원은 언제든지 마이페이지를 통해 본인의 개인정보를 열람·수정할 수
            있으며, 회원탈퇴를 통해 개인정보의 삭제를 요구할 수 있습니다. 그
            밖의 열람·정정·삭제·처리정지 요구는 아래 문의처를 통해 접수할 수
            있습니다.
          </p>
        </section>

        <section>
          <h2>제6조 (개인정보의 안전성 확보 조치)</h2>
          <ol>
            <li>
              비밀번호는 복호화가 불가능한 방식으로 암호화하여 저장합니다.
            </li>
            <li>
              로그인 세션 유지에 사용되는 토큰은 유효기간을 두어 관리합니다.
            </li>
            <li>
              카드 결제 정보는 서비스가 직접 저장하지 않고 PortOne을 통해
              안전하게 처리합니다.
            </li>
          </ol>
        </section>

        <section>
          <h2>제7조 (개인정보 보호책임자 및 문의처)</h2>
          <p>
            개인정보 처리에 관한 문의, 불만 처리, 피해 구제 등을 위해 아래
            문의처로 연락해 주시기 바랍니다.
          </p>
          <p>담당: [devcom@example.com]</p>
        </section>

        <section>
          <h2>부칙</h2>
          <p>이 개인정보처리방침은 {EFFECTIVE_DATE}부터 시행합니다.</p>
        </section>
      </article>
    </div>
  );
}

export default PrivacyPolicyPage;
