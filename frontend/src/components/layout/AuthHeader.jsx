import { Link, useLocation } from 'react-router-dom'
import '../../styles/auth.css'

function AuthHeader() {
  const location = useLocation()
  const isSignup = location.pathname === '/signup'

  return (
    <header className="auth-header">
      <Link to="/login" className="auth-header__brand">Dev_Community</Link>
      <p className="auth-header__label">{isSignup ? 'SIGN UP' : 'LOGIN'}</p>
    </header>
  )
}

export default AuthHeader
