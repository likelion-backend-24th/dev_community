import client from './client'

export const MAX_ATTACHMENT_SIZE = 2 * 1024 * 1024 // 2MB
export const MAX_ATTACHMENT_COUNT = 5
export const ALLOWED_ATTACHMENT_EXTENSIONS = [
  // 코드
  'java', 'js', 'jsx', 'ts', 'tsx', 'py', 'go', 'rb', 'c', 'cpp', 'h', 'hpp',
  'html', 'css', 'sql', 'json', 'xml', 'yml', 'yaml', 'md', 'sh', 'kt', 'swift', 'php',
  // 이미지
  'png', 'jpg', 'jpeg', 'gif', 'webp',
]

async function uploadAttachments(targetType, targetId, files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  const path = targetType === 'QUESTION'
    ? `/api/questions/${targetId}/attachments`
    : `/api/answers/${targetId}/attachments`
  const res = await client.post(path, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
  return res.data.data
}

export async function uploadQuestionAttachments(questionId, files) {
  return uploadAttachments('QUESTION', questionId, files)
}

export async function uploadAnswerAttachments(answerId, files) {
  return uploadAttachments('ANSWER', answerId, files)
}

export async function getQuestionAttachments(questionId) {
  const res = await client.get(`/api/questions/${questionId}/attachments`)
  return res.data.data
}

export async function getAnswerAttachments(answerId) {
  const res = await client.get(`/api/answers/${answerId}/attachments`)
  return res.data.data
}

export async function deleteAttachment(attachmentId) {
  await client.delete(`/api/attachments/${attachmentId}`)
}

export function getAttachmentUrl(attachmentId) {
  return `${client.defaults.baseURL ?? ''}/api/attachments/${attachmentId}`
}

export function isImageFile(file) {
  const ext = file.name.split('.').pop()?.toLowerCase()
  return ['png', 'jpg', 'jpeg', 'gif', 'webp'].includes(ext)
}
