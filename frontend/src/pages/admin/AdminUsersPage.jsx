import { useEffect, useState } from "react";
import {
  getUsers,
  getUserReportCount,
  suspendUser,
  unsuspendUser,
} from "../../api/adminApi";
import "../../styles/admin.css";

const STATUS_LABEL = {
  ACTIVE: "정상",
  WITHDRAWN: "탈퇴",
  SUSPENDED: "정지",
};

const STATUS_BADGE = {
  ACTIVE: "badge-resolved",
  WITHDRAWN: "badge-open",
  SUSPENDED: "badge-danger",
};

const PAGE_SIZE = 10;

function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [meta, setMeta] = useState({
    page: 0,
    totalPages: 0,
    totalElements: 0,
  });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [processingId, setProcessingId] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [suspendTarget, setSuspendTarget] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const fetchOnce = async () => {
      const { content, meta: resMeta } = await getUsers({
        page,
        size: PAGE_SIZE,
      });
      const reportCounts = await Promise.all(
        content.map((u) => getUserReportCount(u.userId)),
      );
      return { content, resMeta, reportCounts };
    };

    const fetchUsers = async () => {
      setLoading(true);
      setError("");
      try {
        let result;
        try {
          result = await fetchOnce();
        } catch {
          result = await fetchOnce();
        }
        if (cancelled) return;
        setUsers(
          result.content.map((u, i) => ({
            ...u,
            reportCount: result.reportCounts[i],
          })),
        );
        setMeta(result.resMeta);
      } catch (err) {
        if (cancelled) return;
        setError(
          err.response?.data?.message ?? "회원 목록을 불러오지 못했습니다.",
        );
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchUsers();
    return () => {
      cancelled = true;
    };
  }, [page, reloadKey]);

  const confirmSuspend = async () => {
    const userId = suspendTarget.userId;
    setProcessingId(userId);
    setError("");
    try {
      await suspendUser(userId);
      setReloadKey((k) => k + 1);
      setSuspendTarget(null);
    } catch (err) {
      setError(err.response?.data?.message ?? "회원 정지에 실패했습니다.");
    } finally {
      setProcessingId(null);
    }
  };

  const handleUnsuspend = async (userId) => {
    setProcessingId(userId);
    setError("");
    try {
      await unsuspendUser(userId);
      setReloadKey((k) => k + 1);
    } catch (err) {
      setError(err.response?.data?.message ?? "정지 해제에 실패했습니다.");
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <div className="page">
      <div className="page__header">
        <h1 className="page__title">회원 관리</h1>
      </div>

      {loading && <p className="state-text">불러오는 중...</p>}
      {error && (
        <p className="inline-error" role="alert">
          {error}
        </p>
      )}

      {!loading && !error && (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>아이디</th>
                <th>닉네임</th>
                <th>권한</th>
                <th>상태</th>
                <th>평판</th>
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
                  <td>
                    <span
                      className={`badge ${STATUS_BADGE[u.status] ?? "badge-open"}`}
                    >
                      {STATUS_LABEL[u.status] ?? u.status}
                    </span>
                  </td>
                  <td>{u.reputation}</td>
                  <td>{u.reportCount}</td>
                  <td>{u.createdAt.slice(0, 10)}</td>
                  <td>
                    {u.status === "ACTIVE" && u.role !== "ADMIN" && (
                      <button
                        type="button"
                        className="btn btn-destructive btn-sm"
                        disabled={processingId === u.userId}
                        onClick={() => setSuspendTarget(u)}
                      >
                        정지
                      </button>
                    )}
                    {u.status === "SUSPENDED" && (
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        disabled={processingId === u.userId}
                        onClick={() => handleUnsuspend(u.userId)}
                      >
                        정지 해제
                      </button>
                    )}
                    {u.status === "WITHDRAWN" && (
                      <span className="table__empty-cell">-</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {meta.totalPages > 0 && (
        <div className="pagination">
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            이전
          </button>
          <span className="pagination__info">
            {page + 1} / {meta.totalPages}
          </span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setPage((p) => Math.min(meta.totalPages - 1, p + 1))}
            disabled={page >= meta.totalPages - 1}
          >
            다음
          </button>
        </div>
      )}

      {suspendTarget && (
        <div className="modal-overlay">
          <div className="modal" role="dialog" aria-label="회원 정지 확인">
            <div className="modal__header">
              <h2 className="modal__title">회원 정지</h2>
            </div>
            <div className="modal__body">
              <p className="modal__desc">
                <strong>{suspendTarget.nickname}</strong>(
                {suspendTarget.username}) 님을 정지하시겠습니까? 정지된 회원은
                로그인할 수 없으며, 해제는 관리자가 직접 수동으로 처리해야
                합니다.
              </p>
              {error && (
                <p className="inline-error" role="alert">
                  {error}
                </p>
              )}
            </div>
            <div className="modal__footer">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setSuspendTarget(null)}
                disabled={processingId === suspendTarget.userId}
              >
                취소
              </button>
              <button
                type="button"
                className="btn btn-destructive"
                onClick={confirmSuspend}
                disabled={processingId === suspendTarget.userId}
              >
                {processingId === suspendTarget.userId ? "처리 중..." : "정지"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminUsersPage;
