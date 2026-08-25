import { Routes, Route, useLocation } from "react-router-dom";
import Navbar from "./components/layout/Navbar";
import Footer from "./components/layout/Footer";
import ActionRail from "./components/layout/ActionRail";
import NotificationToast from "./components/layout/NotificationToast";
import { useAuth } from "./hooks/useAuth";
import PrivateRoute from "./components/auth/PrivateRoute";
import AdminRoute from "./components/auth/AdminRoute";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import OAuthCallbackPage from "./pages/auth/OAuthCallbackPage";
import OAuthNicknamePage from "./pages/auth/OAuthNicknamePage";
import ForgotPasswordPage from "./pages/auth/ForgotPasswordPage";
import ResetPasswordPage from "./pages/auth/ResetPasswordPage";
import QuestionListPage from "./pages/question/QuestionListPage";
import PremiumQuestionListPage from "./pages/question/PremiumQuestionListPage";
import QuestionDetailPage from "./pages/question/QuestionDetailPage";
import QuestionFormPage from "./pages/question/QuestionFormPage";
import ChatListPage from "./pages/chat/ChatListPage";
import ChatRoomPage from "./pages/chat/ChatRoomPage";
import MyPage from "./pages/mypage/MyPage";
import MembershipPage from "./pages/membership/MembershipPage";
import PersonalDashboardPage from "./pages/dashboard/PersonalDashboardPage";
import AdminReportsPage from "./pages/admin/AdminReportsPage";
import AdminUsersPage from "./pages/admin/AdminUsersPage";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import TermsPage from "./pages/legal/TermsPage";
import PrivacyPolicyPage from "./pages/legal/PrivacyPolicyPage";
import ForbiddenPage from "./pages/error/ForbiddenPage";
import UnauthorizedPage from "./pages/error/UnauthorizedPage";
import NotFoundPage from "./pages/error/NotFoundPage";

const NO_NAVBAR_PATHS = [
  "/",
  "/login",
  "/signup",
  "/oauth/callback",
  "/oauth/nickname",
  "/forgot-password",
  "/reset-password",
];

function App() {
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const showNavbar = !NO_NAVBAR_PATHS.includes(location.pathname);

  return (
    <div className="app-shell">
      {showNavbar && <Navbar />}
      <div className={showNavbar ? "app-body" : undefined}>
      <main className="app-main">
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/questions" element={<QuestionListPage key={location.key} />} />
          <Route path="/questions/premium" element={<PremiumQuestionListPage key={location.key} />} />
          <Route path="/questions/:id" element={<QuestionDetailPage key={location.key} />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          {/* Spring Security OAuth2 Client가 백엔드(8080)에서 인증 처리를 끝낸 뒤
              accessToken을 쿼리스트링에 실어 이 경로로 리다이렉트시켜줌 (provider 구분 없이 공용) */}
          <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
          {/* 처음 인증하는 소셜 계정(GitHub/Google)이면 여기서 닉네임 입력받고 가입 완료 */}
          <Route path="/oauth/nickname" element={<OAuthNicknamePage />} />

          <Route element={<PrivateRoute />}>
            <Route path="/questions/new" element={<QuestionFormPage />} />
            <Route path="/questions/:id/edit" element={<QuestionFormPage />} />
            <Route path="/mypage" element={<MyPage />} />
            <Route path="/membership" element={<MembershipPage />} />
            <Route path="/dashboard" element={<PersonalDashboardPage />} />
            <Route path="/chats" element={<ChatListPage key={location.key} />} />
            <Route path="/chats/:id" element={<ChatRoomPage key={location.key} />} />
          </Route>

          <Route element={<AdminRoute />}>
            <Route path="/admin/reports" element={<AdminReportsPage />} />
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
          </Route>

          <Route path="/terms" element={<TermsPage />} />
          <Route path="/privacy" element={<PrivacyPolicyPage />} />

          <Route path="/401" element={<UnauthorizedPage />} />
          <Route path="/403" element={<ForbiddenPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
        {showNavbar && <ActionRail />}
      </div>
      {isAuthenticated && <NotificationToast />}
      <Footer />
    </div>
  );
}

export default App;