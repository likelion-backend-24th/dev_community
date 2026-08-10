import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getQuestions } from "../../api/questionApi";

const STATUS_LABEL = {
  OPEN: "미해결",
  RESOLVED: "해결",
};

const PAGE_SIZE = 10;

function QuestionListPage() {
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
  const [appliedTag, setAppliedTag] = useState("");
  const [keywordInput, setKeywordInput] = useState("");
  const [tagInput, setTagInput] = useState("");
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
          // 존재하지 않는 태그로 필터링한 경우 (백엔드가 404를 반환)
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
    <div>
      <h1>질문 목록</h1>

      <form onSubmit={handleSearchSubmit}>
        <input
          type="text"
          placeholder="검색어"
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
        />
        <input
          type="text"
          placeholder="태그"
          value={tagInput}
          onChange={(e) => setTagInput(e.target.value)}
        />
        <button type="submit">검색</button>
      </form>

      <div>
        <label htmlFor="status">상태</label>
        <select
          id="status"
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

        <label htmlFor="sort">정렬</label>
        <select
          id="sort"
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

      {loading && <p>불러오는 중...</p>}
      {error && <p role="alert">{error}</p>}

      {!loading && !error && questions.length === 0 && <p>질문이 없습니다.</p>}

      {!loading && !error && questions.length > 0 && (
        <ul>
          {questions.map((q) => (
            <li key={q.id}>
              <Link to={`/questions/${q.id}`}>{q.title}</Link>
              <span> [{STATUS_LABEL[q.status] ?? q.status}]</span>
              <span> · {q.authorNickname}</span>
              <span>
                {" "}
                · 태그: {q.tags.length > 0 ? q.tags.join(", ") : "없음"}
              </span>
              <span> · 조회 {q.viewCount}</span>
              <span> · 추천 {q.likeCount}</span>
              <span> · 답변 {q.answerCount}</span>
              <span> · {new Date(q.createdAt).toLocaleString()}</span>
            </li>
          ))}
        </ul>
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
  );
}

export default QuestionListPage;
