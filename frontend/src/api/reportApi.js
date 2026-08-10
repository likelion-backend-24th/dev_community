import client from './client'

export async function createReport({ targetType, targetId, reason }) {
  const res = await client.post('/api/reports', { targetType, targetId, reason })
  return res.data.data
}
