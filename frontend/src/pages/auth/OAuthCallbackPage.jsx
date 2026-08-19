import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { oauthLogin } from "../../api/authApi";
import "../../styles/auth.css";

function OAuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login: setAuth } = useAuth();
  const [error, setError] = useState("");
  const calledRef = useRef(false);

  useEffect(() => {
    const code = searchParams.get("code");

    if (!code) {
      setError("잘못된 접근입니다.");
      return;
    }
    if (calledRef.current) return;
    calledRef.current = true;

    (async () => {
      try {
        const result = await oauthLogin("github", code);

        if (result.registered) {
          setAuth(result.token.accessToken);
          navigate("/questions", { replace: true });
        } else {
          navigate("/oauth/nickname", {
            replace: true,
            state: {
              signupToken: result.signupToken,
              suggestedNickname: result.suggestedNickname,
            },
          });
        }
      } catch (err) {
        setError(err.response?.data?.message ?? "GitHub 로그인에 실패했습니다.");
      }
    })();
  }, [searchParams, navigate, setAuth]);

  return (
    <div className="auth-page">
      <main className="auth-main">
        <div className="auth-card">
          {error ? <p className="auth-error" role="alert">{error}</p> : <p>GitHub 로그인 처리 중...</p>}
        </div>
      </main>
    </div>
  );
}

export default OAuthCallbackPage;