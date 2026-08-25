import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useNotification } from "../../hooks/useNotification";
import { useChatBadge } from "../../hooks/useChatBadge";
import { logout as logoutApi } from "../../api/authApi";
import {
  getRecentNotifications,
  markAllNotificationsRead,
} from "../../api/notificationApi";

/**
 * 우측 액션 레일 — 예전 Navbar의 알림/계정 메뉴와 플로팅 버튼(질문 작성, 내 채팅)이
 * 하던 일을 한 곳에 모은 커맨드 슬랩. 동작은 옮기기 전과 동일하다.
 */
function ActionRail() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const { notification, dismiss } = useNotification();
  const { unreadCount } = useChatBadge();
  const navigate = useNavigate();

  const [notifOpen, setNotifOpen] = useState(false);
  const [notifItems, setNotifItems] = useState([]);
  const [notifLoading, setNotifLoading] = useState(false);
  const notifRef = useRef(null);

  useEffect(() => {
    if (!notifOpen) return undefined;
    const handleClickOutside = (e) => {
      if (notifRef.current && !notifRef.current.contains(e.target)) {
        setNotifOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [notifOpen]);

  const handleBellClick = async () => {
    const opening = !notifOpen;
    setNotifOpen(opening);
    if (!opening) return;

    dismiss();
    setNotifLoading(true);
    try {
      const items = await getRecentNotifications();
      setNotifItems(items);
      await markAllNotificationsRead();
    } catch {
      setNotifItems([]);
    } finally {
      setNotifLoading(false);
    }
  };

  const handleNotifItemClick = (link) => {
    setNotifOpen(false);
    navigate(link);
  };

  const handleLogout = async () => {
    try {
      await logoutApi();
    } catch (err) {
      // 실패해도 클라이언트 쪽 로그아웃은 진행
      console.error("로그아웃 요청 실패", err);
    } finally {
      navigate("/login", { replace: true });
      logout();
    }
  };

  return (
    <aside className="rail" aria-label="바로가기">
      {isAuthenticated ? (
        <>
          <Link to="/questions/new" className="rail__btn rail__btn--go">
            <span className="rail__icon" aria-hidden="true">
              ✎
            </span>
            질문 작성
          </Link>

          <div className="rail__notif-wrap" ref={notifRef}>
            <button
              type="button"
              className="rail__btn"
              onClick={handleBellClick}
              aria-haspopup="true"
              aria-expanded={notifOpen}
            >
              <span className="rail__icon" aria-hidden="true">
                ✉
              </span>
              알림
              {notification && <span className="rail__dot" />}
            </button>

            {notifOpen && (
              <div className="rail__menu">
                <p className="rail__menu-title">알림</p>
                {notifLoading && <p className="rail__menu-empty">불러오는 중...</p>}
                {!notifLoading && notifItems.length === 0 && (
                  <p className="rail__menu-empty">새 알림이 없어요.</p>
                )}
                {!notifLoading &&
                  notifItems.map((item) => (
                    <button
                      key={item.id}
                      type="button"
                      className={`rail__menu-item${item.isRead ? "" : " rail__menu-item--unread"}`}
                      onClick={() => handleNotifItemClick(item.link)}
                    >
                      <span className="rail__menu-item-message">{item.message}</span>
                      <span className="rail__menu-item-time">
                        {new Date(item.createdAt).toLocaleString()}
                      </span>
                    </button>
                  ))}
              </div>
            )}
          </div>

          <Link to="/chats" className="rail__btn">
            <span className="rail__icon" aria-hidden="true">
              ▤
            </span>
            내 채팅
            {unreadCount > 0 && (
              <span className="rail__badge">
                {unreadCount > 99 ? "99+" : unreadCount}
              </span>
            )}
          </Link>

          <Link to="/mypage" className="rail__btn">
            <span className="rail__icon" aria-hidden="true">
              ●
            </span>
            마이페이지
          </Link>

          <Link to="/dashboard" className="rail__btn">
            <span className="rail__icon" aria-hidden="true">
              ▦
            </span>
            내 활동
          </Link>

          <Link to="/membership" className="rail__btn rail__btn--amber">
            <span className="rail__icon" aria-hidden="true">
              ★
            </span>
            멤버십
          </Link>

          {isAdmin && (
            <>
              <p className="rail__group-label">관리</p>
              <Link to="/admin/reports" className="rail__btn">
                <span className="rail__icon" aria-hidden="true">
                  ⚑
                </span>
                신고 관리
              </Link>
              <Link to="/admin/users" className="rail__btn">
                <span className="rail__icon" aria-hidden="true">
                  ⚇
                </span>
                회원 관리
              </Link>
              <Link to="/admin/dashboard" className="rail__btn">
                <span className="rail__icon" aria-hidden="true">
                  ▩
                </span>
                관리자 대시보드
              </Link>
            </>
          )}

          <button type="button" className="rail__btn" onClick={handleLogout}>
            <span className="rail__icon" aria-hidden="true">
              ⏻
            </span>
            로그아웃
          </button>

          <div className="rail__info">
            <p className="rail__info-head">
              {user?.nickname ?? "회원"}님
            </p>
            <p className="rail__info-body">
              질문에 답변이 달리면 알림으로 알려드려요.
            </p>
          </div>
        </>
      ) : (
        <>
          <Link to="/login" className="rail__btn rail__btn--go">
            <span className="rail__icon" aria-hidden="true">
              ▸
            </span>
            로그인
          </Link>
          <Link to="/signup" className="rail__btn rail__btn--amber">
            <span className="rail__icon" aria-hidden="true">
              ✚
            </span>
            회원가입
          </Link>

          <div className="rail__info">
            <p className="rail__info-head">처음이신가요?</p>
            <p className="rail__info-body">
              가입하면 질문 작성, 답변 채택, 실시간 알림을 쓸 수 있어요.
            </p>
          </div>
        </>
      )}
    </aside>
  );
}

export default ActionRail;
