import { Link, useLocation } from 'react-router-dom'
import '../../styles/auth.css'

function AuthHeader() {
  const location = useLocation()
  const isSignup = location.pathname === '/signup'

  return (
    <header className="auth-header">
      <Link to="/login" className="auth-header__brand">
        <span className="navbar__brand-arrow" aria-hidden="true">&gt;</span>
        <span>
          dev_com
          <span className="navbar__brand-caret">_</span>
        </span>
      </Link>
      <p className="auth-header__label">{isSignup ? 'SIGN UP' : 'LOGIN'}</p>
    </header>
  )
}

export default AuthHeader
