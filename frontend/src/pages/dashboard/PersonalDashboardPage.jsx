import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  getMyDashboardSummary,
  getMyActivityTimeline,
  getMyQuestionsForDashboard,
  getMyAnswersForDashboard,
} from "../../api/dashboardApi";
import { STATUS_LABEL } from "../../constants/questionStatus";
import "../../styles/dashboard.css";
import "../../styles/mypage.css";

const FILTERS = {
  questionCount: { label: "작성한 질문", source: "questions", predicate: () => true },
  answerCount: { label: "작성한 답변", source: "answers", predicate: () => true },
  adoptedAnswerCount: { label: "채택된 답변", source: "answers", predicate: (a) => a.adopted },
  unresolvedQuestionCount: { label: "미해결 질문", source: "questions", predicate: (q) => q.status === "OPEN" },
};

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

function PersonalDashboardPage() {
  const [summary, setSummary] = useState(null);
  const [timeline, setTimeline] = useState(null);
  const [error, setError] = useState("");

  const [questions, setQuestions] = useState(null);
  const [answers, setAnswers] = useState(null);
  const [activeFilter, setActiveFilter] = useState(null);
  const [filterLoading, setFilterLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    Promise.all([getMyDashboardSummary(), getMyActivityTimeline()])
      .then(([summaryRes, timelineRes]) => {
        if (cancelled) return;
        setSummary(summaryRes);
        setTimeline(timelineRes);
      })
      .catch(
        (err) =>
          !cancelled &&
          setError(err.response?.data?.message ?? "데이터를 불러오지 못했습니다."),
      );
    return () => {
      cancelled = true;
    };
  }, []);

  const handleTileClick = async (filterKey) => {
    if (activeFilter === filterKey) {
      setActiveFilter(null);
      return;
    }

    const { source } = FILTERS[filterKey];
    setFilterLoading(true);
    try {
      if (source === "questions" && questions === null) {
        setQuestions(await getMyQuestionsForDashboard());
      } else if (source === "answers" && answers === null) {
        setAnswers(await getMyAnswersForDashboard());
      }
      setActiveFilter(filterKey);
    } catch (err) {
      setError(err.response?.data?.message ?? "목록을 불러오지 못했습니다.");
    } finally {
      setFilterLoading(false);
    }
  };

  if (error)
    return (
      <div className="page">
        <p className="inline-error" role="alert">
          {error}
        </p>
      </div>
    );
  if (!summary || !timeline) return <p className="state-text">불러오는 중...</p>;

  const activeFilterDef = activeFilter ? FILTERS[activeFilter] : null;
  const filteredList = activeFilterDef
    ? (activeFilterDef.source === "questions" ? questions : answers)?.filter(activeFilterDef.predicate)
    : null;

  return (
    <div className="page">
      <div className="page__header">
        <h1 className="page__title">내 활동 대시보드</h1>
      </div>

      <div className="stat-grid">
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

      {activeFilterDef ? (
        <div className="stat-card">
          <h2 className="stat-card__title">{activeFilterDef.label} 목록</h2>
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
                    <Link to={`/questions/${item.id}`} className="mypage-list__item">
                      <span className={`badge ${item.status === "RESOLVED" ? "badge-resolved" : "badge-open"}`}>
                        {STATUS_LABEL[item.status] ?? item.status}
                      </span>
                      <span className="mypage-list__title">{item.title}</span>
                      <span className="mypage-list__date">{new Date(item.createdAt).toLocaleString()}</span>
                    </Link>
                  </li>
                ) : (
                  <li key={item.id}>
                    <Link to={`/questions/${item.questionId}`} className="mypage-list__item">
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
          <h2 className="stat-card__title">최근 활동</h2>
          {timeline.length === 0 ? (
            <div className="empty-state">
              <p>아직 활동 내역이 없어요.</p>
            </div>
          ) : (
            <ul className="mypage-list">
              {timeline.map((item, index) => (
                <li key={`${item.type}-${item.questionId}-${item.createdAt}-${index}`}>
                  <Link to={`/questions/${item.questionId}`} className="mypage-list__item">
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
    </div>
  );
}

export default PersonalDashboardPage;
