import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getQuestion, deleteQuestion } from "../../api/questionApi";
import { getAnswers, createAnswer } from "../../api/answerApi";
import { toggleQuestionLike } from "../../api/likeApi";
import { STATUS_LABEL } from "../../constants/questionStatus";
import ReportButton from "../../components/question/ReportButton";
import AnswerItem from "../../components/question/AnswerItem";
import AlertModal from "../../components/common/AlertModal";
import "../../styles/question.css";

function QuestionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();

  const [question, setQuestion] = useState(null);
  const [answers, setAnswers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [answerContent, setAnswerContent] = useState("");
  const [answerSubmitting, setAnswerSubmitting] = useState(false);
  const [answerError, setAnswerError] = useState("");
  const [selfAnswerAlertOpen, setSelfAnswerAlertOpen] = useState(false);

  const [reloadKey, setReloadKey] = useState(0);
  const reload = () => setReloadKey((k) => k + 1);

  useEffect(() => {
    let cancelled = false;

    const fetchData = async () => {
      setLoading(true);
      setError("");
      try {
        const [questionData, answersData] = await Promise.all([
          getQuestion(id),
          getAnswers(id),
        ]);
        if (cancelled) return;
        setQuestion(questionData);
        setAnswers(answersData);
      } catch (err) {
        if (cancelled) return;
        setError(err.response?.data?.message ?? "질문을 불러오지 못했습니다.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchData();
    return () => {
      cancelled = true;
    };
  }, [id, reloadKey]);

  const handleLikeQuestion = async () => {
    try {
      await toggleQuestionLike(id);
      reload();
    } catch (err) {
      setError(err.response?.data?.message ?? "추천 처리에 실패했습니다.");
    }
  };

  const handleDeleteQuestion = async () => {
    if (!window.confirm("질문을 삭제하시겠습니까? 답변도 함께 삭제됩니다."))
      return;
    try {
      await deleteQuestion(id);
      navigate("/questions");
    } catch (err) {
      setError(err.response?.data?.message ?? "질문 삭제에 실패했습니다.");
    }
  };

  const handleAnswerSubmit = async (e) => {
    e.preventDefault();
    if (isOwner) {
      setSelfAnswerAlertOpen(true);
      return;
    }
    setAnswerSubmitting(true);
    setAnswerError("");
    try {
      await createAnswer(id, answerContent);
      setAnswerContent("");
      reload();
    } catch (err) {
      setAnswerError(
        err.response?.data?.message ?? "답변 등록에 실패했습니다.",
      );
    } finally {
      setAnswerSubmitting(false);
    }
  };

  if (loading) return <p className="state-text">불러오는 중...</p>;
  if (error)
    return (
      <div className="page">
        <p className="inline-error" role="alert">
          {error}
        </p>
      </div>
    );
  if (!question) return null;

  const isOwner = user?.id === question.authorId;
  const canEdit = isOwner || isAdmin;
  const canDelete = isOwner || isAdmin;

  return (
    <div className="page">
      <Link to="/questions" className="back-link">
        ← 목록으로
      </Link>

      <div className="question-detail__title-row">
        <h1 className="question-detail__title">{question.title}</h1>
        <span
          className={`badge ${question.status === "RESOLVED" ? "badge-resolved" : "badge-open"}`}
        >
          {STATUS_LABEL[question.status] ?? question.status}
        </span>
      </div>

      <div className="question-detail__meta">
        <span>{question.authorNickname}</span>
        <span>조회 {question.viewCount}</span>
        <span>추천 {question.likeCount}</span>
        <span>{new Date(question.createdAt).toLocaleString()}</span>
      </div>

      <div className="question-detail__tags">
        {question.tags.length > 0 ? (
          question.tags.map((tag) => (
            <span key={tag} className="tag-chip">
              {tag}
            </span>
          ))
        ) : (
          <span className="question-card__meta">태그 없음</span>
        )}
      </div>

      <p className="question-detail__body">{question.content}</p>

      <div className="question-detail__actions">
        <button type="button" className="btn btn-secondary btn-sm" onClick={handleLikeQuestion}>
          추천
        </button>
        {canEdit && (
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => navigate(`/questions/${id}/edit`)}
          >
            수정
          </button>
        )}
        {canDelete && (
          <button type="button" className="btn btn-destructive btn-sm" onClick={handleDeleteQuestion}>
            삭제
          </button>
        )}
        {user && !isOwner && (
          <ReportButton targetType="QUESTION" targetId={Number(id)} />
        )}
      </div>

      <h2 className="answers-heading">답변 {answers.length}개</h2>
      <ul className="answer-list">
        {answers.map((answer) => (
          <AnswerItem
            key={answer.id}
            answer={answer}
            currentUser={user}
            isAdmin={isAdmin}
            isQuestionOwner={isOwner}
            questionResolved={question.status === "RESOLVED"}
            onChanged={reload}
          />
        ))}
      </ul>

      {user ? (
        <form className="answer-form" onSubmit={handleAnswerSubmit}>
          <textarea
            className="textarea"
            value={answerContent}
            onChange={(e) => setAnswerContent(e.target.value)}
            placeholder="답변을 입력하세요"
            required
          />
          {answerError && (
            <p className="inline-error" role="alert">
              {answerError}
            </p>
          )}
          <button type="submit" className="btn btn-primary" disabled={answerSubmitting}>
            {answerSubmitting ? "등록 중..." : "답변 등록"}
          </button>
        </form>
      ) : (
        <p className="answer-form__prompt">
          <Link to="/login">로그인</Link> 후 답변을 작성할 수 있습니다.
        </p>
      )}

      {selfAnswerAlertOpen && (
        <AlertModal
          message="본인의 글에는 답변을 등록할 수 없습니다."
          onClose={() => setSelfAnswerAlertOpen(false)}
        />
      )}
    </div>
  );
}

export default QuestionDetailPage;
