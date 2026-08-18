import client from './client'

export async function getMySubscription() {
  const res = await client.get('/api/members/me/subscription')
  return res.data.data
}
