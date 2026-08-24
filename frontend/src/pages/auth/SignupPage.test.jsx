import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import SignupPage from './SignupPage'
import { signup, checkUsername, checkNickname, checkEmail } from '../../api/authApi'

vi.mock('../../api/authApi')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

function renderPage() {
  return render(
    <MemoryRouter>
      <SignupPage />
    </MemoryRouter>,
  )
}

async function fillAndVerifyAllFields(user) {
  checkUsername.mockResolvedValue({ message: '사용 가능한 아이디입니다.' })
  checkNickname.mockResolvedValue({ message: '사용 가능한 닉네임입니다.' })
  checkEmail.mockResolvedValue({ message: '사용 가능한 이메일입니다.' })

  await user.type(screen.getByLabelText('아이디'), 'leocho')
  await user.click(screen.getAllByRole('button', { name: '중복 확인' })[0])
  await user.type(screen.getByLabelText('닉네임'), '레오')
  await user.click(screen.getAllByRole('button', { name: '중복 확인' })[1])
  await user.type(screen.getByLabelText('이메일'), 'leo@example.com')
  await user.click(screen.getAllByRole('button', { name: '중복 확인' })[2])

  await waitFor(() => {
    expect(screen.getByText('사용 가능한 아이디입니다.')).toBeInTheDocument()
    expect(screen.getByText('사용 가능한 닉네임입니다.')).toBeInTheDocument()
    expect(screen.getByText('사용 가능한 이메일입니다.')).toBeInTheDocument()
  })
}

describe('SignupPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows an error message when a duplicate-checked username is unavailable', async () => {
    const user = userEvent.setup()
    checkUsername.mockRejectedValue({ response: { data: { message: '이미 사용중인 아이디입니다.' } } })
    renderPage()

    await user.type(screen.getByLabelText('아이디'), 'taken')
    await user.click(screen.getAllByRole('button', { name: '중복 확인' })[0])

    expect(await screen.findByText('이미 사용중인 아이디입니다.')).toBeInTheDocument()
  })

  it('keeps the submit button disabled until every duplicate check passes and terms are agreed', async () => {
    const user = userEvent.setup()
    renderPage()

    const submitButton = screen.getByRole('button', { name: '회원가입' })
    expect(submitButton).toBeDisabled()

    await fillAndVerifyAllFields(user)
    expect(submitButton).toBeDisabled()

    await user.click(screen.getByRole('checkbox'))
    expect(submitButton).toBeEnabled()
  })

  it('rejects submission when the password is shorter than 8 characters', async () => {
    const user = userEvent.setup()
    renderPage()
    await fillAndVerifyAllFields(user)
    await user.click(screen.getByRole('checkbox'))

    await user.type(screen.getByLabelText('비밀번호'), 'short1')
    await user.type(screen.getByLabelText('비밀번호 확인'), 'short1')
    await user.click(screen.getByRole('button', { name: '회원가입' }))

    expect(await screen.findByText('비밀번호는 8자 이상, 64자 이하여야 합니다.')).toBeInTheDocument()
    expect(signup).not.toHaveBeenCalled()
  })

  it('rejects submission when password and confirmation do not match', async () => {
    const user = userEvent.setup()
    renderPage()
    await fillAndVerifyAllFields(user)
    await user.click(screen.getByRole('checkbox'))

    await user.type(screen.getByLabelText('비밀번호'), 'password123')
    await user.type(screen.getByLabelText('비밀번호 확인'), 'password456')
    await user.click(screen.getByRole('button', { name: '회원가입' }))

    expect(await screen.findByText('비밀번호가 일치하지 않습니다.')).toBeInTheDocument()
    expect(signup).not.toHaveBeenCalled()
  })

  it('submits the form and redirects to /login on success', async () => {
    const user = userEvent.setup()
    signup.mockResolvedValue({})
    renderPage()
    await fillAndVerifyAllFields(user)
    await user.click(screen.getByRole('checkbox'))

    await user.type(screen.getByLabelText('비밀번호'), 'password123')
    await user.type(screen.getByLabelText('비밀번호 확인'), 'password123')
    await user.click(screen.getByRole('button', { name: '회원가입' }))

    await waitFor(() => {
      expect(signup).toHaveBeenCalledWith(
        expect.objectContaining({
          username: 'leocho',
          nickname: '레오',
          email: 'leo@example.com',
          password: 'password123',
        }),
      )
      expect(mockNavigate).toHaveBeenCalledWith('/login', { state: { signupSuccess: true } })
    })
  })

  it('shows the server error message when signup fails', async () => {
    const user = userEvent.setup()
    signup.mockRejectedValue({ response: { data: { message: '서버 오류가 발생했습니다.' } } })
    renderPage()
    await fillAndVerifyAllFields(user)
    await user.click(screen.getByRole('checkbox'))

    await user.type(screen.getByLabelText('비밀번호'), 'password123')
    await user.type(screen.getByLabelText('비밀번호 확인'), 'password123')
    await user.click(screen.getByRole('button', { name: '회원가입' }))

    expect(await screen.findByText('서버 오류가 발생했습니다.')).toBeInTheDocument()
    expect(mockNavigate).not.toHaveBeenCalled()
  })
})
