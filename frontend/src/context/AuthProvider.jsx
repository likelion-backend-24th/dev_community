import { useState } from 'react'
import AuthContext from './AuthContext'

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() =>
    localStorage.getItem('accessToken'),
  )

  const login = (token) => {
    localStorage.setItem('accessToken', token)
    setAccessToken(token)
  }

  const logout = () => {
    localStorage.removeItem('accessToken')
    setAccessToken(null)
  }

  const value = {
    accessToken,
    isAuthenticated: Boolean(accessToken),
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
