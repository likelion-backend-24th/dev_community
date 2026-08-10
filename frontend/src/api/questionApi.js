import client from './client'

export async function getQuestions({ page = 0, size = 10, sort, keyword, tag, status } = {}) {
  const res = await client.get('/api/questions', {
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
