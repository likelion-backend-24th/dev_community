import { useEffect, useState } from 'react'
import {
  getUsers,
  getUserReportCount,
  suspendUser,
  unsuspendUser,
} from '../../api/adminApi'

const STATUS_LABEL = {
  ACTIVE: '정상',
  WITHDRAWN: '탈퇴',
  SUSPENDED: '정지',
}

const PAGE_SIZE = 10

function AdminUsersPage() {
  const [users, setUsers] = useState([])
  const [meta, setMeta] = useState({ page: 0, totalPages: 0, totalElements: 0 })
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [processingId, setProcessingId] = useState(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let cancelled = false

    const fetchUsers = async () => {
      setLoading(true)
      setError('')
      try {
        const { content, meta: resMeta } = await getUsers({
          page,
          size: PAGE_SIZE,
        })
        const reportCounts = await Promise.all(
          content.map((u) => getUserReportCount(u.userId)),
        )
        if (cancelled) return
        setUsers(
          content.map((u, i) => ({ ...u, reportCount: reportCounts[i] })),
        )
        setMeta(resMeta)
      } catch (err) {
        if (cancelled) return
        setError(err.response?.data?.message ?? '회원 목록을 불러오지 못했습니다.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    fetchUsers()
    return () => {
      cancelled = true
    }
  }, [page, reloadKey])

  const handleSuspend = async (userId) => {
    setProcessingId(userId)
    setError('')
    try {
      await suspendUser(userId)
      setReloadKey((k) => k + 1)
    } catch (err) {
      setError(err.response?.data?.message ?? '회원 정지에 실패했습니다.')
    } finally {
      setProcessingId(null)
    }
  }

  const handleUnsuspend = async (userId) => {
    setProcessingId(userId)
    setError('')
    try {
      await unsuspendUser(userId)
      setReloadKey((k) => k + 1)
    } catch (err) {
      setError(err.response?.data?.message ?? '정지 해제에 실패했습니다.')
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <div>
      <h1>회원 관리</h1>

      {loading && <p>불러오는 중...</p>}
      {error && <p role="alert">{error}</p>}

      {!loading && !error && (
        <table>
          <thead>
            <tr>
              <th>아이디</th>
              <th>닉네임</th>
              <th>권한</th>
              <th>상태</th>
              <th>누적 신고</th>
              <th>가입일</th>
              <th>처리</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.userId}>
                <td>{u.username}</td>
                <td>{u.nickname}</td>
                <td>{u.role}</td>
                <td>{STATUS_LABEL[u.status] ?? u.status}</td>
                <td>{u.reportCount}</td>
                <td>{u.createdAt.slice(0, 10)}</td>
                <td>
                  {u.status === 'ACTIVE' && u.role !== 'ADMIN' && (
                    <button
                      type="button"
                      disabled={processingId === u.userId}
                      onClick={() => handleSuspend(u.userId)}
                    >
                      정지
                    </button>
                  )}
                  {u.status === 'SUSPENDED' && (
                    <button
                      type="button"
                      disabled={processingId === u.userId}
                      onClick={() => handleUnsuspend(u.userId)}
                    >
                      정지 해제
                    </button>
                  )}
                  {u.status === 'WITHDRAWN' && '-'}
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

export default AdminUsersPage
