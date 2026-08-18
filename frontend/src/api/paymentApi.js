import client from "./client";

// TODO: 배포 시 실제 API 주소로 교체
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
