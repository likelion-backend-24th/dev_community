import client from './client'

export async function toggleQuestionLike(questionId) {
  const res = await client.post(`/api/questions/${questionId}/like`)
  return res.data.data
}

export async function toggleAnswerLike(answerId) {
  const res = await client.post(`/api/answers/${answerId}/like`)
  return res.data.data
}

export async function getLikeStatus(questionId, answerIds = []) {
  const res = await client.get(`/api/questions/${questionId}/like-status`, {
    params: { answerIds },
  })
  return res.data.data
}
