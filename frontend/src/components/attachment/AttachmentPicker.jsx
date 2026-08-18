import {
  ALLOWED_ATTACHMENT_EXTENSIONS,
  MAX_ATTACHMENT_COUNT,
  MAX_ATTACHMENT_SIZE,
} from "../../api/attachmentApi";
import "../../styles/attachment.css";

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes}B`;
  return `${(bytes / 1024).toFixed(1)}KB`;
}

// 질문/답변 작성 폼에서 아직 서버로 업로드되지 않은 파일을 선택/검증/미리보기하는 컴포넌트.
// 실제 업로드는 질문/답변이 먼저 생성되어 id가 생긴 뒤 부모가 처리한다.
function AttachmentPicker({ files, onChange, error, onError }) {
  const handleFileSelect = (e) => {
    const selected = Array.from(e.target.files ?? []);
    e.target.value = "";
    if (selected.length === 0) return;

    if (files.length + selected.length > MAX_ATTACHMENT_COUNT) {
      onError(`첨부파일은 최대 ${MAX_ATTACHMENT_COUNT}개까지 등록할 수 있습니다.`);
      return;
    }

    for (const file of selected) {
      const ext = file.name.split(".").pop()?.toLowerCase();
      if (!ALLOWED_ATTACHMENT_EXTENSIONS.includes(ext)) {
        onError(`지원하지 않는 파일 형식입니다: ${file.name}`);
        return;
      }
      if (file.size > MAX_ATTACHMENT_SIZE) {
        onError(`파일 용량이 너무 큽니다 (최대 2MB): ${file.name}`);
        return;
      }
    }

    onError("");
    onChange([...files, ...selected]);
  };

  const handleRemove = (index) => {
    onChange(files.filter((_, i) => i !== index));
  };

  return (
    <div className="attachment-picker">
      <label htmlFor="attachmentInput" className="btn btn-secondary btn-sm attachment-picker__button">
        파일 첨부
      </label>
      <input
        id="attachmentInput"
        type="file"
        multiple
        className="attachment-picker__input"
        accept={ALLOWED_ATTACHMENT_EXTENSIONS.map((ext) => `.${ext}`).join(",")}
        onChange={handleFileSelect}
      />
      <span className="attachment-picker__hint">
        코드 파일 또는 작은 이미지, 파일당 최대 2MB, 최대 {MAX_ATTACHMENT_COUNT}개
      </span>

      {error && (
        <p className="inline-error" role="alert">
          {error}
        </p>
      )}

      {files.length > 0 && (
        <ul className="attachment-picker__list">
          {files.map((file, index) => (
            <li key={`${file.name}-${index}`} className="attachment-picker__item">
              <span className="attachment-picker__name">{file.name}</span>
              <span className="attachment-picker__size">{formatSize(file.size)}</span>
              <button
                type="button"
                className="attachment-picker__remove"
                onClick={() => handleRemove(index)}
                aria-label={`${file.name} 제거`}
              >
                ×
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default AttachmentPicker;
