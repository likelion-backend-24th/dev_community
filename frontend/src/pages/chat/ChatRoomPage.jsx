import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useChatSocket } from "../../hooks/useChatSocket";
import { useChatBadge } from "../../hooks/useChatBadge";
import ExpertBadge from "../../components/common/ExpertBadge";
import {
  getChatRoom,
  sendChatMessage,
  acceptChat,
  adoptChat,
  markChatRoomRead,
} from "../../api/chatApi";
import "../../styles/chat.css";

const STATUS_LABEL = {
  PENDING: "수락 대기",
  ACTIVE: "진행중",
  ADOPTED: "채택됨",
  CLOSED: "종료됨",
};

function ChatRoomPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const { refreshUnreadCount } = useChatBadge();

  const [room, setRoom] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [content, setContent] = useState("");
  const [sending, setSending] = useState(false);
  const [actionBusy, setActionBusy] = useState(false);
  const [actionError, setActionError] = useState("");

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      setError("");
      try {
        const data = await getChatRoom(id);
        if (cancelled) return;
        setRoom(data);
        setMessages(data.messages);
        refreshUnreadCount();
      } catch (err) {
        if (!cancelled) {
          setError(err.response?.data?.message ?? "채팅방을 불러오지 못했습니다.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [id, refreshUnreadCount]);

  useChatSocket({
    onMessage: (message) => {
      if (String(message.chatRoomId) !== String(id)) return;
      setMessages((prev) =>
        prev.some((m) => m.id === message.id) ? prev : [...prev, message],
      );
      // 이 방을 이미 보고 있는 중에 실시간으로 온 메시지이므로 곧바로 읽음 처리해서
      // 내 채팅 목록에 안읽음으로 남지 않게 한다.
      markChatRoomRead(id).then(refreshUnreadCount);
    },
    onRoomUpdate: (update) => {
      if (String(update.roomId) !== String(id)) return;
      setRoom((prev) => (prev ? { ...prev, status: update.status } : prev));
    },
  });

  const handleSend = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;
    setSending(true);
    setActionError("");
    try {
      const message = await sendChatMessage(id, content);
      setMessages((prev) => [...prev, message]);
      setContent("");
    } catch (err) {
      setActionError(err.response?.data?.message ?? "메시지 전송에 실패했습니다.");
    } finally {
      setSending(false);
    }
  };

  const handleAccept = async () => {
    if (!window.confirm("이 채팅을 수락하시겠습니까? 이후 자유롭게 대화할 수 있어요."))
      return;
    setActionBusy(true);
    setActionError("");
    try {
      const updated = await acceptChat(id);
      setRoom(updated);
      refreshUnreadCount();
    } catch (err) {
      setActionError(err.response?.data?.message ?? "채팅 수락에 실패했습니다.");
    } finally {
      setActionBusy(false);
    }
  };

  const handleAdopt = async () => {
    if (!window.confirm("이 채팅을 답변으로 채택하시겠습니까? 질문이 해결 처리됩니다."))
      return;
    setActionBusy(true);
    setActionError("");
    try {
      const updated = await adoptChat(id);
      setRoom(updated);
      refreshUnreadCount();
    } catch (err) {
      setActionError(err.response?.data?.message ?? "채택에 실패했습니다.");
    } finally {
      setActionBusy(false);
    }
  };

  if (loading) return <p className="state-text">불러오는 중...</p>;
  if (error)
    return (
      <div className="page">
        <p className="inline-error" role="alert">
          {error}
        </p>
      </div>
    );
  if (!room) return null;

  const isQuestioner = room.role === "QUESTIONER";
  const canSend = room.status === "ACTIVE";

  return (
    <div className="page">
      <Link to="/chats" className="back-link">
        ← 내 채팅으로
      </Link>

      <div className="page__header">
        <h1 className="page__title">
          <Link to={`/questions/${room.questionId}`}>{room.questionTitle}</Link>
        </h1>
        <span className={`badge ${room.status === "ADOPTED" ? "badge-resolved" : "badge-open"}`}>
          {STATUS_LABEL[room.status] ?? room.status}
        </span>
      </div>

      <div className="chat-room__counterpart">
        {isQuestioner ? (
          <>
            <span className="author-with-badge">
              {room.answererNickname}
              {room.answererIsExpert && <ExpertBadge className="expert-badge--sm" />}
            </span>
            <span>평판 {room.answererReputation}</span>
          </>
        ) : (
          <span>질문자: {room.questionerNickname}</span>
        )}
      </div>

      {isQuestioner && room.status === "PENDING" && (
        <p className="state-text state-text--plain">
          답변자의 첫 메시지를 확인한 뒤 채팅을 수락하면 대화를 이어갈 수 있어요.
        </p>
      )}
      {!isQuestioner && room.status === "PENDING" && (
        <p className="state-text state-text--plain">질문자가 채팅을 수락하면 대화를 이어갈 수 있어요.</p>
      )}
      {room.status === "ADOPTED" && (
        <p className="state-text state-text--plain">이 채팅이 답변으로 채택되었어요.</p>
      )}
      {room.status === "CLOSED" && (
        <p className="state-text state-text--plain">
          다른 답변자의 채팅이 채택되어 이 질문은 해결되었어요. 채팅이 종료되었습니다.
        </p>
      )}

      <ul className="chat-message-list">
        {messages.map((message) => (
          <li
            key={message.id}
            className={`chat-message${message.senderId === user?.id ? " chat-message--mine" : ""}`}
          >
            <div className="chat-message__bubble">
              <p className="chat-message__sender">{message.senderNickname}</p>
              <p className="chat-message__content">{message.content}</p>
              <p className="chat-message__time">
                {new Date(message.createdAt).toLocaleString()}
              </p>
            </div>
          </li>
        ))}
      </ul>

      {actionError && (
        <p className="inline-error" role="alert">
          {actionError}
        </p>
      )}

      <div className="chat-room__actions">
        {isQuestioner && room.status === "PENDING" && (
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleAccept}
            disabled={actionBusy}
          >
            채팅 수락
          </button>
        )}
        {isQuestioner && room.status === "ACTIVE" && (
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleAdopt}
            disabled={actionBusy}
          >
            답변으로 채택
          </button>
        )}
      </div>

      {canSend && (
        <form className="chat-message-form" onSubmit={handleSend}>
          <textarea
            className="textarea"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="메시지를 입력하세요"
            required
          />
          <button type="submit" className="btn btn-primary" disabled={sending}>
            {sending ? "전송 중..." : "전송"}
          </button>
        </form>
      )}
    </div>
  );
}

export default ChatRoomPage;
