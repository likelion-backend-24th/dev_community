import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signup, checkUsername, checkNickname } from '../../api/authApi'
import AuthHeader from '../../components/layout/AuthHeader'
import hideIcon from '../../assets/hide.png'
import showIcon from '../../assets/show.png'
import '../../styles/auth.css'

const IDLE = { status: 'idle', message: '' }

function EyeIcon({ open }) {
  return (
    <img
      src={open ? showIcon : hideIcon}
      alt=""
      width="18"
      height="18"
      aria-hidden="true"
    />
  )
}

function SignupPage() {
  const navigate = useNavigate()

  const [form, setForm] = useState({
    username: '',
    password: '',
    passwordConfirm: '',
    nickname: '',
  })
  const [usernameCheck, setUsernameCheck] = useState(IDLE)
  const [nicknameCheck, setNicknameCheck] = useState(IDLE)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
    if (name === 'username') setUsernameCheck(IDLE)
    if (name === 'nickname') setNicknameCheck(IDLE)
  }

  const handleCheckUsername = async () => {
    if (!form.username.trim()) return
    setUsernameCheck({ status: 'checking', message: '' })
    try {
      const res = await checkUsername(form.username)
      setUsernameCheck({ status: 'available', message: res.message })
    } catch (err) {
      setUsernameCheck({
        status: 'unavailable',
        message: err.response?.data?.message ?? '이미 사용중인 아이디입니다.',
      })
    }
  }

  const handleCheckNickname = async () => {
    if (!form.nickname.trim()) return
    setNicknameCheck({ status: 'checking', message: '' })
    try {
      const res = await checkNickname(form.nickname)
      setNicknameCheck({ status: 'available', message: res.message })
    } catch (err) {
      setNicknameCheck({
        status: 'unavailable',
        message: err.response?.data?.message ?? '이미 사용중인 닉네임입니다.',
      })
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (form.password.length < 8 || form.password.length > 64) {
      setError('비밀번호는 8자 이상, 64자 이하여야 합니다.')
      return
    }
    if (form.password !== form.passwordConfirm) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }

    setSubmitting(true)
    try {
      await signup(form)
      navigate('/login', { state: { signupSuccess: true } })
    } catch (err) {
      setError(err.response?.data?.message ?? '회원가입에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <AuthHeader />

      <main className="auth-main">
        <div className="auth-card">
          <h1 className="auth-card__title">Dev_Community에 합류하기</h1>
          <p className="auth-card__subtitle">
            질문하고 답변하며 함께 성장하는 커뮤니티예요.
          </p>

          <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <div className="field">
              <label className="field__label" htmlFor="username">
                아이디
              </label>
              <div className="field__row">
                <input
                  id="username"
                  name="username"
                  className={`field__input${usernameCheck.status === 'unavailable' ? ' is-error' : ''}`}
                  value={form.username}
                  onChange={handleChange}
                  autoComplete="username"
                  required
                />
                <button
                  type="button"
                  className="field__check-btn"
                  onClick={handleCheckUsername}
                  disabled={usernameCheck.status === 'checking'}
                >
                  중복 확인
                </button>
              </div>
              {usernameCheck.message && (
                <p className={`field__hint is-${usernameCheck.status}`}>
                  {usernameCheck.message}
                </p>
              )}
            </div>

            <div className="field">
              <label className="field__label" htmlFor="nickname">
                닉네임
              </label>
              <div className="field__row">
                <input
                  id="nickname"
                  name="nickname"
                  className={`field__input${nicknameCheck.status === 'unavailable' ? ' is-error' : ''}`}
                  value={form.nickname}
                  onChange={handleChange}
                  autoComplete="nickname"
                  required
                />
                <button
                  type="button"
                  className="field__check-btn"
                  onClick={handleCheckNickname}
                  disabled={nicknameCheck.status === 'checking'}
                >
                  중복 확인
                </button>
              </div>
              {nicknameCheck.message && (
                <p className={`field__hint is-${nicknameCheck.status}`}>
                  {nicknameCheck.message}
                </p>
              )}
            </div>

            <div className="field">
              <label className="field__label" htmlFor="password">
                비밀번호
              </label>
              <div className="field__password-row">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  className="field__input"
                  value={form.password}
                  onChange={handleChange}
                  autoComplete="new-password"
                  required
                />
                <button
                  type="button"
                  className="field__toggle-visibility"
                  onClick={() => setShowPassword((v) => !v)}
                  aria-label={showPassword ? '비밀번호 숨기기' : '비밀번호 보기'}
                  aria-pressed={showPassword}
                >
                  <EyeIcon open={showPassword} />
                </button>
              </div>
            </div>

            <div className="field">
              <label className="field__label" htmlFor="passwordConfirm">
                비밀번호 확인
              </label>
              <div className="field__password-row">
                <input
                  id="passwordConfirm"
                  name="passwordConfirm"
                  type={showPasswordConfirm ? 'text' : 'password'}
                  className="field__input"
                  value={form.passwordConfirm}
                  onChange={handleChange}
                  autoComplete="new-password"
                  required
                />
                <button
                  type="button"
                  className="field__toggle-visibility"
                  onClick={() => setShowPasswordConfirm((v) => !v)}
                  aria-label={showPasswordConfirm ? '비밀번호 숨기기' : '비밀번호 보기'}
                  aria-pressed={showPasswordConfirm}
                >
                  <EyeIcon open={showPasswordConfirm} />
                </button>
              </div>
            </div>

            {error && (
              <p className="auth-error" role="alert">
                {error}
              </p>
            )}

            <button
              className="auth-submit"
              type="submit"
              disabled={
                submitting ||
                usernameCheck.status !== 'available' ||
                nicknameCheck.status !== 'available'
              }
            >
              {submitting ? '가입 중...' : '회원가입'}
            </button>
          </form>

          <div className="auth-links">
            <p className="auth-links__muted">
              이미 계정이 있으신가요?
              <Link to="/login">로그인</Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  )
}

export default SignupPage
