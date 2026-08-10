import client from './client'

export async function getReports({ status, page = 0, size = 10 } = {}) {
  const res = await client.get('/api/admin/reports', {
    params: { status: status || undefined, page, size },
  })
  return { content: res.data.data, meta: res.data.meta }
}

export async function processReport(id, status) {
  const res = await client.patch(`/api/admin/reports/${id}`, { status })
  return res.data.data
}

export async function getUsers({ page = 0, size = 10 } = {}) {
  const res = await client.get('/api/admin/users', { params: { page, size } })
  return { content: res.data.data, meta: res.data.meta }
}

export async function getUserReportCount(userId) {
  const res = await client.get(`/api/admin/users/${userId}/reports`)
  return res.data.data
}

export async function suspendUser(userId) {
  const res = await client.patch(`/api/admin/users/${userId}/suspend`)
  return res.data.data
}

export async function unsuspendUser(userId) {
  const res = await client.patch(`/api/admin/users/${userId}/unsuspend`)
  return res.data.data
}
