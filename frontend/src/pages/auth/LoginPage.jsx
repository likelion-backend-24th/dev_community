import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { login } from "../../api/authApi";
import AuthHeader from "../../components/layout/AuthHeader";
import "../../styles/auth.css";

const OAUTH_ERROR_MESSAGES = {
  SUSPENDED_ACCOUNT: "정지된 계정입니다.",
  WITHDRAWN_ACCOUNT: "탈퇴한 계정입니다.",
  OAUTH_LOGIN_FAILED: "소셜 로그인에 실패했습니다.",
};

function LoginPage() {
  const { login: setAuth } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState(
    location.state?.signupSuccess
      ? "회원가입이 완료되었습니다."
      : location.state?.passwordResetSuccess
        ? "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해주세요."
        : "",
  );

  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(""), 3000);
    return () => clearTimeout(timer);
  }, [toast]);

  // 소셜 로그인 실패/정지·탈퇴 계정으로 리다이렉트돼서 돌아온 경우, 쿼리스트링의 error 코드를 메시지로 보여줌
  useEffect(() => {
    const errorCode = searchParams.get("error");
    if (errorCode) {
      setError(OAUTH_ERROR_MESSAGES[errorCode] ?? "로그인에 실패했습니다.");
    }
  }, [searchParams]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      const { accessToken } = await login(form);
      setAuth(accessToken);
      navigate("/questions");
    } catch (err) {
      setError(err.response?.data?.message ?? "로그인에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  // 백엔드가 GitHub 인증을 시작하는 엔드포인트로 이동 (Spring Security OAuth2 Client가 자동 생성)
  // 인가 코드 교환/콜백 처리까지 전부 백엔드가 하고, 끝나면 /oauth/callback으로 다시 리다이렉트해줌
  const handleGithubLogin = () => {
    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/github`;
  };

  const handleGoogleLogin = () => {
    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`;
  };

  return (
    <div className="auth-page">
      <AuthHeader />

      <main className="auth-main">
        <div className="auth-card">
          <h1 className="auth-card__title">다시 만나서 반가워요</h1>
          <p className="auth-card__subtitle">
            질문하고, 답변하고, 채택받으세요.
          </p>

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label className="field__label" htmlFor="username">
                아이디
              </label>
              <input
                id="username"
                name="username"
                className="field__input"
                value={form.username}
                onChange={handleChange}
                autoComplete="username"
                required
              />
            </div>

            <div className="field">
              <label className="field__label" htmlFor="password">
                비밀번호
              </label>
              <input
                id="password"
                name="password"
                type="password"
                className="field__input"
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
                required
              />
              <Link to="/forgot-password" className="field__forgot-link">
                비밀번호를 잊으셨나요?
              </Link>
            </div>

            {error && (
              <p className="auth-error" role="alert">
                {error}
              </p>
            )}

            <button className="auth-submit" type="submit" disabled={submitting}>
              {submitting ? "로그인 중..." : "로그인"}
            </button>
          </form>

          <div className="auth-divider">
            <span>또는</span>
          </div>

          <button
            type="button"
            className="auth-oauth-btn"
            onClick={handleGithubLogin}
          >
            <i className="ti ti-brand-github" aria-hidden="true"></i>
            GitHub로 로그인
          </button>

          <button
            type="button"
            className="auth-oauth-btn"
            onClick={handleGoogleLogin}
          >
            <i className="ti ti-brand-google" aria-hidden="true"></i>
            Google로 로그인
          </button>

          <div className="auth-links">
            <p className="auth-links__muted">
              계정이 없으신가요?
              <Link to="/signup">회원가입</Link>
            </p>
            <Link to="/questions">로그인 없이 둘러보기 ⭢</Link>
          </div>
        </div>
      </main>

      {toast && <div className="toast" role="status">{toast}</div>}
    </div>
  );
}

export default LoginPage;