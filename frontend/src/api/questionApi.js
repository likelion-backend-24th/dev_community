import client from './client'

export async function getQuestions({ page = 0, size = 10, sort, keyword, tag, status } = {}) {
  const res = await client.get('/api/questions', {
    params: { page, size, sort, keyword, tag, status },
  })
  return { content: res.data.data, meta: res.data.meta }
}
