import { Link } from 'react-router-dom'
import '../../styles/error.css'

function NotFoundPage() {
  return (
    <div className="error-page">
      <p className="error-page__code">404</p>
      <h1 className="error-page__title">페이지를 찾을 수 없습니다</h1>
      <p className="error-page__desc">주소가 바뀌었거나 삭제된 페이지예요.</p>
      <div className="error-page__actions">
        <Link to="/questions" className="btn btn-primary">
          질문 목록으로
        </Link>
      </div>
    </div>
  )
}

export default NotFoundPage
