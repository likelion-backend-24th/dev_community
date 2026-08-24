import { describe, it, expect } from 'vitest'
import { decodeToken } from './jwt'

function makeToken(payload) {
  const base64UrlEncode = (obj) => {
    const bytes = new TextEncoder().encode(JSON.stringify(obj))
    const binary = Array.from(bytes, (b) => String.fromCharCode(b)).join('')
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
  }
  return `${base64UrlEncode({ alg: 'HS256' })}.${base64UrlEncode(payload)}.signature`
}

describe('decodeToken', () => {
  it('returns null when token is missing', () => {
    expect(decodeToken(null)).toBeNull()
    expect(decodeToken(undefined)).toBeNull()
    expect(decodeToken('')).toBeNull()
  })

  it('returns null for a malformed token', () => {
    expect(decodeToken('not-a-jwt')).toBeNull()
  })

  it('decodes the payload of a valid JWT', () => {
    const token = makeToken({ sub: '1', username: 'leo', roles: ['USER'], exp: 9999999999 })
    expect(decodeToken(token)).toEqual({
      sub: '1',
      username: 'leo',
      roles: ['USER'],
      exp: 9999999999,
    })
  })

  it('decodes payloads containing non-ASCII (UTF-8) characters', () => {
    const token = makeToken({ nickname: '민규' })
    expect(decodeToken(token)).toEqual({ nickname: '민규' })
  })
})
