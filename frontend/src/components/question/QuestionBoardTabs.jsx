import { Link, useLocation } from "react-router-dom";

function QuestionBoardTabs() {
  const location = useLocation();
  const isPremium = location.pathname === "/questions/premium";

  return (
    <div className="board-tabs">
      <Link
        to="/questions"
        className={`board-tab ${!isPremium ? "board-tab--active" : ""}`}
      >
        전체
      </Link>
      <Link
        to="/questions/premium"
        className={`board-tab ${isPremium ? "board-tab--active" : ""}`}
      >
        <svg
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
        </svg>
        멤버십
      </Link>
    </div>
  );
}

export default QuestionBoardTabs;
