import client from "./client";

export const PAYMENT_WEBHOOK_URL =
  "https://dev-com.duckdns.org/api/payments/webhook";

export async function prepareBillingKeyIssue() {
  const res = await client.post("/api/payments/billing/prepare");
  return res.data.data;
}

export async function issueBillingKey(billingKey, planType) {
  const res = await client.post("/api/payments/billing/issue", {
    billingKey,
    planType,
  });
  return res.data.data;
}

export async function getLatestPaidPayment() {
  const res = await client.get("/api/payments/me/latest");
  return res.data.data;
}

export async function cancelPayment(paymentId, reason) {
  const res = await client.post(`/api/payments/${paymentId}/cancel`, {
    reason,
  });
  return res.data.data;
}
