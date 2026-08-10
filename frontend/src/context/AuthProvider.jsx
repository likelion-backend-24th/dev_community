import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AuthContext from './AuthContext'
import client from '../api/client'

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() =>
    localStorage.getItem('accessToken'),
  )
  const navigate = useNavigate()

  const login = (token) => {
    localStorage.setItem('accessToken', token)
    setAccessToken(token)
  }

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken')
    setAccessToken(null)
  }, [])

  useEffect(() => {
    const interceptorId = client.interceptors.response.use(
      (response) => response,
      (error) => {
        const status = error.response?.status
        const url = error.config?.url ?? ''

        if (status === 401 && !url.includes('/api/auth/login')) {
          logout()
          navigate('/401', {
            state: { message: '로그인이 필요합니다. 다시 로그인해주세요.' },
          })
        } else if (status === 403) {
          navigate('/403')
        }

        return Promise.reject(error)
      },
    )

    return () => client.interceptors.response.eject(interceptorId)
  }, [logout, navigate])

  const value = {
    accessToken,
    isAuthenticated: Boolean(accessToken),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
