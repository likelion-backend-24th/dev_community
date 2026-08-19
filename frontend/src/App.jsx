import { Routes, Route, useLocation } from "react-router-dom";
import Navbar from "./components/layout/Navbar";
import Footer from "./components/layout/Footer";
import PrivateRoute from "./components/auth/PrivateRoute";
import AdminRoute from "./components/auth/AdminRoute";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
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

const NO_NAVBAR_PATHS = ["/", "/login", "/signup"];

function App() {
  const location = useLocation();
  const showNavbar = !NO_NAVBAR_PATHS.includes(location.pathname);

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
      <Footer />
    </div>
  );
}

export default App;
