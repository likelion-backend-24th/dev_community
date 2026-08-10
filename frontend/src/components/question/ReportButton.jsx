import { useState } from 'react'
import { createReport } from '../../api/reportApi'
import '../../styles/question.css'

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
    return <span className="report-widget__done">신고 접수됨</span>
  }

  if (!open) {
    return (
      <button type="button" className="btn btn-ghost btn-sm" onClick={() => setOpen(true)}>
        신고
      </button>
    )
  }

  return (
    <span className="report-widget">
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          className="input"
          placeholder="신고 사유"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          required
        />
        <button type="submit" className="btn btn-destructive btn-sm" disabled={submitting}>
          제출
        </button>
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setOpen(false)}>
          취소
        </button>
      </form>
      {error && (
        <span className="inline-error" role="alert">
          {error}
        </span>
      )}
    </span>
  )
}

export default ReportButton
