import { Link } from "react-router-dom";
import { STATUS_LABEL } from "../../constants/questionStatus";
import { TYPE_LABEL } from "../../constants/questionType";
import ExpertBadge from "../common/ExpertBadge";
import { formatRelativeTime } from "../../utils/formatDate";

// 일반/멤버십 게시판이 공유하는 질문 카드.
// showType은 멤버십 게시판에서만 켜서 글 유형 뱃지를 함께 보여준다.
function QuestionCard({ question, showType = false }) {
  const q = question;

  return (
    <Link to={`/questions/${q.id}`} className="question-card">
      <div className="question-card__top">
        <span className="question-card__title">{q.title}</span>
        <div className="question-card__badges">
          {showType && (
            <span className={`badge badge-type badge-type--${q.type?.toLowerCase()}`}>
              {TYPE_LABEL[q.type] ?? q.type}
            </span>
          )}
          <span
            className={`badge ${q.status === "RESOLVED" ? "badge-resolved" : "badge-open"}`}
          >
            {STATUS_LABEL[q.status] ?? q.status}
          </span>
        </div>
      </div>

      <div className="question-card__tags">
        {q.tags.map((tag) => (
          <span key={tag} className="tag-chip">
            {tag}
          </span>
        ))}
      </div>

      <div className="question-card__meta">
        <span className="author-with-badge">
          <span
            className="avatar-circle"
            style={
              q.authorAvatarColor
                ? { backgroundColor: q.authorAvatarColor }
                : undefined
            }
            aria-hidden="true"
          >
            {q.authorNickname?.[0] ?? "?"}
          </span>
          {q.authorNickname}
          {q.authorIsExpert && <ExpertBadge className="expert-badge--sm" />}
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

        <span className="question-card__time">
          {formatRelativeTime(q.createdAt)}
        </span>
      </div>
    </Link>
  );
}

export default QuestionCard;
