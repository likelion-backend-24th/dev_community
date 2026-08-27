import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { getPremiumQuestions } from "../../api/questionApi";
import QuestionBoardTabs from "../../components/question/QuestionBoardTabs";
import QuestionSearchBar from "../../components/question/QuestionSearchBar";
import QuestionFilters from "../../components/question/QuestionFilters";
import QuestionCard from "../../components/question/QuestionCard";
import Pagination from "../../components/common/Pagination";
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
  const [type, setType] = useState("");
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
          type: type || undefined,
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
  }, [page, sort, status, type, appliedKeyword, appliedTag]);

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

      <QuestionSearchBar
        keywordInput={keywordInput}
        onKeywordChange={setKeywordInput}
        tagInput={tagInput}
        onTagChange={setTagInput}
        onSubmit={handleSearchSubmit}
      />

      <QuestionFilters
        status={status}
        onStatusChange={(value) => {
          setPage(0);
          setStatus(value);
        }}
        sort={sort}
        onSortChange={(value) => {
          setPage(0);
          setSort(value);
        }}
        type={type}
        onTypeChange={(value) => {
          setPage(0);
          setType(value);
        }}
      />

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
            <li key={q.id}>
              <QuestionCard question={q} showType />
            </li>
          ))}
        </ul>
      )}

      <Pagination
        page={page}
        totalPages={meta.totalPages}
        onPageChange={setPage}
      />
    </div>
  );
}

export default PremiumQuestionListPage;
