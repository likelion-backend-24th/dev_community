import { useState } from "react";
import { useNavigate, useSearchParams, Navigate } from "react-router-dom";
import { confirmPasswordReset } from "../../api/authApi";
import AuthHeader from "../../components/layout/AuthHeader";
import "../../styles/auth.css";

function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [newPassword, setNewPassword] = useState("");
  const [newPasswordConfirm, setNewPasswordConfirm] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (!token) {
    return <Navigate to="/forgot-password" replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (newPassword.length < 8 || newPassword.length > 64) {
      setError("비밀번호는 8자 이상, 64자 이하여야 합니다.");
      return;
    }
    if (newPassword !== newPasswordConfirm) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      await confirmPasswordReset(token, newPassword);
      navigate("/login", { state: { passwordResetSuccess: true } });
    } catch (err) {
      setError(err.response?.data?.message ?? "비밀번호 재설정에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <AuthHeader />
      <main className="auth-main">
        <div className="auth-card">
          <h1 className="auth-card__title">새 비밀번호 설정</h1>
          <p className="auth-card__subtitle">새로 사용할 비밀번호를 입력해주세요.</p>

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label className="field__label" htmlFor="newPassword">
                새 비밀번호
              </label>
              <input
                id="newPassword"
                type="password"
                className="field__input"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                autoComplete="new-password"
                autoFocus
                required
              />
            </div>

            <div className="field">
              <label className="field__label" htmlFor="newPasswordConfirm">
                새 비밀번호 확인
              </label>
              <input
                id="newPasswordConfirm"
                type="password"
                className="field__input"
                value={newPasswordConfirm}
                onChange={(e) => setNewPasswordConfirm(e.target.value)}
                autoComplete="new-password"
                required
              />
            </div>

            {error && (
              <p className="auth-error" role="alert">
                {error}
              </p>
            )}

            <button className="auth-submit" type="submit" disabled={submitting}>
              {submitting ? "처리 중..." : "비밀번호 재설정"}
            </button>
          </form>
        </div>
      </main>
    </div>
  );
}

export default ResetPasswordPage;
