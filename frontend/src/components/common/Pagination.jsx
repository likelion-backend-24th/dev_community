// 목록 페이지 공용 페이지네이션. totalPages가 0이면 아무것도 렌더링하지 않는다.
function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 0) return null;

  return (
    <div className="pagination">
      <button
        type="button"
        className="btn btn-secondary btn-sm"
        onClick={() => onPageChange(Math.max(0, page - 1))}
        disabled={page === 0}
      >
        이전
      </button>
      <span className="pagination__info">
        {page + 1} / {totalPages}
      </span>
      <button
        type="button"
        className="btn btn-secondary btn-sm"
        onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
        disabled={page >= totalPages - 1}
      >
        다음
      </button>
    </div>
  );
}

export default Pagination;
