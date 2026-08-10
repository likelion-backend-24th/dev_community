import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getReports, processReport } from '../../api/adminApi'
import { getAnswer } from '../../api/answerApi'

const STATUS_LABEL = {
  PENDING: '대기중',
  RESOLVED: '정당함',
  REJECTED: '부당함',
}

const PAGE_SIZE = 10

function AdminReportsPage() {
  const [reports, setReports] = useState([])
  const [meta, setMeta] = useState({ page: 0, totalPages: 0, totalElements: 0 })
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState('PENDING')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [processingId, setProcessingId] = useState(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false

    const fetchReports = async () => {
      setLoading(true)
      setError('')
      try {
        const { content, meta: resMeta } = await getReports({
          status: status || undefined,
          page,
          size: PAGE_SIZE,
        })
        const withLinks = await Promise.all(
          content.map(async (r) => {
            if (r.targetType === 'QUESTION') {
              return { ...r, linkQuestionId: r.targetId }
            }
            try {
              const answer = await getAnswer(r.targetId)
              return { ...r, linkQuestionId: answer.questionId }
            } catch {
              return { ...r, linkQuestionId: null }
            }
          }),
        )
        if (cancelled) return
        setReports(withLinks)
        setMeta(resMeta)
      } catch (err) {
        if (cancelled) return
        setError(err.response?.data?.message ?? '신고 목록을 불러오지 못했습니다.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    fetchReports()
    return () => {
      cancelled = true
    }
  }, [status, page, reloadKey])

  const handleProcess = async (id, newStatus) => {
    setProcessingId(id)
    setError('')
    try {
      await processReport(id, newStatus)
      setReloadKey((k) => k + 1)
    } catch (err) {
      setError(err.response?.data?.message ?? '신고 처리에 실패했습니다.')
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <div>
      <h1>신고 관리</h1>

      <div>
        <label htmlFor="status">상태</label>
        <select
          id="status"
          value={status}
          onChange={(e) => {
            setPage(0)
            setStatus(e.target.value)
          }}
        >
          <option value="">전체</option>
          <option value="PENDING">대기중</option>
          <option value="RESOLVED">정당함</option>
          <option value="REJECTED">부당함</option>
        </select>
      </div>

      {loading && <p>불러오는 중...</p>}
      {error && <p role="alert">{error}</p>}

      {!loading && !error && reports.length === 0 && <p>신고가 없습니다.</p>}

      {!loading && !error && reports.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>대상</th>
              <th>신고자</th>
              <th>피신고자</th>
              <th>사유</th>
              <th>상태</th>
              <th>신고일</th>
              <th>처리</th>
            </tr>
          </thead>
          <tbody>
            {reports.map((r) => (
              <tr key={r.id}>
                <td>
                  {r.linkQuestionId ? (
                    <Link to={`/questions/${r.linkQuestionId}`}>
                      {r.targetType} #{r.targetId}
                    </Link>
                  ) : (
                    <>
                      {r.targetType} #{r.targetId}
                    </>
                  )}
                </td>
                <td>{r.reporterNickname}</td>
                <td>{r.targetUserNickname}</td>
                <td>{r.reason}</td>
                <td>{STATUS_LABEL[r.status] ?? r.status}</td>
                <td>{new Date(r.createdAt).toLocaleString()}</td>
                <td>
                  {r.status === 'PENDING' ? (
                    <>
                      <button
                        type="button"
                        disabled={processingId === r.id}
                        onClick={() => handleProcess(r.id, 'RESOLVED')}
                      >
                        정당함
                      </button>
                      <button
                        type="button"
                        disabled={processingId === r.id}
                        onClick={() => handleProcess(r.id, 'REJECTED')}
                      >
                        부당함
                      </button>
                    </>
                  ) : (
                    '-'
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {meta.totalPages > 0 && (
        <div>
          <button
            type="button"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            이전
          </button>
          <span>
            {page + 1} / {meta.totalPages}
          </span>
          <button
            type="button"
            onClick={() => setPage((p) => Math.min(meta.totalPages - 1, p + 1))}
            disabled={page >= meta.totalPages - 1}
          >
            다음
          </button>
        </div>
      )}
    </div>
  )
}

export default AdminReportsPage
