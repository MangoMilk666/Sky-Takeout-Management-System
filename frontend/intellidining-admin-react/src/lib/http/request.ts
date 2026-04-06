import axios, { type AxiosError, type AxiosResponse } from 'axios'
import { getToken, removeToken } from '@/lib/auth/cookies'
import { attachPendingController, clearPending, getRequestKey } from '@/lib/http/pending'
import { message } from '@/lib/ui/message'

function normalizeGetUrl(url: string, params: Record<string, unknown>) {
  const parts: string[] = []
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    if (value === null || value === undefined) continue
    if (typeof value === 'object' && !Array.isArray(value)) {
      for (const key of Object.keys(value as Record<string, unknown>)) {
        const v = (value as Record<string, unknown>)[key]
        if (v === null || v === undefined) continue
        const k = `${propName}[${key}]`
        parts.push(`${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      }
      continue
    }
    parts.push(`${encodeURIComponent(propName)}=${encodeURIComponent(String(value))}`)
  }
  if (parts.length === 0) return url
  const sep = url.includes('?') ? '&' : '?'
  return `${url}${sep}${parts.join('&')}`
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_BASE_API || '/api',
  timeout: 600000,
})

function redirectToLogin() {
  const base = (import.meta.env.BASE_URL || '/').replace(/\/$/, '')
  window.location.replace(`${base}/#/login`)
}

http.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.token = token
    }

    if (
      config.method?.toLowerCase() === 'get' &&
      config.params &&
      typeof config.params === 'object'
    ) {
      const url = normalizeGetUrl(String(config.url || ''), config.params as any)
      config.url = url
      config.params = {}
    }

    const key = getRequestKey(config)
    return attachPendingController(config, key)
  },
  (error) => Promise.reject(error),
)

http.interceptors.response.use(
  (response: AxiosResponse) => {
    const key = getRequestKey(response.config)
    clearPending(key)

    const status = (response.data && (response.data.status as number)) || response.status
    if (status === 401) {
      removeToken()
      redirectToLogin()
    }

    return response
  },
  (error: AxiosError) => {
    if (error.config) {
      const key = getRequestKey(error.config as any)
      clearPending(key)
    }

    const status = error.response?.status
    if (status === 401) {
      removeToken()
      redirectToLogin()
      return Promise.reject(error)
    }

    if (status === 405) {
      message.error('请求错误')
      return Promise.reject(error)
    }

    return Promise.reject(error)
  },
)
