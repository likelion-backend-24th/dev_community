import { useState } from 'react'
import { createReport } from '../../api/reportApi'

function ReportButton({ targetType, targetId }) {
  const [open, setOpen] = useState(false)
  const [reason, setReason] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [done, setDone] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await createReport({ targetType, targetId, reason })
      setDone(true)
      setOpen(false)
    } catch (err) {
      setError(err.response?.data?.message ?? '신고 접수에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  if (done) {
    return <span>신고 접수됨</span>
  }

  if (!open) {
    return (
      <button type="button" onClick={() => setOpen(true)}>
        신고
      </button>
    )
  }

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="신고 사유"
        value={reason}
        onChange={(e) => setReason(e.target.value)}
        required
      />
      <button type="submit" disabled={submitting}>
        제출
      </button>
      <button type="button" onClick={() => setOpen(false)}>
        취소
      </button>
      {error && <span role="alert">{error}</span>}
    </form>
  )
}

export default ReportButton
