import { Link, useLocation } from 'react-router-dom'
import '../../styles/error.css'

function UnauthorizedPage() {
  const location = useLocation()
  const message = location.state?.message ?? '로그인이 필요합니다.'

  return (
    <div className="error-page">
      <p className="error-page__code">401</p>
      <h1 className="error-page__title">로그인이 필요합니다</h1>
      <p className="error-page__desc">{message}</p>
      <div className="error-page__actions">
        <Link to="/login" className="btn btn-primary">
          로그인하기
        </Link>
        <Link to="/questions" className="btn btn-ghost">
          목록으로
        </Link>
      </div>
    </div>
  )
}

export default UnauthorizedPage
