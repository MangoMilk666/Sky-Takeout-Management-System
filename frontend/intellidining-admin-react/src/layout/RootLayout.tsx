import { useEffect, useMemo, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate, useNavigation } from 'react-router-dom'
import NProgress from 'nprogress'
import { logout } from '@/lib/auth/auth'
import { getUsername } from '@/lib/auth/cookies'
import logo from '@/assets/login/logo.png'
import miniLogo from '@/assets/login/mini-logo.png'

type MenuItem = {
  to: string
  label: string
  iconClass: string
}

const menuItems: MenuItem[] = [
  { to: '/dashboard', label: '工作台', iconClass: 'dashboard' },
  { to: '/statistics', label: '数据统计', iconClass: 'icon-statistics' },
  { to: '/order', label: '订单管理', iconClass: 'icon-order' },
  { to: '/setmeal', label: '套餐管理', iconClass: 'icon-combo' },
  { to: '/dish', label: '菜品管理', iconClass: 'icon-dish' },
  { to: '/category', label: '分类管理', iconClass: 'icon-category' },
  { to: '/employee', label: '员工管理', iconClass: 'icon-employee' },
]

function getMenuSelectedKey(pathname: string) {
  const hit = menuItems.find((it) => pathname === it.to || pathname.startsWith(`${it.to}/`))
  return hit ? hit.to : ''
}

export function RootLayout() {
  const location = useLocation()
  const navigate = useNavigate()
  const navigation = useNavigation()
  const [sidebarOpened, setSidebarOpened] = useState(true)

  const selectedKey = useMemo(() => getMenuSelectedKey(location.pathname), [location.pathname])
  const username = getUsername() || 'admin'

  useEffect(() => {
    if (navigation.state !== 'idle') {
      NProgress.start()
      return
    }
    NProgress.done()
  }, [navigation.state])

  const wrapperClass = useMemo(() => {
    const base = ['app-wrapper']
    if (!sidebarOpened) base.push('hideSidebar')
    else base.push('openSidebar')
    return base.join(' ')
  }, [sidebarOpened])

  return (
    <div className={wrapperClass}>
      <div className="sidebar-container">
        <div>
          <div className="logo">
            {!sidebarOpened ? (
              <div className="sidebar-logo-mini">
                <img src={miniLogo} alt="" />
              </div>
            ) : (
              <div className="sidebar-logo">
                <img src={logo} style={{ width: 120, height: 31 }} alt="" />
              </div>
            )}
          </div>

          <div className="el-scrollbar">
            <div className="el-scrollbar__wrap scrollbar-wrapper">
              <div className="el-scrollbar__view">
                <nav className="el-menu" aria-label="侧边菜单">
                  {menuItems.map((it) => (
                    <NavLink
                      key={it.to}
                      to={it.to}
                      className={({ isActive }) => (isActive || selectedKey === it.to ? 'router-link-active' : '')}
                    >
                      <div className="el-menu-item" role="menuitem">
                        <i className={`iconfont ${it.iconClass}`} />
                        {sidebarOpened ? <span>{it.label}</span> : null}
                      </div>
                    </NavLink>
                  ))}
                </nav>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="main-container">
        <div className="navbar">
          <div className="statusBox">
            <div
              id="hamburger-container"
              className="hamburger-container"
              onClick={() => setSidebarOpened((v) => !v)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') setSidebarOpened((v) => !v)
              }}
            >
              <i className="iconfont dashboard" />
            </div>
            <span className="businessBtn">营业中</span>
          </div>

          <div className="right-menu">
            <div className="rightStatus">
              <span className="navicon operatingState">
                <i />
                营业状态设置
              </span>
            </div>
            <div className="avatar-wrapper">
              <div>
                <button
                  type="button"
                  className="el-button el-button--primary"
                  onClick={async () => {
                    await logout()
                    navigate('/login', { replace: true })
                  }}
                >
                  {username}
                  <i className="el-icon-arrow-down" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <section className="app-main">
          <Outlet />
        </section>
      </div>
    </div>
  )
}
