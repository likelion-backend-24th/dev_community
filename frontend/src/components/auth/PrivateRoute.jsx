import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

function PrivateRoute() {
  const { isAuthenticated } = useAuth()

  return isAuthenticated ? (
    <Outlet />
  ) : (
    <Navigate
      to="/401"
      replace
      state={{ message: '로그인이 필요한 서비스입니다.' }}
    />
  )
}

export default PrivateRoute
