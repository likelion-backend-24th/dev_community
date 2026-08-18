import { useEffect, useState } from "react";
import { useAuth } from "../../hooks/useAuth";
import { getMySubscription } from "../../api/subscriptionApi";
import {
  prepareBillingKeyIssue,
  issueBillingKey,
  PAYMENT_WEBHOOK_URL,
} from "../../api/paymentApi";
import "../../styles/membership.css";

function CheckIcon() {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M20 6 9 17l-5-5"
        stroke="currentColor"
        strokeWidth="2.4"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function CrossIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M6 6l12 12M18 6 6 18"
        stroke="currentColor"
        strokeWidth="2.2"
        strokeLinecap="round"
      />
    </svg>
  );
}

function ChevronIcon({ open }) {
  return (
    <svg
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
      style={{ transform: `rotate(${open ? 180 : 0}deg)` }}
    >
      <path
        d="m6 9 6 6 6-6"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function ShieldIcon() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M12 3 4 7v6c0 4.4 3.4 8.4 8 9 4.6-.6 8-4.6 8-9V7l-8-4Z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function StarIcon() {
  return (
    <svg
      width="20"
      height="20"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="m12 3 2.5 5.6L21 9.3l-4.5 4.2L17.6 21 12 17.8 6.4 21l1.1-7.5L3 9.3l6.5-.7L12 3Z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg
      width="13"
      height="13"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <rect
        x="5"
        y="11"
        width="14"
        height="9"
        rx="2"
        stroke="currentColor"
        strokeWidth="1.6"
      />
      <path
        d="M8 11V8a4 4 0 0 1 8 0v3"
        stroke="currentColor"
        strokeWidth="1.6"
      />
    </svg>
  );
}

const FAQ_ITEMS = [
  {
    q: "결제는 어떻게 진행되나요?",
    a: "PortOne을 통한 카드 정기결제를 지원해요. 최초 결제 후 30일마다 등록하신 카드로 자동 청구돼요.",
  },
  {
    q: "자동으로 매달 갱신되나요?",
    a: "네, 자동결제예요. 구독 시 등록한 카드로 30일마다 자동으로 결제되어 구독이 이어져요.",
  },
];

function MembershipPage() {
  const { user } = useAuth();
  const [subscription, setSubscription] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [paying, setPaying] = useState(false);
  const [justCompleted, setJustCompleted] = useState(false);
  const [openFaq, setOpenFaq] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const fetchSubscription = async () => {
      setLoading(true);
      try {
        const res = await getMySubscription();
        if (!cancelled) setSubscription(res);
      } catch (err) {
        if (!cancelled) {
          setError(
            err.response?.data?.message ?? "구독 정보를 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchSubscription();
    return () => {
      cancelled = true;
    };
  }, []);

  const isSubscribed = subscription?.status === "ACTIVE";

  const handleSubscribe = async () => {
    setError("");
    setPaying(true);
    try {
      const prepared = await prepareBillingKeyIssue();

      const issueResponse = await window.PortOne.requestIssueBillingKey({
        storeId: prepared.storeId,
        channelKey: prepared.channelKey,
        billingKeyMethod: "CARD",
        issueId: prepared.issueId,
        customer: {
          customerId: `user-${user.id}`,
          fullName: user.nickname,
        },
        noticeUrls: [PAYMENT_WEBHOOK_URL],
      });

      if (issueResponse.code !== undefined) {
        setError(`정기결제 등록 실패: ${issueResponse.message}`);
        return;
      }

      const completed = await issueBillingKey(
        issueResponse.billingKey,
        "PREMIUM",
      );
      if (completed.status !== "PAID") {
        setError("결제 확인에 실패했습니다. 잠시 후 다시 시도해주세요.");
        return;
      }

      const res = await getMySubscription();
      setSubscription(res);
      setJustCompleted(true);
    } catch (err) {
      setError(
        err.response?.data?.message ??
          err.message ??
          "결제 중 오류가 발생했습니다.",
      );
    } finally {
      setPaying(false);
    }
  };

  return (
    <div className="page membership-page">
      <header className="membership-hero">
        <p className="eyebrow">MEMBERSHIP</p>
        <h1>나에게 맞는 플랜을 선택하세요</h1>
        <p className="membership-hero__sub">
          무료로 질문하고 답하면서 시작하고,
          <br /> 필요할 때 프리미엄 게시판까지 열어보세요.
        </p>
      </header>

      {loading && <p className="state-text">불러오는 중...</p>}

      {!loading && (
        <>
          {justCompleted && (
            <div className="banner">
              <CheckIcon />
              결제가 완료됐어요. 이제 프리미엄 게시판을 이용할 수 있어요.
            </div>
          )}

          {error && (
            <p className="inline-error" role="alert">
              {error}
            </p>
          )}

          <div className="plan-grid">
            <div className="plan-card">
              <div className="plan-card__head">
                <div className="plan-card__icon">
                  <ShieldIcon />
                </div>
                <h2 className="plan-card__name">무료 플랜</h2>
                <div className="plan-card__price">
                  <strong>0원</strong>
                  <span>영구 무료</span>
                </div>
              </div>

              <ul className="plan-card__features">
                <li>
                  <CheckIcon />
                  질문 · 답변 등록 및 열람
                </li>
                <li>
                  <CheckIcon />
                  추천 · 신고 기능
                </li>
              </ul>

              <div className="plan-card__foot">
                {!isSubscribed ? (
                  <span className="plan-card__badge">
                    <CheckIcon />
                    현재 이용 중
                  </span>
                ) : (
                  <button type="button" className="btn btn-secondary" disabled>
                    포함된 플랜
                  </button>
                )}
              </div>
            </div>

            <div className="plan-card plan-card--premium">
              <span className="plan-card__ribbon">추천</span>
              <div className="plan-card__head">
                <div className="plan-card__icon">
                  <StarIcon />
                </div>
                <h2 className="plan-card__name">구독 플랜</h2>
                <div className="plan-card__price">
                  <strong>4,900원</strong>
                  <span>/ 30일</span>
                </div>
              </div>

              <ul className="plan-card__features">
                <li>
                  <CheckIcon />
                  무료 플랜의 모든 기능
                </li>
                <li>
                  <CheckIcon />
                  프리미엄 전용 질문 게시판 작성 · 열람
                </li>
                <li>
                  <CheckIcon />
                  마크다운, 코드 하이라이팅 지원
                </li>
                <li>
                  <CheckIcon />
                  AI 기반 글 내용 요약
                </li>
                <li>
                  <CheckIcon />
                  커리어 상담 시 1:1 채팅
                </li>
              </ul>

              <div className="plan-card__foot">
                {isSubscribed ? (
                  <span className="plan-card__badge">
                    <CheckIcon />
                    구독 중 · {subscription.expiresAt?.slice(0, 10)}까지
                  </span>
                ) : (
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleSubscribe}
                    disabled={paying}
                  >
                    {paying ? "결제 진행 중..." : "구독하기"}
                  </button>
                )}
              </div>
            </div>
          </div>

          <section className="compare">
            <h2>플랜 비교</h2>
            <table>
              <thead>
                <tr>
                  <th>기능</th>
                  <th>무료</th>
                  <th>구독</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>질문 · 답변 등록/열람</td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                </tr>
                <tr>
                  <td>추천 · 신고</td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                </tr>
                <tr>
                  <td>프리미엄 전용 게시판</td>
                  <td className="compare__no">
                    <CrossIcon />
                  </td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                </tr>
              </tbody>
            </table>
          </section>

          <section className="faq">
            <h2>자주 묻는 질문</h2>
            {FAQ_ITEMS.map((item, i) => {
              const isOpen = openFaq === i;
              return (
                <div className="faq-item" key={item.q}>
                  <button
                    type="button"
                    className="faq-item__q"
                    onClick={() => setOpenFaq(isOpen ? null : i)}
                    aria-expanded={isOpen}
                  >
                    {item.q}
                    <ChevronIcon open={isOpen} />
                  </button>
                  {isOpen && (
                    <div className="faq-item__a">
                      <p>{item.a}</p>
                    </div>
                  )}
                </div>
              );
            })}
          </section>

          <p className="trust-line">
            <LockIcon />
            카드 결제는 PortOne을 통해 안전하게 처리돼요
          </p>
        </>
      )}
    </div>
  );
}

export default MembershipPage;
