import { Routes, Route } from "react-router-dom";
import Navbar from "./components/layout/Navbar";
import PrivateRoute from "./components/auth/PrivateRoute";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import QuestionListPage from "./pages/question/QuestionListPage";
import QuestionDetailPage from "./pages/question/QuestionDetailPage";
import QuestionFormPage from "./pages/question/QuestionFormPage";
import MyPage from "./pages/mypage/MyPage";
import ForbiddenPage from "./pages/error/ForbiddenPage";
import UnauthorizedPage from "./pages/error/UnauthorizedPage";
import NotFoundPage from "./pages/error/NotFoundPage";

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/questions" element={<QuestionListPage />} />
        <Route path="/questions/:id" element={<QuestionDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        <Route element={<PrivateRoute />}>
          <Route path="/questions/new" element={<QuestionFormPage />} />
          <Route path="/questions/:id/edit" element={<QuestionFormPage />} />
          <Route path="/mypage" element={<MyPage />} />
        </Route>

        <Route path="/401" element={<UnauthorizedPage />} />
        <Route path="/403" element={<ForbiddenPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  );
}

export default App;
