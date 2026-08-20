import client from "./client";

export async function getCodeComments(questionId) {
  const res = await client.get(`/api/questions/${questionId}/code-comments`);
  return res.data.data;
}

export async function createCodeComment(questionId, { lineNumber, content }) {
  const res = await client.post(`/api/questions/${questionId}/code-comments`, {
    lineNumber,
    content,
  });
  return res.data.data;
}

export async function updateCodeComment(questionId, commentId, content) {
  const res = await client.put(
    `/api/questions/${questionId}/code-comments/${commentId}`,
    { content },
  );
  return res.data.data;
}

export async function deleteCodeComment(questionId, commentId) {
  await client.delete(
    `/api/questions/${questionId}/code-comments/${commentId}`,
  );
}
