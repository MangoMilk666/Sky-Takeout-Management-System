import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { getToken } from '@/lib/auth/cookies'

export function RequireAuth() {
  const location = useLocation()
  const token = getToken()
  if (!token) {
    const from = `${location.pathname}${location.search}`
    const redirect = encodeURIComponent(from)
    return <Navigate to={`/login?redirect=${redirect}`} replace state={{ from }} />
  }
  return <Outlet />
}
