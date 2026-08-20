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

export async function getUsers({ page = 0, size = 10, expertRequested } = {}) {
  const res = await client.get('/api/admin/users', {
    params: { page, size, expertRequested: expertRequested || undefined },
  })
  return { content: res.data.data, meta: res.data.meta }
}

export async function getUserReportCount(userId) {
  const res = await client.get(`/api/admin/users/${userId}/reports`)
  return res.data.data
}

export async function grantExpert(userId) {
  const res = await client.patch(`/api/admin/users/${userId}/expert`)
  return res.data.data
}

export async function rejectExpertRequest(userId) {
  const res = await client.post(`/api/admin/users/${userId}/expert-request/reject`)
  return res.data.data
}

export async function getUserDashboardSummary(userId) {
  const res = await client.get(`/api/admin/users/${userId}/dashboard/summary`)
  return res.data.data
}

export async function getUserDashboardTimeline(userId) {
  const res = await client.get(`/api/admin/users/${userId}/dashboard/timeline`)
  return res.data.data
}

// 대시보드 통계 클릭 시 상세 목록 표시용 (요약 카운트와 어긋나지 않도록 페이지 크기를 넉넉히 요청)
export async function getUserQuestionsForDashboard(userId) {
  const res = await client.get(`/api/admin/users/${userId}/questions`, { params: { size: 200 } })
  return res.data.data
}

export async function getUserAnswersForDashboard(userId) {
  const res = await client.get(`/api/admin/users/${userId}/answers`, { params: { size: 200 } })
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

export async function getDailyTrend() {
  const res = await client.get('/api/admin/stats/daily-trend')
  return res.data.data
}

export async function getResolutionRate() {
  const res = await client.get('/api/admin/stats/resolution-rate')
  return res.data.data
}

export async function getStaleQuestions() {
  const res = await client.get('/api/admin/stats/stale-questions')
  return res.data.data
}

export async function getTopQuestions() {
  const res = await client.get('/api/admin/stats/top-questions')
  return res.data.data
}
