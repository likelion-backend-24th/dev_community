import { useEffect, useRef, useState } from "react";
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

// 터미널 로그인 흐름의 단계.
// login(아이디 입력) -> password(비밀번호 입력) -> confirm(로그인 확인 대기) -> loading(인증 중) -> failed(실패, 잠시 후 login으로 복귀)
const STEP = {
  LOGIN: "login",
  PASSWORD: "password",
  CONFIRM: "confirm",
  LOADING: "loading",
  DONE: "done",
  FAILED: "failed",
};

// 실제 응답이 아무리 빨라도 터미널 감성을 느낄 최소 시간은 보장하고,
// 로그인 성공 후에도 "Last login" 화면을 잠깐 보여준 뒤 이동한다.
const MIN_LOADING_MS = 1300;
const SUCCESS_HOLD_MS = 1300;
const FAIL_HOLD_MS = 1600;

function LoginPage() {
  const { login: setAuth } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const [step, setStep] = useState(STEP.LOGIN);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [failMessage, setFailMessage] = useState("");
  const [progress, setProgress] = useState(0);
  const [lastLoginStamp, setLastLoginStamp] = useState("");
  const [lastLoginIp, setLastLoginIp] = useState("");

  const [error, setError] = useState("");
  const [toast, setToast] = useState(
    location.state?.signupSuccess
      ? "회원가입이 완료되었습니다."
      : location.state?.passwordResetSuccess
        ? "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해주세요."
        : "",
  );

  const usernameInputRef = useRef(null);
  const passwordInputRef = useRef(null);
  const confirmBtnRef = useRef(null);
  const progressTimerRef = useRef(null);

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

  useEffect(() => {
    if (step === STEP.PASSWORD) passwordInputRef.current?.focus();
    if (step === STEP.CONFIRM) confirmBtnRef.current?.focus();
  }, [step]);

  useEffect(() => {
    return () => clearInterval(progressTimerRef.current);
  }, []);

  const resetTerminal = () => {
    setUsername("");
    setPassword("");
    setFailMessage("");
    setProgress(0);
    setLastLoginStamp("");
    setLastLoginIp("");
    setStep(STEP.LOGIN);
    usernameInputRef.current?.focus();
  };

  const triggerFail = (message) => {
    setFailMessage(message);
    setStep(STEP.FAILED);
    setTimeout(resetTerminal, FAIL_HOLD_MS);
  };

  const handleUsernameKeyDown = (e) => {
    if (e.key !== "Enter") return;
    e.preventDefault();
    if (!username.trim()) return;
    setStep(STEP.PASSWORD);
  };

  const handlePasswordKeyDown = (e) => {
    if (e.key !== "Enter") return;
    e.preventDefault();
    if (!password) return;
    setStep(STEP.CONFIRM);
  };

  const handleAuthenticate = async () => {
    if (step !== STEP.CONFIRM) return;
    setStep(STEP.LOADING);
    setProgress(0);
    const startedAt = Date.now();
    progressTimerRef.current = setInterval(() => {
      // 92%까지만 채워두고, 실제 응답이 도착한 뒤(+최소 대기 시간 이후)에 100%로 마무리한다.
      setProgress((p) => (p >= 92 ? p : p + 4 + Math.floor(Math.random() * 6)));
    }, 70);

    let accessToken = null;
    let prevLoginAt = null;
    let prevLoginIp = null;
    let failureMessage = null;
    try {
      ({ accessToken, lastLoginAt: prevLoginAt, lastLoginIp: prevLoginIp } = await login({ username, password }));
    } catch (err) {
      failureMessage = err.response?.data?.message ?? "로그인에 실패했습니다.";
    }

    const elapsed = Date.now() - startedAt;
    const remaining = Math.max(0, MIN_LOADING_MS - elapsed);
    await new Promise((resolve) => setTimeout(resolve, remaining));
    clearInterval(progressTimerRef.current);

    if (failureMessage) {
      triggerFail(failureMessage);
      return;
    }

    setProgress(100);
    // 이번이 첫 로그인이면(직전 기록 없음) Last login 줄 없이 바로 셸 프롬프트만 보여준다.
    if (prevLoginAt) {
      const days = ["일", "월", "화", "수", "목", "금", "토"];
      const prev = new Date(prevLoginAt);
      setLastLoginStamp(
        `${days[prev.getDay()]} ${prev.getMonth() + 1}월 ${prev.getDate()}일 ` +
          `${String(prev.getHours()).padStart(2, "0")}:${String(prev.getMinutes()).padStart(2, "0")}`,
      );
      setLastLoginIp(prevLoginIp ?? "");
    } else {
      setLastLoginStamp("");
    }
    setStep(STEP.DONE);
    setTimeout(() => {
      setAuth(accessToken);
      navigate("/questions");
    }, SUCCESS_HOLD_MS);
  };

  const handleConfirmKeyDown = (e) => {
    if (e.key !== "Enter") return;
    e.preventDefault();
    handleAuthenticate();
  };

  // 백엔드가 GitHub 인증을 시작하는 엔드포인트로 이동 (Spring Security OAuth2 Client가 자동 생성)
  // 인가 코드 교환/콜백 처리까지 전부 백엔드가 하고, 끝나면 /oauth/callback으로 다시 리다이렉트해줌
  const handleGithubLogin = () => {
    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/github`;
  };

  const handleGoogleLogin = () => {
    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google`;
  };

  const progressCells = 28;
  const filledCells = Math.round((progress / 100) * progressCells);
  const progressBar = "█".repeat(filledCells) + "░".repeat(progressCells - filledCells);

  return (
    <div className="auth-page">
      <AuthHeader />

      <main className="auth-main">
        <div className="auth-card auth-card--terminal">
          <h1 className="auth-card__title">다시 만나서 반가워요</h1>
          <p className="auth-card__subtitle">
            질문하고, 답변하고, 채택받으세요.
          </p>

          <div className="term-window">
            <div className="term-titlebar">
              <span className="term-dot term-dot--red"></span>
              <span className="term-dot term-dot--yellow"></span>
              <span className="term-dot term-dot--green"></span>
              <span className="term-titlebar__title">login — dev-com — 80x24</span>
            </div>

            <div className="term-body">
              <p className="term-line term-line--dim">dev-com OS 1.0 LTS (Carbon)</p>

              <div className="term-row">
                <label htmlFor="username">dev-com login:</label>
                <input
                  id="username"
                  ref={usernameInputRef}
                  className="term-input"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  onKeyDown={handleUsernameKeyDown}
                  disabled={step !== STEP.LOGIN}
                  autoComplete="username"
                  spellCheck="false"
                  autoCapitalize="off"
                  autoFocus
                />
              </div>

              {(step === STEP.PASSWORD || step === STEP.CONFIRM || step === STEP.LOADING) && (
                <div className="term-row">
                  <label htmlFor="password">Password:</label>
                  <input
                    id="password"
                    ref={passwordInputRef}
                    className="term-input term-input--silent"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onKeyDown={handlePasswordKeyDown}
                    disabled={step !== STEP.PASSWORD}
                    autoComplete="current-password"
                  />
                </div>
              )}

              {step === STEP.CONFIRM && (
                <div className="term-confirm-row">
                  <button
                    ref={confirmBtnRef}
                    type="button"
                    className="term-submit"
                    onClick={handleAuthenticate}
                    onKeyDown={handleConfirmKeyDown}
                  >
                    <span aria-hidden="true">›</span>
                    <span className="term-submit__label">press ENTER to sign in</span>
                    <span className="term-submit__cursor" aria-hidden="true"></span>
                  </button>
                </div>
              )}

              {step === STEP.LOADING && (
                <p className="term-progress" aria-live="polite">
                  {`Authenticating [${progressBar}] ${progress}%`}
                </p>
              )}

              {step === STEP.DONE && (
                <div className="term-result" aria-live="polite">
                  {lastLoginStamp && (
                    <p className="term-line term-line--dim">
                      {`Last login: ${lastLoginStamp} from ${lastLoginIp}`}
                    </p>
                  )}
                  <p className="term-line term-line--prompt">
                    {username}@dev-com:~$
                    <span className="term-cursor-block" aria-hidden="true"></span>
                  </p>
                </div>
              )}

              {step === STEP.FAILED && (
                <p className="term-line" role="alert">{failMessage}</p>
              )}
            </div>
          </div>

          {error && (
            <p className="auth-error" role="alert">
              {error}
            </p>
          )}

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
            <Link to="/forgot-password">
              비밀번호를 잊으셨나요?
            </Link>
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
