import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import QuestionListPage from "./pages/question/QuestionListPage";
import QuestionDetailPage from "./pages/question/QuestionDetailPage";
import QuestionFormPage from "./pages/question/QuestionFormPage";
import MyPage from "./pages/mypage/MyPage";
import ForbiddenPage from "./pages/error/ForbiddenPage";
import NotFoundPage from "./pages/error/NotFoundPage";

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<QuestionListPage />} />
        <Route path="/questions/:id" element={<QuestionDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        <Route path="/questions/new" element={<QuestionFormPage />} />
        <Route path="/questions/:id/edit" element={<QuestionFormPage />} />
        <Route path="/mypage" element={<MyPage />} />

        <Route path="/403" element={<ForbiddenPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  );
}

export default App;
