// 일반/멤버십 게시판이 공유하는 검색어 + 태그 검색 폼.
function QuestionSearchBar({
  keywordInput,
  onKeywordChange,
  tagInput,
  onTagChange,
  onSubmit,
}) {
  return (
    <form className="filter-bar" onSubmit={onSubmit}>
      <div className="filter-bar__field filter-bar__field--keyword">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="m21 21-4.3-4.3" />
        </svg>
        <input
          type="text"
          className="input"
          placeholder="검색어를 입력하세요"
          value={keywordInput}
          onChange={(e) => onKeywordChange(e.target.value)}
        />
      </div>

      <div className="filter-bar__field filter-bar__field--tag">
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M20.59 13.41 11 3.83A2 2 0 0 0 9.59 3.24H4a1 1 0 0 0-1 1v5.59a2 2 0 0 0 .59 1.41l9.58 9.59a2 2 0 0 0 2.83 0l4.59-4.59a2 2 0 0 0 0-2.83Z" />
          <circle cx="7.5" cy="7.5" r="1.5" />
        </svg>
        <input
          type="text"
          className="input"
          placeholder="태그"
          value={tagInput}
          onChange={(e) => onTagChange(e.target.value)}
        />
      </div>

      <button type="submit" className="btn btn-primary">
        검색
      </button>
    </form>
  );
}

export default QuestionSearchBar;
