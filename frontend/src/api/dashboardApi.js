import client from './client'

export async function getMyDashboardSummary() {
  const res = await client.get('/api/members/me/dashboard/summary')
  return res.data.data
}

export async function getMyActivityTimeline() {
  const res = await client.get('/api/members/me/dashboard/timeline')
  return res.data.data
}

// 대시보드 통계 클릭 시 상세 목록 표시용 (요약 카운트와 어긋나지 않도록 페이지 크기를 넉넉히 요청)
export async function getMyQuestionsForDashboard() {
  const res = await client.get('/api/members/me/questions', { params: { size: 200 } })
  return res.data.data
}

export async function getMyAnswersForDashboard() {
  const res = await client.get('/api/members/me/answers', { params: { size: 200 } })
  return res.data.data
}
