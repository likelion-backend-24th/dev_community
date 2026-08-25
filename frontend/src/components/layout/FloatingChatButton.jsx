import { Link } from "react-router-dom";
import { useChatBadge } from "../../hooks/useChatBadge";

function FloatingChatButton() {
  const { unreadCount } = useChatBadge();

  return (
    <Link to="/chats" className="floating-chat-btn" aria-label="내 채팅">
      내 채팅
      {unreadCount > 0 && (
        <span className="floating-chat-btn__badge">
          {unreadCount > 99 ? "99+" : unreadCount}
        </span>
      )}
    </Link>
  );
}

export default FloatingChatButton;
