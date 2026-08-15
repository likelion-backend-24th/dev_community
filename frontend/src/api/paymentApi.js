import client from './client'

export async function preparePayment(planType) {
  const res = await client.post('/api/payments/prepare', { planType })
  return res.data.data
}

export async function completePayment(paymentId) {
  const res = await client.post(`/api/payments/${paymentId}/complete`)
  return res.data.data
}
