import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getMySubscription } from "../../api/subscriptionApi";
import {
  prepareBillingKeyIssue,
  issueBillingKey,
  getLatestPaidPayment,
  cancelPayment,
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
  {
    q: "구독을 취소하고 싶어요. 환불되나요?",
    a: "결제일로부터 7일 이내의 결제 건은 멤버십 페이지에서 직접 취소(환불) 요청할 수 있어요. 취소하면 구독이 즉시 해지되고, 예약된 다음 회차 자동결제도 함께 취소돼요. 7일이 지난 결제 건은 취소할 수 없어요.",
  },
  {
    q: "회원 탈퇴하면 구독은 어떻게 되나요?",
    a: "탈퇴 즉시 구독이 해지되고, 예약된 다음 회차 자동결제도 함께 취소돼요. 이미 결제한 금액은 자동 환불되지 않으니, 환불을 원하시면 탈퇴 전에 결제 취소(7일 이내)를 먼저 진행해주세요.",
  },
  {
    q: "AI 요약은 모든 질문에 나오나요?",
    a: "아니요, 본문 길이가 일정 글자 수 이상인 질문에만 'AI 요약 보기' 버튼이 나타나요. 짧은 질문은 굳이 요약할 필요가 없어서 버튼 자체가 표시되지 않아요.",
  },
  {
    q: "멤버십 게시판에서 익명으로 글을 쓸 수 있나요?",
    a: "네, 멤버십 게시판에서는 답변을 작성할 때 '익명으로 작성' 체크박스를 선택하면 닉네임 대신 익명으로 등록할 수 있어요.",
  },
  {
    q: "커리어 상담 1:1 채팅은 어떻게 시작하나요?",
    a: "커리어 상담 유형으로 등록된 질문에서 채팅 시작 버튼을 누르면 질문자와 1:1 채팅방이 열려요. 질문자가 채팅을 수락하면 대화를 이어갈 수 있어요.",
  },
];

function MembershipPage() {
  const { user } = useAuth();
  const location = useLocation();
  const [toast, setToast] = useState(location.state?.message ?? "");
  const [subscription, setSubscription] = useState(null);
  const [latestPayment, setLatestPayment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [paying, setPaying] = useState(false);
  const [justCompleted, setJustCompleted] = useState(false);
  const [justCancelled, setJustCancelled] = useState(false);
  const [openFaq, setOpenFaq] = useState(null);
  const [showCancelForm, setShowCancelForm] = useState(false);
  const [cancelReason, setCancelReason] = useState("");
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(""), 3000);
    return () => clearTimeout(timer);
  }, [toast]);

  useEffect(() => {
    let cancelledEffect = false;

    const fetchSubscription = async () => {
      setLoading(true);
      try {
        let subRes;
        try {
          subRes = await getMySubscription();
        } catch {
          subRes = await getMySubscription();
        }
        if (!cancelledEffect) setSubscription(subRes);
      } catch (err) {
        if (!cancelledEffect) {
          setError(
            err.response?.data?.message ?? "구독 정보를 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelledEffect) setLoading(false);
      }

      try {
        const paymentRes = await getLatestPaidPayment();
        if (!cancelledEffect) setLatestPayment(paymentRes);
      } catch {
        // ignore
      }
    };

    fetchSubscription();
    return () => {
      cancelledEffect = true;
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

  const handleCancelSubscription = async () => {
    if (!latestPayment || !cancelReason.trim()) return;

    setError("");
    setCancelling(true);
    try {
      await cancelPayment(latestPayment.paymentId, cancelReason.trim());

      setShowCancelForm(false);
      setCancelReason("");
      setJustCancelled(true);
      setJustCompleted(false);

      try {
        setSubscription(await getMySubscription());
      } catch {
        // ignore
      }
      try {
        setLatestPayment(await getLatestPaidPayment());
      } catch {
        // ignore
      }
    } catch (err) {
      setError(
        err.response?.data?.message ??
          err.message ??
          "결제 취소 중 오류가 발생했습니다.",
      );
    } finally {
      setCancelling(false);
    }
  };

  return (
    <div className="page membership-page">
      <header className="membership-hero">
        <p className="eyebrow">MEMBERSHIP</p>
        <h1>나에게 맞는 플랜을 선택하세요</h1>
        <p className="membership-hero__sub">
          무료로 질문하고 답하면서 시작하고,
          <br /> 필요할 때 멤버십 게시판까지 열어보세요.
        </p>
      </header>

      {loading && <p className="state-text">불러오는 중...</p>}

      {!loading && (
        <>
          {justCompleted && (
            <div className="banner">
              <CheckIcon />
              결제가 완료됐어요. 이제 멤버십 게시판을 이용할 수 있어요.
            </div>
          )}

          {justCancelled && (
            <div className="banner banner--neutral">
              <CheckIcon />
              결제가 취소되고 구독이 해지됐어요.
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
                <li>
                  <CheckIcon />
                  마크다운, 코드 하이라이팅 지원
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
                  멤버십 전용 질문 게시판 작성 · 열람
                </li>
                <li>
                  <CheckIcon />
                  멤버십 게시판에 익명으로 글/답변 작성 가능
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
                  <div className="plan-card__subscribed">
                    <span className="plan-card__badge">
                      <CheckIcon />
                      구독 중 · {subscription.expiresAt?.slice(0, 10)}까지
                    </span>
                    {latestPayment && !showCancelForm && (
                      <button
                        type="button"
                        className="btn btn-ghost cancel-trigger"
                        onClick={() => setShowCancelForm(true)}
                      >
                        결제 취소 / 구독 해지
                      </button>
                    )}
                  </div>
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

              {isSubscribed && showCancelForm && (
                <div className="cancel-panel">
                  <p className="cancel-panel__desc">
                    결제일로부터 7일 이내의 결제만 취소(환불)할 수 있어요.
                    취소하면 멤버십 이용이 즉시 종료돼요.
                  </p>
                  <textarea
                    className="textarea cancel-panel__textarea"
                    placeholder="취소 사유를 입력해주세요"
                    value={cancelReason}
                    onChange={(e) => setCancelReason(e.target.value)}
                    disabled={cancelling}
                  />
                  <div className="cancel-panel__actions">
                    <button
                      type="button"
                      className="btn btn-ghost"
                      onClick={() => {
                        setShowCancelForm(false);
                        setCancelReason("");
                      }}
                      disabled={cancelling}
                    >
                      닫기
                    </button>
                    <button
                      type="button"
                      className="btn btn-danger"
                      onClick={handleCancelSubscription}
                      disabled={cancelling || !cancelReason.trim()}
                    >
                      {cancelling ? "처리 중..." : "결제 취소 및 해지"}
                    </button>
                  </div>
                </div>
              )}
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
                  <td>멤버십 전용 게시판</td>
                  <td className="compare__no">
                    <CrossIcon />
                  </td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                </tr>
                <tr>
                  <td>멤버십 게시판 익명 작성</td>
                  <td className="compare__no">
                    <CrossIcon />
                  </td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                </tr>
                <tr>
                  <td>AI 기반 글 내용 요약</td>
                  <td className="compare__no">
                    <CrossIcon />
                  </td>
                  <td className="compare__yes">
                    <CheckIcon />
                  </td>
                </tr>
                <tr>
                  <td>커리어 상담 1:1 채팅</td>
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

      {toast && (
        <div className="toast" role="status">
          {toast}
        </div>
      )}
    </div>
  );
}

export default MembershipPage;
