import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

function AdminRoute() {
  const { isAuthenticated, isAdmin } = useAuth()

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/401"
        replace
        state={{ message: '로그인이 필요한 서비스입니다.' }}
      />
    )
  }

  if (!isAdmin) {
    return <Navigate to="/403" replace />
  }

  return <Outlet />
}

export default AdminRoute
