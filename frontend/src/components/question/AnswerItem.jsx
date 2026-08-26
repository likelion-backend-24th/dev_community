import { useState } from "react";
import {
  updateAnswer,
  deleteAnswer,
  adoptAnswer,
  cancelAdoption,
} from "../../api/answerApi";
import { toggleAnswerLike } from "../../api/likeApi";
import ReportButton from "./ReportButton";
import AttachmentList from "../attachment/AttachmentList";
import ExpertBadge from "../common/ExpertBadge";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeHighlight from "rehype-highlight";
import "highlight.js/styles/github-dark.css";
import "../../styles/question.css";
import "../../styles/markdown.css";

function AnswerItem({
  answer,
  currentUser,
  isAdmin,
  isQuestionOwner,
  questionResolved,
  isLiked,
  onChanged,
}) {
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(answer.content);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const isAuthor = currentUser?.id === answer.authorId;
  const canEdit = isAuthor || isAdmin;
  const canDelete = isAuthor || isAdmin;

  const runAction = async (action, failMessage) => {
    setBusy(true);
    setError("");
    try {
      await action();
      onChanged();
    } catch (err) {
      setError(err.response?.data?.message ?? failMessage);
    } finally {
      setBusy(false);
    }
  };

  const handleUpdate = (e) => {
    e.preventDefault();
    runAction(
      () => updateAnswer(answer.id, content),
      "답변 수정에 실패했습니다.",
    );
    setEditing(false);
  };

  const handleDelete = () => {
    if (!window.confirm("답변을 삭제하시겠습니까?")) return;
    runAction(() => deleteAnswer(answer.id), "답변 삭제에 실패했습니다.");
  };

  const handleAdopt = () =>
    runAction(() => adoptAnswer(answer.id), "채택에 실패했습니다.");

  const handleCancelAdoption = () =>
    runAction(() => cancelAdoption(answer.id), "채택 취소에 실패했습니다.");

  const handleLike = () =>
    runAction(() => toggleAnswerLike(answer.id), "추천 처리에 실패했습니다.");

  return (
    <li className={`answer-card${answer.adopted ? " is-adopted" : ""}`}>
      {answer.adopted && <span className="badge badge-resolved">✔ 채택됨</span>}

      {editing ? (
        <form onSubmit={handleUpdate}>
          <textarea
            className="textarea"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
          <div className="answer-card__actions">
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={busy}
            >
              저장
            </button>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() => {
                setEditing(false);
                setContent(answer.content);
              }}
            >
              취소
            </button>
          </div>
        </form>
      ) : (
        <div className="answer-card__body markdown-body">
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            rehypePlugins={[rehypeHighlight]}
          >
            {answer.content}
          </ReactMarkdown>
        </div>
      )}

      {!editing && (
        <AttachmentList
          targetType="ANSWER"
          targetId={answer.id}
          canDelete={canDelete}
        />
      )}

      <div className="answer-card__meta">
        <span className="author-with-badge">
          <span
            className="avatar-circle"
            style={answer.authorAvatarColor ? { backgroundColor: answer.authorAvatarColor } : undefined}
            aria-hidden="true"
          >
            {answer.authorNickname?.[0] ?? "?"}
          </span>
          {answer.authorNickname}
          {answer.authorIsExpert && (
            <ExpertBadge className="expert-badge--sm" />
          )}
        </span>
        <span>추천 {answer.likeCount}</span>
        <span>{new Date(answer.createdAt).toLocaleString()}</span>
      </div>

      {!editing && (
        <div className="answer-card__actions">
          <button
            type="button"
            className={`btn btn-secondary btn-sm${isLiked ? " btn-liked" : ""}`}
            onClick={handleLike}
            disabled={busy}
          >
            추천
          </button>
          {canEdit && (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => setEditing(true)}
            >
              수정
            </button>
          )}
          {canDelete && (
            <button
              type="button"
              className="btn btn-destructive btn-sm"
              onClick={handleDelete}
              disabled={busy}
            >
              삭제
            </button>
          )}
          {isQuestionOwner && !questionResolved && !answer.adopted && (
            <button
              type="button"
              className="btn btn-primary btn-sm"
              onClick={handleAdopt}
              disabled={busy}
            >
              채택
            </button>
          )}
          {isQuestionOwner && answer.adopted && (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={handleCancelAdoption}
              disabled={busy}
            >
              채택 취소
            </button>
          )}
          {currentUser && !isAuthor && (
            <ReportButton targetType="ANSWER" targetId={answer.id} />
          )}
        </div>
      )}

      {error && (
        <p className="inline-error" role="alert">
          {error}
        </p>
      )}
    </li>
  );
}

export default AnswerItem;
