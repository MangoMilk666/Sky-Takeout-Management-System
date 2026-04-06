import Cookies from 'js-cookie'

const tokenKey = 'token'
const usernameKey = 'username'
const userInfoKey = 'user_info'

export function getToken() {
  return Cookies.get(tokenKey) || ''
}

export function setToken(token: string) {
  Cookies.set(tokenKey, token)
}

export function removeToken() {
  Cookies.remove(tokenKey)
}

export function getUsername() {
  return Cookies.get(usernameKey) || ''
}

export function setUsername(username: string) {
  Cookies.set(usernameKey, username)
}

export function removeUsername() {
  Cookies.remove(usernameKey)
}

export function getUserInfoRaw() {
  return Cookies.get(userInfoKey) || ''
}

export function setUserInfoRaw(userInfo: unknown) {
  Cookies.set(userInfoKey, JSON.stringify(userInfo))
}

export function removeUserInfoRaw() {
  Cookies.remove(userInfoKey)
}

