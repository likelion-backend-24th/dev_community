import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  createQuestion,
  getQuestion,
  updateQuestion,
  suggestQuestionTags,
} from "../../api/questionApi";
import { useAuth } from "../../hooks/useAuth";
import { getMySubscription } from "../../api/subscriptionApi";
import { getCodeComments } from "../../api/codeCommentApi";
import { TYPE_CONTENT_TEMPLATE } from "../../constants/questionTemplates";
import { uploadQuestionAttachments } from "../../api/attachmentApi";
import AttachmentPicker from "../../components/attachment/AttachmentPicker";
import AttachmentList from "../../components/attachment/AttachmentList";
import ConfirmModal from "../../components/common/ConfirmModal";
import AlertModal from "../../components/common/AlertModal";
import "../../styles/question-form.css";

const MAX_TAG_COUNT = 5;

function QuestionFormPage() {
  const { id } = useParams();
  const isEditMode = Boolean(id);
  const navigate = useNavigate();
  const { isAdmin } = useAuth();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [tags, setTags] = useState([]);
  const [tagInput, setTagInput] = useState("");

  const [isPremium, setIsPremium] = useState(false);
  const [isAnonymous, setIsAnonymous] = useState(false);
  const [type, setType] = useState("GENERAL");
  const [typeLocked, setTypeLocked] = useState(false);
  const [contentLocked, setContentLocked] = useState(false);
  const [isSubscriber, setIsSubscriber] = useState(false);

  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(isEditMode);

  const [attachmentFiles, setAttachmentFiles] = useState([]);
  const [attachmentError, setAttachmentError] = useState("");

  // 본문에 작성한 내용이 있는 상태에서 유형을 바꿨을 때, 템플릿 적용 여부를
  // 확인받는 동안 대기 중인 다음 유형.
  const [pendingTypeChange, setPendingTypeChange] = useState(null);

  const [alertMessage, setAlertMessage] = useState("");

  useEffect(() => {
    if (!isEditMode) return;

    let cancelled = false;

    (async () => {
      try {
        let question;
        try {
          question = await getQuestion(id);
        } catch {
          question = await getQuestion(id);
        }
        if (cancelled) return;
        setTitle(question.title);
        setContent(question.content);
        setTags(question.tags ?? []);
        setIsPremium(question.premium ?? false);
        setIsAnonymous(question.anonymous ?? false);
        setType(question.type ?? "GENERAL");
        setTypeLocked(question.typeLocked ?? false);

        if (question.type === "CODE_REVIEW") {
          try {
            const comments = await getCodeComments(id);
            if (!cancelled) setContentLocked(comments.length > 0);
          } catch {}
        }
      } catch {
        if (!cancelled) setError("질문 정보를 불러오지 못했습니다.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [id, isEditMode]);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        let subscription;
        try {
          subscription = await getMySubscription();
        } catch {
          subscription = await getMySubscription();
        }
        if (!cancelled) setIsSubscriber(Boolean(subscription));
      } catch {
        if (!cancelled) setIsSubscriber(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  const handleTagKeyDown = (e) => {
    if (e.key !== "Enter") return;
    e.preventDefault();

    const value = tagInput.trim();
    if (!value) return;

    if (tags.length >= MAX_TAG_COUNT) {
      setError(`태그는 최대 ${MAX_TAG_COUNT}개까지 등록할 수 있습니다.`);
      return;
    }
    if (tags.includes(value)) {
      setTagInput("");
      return;
    }

    setTags((prev) => [...prev, value]);
    setTagInput("");
    setError("");
  };

  const handleRemoveTag = (target) => {
    setTags((prev) => prev.filter((t) => t !== target));
  };

  const [tagSuggestLoading, setTagSuggestLoading] = useState(false);
  const [tagSuggestError, setTagSuggestError] = useState("");

  const handleSuggestTags = async () => {
    setTagSuggestLoading(true);
    setTagSuggestError("");
    try {
      const suggested = await suggestQuestionTags(title, content);
      setTags(suggested);
    } catch (err) {
      setTagSuggestError(
        err.response?.data?.message ?? "태그 추천에 실패했습니다.",
      );
    } finally {
      setTagSuggestLoading(false);
    }
  };

  const handleCancel = () => {
    navigate(-1);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!title.trim() || !content.trim()) {
      setError("제목과 본문을 모두 입력해주세요.");
      return;
    }

    setError("");
    setSubmitting(true);

    const payload = isEditMode
      ? { title, content, tags, isAnonymous, type }
      : { title, content, tags, isPremium, isAnonymous, type };

    try {
      const questionId = isEditMode ? id : (await createQuestion(payload)).id;
      if (isEditMode) {
        await updateQuestion(id, payload);
      }
      if (attachmentFiles.length > 0) {
        await uploadQuestionAttachments(questionId, attachmentFiles);
      }
      navigate(`/questions/${questionId}`);
    } catch (err) {
      setError(err.response?.data?.message ?? "저장 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p className="state-text">불러오는 중...</p>;
  }

  return (
    <div className="page">
      <div className="page__header">
        <h1 className="page__title">
          {isEditMode ? "질문 수정하기" : "질문 작성하기"}
        </h1>
      </div>

      <form className="question-form" onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="title">제목</label>
          <input
            id="title"
            name="title"
            className="input"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="질문 제목을 입력하세요"
          />
        </div>

        <div className="form-field">
          <label htmlFor="content">본문</label>
          <textarea
            id="content"
            name="content"
            className="textarea question-form__content"
            rows={12}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="질문 내용을 자세히 작성해주세요"
            disabled={contentLocked}
          />
        </div>

        <div className="form-field">
          <label htmlFor="tagInput">태그 (최대 {MAX_TAG_COUNT}개)</label>
          <div className="tag-input">
            {tags.map((tag) => (
              <span key={tag} className="tag-chip tag-chip--removable">
                #{tag}
                <button
                  type="button"
                  className="tag-chip__remove"
                  onClick={() => handleRemoveTag(tag)}
                  aria-label={`${tag} 태그 삭제`}
                >
                  ×
                </button>
              </span>
            ))}
            <input
              id="tagInput"
              className="tag-input__field"
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              onKeyDown={handleTagKeyDown}
              placeholder="태그 입력 후 Enter"
              disabled={tags.length >= MAX_TAG_COUNT}
            />
          </div>

          {(isAdmin || isSubscriber) && (
            <>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={handleSuggestTags}
                disabled={tagSuggestLoading || !content.trim()}
              >
                {tagSuggestLoading ? "추천 태그 생성 중..." : "추천 태그"}
              </button>
              {tagSuggestError && (
                <p className="inline-error" role="alert">
                  {tagSuggestError}
                </p>
              )}
            </>
          )}
        </div>

        {(isSubscriber || isEditMode) && (
          <div className="form-field">
            <label className="question-form__checkbox">
              <input
                type="checkbox"
                checked={isPremium}
                disabled={isEditMode}
                onChange={(e) => {
                  setIsPremium(e.target.checked);
                  if (!e.target.checked) {
                    setIsAnonymous(false);
                    setType("GENERAL");
                  }
                }}
              />
              멤버십(구독자 전용) 게시판에 작성
            </label>
            {isEditMode && (
              <p className="form-field__hint">
                게시판 구분은 작성 후에는 바꿀 수 없어요.
              </p>
            )}
          </div>
        )}

        {isPremium && (
          <>
            <div className="form-field">
              <label htmlFor="questionType">글 유형</label>
              <select
                id="questionType"
                className="select"
                value={type}
                onChange={(e) => {
                  if (typeLocked) {
                    setAlertMessage(
                      "채팅이 시작된 질문은 글 유형을 변경할 수 없어요.",
                    );
                    return;
                  }
                  if (contentLocked) {
                    setAlertMessage(
                      "코드 코멘트가 달려있어 글 유형을 변경할 수 없어요.",
                    );
                    return;
                  }

                  const nextType = e.target.value;

                  const isUntouched =
                    !content.trim() || content === TYPE_CONTENT_TEMPLATE[type];

                  if (isUntouched || !TYPE_CONTENT_TEMPLATE[nextType]) {
                    setType(nextType);
                    if (isUntouched) {
                      setContent(TYPE_CONTENT_TEMPLATE[nextType] ?? "");
                    }
                    return;
                  }
                  setPendingTypeChange(nextType);
                }}
              >
                <option value="GENERAL">일반</option>
                <option value="CODE_REVIEW">코드리뷰</option>
                <option value="CAREER_CONSULT">커리어상담</option>
              </select>
              {typeLocked && (
                <p className="form-field__hint">
                  채팅이 시작된 질문은 글 유형을 변경할 수 없어요.
                </p>
              )}
              {type === "CODE_REVIEW" && (
                <p className="inline-error" role="alert">
                  {contentLocked ? (
                    "코드 코멘트가 달려있어 지금은 본문을 수정할 수 없어요!"
                  ) : (
                    <>
                      코드리뷰 유형은 코드 코멘트가 하나라도 달리면 이후 본문을
                      수정할 수 없어요!!
                      <br />
                      <br />
                      코드 블록을 삽입하는 경우 등록 전 다시 한번 더 본문 내용을
                      확인해주세요.
                    </>
                  )}
                </p>
              )}
            </div>

            <div className="form-field">
              <label className="question-form__checkbox">
                <input
                  type="checkbox"
                  checked={isAnonymous}
                  onChange={(e) => setIsAnonymous(e.target.checked)}
                />
                익명으로 작성
              </label>
            </div>
          </>
        )}

        <div className="form-field">
          <label>첨부파일</label>
          {isEditMode && (
            <AttachmentList targetType="QUESTION" targetId={id} canDelete />
          )}
          <AttachmentPicker
            files={attachmentFiles}
            onChange={setAttachmentFiles}
            error={attachmentError}
            onError={setAttachmentError}
          />
        </div>

        {error && (
          <p className="inline-error" role="alert">
            {error}
          </p>
        )}

        <div className="question-form__actions">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={handleCancel}
          >
            취소
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting ? "처리 중..." : isEditMode ? "수정완료" : "등록하기"}
          </button>
        </div>
      </form>

      {pendingTypeChange && (
        <ConfirmModal
          message="작성한 내용이 사라져요. 새 템플릿으로 변경할까요?"
          confirmLabel="변경"
          cancelLabel="취소"
          onConfirm={() => {
            setType(pendingTypeChange);
            setContent(TYPE_CONTENT_TEMPLATE[pendingTypeChange]);
            setPendingTypeChange(null);
          }}
          onCancel={() => {
            setPendingTypeChange(null);
          }}
        />
      )}

      {alertMessage && (
        <AlertModal
          message={alertMessage}
          onClose={() => setAlertMessage("")}
        />
      )}
    </div>
  );
}

export default QuestionFormPage;
