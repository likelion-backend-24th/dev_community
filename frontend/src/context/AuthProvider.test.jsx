import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider } from './AuthProvider'
import { useAuth } from '../hooks/useAuth'
import client, { refreshAccessToken } from '../api/client'
import { setAccessToken } from '../api/tokenStore'

vi.mock('../api/client', async () => {
  const actual = await vi.importActual('../api/client')
  return { ...actual, refreshAccessToken: vi.fn() }
})

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

function makeToken(payload) {
  const encode = (obj) => {
    const bytes = new TextEncoder().encode(JSON.stringify(obj))
    const binary = Array.from(bytes, (b) => String.fromCharCode(b)).join('')
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
  }
  return `${encode({ alg: 'HS256' })}.${encode(payload)}.sig`
}

function Probe() {
  const { isAuthenticated, user, isAdmin } = useAuth()
  return (
    <div>
      <span data-testid="authenticated">{String(isAuthenticated)}</span>
      <span data-testid="user">{user ? `${user.username}/${user.nickname}` : 'none'}</span>
      <span data-testid="admin">{String(isAdmin)}</span>
    </div>
  )
}

function renderWithProvider() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <Probe />
      </AuthProvider>
    </MemoryRouter>,
  )
}

describe('AuthProvider', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  afterEach(() => {
    localStorage.clear()
  })

  it('reports unauthenticated with no user when there is no stored token', () => {
    renderWithProvider()
    expect(screen.getByTestId('authenticated')).toHaveTextContent('false')
    expect(screen.getByTestId('user')).toHaveTextContent('none')
  })

  it('derives the user and admin flag from a stored token', () => {
    setAccessToken(
      makeToken({ sub: '7', username: 'leocho', nickname: '레오', roles: ['USER', 'ADMIN'] }),
    )
    renderWithProvider()

    expect(screen.getByTestId('authenticated')).toHaveTextContent('true')
    expect(screen.getByTestId('user')).toHaveTextContent('leocho/레오')
    expect(screen.getByTestId('admin')).toHaveTextContent('true')
  })

  it('updates reactively when the token changes after mount', async () => {
    renderWithProvider()
    expect(screen.getByTestId('authenticated')).toHaveTextContent('false')

    setAccessToken(makeToken({ sub: '1', username: 'a', nickname: 'A', roles: [] }))

    await waitFor(() => {
      expect(screen.getByTestId('authenticated')).toHaveTextContent('true')
    })
  })

  it('redirects to /login and clears the token on a 401 that survives refresh', async () => {
    setAccessToken('existing-token')
    refreshAccessToken.mockRejectedValue(new Error('refresh failed'))
    const useSpy = vi.spyOn(client.interceptors.response, 'use')

    renderWithProvider()

    const errorHandler = useSpy.mock.calls.at(-1)[1]
    const error = {
      response: { status: 401 },
      config: { url: '/api/questions', method: 'get' },
    }

    await expect(errorHandler(error)).rejects.toBe(error)

    expect(refreshAccessToken).toHaveBeenCalled()
    expect(mockNavigate).toHaveBeenCalledWith('/login', {
      state: { message: '로그인이 필요합니다. 다시 로그인해주세요.' },
    })
    expect(localStorage.getItem('accessToken')).toBeNull()
  })

  it('does not attempt refresh/logout for 401s from the login endpoint itself', async () => {
    const useSpy = vi.spyOn(client.interceptors.response, 'use')
    renderWithProvider()

    const errorHandler = useSpy.mock.calls.at(-1)[1]
    const error = {
      response: { status: 401 },
      config: { url: '/api/auth/login', method: 'post' },
    }

    await expect(errorHandler(error)).rejects.toBe(error)
    expect(refreshAccessToken).not.toHaveBeenCalled()
    expect(mockNavigate).not.toHaveBeenCalled()
  })

  it('redirects to /membership on a 403 from the premium questions endpoint', async () => {
    const useSpy = vi.spyOn(client.interceptors.response, 'use')
    renderWithProvider()

    const errorHandler = useSpy.mock.calls.at(-1)[1]
    const error = {
      response: { status: 403 },
      config: { url: '/api/questions/premium', method: 'get' },
    }

    await expect(errorHandler(error)).rejects.toBe(error)
    expect(mockNavigate).toHaveBeenCalledWith('/membership', {
      state: { message: '멤버십 가입 후 이용할 수 있어요.' },
    })
  })

  it('redirects to /403 on any other 403', async () => {
    const useSpy = vi.spyOn(client.interceptors.response, 'use')
    renderWithProvider()

    const errorHandler = useSpy.mock.calls.at(-1)[1]
    const error = {
      response: { status: 403 },
      config: { url: '/api/admin/stats', method: 'get' },
    }

    await expect(errorHandler(error)).rejects.toBe(error)
    expect(mockNavigate).toHaveBeenCalledWith('/403')
  })

  it('does not log out on a 401 from the password-check endpoint (wrong current password)', async () => {
    setAccessToken('existing-token')
    const useSpy = vi.spyOn(client.interceptors.response, 'use')
    renderWithProvider()

    const errorHandler = useSpy.mock.calls.at(-1)[1]
    const error = {
      response: { status: 401 },
      config: { url: '/api/members/me/password', method: 'put' },
    }

    await expect(errorHandler(error)).rejects.toBe(error)
    expect(refreshAccessToken).not.toHaveBeenCalled()
    expect(mockNavigate).not.toHaveBeenCalled()
    expect(localStorage.getItem('accessToken')).toBe('existing-token')
  })
})
