import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import AuthHeader from "../../components/layout/AuthHeader";
import "../../styles/auth.css";

function OAuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");

    if (!accessToken) {
      navigate("/login?error=OAUTH_LOGIN_FAILED", { replace: true });
      return;
    }

    setAuth(accessToken);
    navigate("/questions", { replace: true });
  }, []);

  return (
    <div className="auth-page">
      <AuthHeader />
      <main className="auth-main">
        <div className="auth-card">
          <p className="auth-card__subtitle">로그인 처리 중입니다...</p>
        </div>
      </main>
    </div>
  );
}

export default OAuthCallbackPage;