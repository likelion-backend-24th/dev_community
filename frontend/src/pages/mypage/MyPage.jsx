import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import {
  getMyInfo,
  getMyQuestions,
  getMyAnswers,
  updateMyInfo,
  updatePassword,
  withdraw,
} from "../../api/userApi";
import { STATUS_LABEL } from "../../constants/questionStatus";

function MyPage() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  const [profile, setProfile] = useState(null);
  const [activeTab, setActiveTab] = useState("questions");
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState([]);
  const [answerCount, setAnswerCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [editInfoOpen, setEditInfoOpen] = useState(false);
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [withdrawModalOpen, setWithdrawModalOpen] = useState(false);
  const [toast, setToast] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const [info, myQuestions] = await Promise.all([
          getMyInfo(),
          getMyQuestions(),
        ]);
        setProfile(info);
        setQuestions(myQuestions);
        const myAnswers = await getMyAnswers();
        setAnswers(myAnswers);
        setAnswerCount(myAnswers.length);
      } catch {
        setError("마이페이지 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleTabClick = async (tab) => {
    setActiveTab(tab);
    if (tab === "answers") {
      const myAnswers = await getMyAnswers();
      setAnswers(myAnswers);
      setAnswerCount(myAnswers.length);
    } else {
      const myQuestions = await getMyQuestions();
      setQuestions(myQuestions);
    }
  };

  const handleItemClick = (questionId) => {
    navigate(`/questions/${questionId}`);
  };

  if (loading) return <div>불러오는 중...</div>;
  if (error) return <div role="alert">{error}</div>;

  return (
    <div>
      <h1>마이페이지</h1>

      <section>
        <div></div>
        <p>{profile.nickname}</p>
        <p>{profile.username}</p>
        <p>가입일 {profile.createdAt.slice(0, 10)}</p>

        <button type="button" onClick={() => setEditInfoOpen(true)}>
          내 정보 수정
        </button>
        <button type="button" onClick={() => setPasswordModalOpen(true)}>
          비밀번호 변경
        </button>
        <button type="button" onClick={() => setWithdrawModalOpen(true)}>
          회원 탈퇴
        </button>
      </section>

      <nav>
        <button
          type="button"
          onClick={() => handleTabClick("questions")}
          disabled={activeTab === "questions"}
        >
          내 질문
        </button>
        <button
          type="button"
          onClick={() => handleTabClick("answers")}
          disabled={activeTab === "answers"}
        >
          내 답변 ({answerCount})
        </button>
      </nav>

      {activeTab === "questions" ? (
        <ul>
          {questions.map((q) => (
            <li key={q.id} onClick={() => handleItemClick(q.id)}>
              <span>{STATUS_LABEL[q.status] ?? q.status}</span>
              <span>{q.title}</span>
              <span>{q.createdAt}</span>
            </li>
          ))}
        </ul>
      ) : (
        <ul>
          {answers.map((a) => (
            <li key={a.id} onClick={() => handleItemClick(a.questionId)}>
              {a.adopted && <span>채택됨</span>}
              <span>{a.questionTitle}</span>
              <span>{a.createdAt}</span>
            </li>
          ))}
        </ul>
      )}

      {toast && <p role="status">{toast}</p>}

      {editInfoOpen && (
        <EditInfoModal
          initialNickname={profile.nickname}
          onClose={() => setEditInfoOpen(false)}
          onSaved={(updated) => {
            setProfile((prev) => ({ ...prev, nickname: updated.nickname }));
            setEditInfoOpen(false);
          }}
        />
      )}

      {passwordModalOpen && (
        <PasswordModal
          onClose={() => setPasswordModalOpen(false)}
          onSaved={() => {
            setPasswordModalOpen(false);
            setToast("비밀번호가 변경되었습니다.");
            setTimeout(() => setToast(""), 3000);
          }}
        />
      )}

      {withdrawModalOpen && (
        <WithdrawModal
          onClose={() => setWithdrawModalOpen(false)}
          onConfirmed={async (currentPassword) => {
            await withdraw({ currentPassword });
            navigate("/login", { replace: true });
            logout();
          }}
        />
      )}
    </div>
  );
}

function EditInfoModal({ initialNickname, onClose, onSaved }) {
  const [nickname, setNickname] = useState(initialNickname);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSave = async () => {
    if (!nickname.trim()) {
      setError("닉네임을 입력해주세요.");
      return;
    }
    setSubmitting(true);
    try {
      const updated = await updateMyInfo({ nickname });
      onSaved(updated);
    } catch (err) {
      setError(err.response?.data?.message ?? "저장에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div role="dialog" aria-label="내 정보 수정">
      <label htmlFor="nickname">변경할 닉네임 입력</label>
      <input
        id="nickname"
        value={nickname}
        onChange={(e) => setNickname(e.target.value)}
      />
      {error && <p role="alert">{error}</p>}
      <button type="button" onClick={onClose}>
        취소
      </button>
      <button type="button" onClick={handleSave} disabled={submitting}>
        {submitting ? "저장 중..." : "수정"}
      </button>
    </div>
  );
}

function PasswordModal({ onClose, onSaved }) {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSave = async () => {
    if (!currentPassword || !newPassword) {
      setError("현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
      return;
    }
    setSubmitting(true);
    try {
      await updatePassword({ currentPassword, newPassword });
      onSaved();
    } catch (err) {
      setError(err.response?.data?.message ?? "비밀번호 변경에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div role="dialog" aria-label="비밀번호 변경">
      <label htmlFor="currentPassword">현재 비밀번호</label>
      <input
        id="currentPassword"
        type="password"
        value={currentPassword}
        onChange={(e) => setCurrentPassword(e.target.value)}
      />
      <label htmlFor="newPassword">새 비밀번호</label>
      <input
        id="newPassword"
        type="password"
        value={newPassword}
        onChange={(e) => setNewPassword(e.target.value)}
      />
      {error && <p role="alert">{error}</p>}
      <button type="button" onClick={onClose}>
        취소
      </button>
      <button type="button" onClick={handleSave} disabled={submitting}>
        {submitting ? "저장 중..." : "저장"}
      </button>
    </div>
  );
}

function WithdrawModal({ onClose, onConfirmed }) {
  const [currentPassword, setCurrentPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const handleConfirm = async () => {
    if (!currentPassword) {
      setError("비밀번호를 입력해주세요.");
      return;
    }
    setSubmitting(true);
    try {
      await onConfirmed(currentPassword);
    } catch (err) {
      setError(err.response?.data?.message ?? "탈퇴 처리에 실패했습니다.");
      setSubmitting(false);
    }
  };

  return (
    <div role="dialog" aria-label="회원 탈퇴 확인">
      <p>정말 탈퇴하시겠습니까?</p>
      <label htmlFor="withdrawPassword">비밀번호 확인</label>
      <input
        id="withdrawPassword"
        type="password"
        value={currentPassword}
        onChange={(e) => setCurrentPassword(e.target.value)}
      />
      {error && <p role="alert">{error}</p>}
      <button type="button" onClick={onClose} disabled={submitting}>
        취소
      </button>
      <button type="button" onClick={handleConfirm} disabled={submitting}>
        {submitting ? "처리 중..." : "탈퇴"}
      </button>
    </div>
  );
}

export default MyPage;
