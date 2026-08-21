const STORAGE_KEY = "accessToken";
const listeners = new Set();

export function getAccessToken() {
  return localStorage.getItem(STORAGE_KEY);
}

export function setAccessToken(token) {
  if (token) {
    localStorage.setItem(STORAGE_KEY, token);
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
  listeners.forEach((listener) => listener(token));
}

export function subscribeAccessToken(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}
