import type { InternalAxiosRequestConfig } from 'axios'

function stableStringify(value: unknown): string {
  if (value === null || value === undefined) return ''
  if (typeof value !== 'object') return String(value)
  if (Array.isArray(value)) return `[${value.map(stableStringify).join(',')}]`
  const obj = value as Record<string, unknown>
  const keys = Object.keys(obj).sort()
  return `{${keys.map((k) => `${k}:${stableStringify(obj[k])}`).join(',')}}`
}

export function getRequestKey(config: InternalAxiosRequestConfig): string {
  const method = (config.method || 'get').toLowerCase()
  const url = String(config.url || '')
  const normalizedUrl = url.startsWith('/api') ? url.slice(4) : url
  const params = stableStringify(config.params)
  const data = stableStringify(config.data)
  return `${method}::${normalizedUrl}::${params}::${data}`
}

const inFlight = new Map<string, AbortController>()

export function attachPendingController(
  config: InternalAxiosRequestConfig,
  key: string,
): InternalAxiosRequestConfig {
  if (inFlight.has(key)) {
    const controller = new AbortController()
    controller.abort('重复请求')
    config.signal = controller.signal
    return config
  }

  const controller = new AbortController()
  inFlight.set(key, controller)
  config.signal = controller.signal
  return config
}

export function clearPending(key: string) {
  inFlight.delete(key)
}

