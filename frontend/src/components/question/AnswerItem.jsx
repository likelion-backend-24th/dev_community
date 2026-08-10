import { useState } from 'react'
import {
  updateAnswer,
  deleteAnswer,
  adoptAnswer,
  cancelAdoption,
} from '../../api/answerApi'
import { toggleAnswerLike } from '../../api/likeApi'
import ReportButton from './ReportButton'

function AnswerItem({ answer, currentUser, isQuestionOwner, questionResolved, onChanged }) {
  const [editing, setEditing] = useState(false)
  const [content, setContent] = useState(answer.content)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const isAuthor = currentUser?.id === answer.authorId

  const runAction = async (action, failMessage) => {
    setBusy(true)
    setError('')
    try {
      await action()
      onChanged()
    } catch (err) {
      setError(err.response?.data?.message ?? failMessage)
    } finally {
      setBusy(false)
    }
  }

  const handleUpdate = (e) => {
    e.preventDefault()
    runAction(() => updateAnswer(answer.id, content), '답변 수정에 실패했습니다.')
    setEditing(false)
  }

  const handleDelete = () => {
    if (!window.confirm('답변을 삭제하시겠습니까?')) return
    runAction(() => deleteAnswer(answer.id), '답변 삭제에 실패했습니다.')
  }

  const handleAdopt = () =>
    runAction(() => adoptAnswer(answer.id), '채택에 실패했습니다.')

  const handleCancelAdoption = () =>
    runAction(() => cancelAdoption(answer.id), '채택 취소에 실패했습니다.')

  const handleLike = () =>
    runAction(() => toggleAnswerLike(answer.id), '추천 처리에 실패했습니다.')

  return (
    <li>
      {editing ? (
        <form onSubmit={handleUpdate}>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            required
          />
          <button type="submit" disabled={busy}>
            저장
          </button>
          <button
            type="button"
            onClick={() => {
              setEditing(false)
              setContent(answer.content)
            }}
          >
            취소
          </button>
        </form>
      ) : (
        <p>{answer.content}</p>
      )}

      {answer.adopted && <strong> [채택된 답변] </strong>}
      <div>
        <span>{answer.authorNickname}</span>
        <span> · 추천 {answer.likeCount}</span>
        <span> · {new Date(answer.createdAt).toLocaleString()}</span>
      </div>

      <div>
        <button type="button" onClick={handleLike} disabled={busy}>
          추천
        </button>
        {isAuthor && !editing && (
          <button type="button" onClick={() => setEditing(true)}>
            수정
          </button>
        )}
        {isAuthor && (
          <button type="button" onClick={handleDelete} disabled={busy}>
            삭제
          </button>
        )}
        {isQuestionOwner && !questionResolved && !answer.adopted && (
          <button type="button" onClick={handleAdopt} disabled={busy}>
            채택
          </button>
        )}
        {isQuestionOwner && answer.adopted && (
          <button type="button" onClick={handleCancelAdoption} disabled={busy}>
            채택 취소
          </button>
        )}
        {currentUser && !isAuthor && (
          <ReportButton targetType="ANSWER" targetId={answer.id} />
        )}
      </div>

      {error && <p role="alert">{error}</p>}
    </li>
  )
}

export default AnswerItem
