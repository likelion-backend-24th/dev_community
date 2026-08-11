import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
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
import "../../styles/mypage.css";

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

  if (loading) return <p className="state-text">불러오는 중...</p>;
  if (error)
    return (
      <div className="page">
        <p className="inline-error" role="alert">
          {error}
        </p>
      </div>
    );

  return (
    <div className="page">
      <div className="page__header">
        <h1 className="page__title">마이페이지</h1>
      </div>

      <section className="profile-card card">
        <div className="profile-card__avatar">{profile.nickname[0]}</div>
        <div className="profile-card__info">
          <p className="profile-card__nickname">{profile.nickname}</p>
          <p className="profile-card__username">@{profile.username}</p>
          <p className="profile-card__joined">
            가입일 {profile.createdAt.slice(0, 10)}
          </p>
        </div>
        <div className="profile-card__actions">
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => setEditInfoOpen(true)}>
            내 정보 수정
          </button>
          <button type="button" className="btn btn-secondary btn-sm" onClick={() => setPasswordModalOpen(true)}>
            비밀번호 변경
          </button>
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => setWithdrawModalOpen(true)}>
            회원 탈퇴
          </button>
        </div>
      </section>

      <nav className="tabs">
        <button
          type="button"
          className="tab"
          onClick={() => handleTabClick("questions")}
          disabled={activeTab === "questions"}
        >
          내 질문 ({questions.length})
        </button>
        <button
          type="button"
          className="tab"
          onClick={() => handleTabClick("answers")}
          disabled={activeTab === "answers"}
        >
          내 답변 ({answerCount})
        </button>
      </nav>

      {activeTab === "questions" ? (
        questions.length === 0 ? (
          <div className="empty-state">
            <p>아직 작성한 질문이 없어요.</p>
          </div>
        ) : (
          <ul className="mypage-list">
            {questions.map((q) => (
              <li key={q.id}>
                <Link to={`/questions/${q.id}`} className="mypage-list__item">
                  <span className={`badge ${q.status === "RESOLVED" ? "badge-resolved" : "badge-open"}`}>
                    {STATUS_LABEL[q.status] ?? q.status}
                  </span>
                  <span className="mypage-list__title">{q.title}</span>
                  <span className="mypage-list__date">{new Date(q.createdAt).toLocaleString()}</span>
                </Link>
              </li>
            ))}
          </ul>
        )
      ) : answers.length === 0 ? (
        <div className="empty-state">
          <p>아직 작성한 답변이 없어요.</p>
        </div>
      ) : (
        <ul className="mypage-list">
          {answers.map((a) => (
            <li key={a.id}>
              <Link to={`/questions/${a.questionId}`} className="mypage-list__item">
                {a.isAdopted && <span className="badge badge-resolved">채택됨</span>}
                <span className="mypage-list__title">{a.content}</span>
                <span className="mypage-list__date">{new Date(a.createdAt).toLocaleString()}</span>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {toast && <div className="toast" role="status">{toast}</div>}

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
    <div className="modal-overlay">
      <div className="modal" role="dialog" aria-label="내 정보 수정">
        <div className="modal__header">
          <h2 className="modal__title">내 정보 수정</h2>
        </div>
        <div className="modal__body">
          <div className="form-field">
            <label htmlFor="nickname">변경할 닉네임 입력</label>
            <input
              id="nickname"
              className="input"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
            />
          </div>
          {error && (
            <p className="inline-error" role="alert">
              {error}
            </p>
          )}
        </div>
        <div className="modal__footer">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            취소
          </button>
          <button type="button" className="btn btn-primary" onClick={handleSave} disabled={submitting}>
            {submitting ? "저장 중..." : "수정"}
          </button>
        </div>
      </div>
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
      if (err.response?.data?.code === "INVALID_CREDENTIALS") {
        setError("비밀번호가 일치하지 않습니다.");
      } else {
        setError(err.response?.data?.message ?? "비밀번호 변경에 실패했습니다.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal" role="dialog" aria-label="비밀번호 변경">
        <div className="modal__header">
          <h2 className="modal__title">비밀번호 변경</h2>
        </div>
        <div className="modal__body">
          <div className="form-field">
            <label htmlFor="currentPassword">현재 비밀번호</label>
            <input
              id="currentPassword"
              type="password"
              className="input"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
            />
          </div>
          <div className="form-field">
            <label htmlFor="newPassword">새 비밀번호</label>
            <input
              id="newPassword"
              type="password"
              className="input"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
          </div>
          {error && (
            <p className="inline-error" role="alert">
              {error}
            </p>
          )}
        </div>
        <div className="modal__footer">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            취소
          </button>
          <button type="button" className="btn btn-primary" onClick={handleSave} disabled={submitting}>
            {submitting ? "저장 중..." : "저장"}
          </button>
        </div>
      </div>
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
      if (err.response?.data?.code === "INVALID_CREDENTIALS") {
        setError("비밀번호가 일치하지 않습니다.");
      } else {
        setError(err.response?.data?.message ?? "탈퇴 처리에 실패했습니다.");
      }
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal" role="dialog" aria-label="회원 탈퇴 확인">
        <div className="modal__header">
          <h2 className="modal__title">회원 탈퇴</h2>
        </div>
        <div className="modal__body">
          <p className="modal__desc">
            탈퇴하면 로그인할 수 없게 됩니다. 작성한 질문과 답변은 삭제되지
            않고 "탈퇴한 사용자"로 표시되어 남습니다.
          </p>
          <div className="form-field">
            <label htmlFor="withdrawPassword">비밀번호 확인</label>
            <input
              id="withdrawPassword"
              type="password"
              className="input"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
            />
          </div>
          {error && (
            <p className="inline-error" role="alert">
              {error}
            </p>
          )}
        </div>
        <div className="modal__footer">
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={submitting}>
            취소
          </button>
          <button type="button" className="btn btn-destructive" onClick={handleConfirm} disabled={submitting}>
            {submitting ? "처리 중..." : "탈퇴"}
          </button>
        </div>
      </div>
    </div>
  );
}

export default MyPage;