import client from './client'

export async function getAnswers(questionId) {
  const res = await client.get(`/api/questions/${questionId}/answers`)
  return res.data.data
}

export async function createAnswer(questionId, content) {
  const res = await client.post(`/api/questions/${questionId}/answers`, {
    content,
  })
  return res.data.data
}

export async function updateAnswer(answerId, content) {
  const res = await client.patch(`/api/answers/${answerId}`, { content })
  return res.data.data
}

export async function deleteAnswer(answerId) {
  await client.delete(`/api/answers/${answerId}`)
}

export async function adoptAnswer(answerId) {
  const res = await client.post(`/api/answers/${answerId}/adopt`)
  return res.data.data
}

export async function cancelAdoption(answerId) {
  const res = await client.delete(`/api/answers/${answerId}/adopt`)
  return res.data.data
}
