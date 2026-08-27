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

// 터미널 UI는 아이디 -> Enter -> 비밀번호 -> Enter -> 확인 프롬프트 순으로 단계가 열린다.
async function fillTerminalLogin(user, username, password) {
  await user.type(screen.getByLabelText('dev-com login:'), `${username}{Enter}`)
  await user.type(screen.getByLabelText('Password:'), `${password}{Enter}`)
  await user.click(screen.getByRole('button', { name: /press ENTER to sign in/ }))
}

// 로딩바(MIN_LOADING_MS)와 결과 표시 대기(SUCCESS_HOLD_MS/FAIL_HOLD_MS)를 합쳐 최대 약 2.6초가 걸린다.
const TERMINAL_TIMEOUT = 6000

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('logs in and navigates to /questions on success', async () => {
    const user = userEvent.setup()
    login.mockResolvedValue({ accessToken: 'token-abc' })
    renderPage()

    await fillTerminalLogin(user, 'leocho', 'password123')

    expect(login).toHaveBeenCalledWith({ username: 'leocho', password: 'password123' })

    await waitFor(
      () => {
        expect(mockSetAuth).toHaveBeenCalledWith('token-abc')
        expect(mockNavigate).toHaveBeenCalledWith('/questions')
      },
      { timeout: TERMINAL_TIMEOUT },
    )
  }, 10000)

  it('shows the server error message when login fails', async () => {
    const user = userEvent.setup()
    login.mockRejectedValue({ response: { data: { message: '아이디 또는 비밀번호가 일치하지 않습니다.' } } })
    renderPage()

    await fillTerminalLogin(user, 'leocho', 'wrongpass')

    expect(
      await screen.findByText('아이디 또는 비밀번호가 일치하지 않습니다.', undefined, {
        timeout: TERMINAL_TIMEOUT,
      }),
    ).toBeInTheDocument()
    expect(mockNavigate).not.toHaveBeenCalled()
  }, 10000)

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
