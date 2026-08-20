import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getMyChatRooms } from "../../api/chatApi";
import ExpertBadge from "../../components/common/ExpertBadge";
import "../../styles/chat.css";

const STATUS_LABEL = {
  PENDING: "수락 대기",
  ACTIVE: "진행중",
  ADOPTED: "채택됨",
  CLOSED: "종료됨",
};

const ROLE_LABEL = {
  ANSWERER: "내가 건 채팅",
};

function ChatListPage() {
  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      setError("");
      try {
        const data = await getMyChatRooms();
        if (!cancelled) setRooms(data);
      } catch (err) {
        if (!cancelled) {
          setError(err.response?.data?.message ?? "채팅 목록을 불러오지 못했습니다.");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="page">
      <div className="page__header">
        <h1 className="page__title">내 채팅</h1>
      </div>

      {loading && <p className="state-text">불러오는 중...</p>}
      {error && (
        <p className="inline-error" role="alert">
          {error}
        </p>
      )}

      {!loading && !error && rooms.length === 0 && (
        <div className="empty-state">
          <p>아직 진행 중인 1:1 채팅이 없어요.</p>
        </div>
      )}

      {!loading && !error && rooms.length > 0 && (
        <ul className="chat-room-list">
          {rooms.map((room) => (
            <li key={room.id}>
              <Link
                to={`/chats/${room.id}`}
                className={`chat-room-card${room.unread ? " chat-room-card--unread" : ""}`}
              >
                <div className="chat-room-card__top">
                  {room.role === "ANSWERER" && (
                    <span className="badge badge-type">{ROLE_LABEL[room.role]}</span>
                  )}
                  <span
                    className={`badge ${room.status === "ADOPTED" ? "badge-resolved" : "badge-open"}`}
                  >
                    {STATUS_LABEL[room.status] ?? room.status}
                  </span>
                </div>
                <p className="chat-room-card__title">{room.questionTitle}</p>
                <div className="chat-room-card__meta">
                  <span className="author-with-badge">
                    {room.counterpartNickname}
                    {room.counterpartIsExpert && <ExpertBadge className="expert-badge--sm" />}
                  </span>
                  <span>평판 {room.counterpartReputation}</span>
                </div>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default ChatListPage;
