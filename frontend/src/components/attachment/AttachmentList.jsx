import { useEffect, useState } from "react";
import {
  getQuestionAttachments,
  getAnswerAttachments,
  deleteAttachment,
  getAttachmentUrl,
} from "../../api/attachmentApi";
import "../../styles/attachment.css";

const IMAGE_EXTENSIONS = ["png", "jpg", "jpeg", "gif", "webp"];

function isImage(filename) {
  const ext = filename.split(".").pop()?.toLowerCase();
  return IMAGE_EXTENSIONS.includes(ext);
}

function RemoveButton({ onClick, filename }) {
  return (
    <button
      type="button"
      className="attachment-list__remove"
      onClick={onClick}
      aria-label={`${filename} 삭제`}
    >
      ×
    </button>
  );
}

// 질문/답변 상세에서 이미 업로드된 첨부파일 목록을 보여준다.
// 이미지(사진)와 코드 파일은 레이아웃이 달라서 서로 다른 줄에 분리해서 표시한다.
// canDelete가 true면(작성자 본인 또는 관리자) 삭제 버튼을 노출한다.
function AttachmentList({ targetType, targetId, canDelete, reloadKey = 0 }) {
  const [attachments, setAttachments] = useState([]);

  useEffect(() => {
    let cancelled = false;
    const fetcher = targetType === "QUESTION" ? getQuestionAttachments : getAnswerAttachments;
    fetcher(targetId)
      .then((data) => !cancelled && setAttachments(data))
      .catch(() => {});
    return () => {
      cancelled = true;
    };
  }, [targetType, targetId, reloadKey]);

  const handleDelete = async (attachmentId) => {
    if (!window.confirm("첨부파일을 삭제하시겠습니까?")) return;
    try {
      await deleteAttachment(attachmentId);
      setAttachments((prev) => prev.filter((a) => a.id !== attachmentId));
    } catch {
      // 목록은 그대로 유지, 다음 새로고침 때 다시 시도 가능
    }
  };

  if (attachments.length === 0) return null;

  const images = attachments.filter((a) => isImage(a.originalFilename));
  const files = attachments.filter((a) => !isImage(a.originalFilename));

  return (
    <div className="attachment-groups">
      {images.length > 0 && (
        <ul className="attachment-list attachment-list--images">
          {images.map((a) => (
            <li key={a.id} className="attachment-list__item attachment-list__item--image">
              <a href={getAttachmentUrl(a.id)} target="_blank" rel="noreferrer">
                <img src={getAttachmentUrl(a.id)} alt={a.originalFilename} className="attachment-list__thumbnail" />
              </a>
              {canDelete && <RemoveButton onClick={() => handleDelete(a.id)} filename={a.originalFilename} />}
            </li>
          ))}
        </ul>
      )}

      {files.length > 0 && (
        <ul className="attachment-list attachment-list--files">
          {files.map((a) => (
            <li key={a.id} className="attachment-list__item attachment-list__item--file">
              <a href={getAttachmentUrl(a.id)} className="attachment-list__filename">
                📎 {a.originalFilename}
              </a>
              {canDelete && <RemoveButton onClick={() => handleDelete(a.id)} filename={a.originalFilename} />}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default AttachmentList;
