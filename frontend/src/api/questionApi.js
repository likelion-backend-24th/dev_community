import client from './client'

export async function getQuestions({ page = 0, size = 10, sort, keyword, tag, status } = {}) {
  const res = await client.get('/api/questions', {
    params: { page, size, sort, keyword, tag, status },
  })
  return { content: res.data.data, meta: res.data.meta }
}

export async function getPremiumQuestions({ page = 0, size = 10, sort, keyword, tag, status } = {}) {
  const res = await client.get('/api/questions/premium', {
    params: { page, size, sort, keyword, tag, status },
  })
  return { content: res.data.data, meta: res.data.meta }
}

export async function getQuestion(id) {
  const res = await client.get(`/api/questions/${id}`)
  return res.data.data
}

export async function deleteQuestion(id) {
  await client.delete(`/api/questions/${id}`)
}

export async function createQuestion(payload) {
  const res = await client.post('/api/questions', payload)
  return res.data.data
}

export async function updateQuestion(id, payload) {
  const res = await client.put(`/api/questions/${id}`, payload)
  return res.data.data
}

export async function getQuestionSummary(id) {
  const res = await client.get(`/api/questions/${id}/summary`)
  return res.data.data
}

export async function suggestQuestionTags(title, content) {
  const res = await client.post('/api/questions/tags/suggest', { title, content })
  return res.data.data
}
