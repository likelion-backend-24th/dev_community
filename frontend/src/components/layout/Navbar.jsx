import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

/**
 * 카본 커맨드 바 + 그 아래 서브내비 스트립.
 * 계정 관련 동작(알림, 마이페이지, 로그아웃 등)은 ActionRail이 담당하고,
 * 여기는 콘텐츠 구역 이동만 맡는다.
 */
function Navbar() {
  const { isAuthenticated, isAdmin, user } = useAuth();

  return (
    <header className="navbar-stack">
      <nav className="navbar" aria-label="주요 메뉴">
        <Link to="/questions" className="navbar__brand">
          <span className="navbar__brand-arrow" aria-hidden="true">
            &gt;
          </span>
          <span>
            dev_com
            <span className="navbar__brand-caret">_</span>
          </span>
        </Link>

        <div className="navbar__links">
          <Link to="/questions">질문</Link>
          <Link to="/questions/premium">멤버십 게시판</Link>
          {isAuthenticated && <Link to="/chats">내 채팅</Link>}
          {isAdmin && <Link to="/admin/dashboard">관리자</Link>}
        </div>

        <div className="navbar__chips">
          {isAuthenticated ? (
            <Link to="/mypage" className="navbar__chip">
              {user?.nickname ?? "내 정보"}
            </Link>
          ) : (
            <Link to="/login" className="navbar__chip">
              로그인
            </Link>
          )}
        </div>
      </nav>

      <div className="subnav">
        {isAuthenticated && <Link to="/dashboard">내 활동</Link>}
        <Link to="/terms">이용약관</Link>
        <Link to="/privacy">개인정보처리방침</Link>
      </div>
    </header>
  );
}

export default Navbar;
