import { useCallback, useEffect, useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { Highlight } from "prism-react-renderer";
import Prism from "prismjs/components/prism-core";
import "prismjs/components/prism-markup";
import "prismjs/components/prism-css";
import "prismjs/components/prism-clike";
import "prismjs/components/prism-javascript";
import "prismjs/components/prism-typescript";
import "prismjs/components/prism-jsx";
import "prismjs/components/prism-tsx";
import "prismjs/components/prism-c";
import "prismjs/components/prism-cpp";
import "prismjs/components/prism-csharp";
import "prismjs/components/prism-python";
import "prismjs/components/prism-java";
import "prismjs/components/prism-kotlin";
import "prismjs/components/prism-swift";
import "prismjs/components/prism-go";
import "prismjs/components/prism-sql";
import "prismjs/components/prism-bash";
import "prismjs/components/prism-json";
import "prismjs/components/prism-yaml";
import "../../styles/prism.css";
import {
  getCodeComments,
  createCodeComment,
  updateCodeComment,
  deleteCodeComment,
} from "../../api/codeCommentApi";

const NO_INLINE_THEME = { plain: {}, styles: [] };

function CodeReviewBody({
  questionId,
  content,
  canComment,
  currentUserId,
  isAdmin,
}) {
  const [comments, setComments] = useState([]);

  useEffect(() => {
    if (!canComment) return;
    let cancelled = false;

    (async () => {
      try {
        const data = await getCodeComments(questionId);
        if (!cancelled) setComments(data);
      } catch {}
    })();

    return () => {
      cancelled = true;
    };
  }, [questionId, canComment]);

  const commentsByLine = comments.reduce((acc, comment) => {
    (acc[comment.lineNumber] ??= []).push(comment);
    return acc;
  }, {});

  const handleCommentCreated = useCallback((comment) => {
    setComments((prev) => [...prev, comment]);
  }, []);

  const handleCommentUpdated = useCallback((comment) => {
    setComments((prev) => prev.map((c) => (c.id === comment.id ? comment : c)));
  }, []);

  const handleCommentDeleted = useCallback((commentId) => {
    setComments((prev) => prev.filter((c) => c.id !== commentId));
  }, []);

  return (
    <div className="markdown-body">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          code({ node, className, children }) {
            const isFenced =
              node.position.start.line !== node.position.end.line;
            if (!isFenced) {
              return <code className={className}>{children}</code>;
            }

            const startLine = node.position.start.line + 1;
            const codeText = String(children).replace(/\n$/, "");
            const language =
              /language-(\w+)/.exec(className ?? "")?.[1] ?? "text";

            return (
              <CodeFenceBlock
                questionId={questionId}
                code={codeText}
                language={language}
                startLine={startLine}
                canComment={canComment}
                currentUserId={currentUserId}
                isAdmin={isAdmin}
                commentsByLine={commentsByLine}
                onCommentCreated={handleCommentCreated}
                onCommentUpdated={handleCommentUpdated}
                onCommentDeleted={handleCommentDeleted}
              />
            );
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}

function CodeFenceBlock({
  questionId,
  code,
  language,
  startLine,
  canComment,
  currentUserId,
  isAdmin,
  commentsByLine,
  onCommentCreated,
  onCommentUpdated,
  onCommentDeleted,
}) {
  const [hoveredLine, setHoveredLine] = useState(null);
  const [openLine, setOpenLine] = useState(null);
  const [draft, setDraft] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editDraft, setEditDraft] = useState("");
  const [editSubmitting, setEditSubmitting] = useState(false);
  const [editError, setEditError] = useState("");

  const [deleteError, setDeleteError] = useState(null);

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
      onCommentCreated(created);
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
      onCommentUpdated(updated);
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
      onCommentDeleted(comment.id);
    } catch (err) {
      setDeleteError({
        commentId: comment.id,
        message: err.response?.data?.message ?? "코멘트 삭제에 실패했습니다.",
      });
    }
  };

  return (
    <Highlight
      prism={Prism}
      code={code}
      language={language}
      theme={NO_INLINE_THEME}
    >
      {({ tokens, getLineProps, getTokenProps }) => (
        <div className="code-review-body">
          {tokens.map((lineTokens, index) => {
            const lineNumber = startLine + index;
            const lineComments = commentsByLine[lineNumber] ?? [];
            const showAddButton =
              canComment &&
              (hoveredLine === lineNumber || openLine === lineNumber);
            const lineProps = getLineProps({ line: lineTokens });

            return (
              <div key={lineNumber} className="code-review-line-wrap">
                <div
                  {...lineProps}
                  className={`code-review-line ${lineProps.className ?? ""}`}
                  onMouseEnter={() => setHoveredLine(lineNumber)}
                  onMouseLeave={() => setHoveredLine(null)}
                  onClick={() => handleToggleLine(lineNumber)}
                >
                  <span className="code-review-line__number">{lineNumber}</span>
                  <span className="code-review-line__content">
                    {lineTokens.length > 0
                      ? lineTokens.map((token, tokenIndex) => (
                          <span
                            key={tokenIndex}
                            {...getTokenProps({ token })}
                          />
                        ))
                      : " "}
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
                        <li
                          key={comment.id}
                          className="code-review-thread__item"
                        >
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
      )}
    </Highlight>
  );
}

export default CodeReviewBody;
