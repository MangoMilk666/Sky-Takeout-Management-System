export function isRequestCanceled(e: unknown): boolean {
  const anyErr = e as any
  const code = String(anyErr?.code || '')
  if (code === 'ERR_CANCELED') return true

  const name = String(anyErr?.name || '')
  if (name === 'CanceledError') return true

  const message = String(anyErr?.message || '')
  if (!message) return false
  const lower = message.toLowerCase()
  if (lower === 'canceled') return true
  if (lower.includes('canceled')) return true
  if (message.includes('被新请求替换')) return true
  if (message.includes('aborted')) return true
  return false
}

