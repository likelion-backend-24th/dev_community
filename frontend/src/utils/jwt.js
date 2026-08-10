function base64UrlDecode(str) {
  const base64 = str.replace(/-/g, '+').replace(/_/g, '/')
  const decoded = atob(base64)
  const bytes = Uint8Array.from(decoded, (c) => c.charCodeAt(0))
  return new TextDecoder('utf-8').decode(bytes)
}

export function decodeToken(token) {
  if (!token) return null
  try {
    const payload = token.split('.')[1]
    return JSON.parse(base64UrlDecode(payload))
  } catch {
    return null
  }
}
