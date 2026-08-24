import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

describe('resolveWsUrl', () => {
  const originalEnv = { ...import.meta.env }

  afterEach(() => {
    Object.assign(import.meta.env, originalEnv)
    vi.unstubAllGlobals()
    vi.resetModules()
  })

  it('converts an http(s) API base URL to ws(s)', async () => {
    import.meta.env.VITE_API_BASE_URL = 'https://api.example.com'
    const { resolveWsUrl } = await import('./wsUrl')
    expect(resolveWsUrl()).toBe('wss://api.example.com/ws')
  })

  it('falls back to window.location.origin when VITE_API_BASE_URL is empty', async () => {
    import.meta.env.VITE_API_BASE_URL = ''
    vi.stubGlobal('location', { origin: 'http://localhost:5173' })
    const { resolveWsUrl } = await import('./wsUrl')
    expect(resolveWsUrl()).toBe('ws://localhost:5173/ws')
  })
})
