import { useEffect, useMemo, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate, useNavigation } from 'react-router-dom'
import NProgress from 'nprogress'
import { getShopStatus, setShopStatus } from '@/api/shop'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElDialog } from '@/components/legacy-vue/ElDialog'
import { logout } from '@/lib/auth/auth'
import { getUsername } from '@/lib/auth/cookies'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { message } from '@/lib/ui/message'
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
  const [shopStatus, setShopStatusState] = useState<0 | 1 | null>(null)
  const [shopDialogOpen, setShopDialogOpen] = useState(false)
  const [shopNextStatus, setShopNextStatus] = useState<0 | 1>(1)
  const [shopSaving, setShopSaving] = useState(false)

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
            <span className="businessBtn">{shopStatus === 0 ? '打烊中' : shopStatus === 1 ? '营业中' : '加载中'}</span>
          </div>

          <div className="right-menu">
            <div className="rightStatus">
              <span
                className="navicon operatingState"
                role="button"
                tabIndex={0}
                onClick={() => {
                  const next = shopStatus === 0 ? 1 : 0
                  setShopNextStatus(next)
                  setShopDialogOpen(true)
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    const next = shopStatus === 0 ? 1 : 0
                    setShopNextStatus(next)
                    setShopDialogOpen(true)
                  }
                }}
              >
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

      <ElDialog
        open={shopDialogOpen}
        title="营业状态设置"
        width="30%"
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
        <div className="el-form demo-form-inline">
          <div className="el-form-item">
            <label className="el-form-item__label" style={{ width: 120 }}>
              当前状态：
            </label>
            <div className="el-form-item__content" style={{ marginLeft: 120, lineHeight: '36px' }}>
              {shopStatus === 0 ? '打烊中' : shopStatus === 1 ? '营业中' : '-'}
            </div>
          </div>

          <div className="el-form-item">
            <label className="el-form-item__label" style={{ width: 120 }}>
              切换为：
            </label>
            <div className="el-form-item__content" style={{ marginLeft: 120 }}>
              <div style={{ display: 'flex', gap: 12 }}>
                <ElButton
                  size="medium"
                  className={shopNextStatus === 1 ? 'continue' : ''}
                  onClick={() => setShopNextStatus(1)}
                >
                  营业中
                </ElButton>
                <ElButton
                  size="medium"
                  className={shopNextStatus === 0 ? 'continue' : ''}
                  onClick={() => setShopNextStatus(0)}
                >
                  打烊中
                </ElButton>
              </div>
            </div>
          </div>
        </div>
      </ElDialog>
    </div>
  )
}
