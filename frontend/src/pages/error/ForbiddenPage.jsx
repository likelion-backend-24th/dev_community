import { Link } from 'react-router-dom'

function ForbiddenPage() {
  return (
    <div>
      <h1>403 - 접근 권한이 없습니다</h1>
      <p>요청하신 작업을 수행할 권한이 없습니다.</p>
      <Link to="/questions">목록으로</Link>
    </div>
  )
}

export default ForbiddenPage
