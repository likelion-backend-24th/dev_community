import { useEffect, useState } from "react";
import {
  getCodeComments,
  createCodeComment,
  updateCodeComment,
  deleteCodeComment,
} from "../../api/codeCommentApi";

// 코드리뷰 유형 질문 본문 줄 단위로 렌더링, 각 줄에 코멘트.
function CodeReviewBody({ questionId, content, canComment, currentUserId, isAdmin }) {
  const [comments, setComments] = useState([]);
  const [hoveredLine, setHoveredLine] = useState(null);
  const [openLine, setOpenLine] = useState(null);
  const [draft, setDraft] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editDraft, setEditDraft] = useState("");
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editError, setEditError] = useState("");

  // 삭제 실패 메시지는 해당 코멘트 밑에 바로 보여준다: { commentId, message }
  const [deleteError, setDeleteError] = useState(null);

  useEffect(() => {
    if (!canComment) return;
    let cancelled = false;

    (async () => {
      try {
        const data = await getCodeComments(questionId);
        if (!cancelled) setComments(data);
      } catch {
        // 코멘트를 못 불러와도 본문 자체는 보여줄 수 있으니 조용히 무시
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [questionId, canComment]);

  const lines = content.split("\n");
  const commentsByLine = comments.reduce((acc, comment) => {
    (acc[comment.lineNumber] ??= []).push(comment);
    return acc;
  }, {});

  const handleStartEdit = (comment) => {
    setEditingCommentId(comment.id);
    setEditDraft(comment.content);
    setEditError("");
  };

  const handleCancelEdit = () => {
    setEditingCommentId(null);
    setEditDraft("");
    setEditError("");
  };

  const handleSaveEdit = async (comment) => {
    if (!editDraft.trim() || editSubmitting) return;
    setEditSubmitting(true);
    setEditError("");
    try {
      const updated = await updateCodeComment(
        questionId,
        comment.id,
        editDraft.trim(),
      );
      setComments((prev) =>
        prev.map((c) => (c.id === comment.id ? updated : c)),
      );
      setEditingCommentId(null);
      setEditDraft("");
    } catch (err) {
      setEditError(
        err.response?.data?.message ?? "코멘트 수정에 실패했습니다.",
      );
    } finally {
      setEditSubmitting(false);
    }
  };

  const handleDelete = async (comment) => {
    if (!window.confirm("코멘트를 삭제하시겠습니까?")) return;
    setDeleteError(null);
    try {
      await deleteCodeComment(questionId, comment.id);
      setComments((prev) => prev.filter((c) => c.id !== comment.id));
    } catch (err) {
      setDeleteError({
        commentId: comment.id,
        message: err.response?.data?.message ?? "코멘트 삭제에 실패했습니다.",
      });
    }
  };

  const handleToggleLine = (lineNumber) => {
    if (!canComment) return;
    setOpenLine((prev) => (prev === lineNumber ? null : lineNumber));
    setDraft("");
    setSubmitError("");
  };

  const handleSubmit = async (lineNumber) => {
    if (!draft.trim() || submitting) return;
    setSubmitting(true);
    setSubmitError("");
    try {
      const created = await createCodeComment(questionId, {
        lineNumber,
        content: draft.trim(),
      });
      setComments((prev) => [...prev, created]);
      setDraft("");
      setOpenLine(null);
    } catch (err) {
      setSubmitError(
        err.response?.data?.message ?? "코멘트 등록에 실패했습니다.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="code-review-body">
      {lines.map((line, index) => {
        const lineNumber = index + 1;
        const lineComments = commentsByLine[lineNumber] ?? [];
        const showAddButton =
          canComment && (hoveredLine === lineNumber || openLine === lineNumber);

        return (
          <div key={lineNumber} className="code-review-line-wrap">
            <div
              className="code-review-line"
              onMouseEnter={() => setHoveredLine(lineNumber)}
              onMouseLeave={() => setHoveredLine(null)}
              onClick={() => handleToggleLine(lineNumber)}
            >
              <span className="code-review-line__number">{lineNumber}</span>
              <span className="code-review-line__content">
                {line.length > 0 ? line : " "}
              </span>
              {showAddButton && (
                <button
                  type="button"
                  className="code-review-line__add"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleToggleLine(lineNumber);
                  }}
                  aria-label={`${lineNumber}번 줄에 코멘트 추가`}
                >
                  +
                </button>
              )}
            </div>

            {lineComments.length > 0 && (
              <ul
                className="code-review-thread"
                onClick={(e) => e.stopPropagation()}
              >
                {lineComments.map((comment) => {
                  const canManage =
                    isAdmin || comment.authorId === currentUserId;
                  const isEditing = editingCommentId === comment.id;

                  return (
                    <li key={comment.id} className="code-review-thread__item">
                      {isEditing ? (
                        <div className="code-review-thread__edit-form">
                          <textarea
                            className="textarea"
                            rows={2}
                            value={editDraft}
                            onChange={(e) => setEditDraft(e.target.value)}
                          />
                          {editError && (
                            <p className="inline-error" role="alert">
                              {editError}
                            </p>
                          )}
                          <div className="code-review-thread__form-actions">
                            <button
                              type="button"
                              className="btn btn-ghost btn-sm"
                              onClick={handleCancelEdit}
                            >
                              취소
                            </button>
                            <button
                              type="button"
                              className="btn btn-primary btn-sm"
                              disabled={editSubmitting}
                              onClick={() => handleSaveEdit(comment)}
                            >
                              {editSubmitting ? "저장 중..." : "저장"}
                            </button>
                          </div>
                        </div>
                      ) : (
                        <>
                          <span className="code-review-thread__author">
                            {comment.authorNickname}
                          </span>
                          <span className="code-review-thread__content">
                            {comment.content}
                          </span>
                          {canManage && (
                            <span className="code-review-thread__item-actions">
                              <button
                                type="button"
                                className="code-review-thread__item-action"
                                onClick={() => handleStartEdit(comment)}
                              >
                                수정
                              </button>
                              <button
                                type="button"
                                className="code-review-thread__item-action"
                                onClick={() => handleDelete(comment)}
                              >
                                삭제
                              </button>
                            </span>
                          )}
                          {deleteError?.commentId === comment.id && (
                            <p className="inline-error" role="alert">
                              {deleteError.message}
                            </p>
                          )}
                        </>
                      )}
                    </li>
                  );
                })}
              </ul>
            )}

            {openLine === lineNumber && (
              <div
                className="code-review-thread__form"
                onClick={(e) => e.stopPropagation()}
              >
                <textarea
                  className="textarea"
                  rows={2}
                  value={draft}
                  onChange={(e) => setDraft(e.target.value)}
                  placeholder={`${lineNumber}번 줄에 코멘트 남기기`}
                />
                {submitError && (
                  <p className="inline-error" role="alert">
                    {submitError}
                  </p>
                )}
                <div className="code-review-thread__form-actions">
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    onClick={() => setOpenLine(null)}
                  >
                    취소
                  </button>
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    disabled={submitting}
                    onClick={() => handleSubmit(lineNumber)}
                  >
                    {submitting ? "등록 중..." : "등록"}
                  </button>
                </div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}

export default CodeReviewBody;
