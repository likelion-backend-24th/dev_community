import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  getUsers,
  getUserReportCount,
  suspendUser,
  unsuspendUser,
  grantExpert,
  rejectExpertRequest,
  getUserDashboardSummary,
  getUserDashboardTimeline,
  getUserQuestionsForDashboard,
  getUserAnswersForDashboard,
} from "../../api/adminApi";
import ExpertBadge from "../../components/common/ExpertBadge";
import { STATUS_LABEL as QUESTION_STATUS_LABEL } from "../../constants/questionStatus";
import "../../styles/admin.css";
import "../../styles/dashboard.css";
import "../../styles/mypage.css";

const DASHBOARD_FILTERS = {
  questionCount: { label: "작성한 질문", source: "questions", predicate: () => true },
  answerCount: { label: "작성한 답변", source: "answers", predicate: () => true },
  adoptedAnswerCount: { label: "채택된 답변", source: "answers", predicate: (a) => a.adopted },
  unresolvedQuestionCount: { label: "미해결 질문", source: "questions", predicate: (q) => q.status === "OPEN" },
};

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

function StatTile({ label, value, active, onClick }) {
  return (
    <button
      type="button"
      className={`stat-tile stat-tile--clickable ${active ? "stat-tile--active" : ""}`}
      onClick={onClick}
    >
      <p className="stat-tile__value">{value}</p>
      <p className="stat-tile__label">{label}</p>
    </button>
  );
}

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
  const [expertOnly, setExpertOnly] = useState(false);
  const [reviewTarget, setReviewTarget] = useState(null);

  useEffect(() => {
    let cancelled = false;

    const fetchOnce = async () => {
      const { content, meta: resMeta } = await getUsers({
        page,
        size: PAGE_SIZE,
        expertRequested: expertOnly || undefined,
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
  }, [page, reloadKey, expertOnly]);

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

      <div className="admin-toolbar">
        <div className="select-group">
          <label htmlFor="expertOnly">구분</label>
          <select
            id="expertOnly"
            className="select"
            value={expertOnly ? "expert" : ""}
            onChange={(e) => {
              setPage(0);
              setExpertOnly(e.target.value === "expert");
            }}
          >
            <option value="">전체</option>
            <option value="expert">전문가 등급 요청</option>
          </select>
        </div>
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
                <tr
                  key={u.userId}
                  className="table-row--clickable"
                  onClick={() => setReviewTarget(u)}
                >
                  <td>{u.username}</td>
                  <td>
                    {u.nickname}
                    {u.expert && <ExpertBadge />}
                    {u.expertRequested && (
                      <span className="badge badge-pending badge--spaced">전문가 신청</span>
                    )}
                  </td>
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
                        onClick={(e) => {
                          e.stopPropagation();
                          setSuspendTarget(u);
                        }}
                      >
                        정지
                      </button>
                    )}
                    {u.status === "SUSPENDED" && (
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        disabled={processingId === u.userId}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleUnsuspend(u.userId);
                        }}
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

      {reviewTarget && (
        <UserDashboardModal
          user={reviewTarget}
          onClose={() => setReviewTarget(null)}
          onResolved={() => {
            setReviewTarget(null);
            setReloadKey((k) => k + 1);
          }}
        />
      )}
    </div>
  );
}

