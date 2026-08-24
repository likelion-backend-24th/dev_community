import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import LoginPage from './LoginPage'
import { login } from '../../api/authApi'

vi.mock('../../api/authApi')

const mockSetAuth = vi.fn()
vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({ login: mockSetAuth }),
}))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

function renderPage(initialEntries = ['/login']) {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      <LoginPage />
    </MemoryRouter>,
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('logs in and navigates to /questions on success', async () => {
    const user = userEvent.setup()
    login.mockResolvedValue({ accessToken: 'token-abc' })
    renderPage()

    await user.type(screen.getByLabelText('아이디'), 'leocho')
    await user.type(screen.getByLabelText('비밀번호'), 'password123')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith({ username: 'leocho', password: 'password123' })
      expect(mockSetAuth).toHaveBeenCalledWith('token-abc')
      expect(mockNavigate).toHaveBeenCalledWith('/questions')
    })
  })

  it('shows the server error message when login fails', async () => {
    const user = userEvent.setup()
    login.mockRejectedValue({ response: { data: { message: '아이디 또는 비밀번호가 일치하지 않습니다.' } } })
    renderPage()

    await user.type(screen.getByLabelText('아이디'), 'leocho')
    await user.type(screen.getByLabelText('비밀번호'), 'wrongpass')
    await user.click(screen.getByRole('button', { name: '로그인' }))

    expect(await screen.findByText('아이디 또는 비밀번호가 일치하지 않습니다.')).toBeInTheDocument()
    expect(mockNavigate).not.toHaveBeenCalled()
  })

  it('maps an OAuth error query param to a Korean message', async () => {
    renderPage(['/login?error=SUSPENDED_ACCOUNT'])
    expect(await screen.findByText('정지된 계정입니다.')).toBeInTheDocument()
  })

  it('falls back to a generic message for an unknown OAuth error code', async () => {
    renderPage(['/login?error=SOME_UNKNOWN_CODE'])
    expect(await screen.findByText('로그인에 실패했습니다.')).toBeInTheDocument()
  })

  it('redirects to the GitHub OAuth authorize endpoint on button click', async () => {
    const user = userEvent.setup()
    delete window.location
    window.location = { href: '' }
    renderPage()

    await user.click(screen.getByRole('button', { name: /GitHub로 로그인/ }))
    expect(window.location.href).toContain('/oauth2/authorization/github')
  })
})
