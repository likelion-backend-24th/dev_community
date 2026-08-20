import { Routes, Route, useLocation } from "react-router-dom";
import Navbar from "./components/layout/Navbar";
import Footer from "./components/layout/Footer";
import FloatingWriteButton from "./components/layout/FloatingWriteButton";
import { useAuth } from "./hooks/useAuth";
import PrivateRoute from "./components/auth/PrivateRoute";
import AdminRoute from "./components/auth/AdminRoute";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import OAuthCallbackPage from "./pages/auth/OAuthCallbackPage";
import OAuthNicknamePage from "./pages/auth/OAuthNicknamePage";
import QuestionListPage from "./pages/question/QuestionListPage";
import PremiumQuestionListPage from "./pages/question/PremiumQuestionListPage";
import QuestionDetailPage from "./pages/question/QuestionDetailPage";
import QuestionFormPage from "./pages/question/QuestionFormPage";
import MyPage from "./pages/mypage/MyPage";
import MembershipPage from "./pages/membership/MembershipPage";
import PersonalDashboardPage from "./pages/dashboard/PersonalDashboardPage";
import AdminReportsPage from "./pages/admin/AdminReportsPage";
import AdminUsersPage from "./pages/admin/AdminUsersPage";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import TermsPage from "./pages/legal/TermsPage";
import ForbiddenPage from "./pages/error/ForbiddenPage";
import UnauthorizedPage from "./pages/error/UnauthorizedPage";
import NotFoundPage from "./pages/error/NotFoundPage";

const NO_NAVBAR_PATHS = [
  "/",
  "/login",
  "/signup",
  "/login/oauth2/code/github",
  "/oauth/nickname",
];

const NO_FLOATING_WRITE_BUTTON_PATHS = ["/questions/new"];

function App() {
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const showNavbar = !NO_NAVBAR_PATHS.includes(location.pathname);
  const showFloatingWriteButton =
    showNavbar &&
    isAuthenticated &&
    !NO_FLOATING_WRITE_BUTTON_PATHS.includes(location.pathname) &&
    !location.pathname.endsWith("/edit");

  return (
    <div className="app-shell">
      {showNavbar && <Navbar />}
      <main className="app-main">
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/questions" element={<QuestionListPage key={location.key} />} />
          <Route path="/questions/premium" element={<PremiumQuestionListPage key={location.key} />} />
          <Route path="/questions/:id" element={<QuestionDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />

          {/* GitHub OAuth 콜백. GitHub App의 Authorization callback URL이랑 경로 정확히 일치해야 함 */}
          <Route path="/login/oauth2/code/github" element={<OAuthCallbackPage />} />
          {/* 처음 인증하는 GitHub 계정이면 여기서 닉네임 입력받고 가입 완료 */}
          <Route path="/oauth/nickname" element={<OAuthNicknamePage />} />

          <Route element={<PrivateRoute />}>
            <Route path="/questions/new" element={<QuestionFormPage />} />
            <Route path="/questions/:id/edit" element={<QuestionFormPage />} />
            <Route path="/mypage" element={<MyPage />} />
            <Route path="/membership" element={<MembershipPage />} />
            <Route path="/dashboard" element={<PersonalDashboardPage />} />
          </Route>

          <Route element={<AdminRoute />}>
            <Route path="/admin/reports" element={<AdminReportsPage />} />
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
          </Route>

          <Route path="/terms" element={<TermsPage />} />

          <Route path="/401" element={<UnauthorizedPage />} />
          <Route path="/403" element={<ForbiddenPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      {showFloatingWriteButton && <FloatingWriteButton />}
      <Footer />
    </div>
  );
}

export default App;