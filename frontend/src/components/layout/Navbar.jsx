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
    <nav>
      <Link to="/questions">Dev_Community</Link>
      <Link to="/questions">질문 목록</Link>
      {isAuthenticated ? (
        <>
          <Link to="/questions/new">질문 작성</Link>
          <Link to="/mypage">마이페이지</Link>
          {isAdmin && (
            <>
              <Link to="/admin/reports">신고 관리</Link>
              <Link to="/admin/users">회원 관리</Link>
            </>
          )}
          <button type="button" onClick={handleLogout}>
            로그아웃
          </button>
        </>
      ) : (
        <>
          <Link to="/login">로그인</Link>
          <Link to="/signup">회원가입</Link>
        </>
      )}
    </nav>
  );
}

export default Navbar;
