import { useMemo, useState } from 'react'
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { loginWithPassword } from '@/lib/auth/auth'
import { usePageTitle } from '@/lib/ui/usePageTitle'
import loginBanner from '@/assets/login/login-l.png'
import logo from '@/assets/smart-dining-logo.png'
import './login.scss'

export function LoginPage() {
  usePageTitle('smart-dining智能点餐系统 - 登录')
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const [loading, setLoading] = useState(false)
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('123456')
  const [touched, setTouched] = useState<{ username: boolean; password: boolean }>({
    username: false,
    password: false,
  })

  const redirectTo = useMemo(() => {
    const raw = searchParams.get('redirect') || ''
    const decoded = raw ? decodeURIComponent(raw) : ''
    if (decoded && decoded.startsWith('/')) return decoded
    const from = (location.state as any)?.from
    if (typeof from === 'string' && from.startsWith('/')) return from
    return '/'
  }, [location.state, searchParams])

  const usernameError = useMemo(() => (touched.username ? username.trim() === '' : false), [touched, username])
  const passwordError = useMemo(
    () => (touched.password ? password.trim().length < 6 : false),
    [touched, password],
  )

  return (
    <div className="login">
      <div className="login-box">
        <img src={loginBanner} alt="" />
        <div className="login-form">
          <form
            className="el-form"
            onSubmit={async (e) => {
              e.preventDefault()
              setTouched({ username: true, password: true })
              if (username.trim() === '' || password.trim().length < 6) return
              setLoading(true)
              try {
                const ok = await loginWithPassword(username, password)
                if (ok) navigate(redirectTo, { replace: true })
              } finally {
                setLoading(false)
              }
            }}
          >
            <div className="login-form-title">
              <img
                src={logo}
                style={{ height: 160, width: 'auto', maxWidth: '100%', objectFit: 'contain', display: 'block' }}
                alt=""
              />
            </div>

            <div className={`el-form-item ${usernameError ? 'is-error' : ''}`}>
              <div className="el-form-item__content">
                <div className="el-input el-input--prefix el-input--medium">
                  <span className="el-input__prefix">
                    <i className="iconfont icon-user" />
                  </span>
                  <input
                    className="el-input__inner"
                    type="text"
                    autoComplete="username"
                    placeholder="账号"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    onBlur={() => setTouched((t) => ({ ...t, username: true }))}
                  />
                </div>
              </div>
            </div>

            <div className={`el-form-item ${passwordError ? 'is-error' : ''}`}>
              <div className="el-form-item__content">
                <div className="el-input el-input--prefix el-input--medium">
                  <span className="el-input__prefix">
                    <i className="iconfont icon-lock" />
                  </span>
                  <input
                    className="el-input__inner"
                    type="password"
                    autoComplete="current-password"
                    placeholder="密码"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onBlur={() => setTouched((t) => ({ ...t, password: true }))}
                  />
                </div>
              </div>
            </div>

            <div className="el-form-item" style={{ width: '100%' }}>
              <div className="el-form-item__content">
                <button className="login-btn" type="submit" disabled={loading} style={{ width: '100%' }}>
                  <span>{loading ? '登录中...' : '登录'}</span>
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
