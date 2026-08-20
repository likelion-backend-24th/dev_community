import { Link } from "react-router-dom";

function FloatingWriteButton() {
  return (
    <Link to="/questions/new" className="floating-write-btn" aria-label="새 질문 작성">
      새 질문
    </Link>
  );
}

export default FloatingWriteButton;
