import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getQuestion, deleteQuestion } from "../../api/questionApi";
import { getAnswers, createAnswer } from "../../api/answerApi";
import { toggleQuestionLike } from "../../api/likeApi";
import { STATUS_LABEL } from "../../constants/questionStatus";
import ReportButton from "../../components/question/ReportButton";
import AnswerItem from "../../components/question/AnswerItem";

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
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message ?? "질문 삭제에 실패했습니다.");
    }
  };

  const handleAnswerSubmit = async (e) => {
    e.preventDefault();
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

  if (loading) return <p>불러오는 중...</p>;
  if (error) return <p role="alert">{error}</p>;
  if (!question) return null;

  const isOwner = user?.id === question.authorId;
  const canEdit = isOwner || isAdmin;
  const canDelete = isOwner || isAdmin;

  return (
    <div>
      <h1>{question.title}</h1>
      <p>
        <span>{question.authorNickname}</span>
        <span> · [{STATUS_LABEL[question.status] ?? question.status}]</span>
        <span> · 조회 {question.viewCount}</span>
        <span> · 추천 {question.likeCount}</span>
        <span> · {new Date(question.createdAt).toLocaleString()}</span>
      </p>
      <p>
        태그: {question.tags.length > 0 ? question.tags.join(", ") : "없음"}
      </p>

      <p>{question.content}</p>

      <div>
        <button type="button" onClick={handleLikeQuestion}>
          추천
        </button>
        {canEdit && <Link to={`/questions/${id}/edit`}>수정</Link>}
        {canDelete && (
          <button type="button" onClick={handleDeleteQuestion}>
            삭제
          </button>
        )}
        {user && !isOwner && (
          <ReportButton targetType="QUESTION" targetId={Number(id)} />
        )}
      </div>

      <Link to="/">목록으로</Link>

      <h2>답변 {answers.length}개</h2>
      <ul>
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
        <form onSubmit={handleAnswerSubmit}>
          <textarea
            value={answerContent}
            onChange={(e) => setAnswerContent(e.target.value)}
            placeholder="답변을 입력하세요"
            required
          />
          {answerError && <p role="alert">{answerError}</p>}
          <button type="submit" disabled={answerSubmitting}>
            {answerSubmitting ? "등록 중..." : "답변 등록"}
          </button>
        </form>
      ) : (
        <p>
          <Link to="/login">로그인</Link> 후 답변을 작성할 수 있습니다.
        </p>
      )}
    </div>
  );
}

export default QuestionDetailPage;
