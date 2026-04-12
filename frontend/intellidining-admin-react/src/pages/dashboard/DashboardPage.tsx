import { Card, Col, Row, Statistic, Typography, message } from 'antd'
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

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Card
        title={
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>今日数据</span>
            <Link to="/statistics">详细数据</Link>
          </div>
        }
      >
        <Row gutter={16}>
          <Col span={6}>
            <Statistic title="营业额" prefix="¥" value={Number(business?.turnover || 0).toFixed(2)} />
          </Col>
          <Col span={6}>
            <Statistic title="有效订单" value={business?.validOrderCount || 0} />
          </Col>
          <Col span={6}>
            <Statistic
              title="订单完成率"
              value={Number(business?.orderCompletionRate || 0) * 100}
              precision={0}
              suffix="%"
            />
          </Col>
          <Col span={6}>
            <Statistic title="平均客单价" prefix="¥" value={Number(business?.unitPrice || 0).toFixed(2)} />
          </Col>
        </Row>
        <div style={{ marginTop: 16 }}>
          <Statistic title="新增用户" value={business?.newUsers || 0} />
        </div>
      </Card>

      <Card
        title={
          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>订单管理</span>
            <Link to="/order">订单明细</Link>
          </div>
        }
      >
        <Row gutter={16}>
          <Col span={4}>
            <Statistic title="待接单" value={orders?.waitingOrders || 0} />
          </Col>
          <Col span={4}>
            <Statistic title="待派送" value={orders?.deliveredOrders || 0} />
          </Col>
          <Col span={4}>
            <Statistic title="已完成" value={orders?.completedOrders || 0} />
          </Col>
          <Col span={4}>
            <Statistic title="已取消" value={orders?.cancelledOrders || 0} />
          </Col>
          <Col span={4}>
            <Statistic title="全部订单" value={orders?.allOrders || 0} />
          </Col>
        </Row>
      </Card>

      <Row gutter={16}>
        <Col span={12}>
          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>菜品总览</span>
                <Link to="/dish">菜品管理</Link>
              </div>
            }
          >
            <Row gutter={16}>
              <Col span={12}>
                <Statistic title="已启售" value={dishes?.sold || 0} />
              </Col>
              <Col span={12}>
                <Statistic title="已停售" value={dishes?.discontinued || 0} />
              </Col>
            </Row>
          </Card>
        </Col>
        <Col span={12}>
          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span>套餐总览</span>
                <Link to="/setmeal">套餐管理</Link>
              </div>
            }
          >
            <Row gutter={16}>
              <Col span={12}>
                <Statistic title="已启售" value={setmeals?.sold || 0} />
              </Col>
              <Col span={12}>
                <Statistic title="已停售" value={setmeals?.discontinued || 0} />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Card>
        <Typography.Text type="secondary">
          smart-dining智能点餐系统 管理端已完成核心页面迁移，后续可继续对齐原项目的视觉细节与表格字段。
        </Typography.Text>
      </Card>
    </div>
  )
}
