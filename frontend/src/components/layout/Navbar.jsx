import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useNotification } from "../../hooks/useNotification";
import { logout as logoutApi } from "../../api/authApi";
import {
  getRecentNotifications,
  markAllNotificationsRead,
} from "../../api/notificationApi";
import ThemeToggle from "./ThemeToggle";
import { getAvatarColor } from "../../utils/avatarColor";

function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const { notification, dismiss } = useNotification();
  const navigate = useNavigate();

  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  const [notifMenuOpen, setNotifMenuOpen] = useState(false);
  const [notifItems, setNotifItems] = useState([]);
  const [notifLoading, setNotifLoading] = useState(false);
  const notifMenuRef = useRef(null);

  useEffect(() => {
    if (!menuOpen && !notifMenuOpen) return undefined;
    const handleClickOutside = (e) => {
      if (menuOpen && menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
      if (
        notifMenuOpen &&
        notifMenuRef.current &&
        !notifMenuRef.current.contains(e.target)
      ) {
        setNotifMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [menuOpen, notifMenuOpen]);

  const handleBellClick = async () => {
    const opening = !notifMenuOpen;
    setNotifMenuOpen(opening);
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
    setNotifMenuOpen(false);
    navigate(link);
  };

  const handleLogout = async () => {
    setMenuOpen(false);
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
    <nav className="navbar">
      <div className="navbar__links">
        <Link to="/questions" className="navbar__brand">
          <span className="navbar__brand-arrow" aria-hidden="true">
            &gt;
          </span>
          <span>
            dev_com
            <span className="navbar__brand-caret">_</span>
          </span>
        </Link>
      </div>

      <div className="navbar__actions">
        <ThemeToggle />
        {isAuthenticated ? (
          <>
            <Link to="/questions/new" className="btn btn-primary btn-sm">
              + 질문 작성
            </Link>

            <div className="navbar__notif-wrap" ref={notifMenuRef}>
              <button
                type="button"
                className="navbar__icon-btn"
                onClick={handleBellClick}
                aria-label="알림"
                aria-haspopup="true"
                aria-expanded={notifMenuOpen}
              >
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
                  <path d="M13.7 21a2 2 0 0 1-3.4 0" />
                </svg>
                {notification && <span className="navbar__dot" />}
              </button>

              {notifMenuOpen && (
                <div className="navbar__notif-menu">
                  <p className="navbar__notif-menu-title">알림</p>
                  {notifLoading && (
                    <p className="navbar__notif-empty">불러오는 중...</p>
                  )}
                  {!notifLoading && notifItems.length === 0 && (
                    <p className="navbar__notif-empty">새 알림이 없어요.</p>
                  )}
                  {!notifLoading &&
                    notifItems.map((item) => (
                      <button
                        key={item.id}
                        type="button"
                        className={`navbar__notif-item${item.isRead ? "" : " navbar__notif-item--unread"}`}
                        onClick={() => handleNotifItemClick(item.link)}
                      >
                        <span className="navbar__notif-item-message">
                          {item.message}
                        </span>
                        <span className="navbar__notif-item-time">
                          {new Date(item.createdAt).toLocaleString()}
                        </span>
                      </button>
                    ))}
                </div>
              )}
            </div>

            <div className="navbar__avatar-wrap" ref={menuRef}>
              <button
                type="button"
                className="navbar__avatar"
                style={{ backgroundColor: getAvatarColor(user?.nickname) }}
                onClick={() => setMenuOpen((v) => !v)}
                aria-haspopup="true"
                aria-expanded={menuOpen}
              >
                {user?.nickname?.[0] ?? "?"}
              </button>
              {menuOpen && (
                <div className="navbar__avatar-menu">
                  <Link to="/mypage" onClick={() => setMenuOpen(false)}>
                    마이페이지
                  </Link>
                  <Link to="/membership" onClick={() => setMenuOpen(false)}>
                    멤버십
                    <svg
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                    </svg>
                  </Link>
                  {isAdmin && (
                    <>
                      <div className="navbar__avatar-menu-divider" />
                      <Link
                        to="/admin/reports"
                        onClick={() => setMenuOpen(false)}
                      >
                        신고 관리
                      </Link>
                      <Link
                        to="/admin/users"
                        onClick={() => setMenuOpen(false)}
                      >
                        회원 관리
                      </Link>
                      <Link
                        to="/admin/dashboard"
                        onClick={() => setMenuOpen(false)}
                      >
                        관리자 대시보드
                      </Link>
                    </>
                  )}
                  <div className="navbar__avatar-menu-divider" />
                  <button type="button" onClick={handleLogout}>
                    로그아웃
                  </button>
                </div>
              )}
            </div>
          </>
        ) : (
          <>
            <Link to="/login">로그인</Link>
            <Link to="/signup" className="btn btn-primary btn-sm">
              회원가입
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
