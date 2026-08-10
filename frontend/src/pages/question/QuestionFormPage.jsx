import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createQuestion, getQuestion, updateQuestion } from '../../api/questionApi'

const MAX_TAGS = 5

function QuestionFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditMode = Boolean(id)

  const [form, setForm] = useState({ title: '', content: '' })
  const [tags, setTags] = useState([])
  const [tagInput, setTagInput] = useState('')

  const [loading, setLoading] = useState(isEditMode)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEditMode) return

    let ignore = false
    getQuestion(id)
      .then((data) => {
        if (ignore) return
        setForm({ title: data.title, content: data.content })
        setTags(data.tags ?? [])
      })
      .catch((err) => {
        if (ignore) return
        setError(err.response?.data?.message ?? '질문 정보를 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!ignore) setLoading(false)
      })

    return () => {
      ignore = true
    }
  }, [id, isEditMode])

  const handleChange = (e) => {
    const { name, value } = e.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleTagInputChange = (e) => {
    setTagInput(e.target.value)
  }

  const handleTagInputKeyDown = (e) => {
    if (e.key !== 'Enter') return
    e.preventDefault()

    const value = tagInput.trim()
    if (!value) return
    if (tags.includes(value)) {
      setTagInput('')
      return
    }
    if (tags.length >= MAX_TAGS) {
      setError(`태그는 최대 ${MAX_TAGS}개까지 입력할 수 있습니다.`)
      return
    }

    setTags((prev) => [...prev, value])
    setTagInput('')
  }

  const handleTagRemove = (target) => {
    setTags((prev) => prev.filter((tag) => tag !== target))
  }

  const handleCancel = () => {
    navigate(-1)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!form.title.trim() || !form.content.trim()) {
      setError('제목과 본문을 모두 입력해주세요.')
      return
    }

    setSubmitting(true)
    try {
      if (isEditMode) {
        await updateQuestion(id, { ...form, tags })
        navigate(`/questions/${id}`)
      } else {
        const response = await createQuestion({ ...form, tags })
        navigate(`/questions/${response.id}`)
      }
    } catch (err) {
      setError(
        err.response?.data?.message ??
          (isEditMode ? '질문 수정에 실패했습니다.' : '질문 등록에 실패했습니다.'),
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div>
        <h1>질문 {isEditMode ? '수정하기' : '작성하기'}</h1>
        <p>불러오는 중...</p>
      </div>
    )
  }

  return (
    <div>
      <h1>질문 {isEditMode ? '수정하기' : '작성하기'}</h1>

      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="title">제목</label><br></br>
          <input
            id="title"
            name="title"
            value={form.title}
            onChange={handleChange}
            placeholder="질문 제목을 입력하세요"
            required
          />
        </div>

        <div>
          <label htmlFor="content">본문</label><br></br>
          <textarea
            id="content"
            name="content"
            value={form.content}
            onChange={handleChange}
            placeholder="질문 내용을 자세히 작성해주세요"
            rows={15}
            required
          />
        </div>

        <div>
          <label htmlFor="tagInput">태그 (최대 {MAX_TAGS}개)</label>
          <div>
            {tags.map((tag) => (
              <span key={tag}>
                #{tag}{' '}
                <button type="button" onClick={() => handleTagRemove(tag)}>
                  X
                </button>
              </span>
            ))}
            <input
              id="tagInput"
              value={tagInput}
              onChange={handleTagInputChange}
              onKeyDown={handleTagInputKeyDown}
              placeholder="태그 입력 후 Enter"
              disabled={tags.length >= MAX_TAGS}
            />
          </div>
        </div>

        {error && <p role="alert">{error}</p>}

        <div>
          <button type="button" onClick={handleCancel}>
            취소
          </button>
          <button type="submit" disabled={submitting}>
            {submitting
              ? isEditMode
                ? '수정 중...'
                : '등록 중...'
              : isEditMode
                ? '수정완료'
                : '등록하기'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default QuestionFormPage