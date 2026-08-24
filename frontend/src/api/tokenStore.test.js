import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getAccessToken, setAccessToken, subscribeAccessToken } from './tokenStore'

describe('tokenStore', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('returns null when no token is stored', () => {
    expect(getAccessToken()).toBeNull()
  })

  it('persists a token to localStorage', () => {
    setAccessToken('abc.def.ghi')
    expect(getAccessToken()).toBe('abc.def.ghi')
    expect(localStorage.getItem('accessToken')).toBe('abc.def.ghi')
  })

  it('removes the token from localStorage when set to null', () => {
    setAccessToken('abc.def.ghi')
    setAccessToken(null)
    expect(getAccessToken()).toBeNull()
  })

  it('notifies subscribers whenever the token changes', () => {
    const listener = vi.fn()
    const unsubscribe = subscribeAccessToken(listener)

    setAccessToken('token-1')
    expect(listener).toHaveBeenCalledWith('token-1')

    setAccessToken(null)
    expect(listener).toHaveBeenCalledWith(null)
    expect(listener).toHaveBeenCalledTimes(2)

    unsubscribe()
    setAccessToken('token-2')
    expect(listener).toHaveBeenCalledTimes(2)
  })
})
