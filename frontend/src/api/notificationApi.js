import client from './client'

export async function getRecentNotifications() {
  const res = await client.get('/api/notifications')
  return res.data.data
}

export async function markAllNotificationsRead() {
  await client.patch('/api/notifications/read-all')
}
