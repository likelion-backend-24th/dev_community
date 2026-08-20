import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { getPremiumQuestions } from "../../api/questionApi";
import { STATUS_LABEL } from "../../constants/questionStatus";
import { TYPE_LABEL } from "../../constants/questionType";
import QuestionBoardTabs from "../../components/question/QuestionBoardTabs";
import ExpertBadge from "../../components/common/ExpertBadge";
import "../../styles/question.css";

const PAGE_SIZE = 10;

function PremiumQuestionListPage() {
  const [searchParams] = useSearchParams();
  const initialTag = searchParams.get("tag") ?? "";

  const [questions, setQuestions] = useState([]);
  const [meta, setMeta] = useState({
    page: 0,
    totalPages: 0,
    totalElements: 0,
  });
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState("");
  const [status, setStatus] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState("");
  const [appliedTag, setAppliedTag] = useState(initialTag);
  const [keywordInput, setKeywordInput] = useState("");
  const [tagInput, setTagInput] = useState(initialTag);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    const fetchQuestions = async () => {
      setLoading(true);
      setError("");
      try {
        const { content, meta: resMeta } = await getPremiumQuestions({
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
        } else if (err.response?.status !== 403) {
          setError(
            err.response?.data?.message ??
              "멤버십 게시판 목록을 불러오지 못했습니다.",
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
      </div>

      <QuestionBoardTabs />

      <form className="filter-bar" onSubmit={handleSearchSubmit}>
        <input
          type="text"
          className="input"
          placeholder="검색어"
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
        />
        <input
          type="text"
          className="input"
          placeholder="태그"
          value={tagInput}
          onChange={(e) => setTagInput(e.target.value)}
        />
        <button type="submit" className="btn btn-secondary">
          검색
        </button>
      </form>

      <div className="filter-row">
        <div className="select-group">
          <label htmlFor="status">상태</label>
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
          <label htmlFor="sort">정렬</label>
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
          <p>아직 멤버십 게시판에 글이 없어요.</p>
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
                <div className="question-card__badges">
                  <span className="badge badge-type">
                    {TYPE_LABEL[q.type] ?? q.type}
                  </span>
                  <span
                    className={`badge ${q.status === "RESOLVED" ? "badge-resolved" : "badge-open"}`}
                  >
                    {STATUS_LABEL[q.status] ?? q.status}
                  </span>
                </div>
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
                  {q.authorNickname}
                  {q.authorIsExpert && <ExpertBadge className="expert-badge--sm" />}
                </span>
                <span>조회 {q.viewCount}</span>
                <span>추천 {q.likeCount}</span>
                <span>답변 {q.answerCount}</span>
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

export default PremiumQuestionListPage;
