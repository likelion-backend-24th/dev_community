import client from './client'

export async function getCodeComments(questionId) {
  const res = await client.get(`/api/questions/${questionId}/code-comments`)
  return res.data.data
}

export async function createCodeComment(questionId, { lineNumber, content }) {
  const res = await client.post(`/api/questions/${questionId}/code-comments`, {
    lineNumber,
    content,
  })
  return res.data.data
}
