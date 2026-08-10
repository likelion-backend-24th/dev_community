import client from './client'

export async function toggleQuestionLike(questionId) {
  const res = await client.post(`/api/questions/${questionId}/like`)
  return res.data.data
}

export async function toggleAnswerLike(answerId) {
  const res = await client.post(`/api/answers/${answerId}/like`)
  return res.data.data
}
