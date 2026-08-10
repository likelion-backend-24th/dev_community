import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signup, checkUsername, checkNickname } from '../../api/authApi'
import AuthHeader from '../../components/layout/AuthHeader'

const IDLE = { status: 'idle', message: '' }

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
      navigate('/login')
    } catch (err) {
      setError(err.response?.data?.message ?? '회원가입에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <AuthHeader />
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="username">아이디</label>
          <input
            id="username"
            name="username"
            value={form.username}
            onChange={handleChange}
            required
          />
          <button
            type="button"
            onClick={handleCheckUsername}
            disabled={usernameCheck.status === 'checking'}
          >
            중복 확인
          </button>
          {usernameCheck.message && <p>{usernameCheck.message}</p>}
        </div>

        <div>
          <label htmlFor="nickname">닉네임</label>
          <input
            id="nickname"
            name="nickname"
            value={form.nickname}
            onChange={handleChange}
            required
          />
          <button
            type="button"
            onClick={handleCheckNickname}
            disabled={nicknameCheck.status === 'checking'}
          >
            중복 확인
          </button>
          {nicknameCheck.message && <p>{nicknameCheck.message}</p>}
        </div>

        <div>
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
          />
        </div>

        <div>
          <label htmlFor="passwordConfirm">비밀번호 확인</label>
          <input
            id="passwordConfirm"
            name="passwordConfirm"
            type="password"
            value={form.passwordConfirm}
            onChange={handleChange}
            required
          />
        </div>

        {error && <p role="alert">{error}</p>}
        <button
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
      <p>
        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
      </p>
    </div>
  )
}

export default SignupPage
