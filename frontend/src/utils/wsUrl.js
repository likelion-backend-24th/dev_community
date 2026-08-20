// VITE_API_BASE_URL이 비어있으면(프로덕션, nginx가 같은 오리진에서 서빙) 현재 origin을 기준으로 만든다.
export function resolveWsUrl() {
  const base = import.meta.env.VITE_API_BASE_URL || window.location.origin;
  return base.replace(/^http/, "ws") + "/ws";
}
