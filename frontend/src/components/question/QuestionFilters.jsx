// 일반/멤버십 게시판이 공유하는 상태/정렬 필터.
// 글 유형(type) 필터는 멤버십 게시판에서만 쓰이므로 onTypeChange가 넘어올 때만 렌더링한다.
function QuestionFilters({
  status,
  onStatusChange,
  sort,
  onSortChange,
  type,
  onTypeChange,
}) {
  return (
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
          onChange={(e) => onStatusChange(e.target.value)}
        >
          <option value="">전체</option>
          <option value="UNRESOLVED">미해결</option>
          <option value="RESOLVED">해결</option>
        </select>
      </div>

      {onTypeChange && (
        <div className="select-group">
          <label htmlFor="type">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M4 6h16M4 12h16M4 18h10" />
            </svg>
            유형
          </label>
          <select
            id="type"
            className="select"
            value={type}
            onChange={(e) => onTypeChange(e.target.value)}
          >
            <option value="">전체</option>
            <option value="GENERAL">일반</option>
            <option value="CODE_REVIEW">코드리뷰</option>
            <option value="CAREER_CONSULT">커리어상담</option>
          </select>
        </div>
      )}

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
          onChange={(e) => onSortChange(e.target.value)}
        >
          <option value="">최신순</option>
          <option value="LIKE">추천순</option>
          <option value="VIEW">조회순</option>
        </select>
      </div>
    </div>
  );
}

export default QuestionFilters;
