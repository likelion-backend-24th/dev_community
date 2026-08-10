import { Link, useLocation } from 'react-router-dom'

function UnauthorizedPage() {
  const location = useLocation()
  const message = location.state?.message ?? '로그인이 필요합니다.'

  return (
    <div>
      <h1>401 - 로그인이 필요합니다</h1>
      <p>{message}</p>
      <Link to="/login">로그인하기</Link>
      <Link to="/questions">목록으로</Link>
    </div>
  )
}

export default UnauthorizedPage
