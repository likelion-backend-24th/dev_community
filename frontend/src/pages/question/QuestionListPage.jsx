import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { getQuestions } from "../../api/questionApi";
import { STATUS_LABEL } from "../../constants/questionStatus";
import QuestionBoardTabs from "../../components/question/QuestionBoardTabs";
import ExpertBadge from "../../components/common/ExpertBadge";
import "../../styles/question.css";

const PAGE_SIZE = 10;

function QuestionListPage() {
  const [searchParams] = useSearchParams();
  const initialTag = searchParams.get("tag") ?? "";
  const initialKeyword = searchParams.get("keyword") ?? "";

  const [questions, setQuestions] = useState([]);
  const [meta, setMeta] = useState({
    page: 0,
    totalPages: 0,
    totalElements: 0,
  });
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState("");
  const [status, setStatus] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState(initialKeyword);
  const [appliedTag, setAppliedTag] = useState(initialTag);
  const [keywordInput, setKeywordInput] = useState(initialKeyword);
  const [tagInput, setTagInput] = useState(initialTag);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const fetchQuestions = async () => {
      setLoading(true);
      setError("");
      try {
        const { content, meta: resMeta } = await getQuestions({
          page,
          size: PAGE_SIZE,
          sort: sort || undefined,
          status: status || undefined,
          keyword: appliedKeyword || undefined,
          tag: appliedTag || undefined,
        });
        if (cancelled) return;
        setQuestions(content);
        setMeta(resMeta);
      } catch (err) {
        if (cancelled) return;
        if (err.response?.status === 404) {
          // 존재하지 않는 태그로 필터링한 경우 (백엔드에서 404 반환)
          setQuestions([]);
          setMeta({ page: 0, totalPages: 0, totalElements: 0 });
        } else {
          setError(
            err.response?.data?.message ?? "질문 목록을 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchQuestions();
    return () => {
      cancelled = true;
    };
  }, [page, sort, status, appliedKeyword, appliedTag]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    setAppliedKeyword(keywordInput.trim());
    setAppliedTag(tagInput.trim());
  };

  return (
    <div className="page">
      <div className="page__header">
        <h1 className="page__title">질문 목록</h1>
        <span className="page__count">전체 글 {meta.totalElements}개</span>
      </div>

      <QuestionBoardTabs />

      <form className="filter-bar" onSubmit={handleSearchSubmit}>
        <div className="filter-bar__field filter-bar__field--keyword">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <circle cx="11" cy="11" r="7" />
            <path d="m21 21-4.3-4.3" />
          </svg>
          <input
            type="text"
            className="input"
            placeholder="검색어를 입력하세요"
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
          />
        </div>
        <div className="filter-bar__field filter-bar__field--tag">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M20.59 13.41 11 3.83A2 2 0 0 0 9.59 3.24H4a1 1 0 0 0-1 1v5.59a2 2 0 0 0 .59 1.41l9.58 9.59a2 2 0 0 0 2.83 0l4.59-4.59a2 2 0 0 0 0-2.83Z" />
            <circle cx="7.5" cy="7.5" r="1.5" />
          </svg>
          <input
            type="text"
            className="input"
            placeholder="태그"
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
          />
        </div>
        <button type="submit" className="btn btn-primary">
          검색
        </button>
      </form>

      <div className="filter-row">
        <div className="select-group">
          <label htmlFor="status">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
            </svg>
            상태
          </label>
          <select
            id="status"
            className="select"
            value={status}
            onChange={(e) => {
              setPage(0);
              setStatus(e.target.value);
            }}
          >
            <option value="">전체</option>
            <option value="UNRESOLVED">미해결</option>
            <option value="RESOLVED">해결</option>
          </select>
        </div>

        <div className="select-group">
          <label htmlFor="sort">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M3 6h18M6 12h12M10 18h4" />
            </svg>
            정렬
          </label>
          <select
            id="sort"
            className="select"
            value={sort}
            onChange={(e) => {
              setPage(0);
              setSort(e.target.value);
            }}
          >
            <option value="">최신순</option>
            <option value="LIKE">추천순</option>
            <option value="UNRESOLVED">미해결 우선</option>
          </select>
        </div>
      </div>

      {loading && <p className="state-text">불러오는 중...</p>}
      {error && (
        <p className="inline-error" role="alert">
          {error}
        </p>
      )}

      {!loading && !error && questions.length === 0 && (
        <div className="empty-state">
          <p>아직 질문이 없어요. 첫 질문을 남겨보세요.</p>
          <Link to="/questions/new" className="btn btn-primary">
            질문하기
          </Link>
        </div>
      )}

      {!loading && !error && questions.length > 0 && (
        <ul className="question-list">
          {questions.map((q) => (
            <li key={q.id} className="question-card">
              <div className="question-card__top">
                <Link
                  to={`/questions/${q.id}`}
                  className="question-card__title"
                >
                  {q.title}
                </Link>
                <span
                  className={`badge ${q.status === "RESOLVED" ? "badge-resolved" : "badge-open"}`}
                >
                  {STATUS_LABEL[q.status] ?? q.status}
                </span>
              </div>

              <div className="question-card__tags">
                {q.tags.length > 0 ? (
                  q.tags.map((tag) => (
                    <span key={tag} className="tag-chip">
                      {tag}
                    </span>
                  ))
                ) : (
                  <span className="question-card__meta">태그 없음</span>
                )}
              </div>

              <div className="question-card__meta">
                <span className="author-with-badge">
                  <span className="question-card__avatar" aria-hidden="true">
                    {q.authorNickname?.[0] ?? "?"}
                  </span>
                  {q.authorNickname}
                  {q.authorIsExpert && (
                    <ExpertBadge className="expert-badge--sm" />
                  )}
                </span>
                <span className="question-card__stat">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                  {q.viewCount}
                </span>
                <span className="question-card__stat">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 1 0-7.8 7.8l1 1L12 21l7.8-7.8 1-1a5.5 5.5 0 0 0 0-7.8z" />
                  </svg>
                  {q.likeCount}
                </span>
                <span className="question-card__stat">
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                  </svg>
                  {q.answerCount}
                </span>
                <span>{new Date(q.createdAt).toLocaleString()}</span>
              </div>
            </li>
          ))}
        </ul>
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
    </div>
  );
}

export default QuestionListPage;
