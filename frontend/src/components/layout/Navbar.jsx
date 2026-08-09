import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

function Navbar() {
  const { isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <nav>
      <Link to="/">질문 목록</Link>
      {isAuthenticated ? (
        <>
          <Link to="/questions/new">질문 작성</Link>
          <Link to="/mypage">마이페이지</Link>
          <button type="button" onClick={handleLogout}>
            로그아웃
          </button>
        </>
      ) : (
        <>
          <Link to="/login">로그인</Link>
          <Link to="/signup">회원가입</Link>
        </>
      )}
    </nav>
  )
}

export default Navbar
