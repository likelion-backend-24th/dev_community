import { Link, useLocation } from 'react-router-dom'

function AuthHeader() {
  const location = useLocation()
  const isSignup = location.pathname === '/signup'

  return (
    <header>
      <Link to="/login">Dev_Community</Link>
      <p>{isSignup ? 'SIGN UP' : 'LOGIN'}</p>
    </header>
  )
}

export default AuthHeader
