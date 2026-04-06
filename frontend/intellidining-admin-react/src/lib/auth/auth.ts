import { login as loginApi, userLogout } from '@/api/employee'
import {
  removeToken,
  removeUserInfoRaw,
  removeUsername,
  setToken,
  setUserInfoRaw,
  setUsername,
} from '@/lib/auth/cookies'
import { message } from '@/lib/ui/message'

export async function loginWithPassword(username: string, password: string) {
  const trimmed = username.trim()
  setUsername(trimmed)

  try {
    const res = await loginApi({ username: trimmed, password })
    const data = res.data
    if (String(data?.code) === '1') {
      const token = String(data?.data?.token || '')
      if (token) setToken(token)
      setUserInfoRaw(data?.data || {})
      return true
    }

    message.error(data?.msg || '登录失败')
    return false
  } catch (e: any) {
    const status = e?.response?.status
    const msg = e?.response?.data?.msg || e?.message
    message.error(msg ? String(msg) : status ? `登录失败(${status})` : '登录失败')
    return false
  }
}

export async function logout() {
  try {
    await userLogout({})
  } catch (e) {
    void e
  } finally {
    removeToken()
    removeUsername()
    removeUserInfoRaw()
  }
}
