import axios from "axios";
import { getAccessToken, setAccessToken } from "./tokenStore";
import { decodeToken } from "../utils/jwt";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
  withCredentials: true,
});

const REISSUE_URL = "/api/auth/reissue";
const EXPIRY_BUFFER_MS = 5000;

let refreshPromise = null;

function isExpiringSoon(accessToken) {
  const claims = decodeToken(accessToken);
  if (!claims?.exp) return false;
  return Date.now() >= claims.exp * 1000 - EXPIRY_BUFFER_MS;
}

export function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = client
      .post(REISSUE_URL)
      .then((res) => {
        const newAccessToken = res.data.data.accessToken;
        setAccessToken(newAccessToken);
        return newAccessToken;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

client.interceptors.request.use(async (config) => {
  if (config.url?.includes(REISSUE_URL)) {
    return config;
  }

  let accessToken = getAccessToken();
  if (accessToken && isExpiringSoon(accessToken)) {
    try {
      accessToken = await refreshAccessToken();
    } catch {}
  }

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

export default client;
