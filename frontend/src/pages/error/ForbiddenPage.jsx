import { Link } from 'react-router-dom'
import '../../styles/error.css'

function ForbiddenPage() {
  return (
    <div className="error-page">
      <p className="error-page__code">403</p>
      <h1 className="error-page__title">접근 권한이 없습니다</h1>
      <p className="error-page__desc">요청하신 작업을 수행할 권한이 없습니다.</p>
      <div className="error-page__actions">
        <Link to="/questions" className="btn btn-primary">
          목록으로
        </Link>
      </div>
    </div>
  )
}

export default ForbiddenPage