function UserDashboardModal({ user, onClose, onResolved }) {
  const [summary, setSummary] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [processing, setProcessing] = useState(false);
  const [confirmingAccept, setConfirmingAccept] = useState(false);

  const [questions, setQuestions] = useState(null);
  const [answers, setAnswers] = useState(null);
  const [activeFilter, setActiveFilter] = useState(null);
  const [filterLoading, setFilterLoading] = useState(false);
  const [filterError, setFilterError] = useState("");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [summaryRes, timelineRes] = await Promise.all([
          getUserDashboardSummary(user.userId),
          getUserDashboardTimeline(user.userId),
        ]);
        if (cancelled) return;
        setSummary(summaryRes);
        setTimeline(timelineRes);
      } catch (err) {
        if (!cancelled) {
          setError(err.response?.data?.message ?? "활동 대시보드를 불러오지 못했습니다.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [user.userId]);

  const handleTileClick = async (filterKey) => {
    if (activeFilter === filterKey) {
      setActiveFilter(null);
      return;
    }

    const { source } = DASHBOARD_FILTERS[filterKey];
    setFilterLoading(true);
    setFilterError("");
    try {
      if (source === "questions" && questions === null) {
        setQuestions(await getUserQuestionsForDashboard(user.userId));
      } else if (source === "answers" && answers === null) {
        setAnswers(await getUserAnswersForDashboard(user.userId));
      }
      setActiveFilter(filterKey);
    } catch (err) {
      setFilterError(err.response?.data?.message ?? "목록을 불러오지 못했습니다.");
    } finally {
      setFilterLoading(false);
    }
  };

  const activeFilterDef = activeFilter ? DASHBOARD_FILTERS[activeFilter] : null;
  const filteredList = activeFilterDef
    ? (activeFilterDef.source === "questions" ? questions : answers)?.filter(activeFilterDef.predicate)
    : null;

  const handleReject = async () => {
    setProcessing(true);
    setError("");
    try {
      await rejectExpertRequest(user.userId);
      onResolved();
    } catch (err) {
      setError(err.response?.data?.message ?? "거절 처리에 실패했습니다.");
      setProcessing(false);
    }
  };

  const handleAccept = async () => {
    setProcessing(true);
    setError("");
    try {
      await grantExpert(user.userId);
      onResolved();
    } catch (err) {
      setError(err.response?.data?.message ?? "전문가 지정에 실패했습니다.");
      setProcessing(false);
      setConfirmingAccept(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal modal--lg"
        role="dialog"
        aria-label={`${user.nickname} 활동 대시보드`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal__header">
          <h2 className="modal__title">
            {user.nickname}({user.username})님의 활동 대시보드
          </h2>
        </div>
        <div className="modal__body">
          {loading && <p className="state-text">불러오는 중...</p>}
          {error && (
            <p className="inline-error" role="alert">
              {error}
            </p>
          )}
          {summary && (
            <>
              <div className="stat-grid stat-grid--admin">
                <StatTile
                  label="작성한 질문"
                  value={summary.questionCount}
                  active={activeFilter === "questionCount"}
                  onClick={() => handleTileClick("questionCount")}
                />
                <StatTile
                  label="작성한 답변"
                  value={summary.answerCount}
                  active={activeFilter === "answerCount"}
                  onClick={() => handleTileClick("answerCount")}
                />
                <StatTile
                  label="채택된 답변"
                  value={summary.adoptedAnswerCount}
                  active={activeFilter === "adoptedAnswerCount"}
                  onClick={() => handleTileClick("adoptedAnswerCount")}
                />
                <StatTile
                  label="미해결 질문"
                  value={summary.unresolvedQuestionCount}
                  active={activeFilter === "unresolvedQuestionCount"}
                  onClick={() => handleTileClick("unresolvedQuestionCount")}
                />
                <div className="stat-tile">
                  <p className="stat-tile__value">{summary.reputation}</p>
                  <p className="stat-tile__label">평판 점수</p>
                </div>
              </div>

              {filterError && (
                <p className="inline-error" role="alert">
                  {filterError}
                </p>
              )}

              {activeFilterDef ? (
                <div className="stat-card">
                  <h3 className="stat-card__title">{activeFilterDef.label} 목록</h3>
                  {filterLoading || filteredList === undefined ? (
                    <p className="state-text">불러오는 중...</p>
                  ) : filteredList.length === 0 ? (
                    <div className="empty-state">
                      <p>해당하는 항목이 없어요.</p>
                    </div>
                  ) : (
                    <ul className="mypage-list">
                      {filteredList.map((item) =>
                        activeFilterDef.source === "questions" ? (
                          <li key={item.id}>
                            <Link
                              to={`/questions/${item.id}`}
                              className="mypage-list__item"
                              target="_blank"
                              rel="noopener noreferrer"
                            >
                              <span className={`badge ${item.status === "RESOLVED" ? "badge-resolved" : "badge-open"}`}>
                                {QUESTION_STATUS_LABEL[item.status] ?? item.status}
                              </span>
                              <span className="mypage-list__title">{item.title}</span>
                              <span className="mypage-list__date">{new Date(item.createdAt).toLocaleString()}</span>
                            </Link>
                          </li>
                        ) : (
                          <li key={item.id}>
                            <Link
                              to={`/questions/${item.questionId}`}
                              className="mypage-list__item"
                              target="_blank"
                              rel="noopener noreferrer"
                            >
                              {item.adopted && <span className="badge badge-resolved">채택됨</span>}
                              <span className="mypage-list__title">{item.content}</span>
                              <span className="mypage-list__date">{new Date(item.createdAt).toLocaleString()}</span>
                            </Link>
                          </li>
                        ),
                      )}
                    </ul>
                  )}
                </div>
              ) : (
                <div className="stat-card">
                  <h3 className="stat-card__title">최근 활동</h3>
                  {timeline.length === 0 ? (
                    <div className="empty-state">
                      <p>아직 활동 내역이 없어요.</p>
                    </div>
                  ) : (
                    <ul className="mypage-list">
                      {timeline.map((item, index) => (
                        <li key={`${item.type}-${item.questionId}-${item.createdAt}-${index}`}>
                          <Link
                            to={`/questions/${item.questionId}`}
                            className="mypage-list__item"
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            <span className={`badge ${item.adopted ? "badge-resolved" : "badge-open"}`}>
                              {item.type === "QUESTION" ? "질문" : item.adopted ? "채택됨" : "답변"}
                            </span>
                            <span className="mypage-list__title">{item.title}</span>
                            <span className="mypage-list__date">
                              {new Date(item.createdAt).toLocaleString()}
                            </span>
                          </Link>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              )}
            </>
          )}
        </div>
        <div className="modal__footer">
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={processing}>
            닫기
          </button>
          {user.expertRequested && (
            <>
              <button
                type="button"
                className="btn btn-destructive"
                onClick={handleReject}
                disabled={processing || loading}
              >
                거절
              </button>
              <button
                type="button"
                className="btn btn-warning"
                onClick={() => setConfirmingAccept(true)}
                disabled={processing || loading}
              >
                수락
              </button>
            </>
          )}
        </div>
      </div>

      {confirmingAccept && (
        <div className="modal-overlay">
          <div className="modal" role="alertdialog" aria-modal="true" aria-label="전문가 지정 확인">
            <div className="modal__header">
              <h2 className="modal__title">전문가로 지정하시겠습니까?</h2>
            </div>
            <div className="modal__body">
              <p className="modal__desc">
                <strong>{user.nickname}</strong>님을 전문가로 지정합니다. 이후 코드리뷰 게시글에서
                답변이 우선 노출되고, 이름 옆에 전문가 배지가 표시됩니다.
              </p>
            </div>
            <div className="modal__footer">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setConfirmingAccept(false)}
                disabled={processing}
              >
                취소
              </button>
              <button
                type="button"
                className="btn btn-warning"
                onClick={handleAccept}
                disabled={processing}
              >
                {processing ? "처리 중..." : "확인"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminUsersPage;
