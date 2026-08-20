import { useEffect, useState } from "react";
import { getCodeComments, createCodeComment } from "../../api/codeCommentApi";

// 코드리뷰 유형 질문 본문 줄 단위로 렌더링, 각 줄에 코멘트.
function CodeReviewBody({ questionId, content, canComment }) {
  const [comments, setComments] = useState([]);
  const [hoveredLine, setHoveredLine] = useState(null);
  const [openLine, setOpenLine] = useState(null);
  const [draft, setDraft] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");

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

  const lines = content.split("\n");
  const commentsByLine = comments.reduce((acc, comment) => {
    (acc[comment.lineNumber] ??= []).push(comment);
    return acc;
  }, {});

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
              <ul className="code-review-thread">
                {lineComments.map((comment) => (
                  <li key={comment.id} className="code-review-thread__item">
                    <span className="code-review-thread__author">
                      {comment.authorNickname}
                    </span>
                    <span className="code-review-thread__content">
                      {comment.content}
                    </span>
                  </li>
                ))}
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
