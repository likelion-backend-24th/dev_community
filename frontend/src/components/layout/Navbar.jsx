import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { logout as logoutApi } from "../../api/authApi";

function Navbar() {
  const { isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

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
    <nav className="navbar">
      <div className="navbar__links">
        <Link to="/questions" className="navbar__brand">
          Dev_Community
        </Link>
        <Link to="/questions">질문 목록</Link>
      </div>
      <div className="navbar__actions">
        {isAuthenticated ? (
          <>
            <Link to="/questions/new" className="btn btn-primary btn-sm">
              질문 작성
            </Link>
            <Link to="/mypage">마이페이지</Link>
            {isAdmin && (
              <>
                <Link to="/admin/reports">신고 관리</Link>
                <Link to="/admin/users">회원 관리</Link>
                <Link to="/admin/dashboard">관리자 대시보드</Link>
              </>
            )}
            <button type="button" className="btn btn-ghost" onClick={handleLogout}>
              로그아웃
            </button>
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
