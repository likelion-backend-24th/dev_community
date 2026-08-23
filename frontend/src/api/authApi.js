import client from './client'

export async function signup({ username, password, nickname, email }) {
  const res = await client.post('/api/auth/signup', {
    username,
    password,
    nickname,
    email,
  })
  return res.data.data
}

export async function login({ username, password }) {
  const res = await client.post('/api/auth/login', { username, password })
  return res.data.data
}

export async function checkUsername(username) {
  const res = await client.get('/api/auth/check-username', {
    params: { username },
  })
  return res.data
}

export async function checkNickname(nickname) {
  const res = await client.get('/api/auth/check-nickname', {
    params: { nickname },
  })
  return res.data
}

export async function checkEmail(email) {
  const res = await client.get('/api/auth/check-email', {
    params: { email },
  })
  return res.data
}

export async function logout() {
  await client.post('/api/auth/logout')
}

export async function reissue() {
  const res = await client.post('/api/auth/reissue')
  return res.data.data
}

export async function oauthComplete(signupToken, nickname, email) {
  const res = await client.post('/api/auth/oauth/complete', { signupToken, nickname, email })
  return res.data.data
}

export async function requestPasswordReset(username, email) {
  const res = await client.post('/api/auth/password-reset/request', { username, email })
  return res.data
}

export async function confirmPasswordReset(token, newPassword) {
  const res = await client.post('/api/auth/password-reset/confirm', { token, newPassword })
  return res.data
}