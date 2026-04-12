import { useEffect, useMemo, useRef, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate, useNavigation } from 'react-router-dom'
import NProgress from 'nprogress'
import { getShopStatus, setShopStatus } from '@/api/shop'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElDialog } from '@/components/legacy-vue/ElDialog'
import { PasswordDialog } from '@/components/password/PasswordDialog'
import { logout } from '@/lib/auth/auth'
import { getUsername } from '@/lib/auth/cookies'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { message } from '@/lib/ui/message'
import logo from '@/assets/smart-dining-logo.png'

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
  const [shopStatus, setShopStatusState] = useState<0 | 1 | null>(null)
  const [shopDialogOpen, setShopDialogOpen] = useState(false)
  const [shopNextStatus, setShopNextStatus] = useState<0 | 1>(1)
  const [shopSaving, setShopSaving] = useState(false)
  const [userMenuOpen, setUserMenuOpen] = useState(false)
  const [pwdDialogOpen, setPwdDialogOpen] = useState(false)
  const userMenuRef = useRef<HTMLDivElement | null>(null)

  const selectedKey = useMemo(() => getMenuSelectedKey(location.pathname), [location.pathname])
  const username = getUsername() || 'admin'

  useEffect(() => {
    if (navigation.state !== 'idle') {
      NProgress.start()
      return
    }
    NProgress.done()
  }, [navigation.state])

  useEffect(() => {
    ;(async () => {
      try {
        const res = await getShopStatus()
        if (String(res.data?.code) === '1') {
          const v = Number(res.data?.data)
          setShopStatusState(v === 0 ? 0 : 1)
          return
        }
        message.error(res.data?.msg || '获取营业状态失败')
      } catch (e: any) {
        if (isRequestCanceled(e)) return
        message.error(e?.message || '获取营业状态失败')
      }
    })()
  }, [])

  useEffect(() => {
    function onDocClick(e: MouseEvent) {
      if (!userMenuOpen) return
      const el = userMenuRef.current
      if (!el) return
      const target = e.target as Node | null
      if (target && el.contains(target)) return
      setUserMenuOpen(false)
    }

    document.addEventListener('click', onDocClick)
    return () => document.removeEventListener('click', onDocClick)
  }, [userMenuOpen])

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
                <img src={logo} style={{ width: 36, height: 36, objectFit: 'contain' }} alt="" />
              </div>
            ) : (
              <div className="sidebar-logo">
                <img
                  src={logo}
                  style={{ height: 40, width: 'auto', maxWidth: 175, objectFit: 'contain' }}
                  alt=""
                />
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
            <span className="businessBtn">{shopStatus === 0 ? '打烊中' : shopStatus === 1 ? '营业中' : '加载中'}</span>
          </div>

          <div className="right-menu">
            <div className="rightStatus">
              <span
                className="navicon operatingState"
                role="button"
                tabIndex={0}
                onClick={() => {
                  setShopNextStatus(shopStatus === 0 ? 0 : 1)
                  setShopDialogOpen(true)
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    setShopNextStatus(shopStatus === 0 ? 0 : 1)
                    setShopDialogOpen(true)
                  }
                }}
              >
                <i />
                营业状态设置
              </span>
            </div>
            <div className="avatar-wrapper">
              <div
                ref={userMenuRef}
                className={userMenuOpen ? 'userInfo' : ''}
                onMouseEnter={() => setUserMenuOpen(true)}
                onMouseLeave={() => setUserMenuOpen(false)}
              >
                <button
                  type="button"
                  className={[
                    'el-button',
                    'el-button--primary',
                    userMenuOpen ? 'active' : '',
                  ]
                    .filter(Boolean)
                    .join(' ')}
                  onClick={() => setUserMenuOpen((v) => !v)}
                >
                  {username}
                  <i className="el-icon-arrow-down" />
                </button>
                {userMenuOpen ? (
                  <div className="userList">
                    <p
                      className="amendPwdIcon"
                      onClick={() => {
                        setPwdDialogOpen(true)
                        setUserMenuOpen(false)
                      }}
                    >
                      修改密码<i />
                    </p>
                    <p
                      className="outLogin"
                      onClick={async () => {
                        setUserMenuOpen(false)
                        await logout()
                        navigate('/login', { replace: true })
                      }}
                    >
                      退出登录<i />
                    </p>
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        </div>

        <section className="app-main">
          <Outlet />
        </section>
      </div>

      <ElDialog
        open={shopDialogOpen}
        title="营业状态设置"
        width="25%"
        showClose={false}
        dialogClassName="shop-status-dialog"
        onClose={() => {
          if (shopSaving) return
          setShopDialogOpen(false)
        }}
        footer={
          <span className="dialog-footer">
            <ElButton
              size="medium"
              onClick={() => {
                if (shopSaving) return
                setShopDialogOpen(false)
              }}
            >
              取 消
            </ElButton>
            <ElButton
              elType="primary"
              size="medium"
              className="continue"
              onClick={async () => {
                if (shopSaving) return
                setShopSaving(true)
                try {
                  const res = await setShopStatus(shopNextStatus)
                  if (String(res.data?.code) === '1' || String(res.status) === '200') {
                    setShopStatusState(shopNextStatus)
                    message.success('操作成功')
                    setShopDialogOpen(false)
                    return
                  }
                  message.error(res.data?.msg || '操作失败')
                } catch (e: any) {
                  if (isRequestCanceled(e)) return
                  message.error(e?.message || '操作失败')
                } finally {
                  setShopSaving(false)
                }
              }}
            >
              确 定
            </ElButton>
          </span>
        }
      >
        <div className="el-radio-group" role="radiogroup" aria-label="营业状态">
          <label
            className={['el-radio', shopNextStatus === 1 ? 'is-checked' : ''].filter(Boolean).join(' ')}
            onClick={() => {
              if (shopSaving) return
              setShopNextStatus(1)
            }}
          >
            <span
              className={['el-radio__input', shopNextStatus === 1 ? 'is-checked' : '']
                .filter(Boolean)
                .join(' ')}
            >
              <span className="el-radio__inner" />
              <input className="el-radio__original" type="radio" checked={shopNextStatus === 1} readOnly />
            </span>
            <span className="el-radio__label">
              营业中
              <span>当前餐厅处于营业状态，自动接收任何订单，可点击打烊进入店铺打烊状态。</span>
            </span>
          </label>

          <label
            className={['el-radio', shopNextStatus === 0 ? 'is-checked' : ''].filter(Boolean).join(' ')}
            onClick={() => {
              if (shopSaving) return
              setShopNextStatus(0)
            }}
          >
            <span
              className={['el-radio__input', shopNextStatus === 0 ? 'is-checked' : '']
                .filter(Boolean)
                .join(' ')}
            >
              <span className="el-radio__inner" />
              <input className="el-radio__original" type="radio" checked={shopNextStatus === 0} readOnly />
            </span>
            <span className="el-radio__label">
              打烊中
              <span>当前餐厅处于打烊状态，仅接受营业时间内的预定订单，可点击营业中手动恢复营业状态。</span>
            </span>
          </label>
        </div>
      </ElDialog>

      <PasswordDialog
        open={pwdDialogOpen}
        onClose={() => {
          setPwdDialogOpen(false)
        }}
      />
    </div>
  )
}
