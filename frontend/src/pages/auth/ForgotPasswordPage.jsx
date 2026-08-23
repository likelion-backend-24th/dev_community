import { useState } from "react";
import { Link } from "react-router-dom";
import { requestPasswordReset } from "../../api/authApi";
import AuthHeader from "../../components/layout/AuthHeader";
import "../../styles/auth.css";

function ForgotPasswordPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username.trim() || !email.trim()) {
      setError("아이디와 이메일을 모두 입력해주세요.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      await requestPasswordReset(username, email);
      setSent(true);
    } catch (err) {
      setError(err.response?.data?.message ?? "요청 처리에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <AuthHeader />
      <main className="auth-main">
        <div className="auth-card">
          <h1 className="auth-card__title">비밀번호를 잊으셨나요?</h1>
          <p className="auth-card__subtitle">
            가입하신 아이디와 이메일 주소가 일치하면 재설정 링크를 보내드려요.
          </p>

          {sent ? (
            <p className="auth-card__subtitle">
              입력하신 정보와 일치하는 계정이 있다면 이메일로 재설정 링크를 보냈습니다.
              <br />
              메일함을 확인해주세요.
            </p>
          ) : (
            <form className="auth-form" onSubmit={handleSubmit} noValidate>
              <div className="field">
                <label className="field__label" htmlFor="username">
                  아이디
                </label>
                <input
                  id="username"
                  className="field__input"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  autoComplete="username"
                  autoFocus
                  required
                />
              </div>

              <div className="field">
                <label className="field__label" htmlFor="email">
                  이메일
                </label>
                <input
                  id="email"
                  type="email"
                  className="field__input"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                  required
                />
              </div>

              {error && (
                <p className="auth-error" role="alert">
                  {error}
                </p>
              )}

              <button className="auth-submit" type="submit" disabled={submitting}>
                {submitting ? "전송 중..." : "재설정 링크 보내기"}
              </button>
            </form>
          )}

          <div className="auth-links">
            <p className="auth-links__muted">
              <Link to="/login">로그인으로 돌아가기</Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}

export default ForgotPasswordPage;
