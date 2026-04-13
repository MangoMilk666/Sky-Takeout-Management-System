import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  getBusinessData,
  getOrderOverview,
  getOverviewDishes,
  getOverviewSetmeals,
} from '@/api/workspace'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { usePageTitle } from '@/lib/ui/usePageTitle'
import { message } from '@/lib/ui/message'

function pad2(n: number) {
  return n < 10 ? `0${n}` : String(n)
}

function getTodayLabel() {
  const d = new Date()
  return `${d.getFullYear()}.${pad2(d.getMonth() + 1)}.${pad2(d.getDate())}`
}

function formatMoney(v: unknown) {
  const n = Number(v)
  if (!Number.isFinite(n)) return '0.00'
  return n.toFixed(2)
}

export function DashboardPage() {
  usePageTitle('smart-dining智能点餐系统 - 工作台')
  const [business, setBusiness] = useState<any>(null)
  const [orders, setOrders] = useState<any>(null)
  const [dishes, setDishes] = useState<any>(null)
  const [setmeals, setSetmeals] = useState<any>(null)

  useEffect(() => {
    ;(async () => {
      try {
        const [b, o, d, s] = await Promise.all([
          getBusinessData(),
          getOrderOverview(),
          getOverviewDishes(),
          getOverviewSetmeals(),
        ])
        if (String(b.data?.code) === '1') setBusiness(b.data?.data)
        if (String(o.data?.code) === '1') setOrders(o.data?.data)
        if (String(d.data?.code) === '1') setDishes(d.data?.data)
        if (String(s.data?.code) === '1') setSetmeals(s.data?.data)
      } catch (e: any) {
        if (isRequestCanceled(e)) return
        message.error(`请求出错了：${e?.message || '未知错误'}`)
      }
    })()
  }, [])

  const today = getTodayLabel()

  return (
    <div className="dashboard-container home">
      <div className="container">
        <h2 className="homeTitle">
          今日数据<i>{today}</i>
          <span>
            <Link to="/statistics">详细数据</Link>
          </span>
        </h2>
        <div className="overviewBox">
          <ul>
            <li>
              <p className="tit">营业额</p>
              <p className="num">¥ {formatMoney(business?.turnover)}</p>
            </li>
            <li>
              <p className="tit">有效订单</p>
              <p className="num">{business?.validOrderCount ?? 0}</p>
            </li>
            <li>
              <p className="tit">订单完成率</p>
              <p className="num">{Math.round(Number(business?.orderCompletionRate || 0) * 100)}%</p>
            </li>
            <li>
              <p className="tit">平均客单价</p>
              <p className="num">¥ {formatMoney(business?.unitPrice)}</p>
            </li>
            <li>
              <p className="tit">新增用户</p>
              <p className="num">{business?.newUsers ?? 0}</p>
            </li>
          </ul>
        </div>
      </div>

      <div className="container">
        <h2 className="homeTitle">
          订单管理<i>{today}</i>
          <span>
            <Link to="/order">订单明细</Link>
          </span>
        </h2>
        <div className="orderviewBox">
          <ul>
            <li>
              <span className="status">
                <i className="iconfont icon-waiting" />待接单
              </span>
              <span className="num tip">
                <Link to="/order?status=2">{orders?.waitingOrders ?? 0}</Link>
              </span>
            </li>
            <li>
              <span className="status">
                <i className="iconfont icon-staySway" />待派送
              </span>
              <span className="num tip">
                <Link to="/order?status=3">{orders?.deliveredOrders ?? 0}</Link>
              </span>
            </li>
            <li>
              <span className="status">
                <i className="iconfont icon-complete" />已完成
              </span>
              <span className="num">
                <Link to="/order?status=5">{orders?.completedOrders ?? 0}</Link>
              </span>
            </li>
            <li>
              <span className="status">
                <i className="iconfont icon-cancel" />已取消
              </span>
              <span className="num">
                <Link to="/order?status=6">{orders?.cancelledOrders ?? 0}</Link>
              </span>
            </li>
            <li>
              <span className="status">
                <i className="iconfont icon-all" />全部订单
              </span>
              <span className="num">
                <Link to="/order">{orders?.allOrders ?? 0}</Link>
              </span>
            </li>
          </ul>
        </div>
      </div>

      <div className="homeMain">
        <div className="container">
          <h2 className="homeTitle">
            菜品总览
            <span>
              <Link to="/dish">菜品管理</Link>
            </span>
          </h2>
          <div className="orderviewBox">
            <ul>
              <li>
                <span className="status">
                  <i className="iconfont icon-open" />已启售
                </span>
                <span className="num">{dishes?.sold ?? 0}</span>
              </li>
              <li>
                <span className="status">
                  <i className="iconfont icon-stop" />已停售
                </span>
                <span className="num">{dishes?.discontinued ?? 0}</span>
              </li>
              <li className="add">
                <Link to="/dish/add">
                  <i />
                  <p>新增菜品</p>
                </Link>
              </li>
            </ul>
          </div>
        </div>

        <div className="container">
          <h2 className="homeTitle">
            套餐总览
            <span>
              <Link to="/setmeal">套餐管理</Link>
            </span>
          </h2>
          <div className="orderviewBox">
            <ul>
              <li>
                <span className="status">
                  <i className="iconfont icon-open" />已启售
                </span>
                <span className="num">{setmeals?.sold ?? 0}</span>
              </li>
              <li>
                <span className="status">
                  <i className="iconfont icon-stop" />已停售
                </span>
                <span className="num">{setmeals?.discontinued ?? 0}</span>
              </li>
              <li className="add">
                <Link to="/setmeal/add">
                  <i />
                  <p>新增套餐</p>
                </Link>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}
