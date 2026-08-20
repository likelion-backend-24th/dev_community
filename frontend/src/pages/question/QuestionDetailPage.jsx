import { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { getQuestion, deleteQuestion } from "../../api/questionApi";
import { getAnswers, createAnswer } from "../../api/answerApi";
import { toggleQuestionLike, getLikeStatus } from "../../api/likeApi";
import { STATUS_LABEL } from "../../constants/questionStatus";
import ReportButton from "../../components/question/ReportButton";
import AnswerItem from "../../components/question/AnswerItem";
import CodeReviewBody from "../../components/question/CodeReviewBody";
import AlertModal from "../../components/common/AlertModal";
import AttachmentList from "../../components/attachment/AttachmentList";
import AttachmentPicker from "../../components/attachment/AttachmentPicker";
import { uploadAnswerAttachments } from "../../api/attachmentApi";
import "../../styles/question.css";
import "../../styles/error.css";

function QuestionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAdmin } = useAuth();

  const [question, setQuestion] = useState(null);
  const [answers, setAnswers] = useState([]);
  const [questionLiked, setQuestionLiked] = useState(false);
  const [likedAnswerIds, setLikedAnswerIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notFound, setNotFound] = useState(false);

  const [answerContent, setAnswerContent] = useState("");
  const [answerSubmitting, setAnswerSubmitting] = useState(false);
  const [answerError, setAnswerError] = useState("");
  const [selfAnswerAlertOpen, setSelfAnswerAlertOpen] = useState(false);
  const [answerAttachmentFiles, setAnswerAttachmentFiles] = useState([]);
  const [answerAttachmentError, setAnswerAttachmentError] = useState("");

  const [reloadKey, setReloadKey] = useState(0);
  const reload = () => setReloadKey((k) => k + 1);

  const [openTag, setOpenTag] = useState(null);
  const tagsRef = useRef(null);

  const previousUserRef = useRef(user);

  useEffect(() => {
    if (!openTag) return;
    const handleClickOutside = (e) => {
      if (tagsRef.current && !tagsRef.current.contains(e.target)) {
        setOpenTag(null);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [openTag]);

  useEffect(() => {
    const loggedOut = previousUserRef.current && !user;
    previousUserRef.current = user;
    if (loggedOut) return;

    let cancelled = false;

    const fetchOnce = async () => {
      const [questionData, answersData] = await Promise.all([
        getQuestion(id),
        user ? getAnswers(id) : Promise.resolve([]),
      ]);

      let likeStatus = { questionLiked: false, likedAnswerIds: [] };
      if (user) {
        likeStatus = await getLikeStatus(
          id,
          answersData.map((a) => a.id),
        );
      }

      return { questionData, answersData, likeStatus };
    };

    const fetchData = async () => {
      setLoading(true);
      setError("");
      setNotFound(false);
      try {
        let result;
        try {
          result = await fetchOnce();
        } catch (err) {
          if (err.response?.status === 404) throw err;
          result = await fetchOnce();
        }
        if (cancelled) return;
        setQuestion(result.questionData);
        setAnswers(result.answersData);
        setQuestionLiked(result.likeStatus.questionLiked);
        setLikedAnswerIds(result.likeStatus.likedAnswerIds);
      } catch (err) {
        if (cancelled) return;
        if (err.response?.status === 404) {
          setNotFound(true);
        } else {
          setError(
            err.response?.data?.message ?? "질문을 불러오지 못했습니다.",
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchData();
    return () => {
      cancelled = true;
    };
  }, [id, reloadKey, user]);

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
      const created = await createAnswer(id, answerContent);
      if (answerAttachmentFiles.length > 0) {
        await uploadAnswerAttachments(created.id, answerAttachmentFiles);
      }
      setAnswerContent("");
      setAnswerAttachmentFiles([]);
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
  if (notFound)
    return (
      <div className="error-page">
        <p className="error-page__code">404</p>
        <h1 className="error-page__title">질문을 찾을 수 없습니다</h1>
        <p className="error-page__desc">
          삭제되었거나 존재하지 않는 질문이에요.
        </p>
        <div className="error-page__actions">
          <Link to="/questions" className="btn btn-primary">
            질문 목록으로
          </Link>
        </div>
      </div>
    );
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

      <div className="question-detail__tags" ref={tagsRef}>
        {question.tags.length > 0 ? (
          question.tags.map((tag) => (
            <span key={tag} className="tag-chip-wrapper">
              <button
                type="button"
                className="tag-chip tag-chip--clickable"
                onClick={() =>
                  setOpenTag((prev) => (prev === tag ? null : tag))
                }
              >
                {tag}
              </button>
              {openTag === tag && (
                <div className="tag-search-popover">
                  <button
                    type="button"
                    className="tag-search-popover__btn"
                    onClick={() =>
                      navigate(`/questions?tag=${encodeURIComponent(tag)}`)
                    }
                  >
                    [{tag}] 검색하기
                  </button>
                </div>
              )}
            </span>
          ))
        ) : (
          <span className="question-card__meta">태그 없음</span>
        )}
      </div>

      {question.type === "CODE_REVIEW" ? (
        <CodeReviewBody
          questionId={id}
          content={question.content}
          canComment={Boolean(user)}
          currentUserId={user?.id}
          isAdmin={isAdmin}
        />
      ) : (
        <p className="question-detail__body">{question.content}</p>
      )}

      <AttachmentList targetType="QUESTION" targetId={id} canDelete={canEdit} />

      <div className="question-detail__actions">
        <button
          type="button"
          className={`btn btn-secondary btn-sm${questionLiked ? " btn-liked" : ""}`}
          onClick={handleLikeQuestion}
        >
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
          <button
            type="button"
            className="btn btn-destructive btn-sm"
            onClick={handleDeleteQuestion}
          >
            삭제
          </button>
        )}
        {user && !isOwner && (
          <ReportButton targetType="QUESTION" targetId={Number(id)} />
        )}
      </div>

      {user ? (
        <>
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
                isLiked={likedAnswerIds.includes(answer.id)}
                onChanged={reload}
              />
            ))}
          </ul>

          <form className="answer-form" onSubmit={handleAnswerSubmit}>
            <textarea
              className="textarea"
              value={answerContent}
              onChange={(e) => setAnswerContent(e.target.value)}
              placeholder="답변을 입력하세요"
              required
            />
            <AttachmentPicker
              files={answerAttachmentFiles}
              onChange={setAnswerAttachmentFiles}
              error={answerAttachmentError}
              onError={setAnswerAttachmentError}
            />
            {answerError && (
              <p className="inline-error" role="alert">
                {answerError}
              </p>
            )}
            <button
              type="submit"
              className="btn btn-primary"
              disabled={answerSubmitting}
            >
              {answerSubmitting ? "등록 중..." : "답변 등록"}
            </button>
          </form>
        </>
      ) : (
        <>
          <h2 className="answers-heading">답변</h2>
          <div className="guest-lock">
            <div className="guest-lock__placeholder" aria-hidden="true" />

            <div className="guest-lock__overlay">
              <p className="guest-lock__message">
                Dev_Community에 가입해서
                <br />
                다른 개발자들에게 도움받아 보세요!
              </p>
              <Link to="/login" className="btn btn-primary">
                로그인하기
              </Link>
            </div>
          </div>
        </>
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
