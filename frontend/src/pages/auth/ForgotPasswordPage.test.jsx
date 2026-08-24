import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ForgotPasswordPage from './ForgotPasswordPage'
import { requestPasswordReset } from '../../api/authApi'

vi.mock('../../api/authApi')

function renderPage() {
  return render(
    <MemoryRouter>
      <ForgotPasswordPage />
    </MemoryRouter>,
  )
}

describe('ForgotPasswordPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('requires both username and email before submitting', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.click(screen.getByRole('button', { name: '재설정 링크 보내기' }))

    expect(await screen.findByText('아이디와 이메일을 모두 입력해주세요.')).toBeInTheDocument()
    expect(requestPasswordReset).not.toHaveBeenCalled()
  })

  it('shows the confirmation message after a successful request', async () => {
    const user = userEvent.setup()
    requestPasswordReset.mockResolvedValue({})
    renderPage()

    await user.type(screen.getByLabelText('아이디'), 'leocho')
    await user.type(screen.getByLabelText('이메일'), 'leo@example.com')
    await user.click(screen.getByRole('button', { name: '재설정 링크 보내기' }))

    expect(requestPasswordReset).toHaveBeenCalledWith('leocho', 'leo@example.com')
    expect(await screen.findByText(/재설정 링크를 보냈습니다/)).toBeInTheDocument()
  })

  it('shows the server error message when the request fails', async () => {
    const user = userEvent.setup()
    requestPasswordReset.mockRejectedValue({ response: { data: { message: '요청이 너무 많습니다.' } } })
    renderPage()

    await user.type(screen.getByLabelText('아이디'), 'leocho')
    await user.type(screen.getByLabelText('이메일'), 'leo@example.com')
    await user.click(screen.getByRole('button', { name: '재설정 링크 보내기' }))

    expect(await screen.findByText('요청이 너무 많습니다.')).toBeInTheDocument()
  })
})
