import { useState } from "react";
import { useSearchParams, useNavigate, Navigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { checkNickname, oauthComplete } from "../../api/authApi";
import "../../styles/auth.css";

function OAuthNicknamePage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();

  const signupToken = searchParams.get("signupToken");
  const suggestedNickname = searchParams.get("nickname");

  const [nickname, setNickname] = useState(suggestedNickname ?? "");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (!signupToken) {
    return <Navigate to="/login" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!nickname.trim()) {
      setError("닉네임을 입력해주세요.");
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
      const { accessToken } = await oauthComplete(signupToken, nickname);
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