import client from './client'

export async function createQuestion({ title, content, tags }) {
  const res = await client.post('/api/questions', { title, content, tags })
  return res.data.data
}

export async function getQuestion(id) {
  const res = await client.get(`/api/questions/${id}`)
  return res.data.data
}

export async function updateQuestion(id, { title, content, tags }) {
  const res = await client.put(`/api/questions/${id}`, { title, content, tags })
  return res.data.data
}