import client from './client'

export async function getMyInfo() {
  const res = await client.get('/api/members/me')
  return res.data.data
}

export async function getMyQuestions() {
  const res = await client.get('/api/members/me/questions')
  return res.data.data
}

export async function getMyAnswers() {
  const res = await client.get('/api/members/me/answers')
  return res.data.data
}

export async function updateMyInfo({ nickname }) {
  const res = await client.put('/api/members/me', { nickname })
  return res.data.data
}

export async function updatePassword({ currentPassword, newPassword }) {
  await client.put('/api/members/me/password', { currentPassword, newPassword })
}

export async function withdraw({ currentPassword }) {
  await client.delete('/api/members/me', { data: { currentPassword } })
}

export async function requestExpert() {
  const res = await client.post('/api/members/me/expert-request')
  return res.data.data
}

export async function rerollAvatarColor() {
  const res = await client.post('/api/members/me/avatar-color/reroll')
  return res.data.data
}