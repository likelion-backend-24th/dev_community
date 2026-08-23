import { useState } from "react";
import { useSearchParams, useNavigate, Navigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { checkNickname, checkEmail, oauthComplete } from "../../api/authApi";
import "../../styles/auth.css";

const IDLE = { status: "idle", message: "" };

function OAuthNicknamePage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();

  const signupToken = searchParams.get("signupToken");
  const suggestedNickname = searchParams.get("nickname");
  const providerEmail = searchParams.get("email");

  const [nickname, setNickname] = useState(suggestedNickname ?? "");
  const [email, setEmail] = useState(providerEmail ?? "");
  const [emailCheck, setEmailCheck] = useState(providerEmail ? { status: "available", message: "" } : IDLE);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (!signupToken) {
    return <Navigate to="/login" replace />;
  }

  const handleCheckEmail = async () => {
    if (!email.trim()) return;
    setEmailCheck({ status: "checking", message: "" });
    try {
      const res = await checkEmail(email);
      setEmailCheck({ status: "available", message: res.message });
    } catch (err) {
      setEmailCheck({
        status: "unavailable",
        message: err.response?.data?.message ?? "이미 사용중인 이메일입니다.",
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!nickname.trim()) {
      setError("닉네임을 입력해주세요.");
      return;
    }

    if (!providerEmail && emailCheck.status !== "available") {
      setError("이메일 중복 확인을 먼저 진행해주세요.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      await checkNickname(nickname);
    } catch (err) {
      setSubmitting(false);
      setError(err.response?.data?.message ?? "사용할 수 없는 닉네임입니다.");
      return;
    }

    try {
      const { accessToken } = await oauthComplete(signupToken, nickname, email);
      setAuth(accessToken);
      navigate("/questions", { replace: true });
    } catch (err) {
      setError(err.response?.data?.message ?? "가입 처리에 실패했습니다. 다시 로그인해주세요.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <main className="auth-main">
        <div className="auth-card">
          <h1 className="auth-card__title">닉네임을 설정해주세요</h1>
          <p className="auth-card__subtitle">나중에 마이페이지에서 바꿀 수 있어요.</p>

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label className="field__label" htmlFor="nickname">닉네임</label>
              <input
                id="nickname"
                className="field__input"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                autoFocus
              />
            </div>

            <div className="field">
              <label className="field__label" htmlFor="email">이메일</label>
              {providerEmail ? (
                <input
                  id="email"
                  className="field__input"
                  value={email}
                  disabled
                />
              ) : (
                <div className="field__row">
                  <input
                    id="email"
                    type="email"
                    className={`field__input${emailCheck.status === "unavailable" ? " is-error" : ""}`}
                    value={email}
                    onChange={(e) => {
                      setEmail(e.target.value);
                      setEmailCheck(IDLE);
                    }}
                  />
                  <button
                    type="button"
                    className="field__check-btn"
                    onClick={handleCheckEmail}
                    disabled={emailCheck.status === "checking"}
                  >
                    중복 확인
                  </button>
                </div>
              )}
              {emailCheck.message && (
                <p className={`field__hint is-${emailCheck.status}`}>{emailCheck.message}</p>
              )}
            </div>

            {error && <p className="auth-error" role="alert">{error}</p>}

            <button className="auth-submit" type="submit" disabled={submitting}>
              {submitting ? "처리 중..." : "가입 완료"}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

export default OAuthNicknamePage;