import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { createQuestion, getQuestion, updateQuestion } from "../../api/questionApi";

const MAX_TAG_COUNT = 5;

function QuestionFormPage() {
  const { id } = useParams();
  const isEditMode = Boolean(id);
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [tags, setTags] = useState([]);
  const [tagInput, setTagInput] = useState("");

  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [loading, setLoading] = useState(isEditMode);

  useEffect(() => {
    if (!isEditMode) return;

    let cancelled = false;

    (async () => {
      try {
        const question = await getQuestion(id);
        if (cancelled) return;
        setTitle(question.title);
        setContent(question.content);
        setTags(question.tags ?? []);
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

    const payload = { title, content, tags };

    try {
      if (isEditMode) {
        await updateQuestion(id, payload);
        navigate(`/questions/${id}`);
      } else {
        const created = await createQuestion(payload);
        navigate(`/questions/${created.id}`);
      }
    } catch (err) {
      setError(err.response?.data?.message ?? "저장 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div>불러오는 중...</div>;
  }

  return (
    <div>
      <h1>{isEditMode ? "질문 수정하기" : "질문 작성하기"}</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="title">제목</label><br></br>
          <input
            id="title"
            name="title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="질문 제목을 입력하세요"
          />
        </div>

        <div>
          <label htmlFor="content">본문</label><br></br>
          <textarea
            id="content"
            name="content"
            rows={12}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="질문 내용을 자세히 작성해주세요"
          />
        </div>

        <div>
          <label htmlFor="tagInput">태그 (최대 {MAX_TAG_COUNT}개)</label>
          <div>
            {tags.map((tag) => (
              <span key={tag}>
                #{tag}
                <button type="button" onClick={() => handleRemoveTag(tag)} aria-label={`${tag} 태그 삭제`}>
                  ×
                </button>
              </span>
            ))}
            <input
              id="tagInput"
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              onKeyDown={handleTagKeyDown}
              placeholder="태그 입력 후 Enter"
              disabled={tags.length >= MAX_TAG_COUNT}
            />
          </div>
        </div>

        {error && <p role="alert">{error}</p>}

        <div>
          <button type="button" onClick={handleCancel}>
            취소
          </button>
          <button type="submit" disabled={submitting}>
            {submitting ? "처리 중..." : isEditMode ? "수정완료" : "등록하기"}
          </button>
        </div>
      </form>
    </div>
  );
}

export default QuestionFormPage;