import client from './client'

export async function signup({ username, password, nickname }) {
  const res = await client.post('/api/auth/signup', {
    username,
    password,
    nickname,
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

export async function logout() {
  await client.post('/api/auth/logout')
}
