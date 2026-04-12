import { Card, Col, Row, Select, Space, Typography, message } from 'antd'
import type { EChartsOption } from 'echarts'
import ReactECharts from 'echarts-for-react'
import dayjs from 'dayjs'
import { useEffect, useMemo, useState } from 'react'
import { getOrderStatistics, getTop10, getTurnoverStatistics, getUserStatistics } from '@/api/workspace'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { usePageTitle } from '@/lib/ui/usePageTitle'

function splitCsv(value: any) {
  if (!value) return []
  if (Array.isArray(value)) return value
  return String(value)
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
}

export function StatisticsPage() {
  usePageTitle('smart-dining智能点餐系统 - 数据统计')
  const [rangeType, setRangeType] = useState<number>(2)
  const [turnover, setTurnover] = useState<any>(null)
  const [users, setUsers] = useState<any>(null)
  const [orders, setOrders] = useState<any>(null)
  const [top10, setTop10] = useState<any>(null)

  const [begin, end] = useMemo(() => {
    const today = dayjs()
    if (rangeType === 1) return [today.startOf('month').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
    if (rangeType === 2) return [today.subtract(6, 'day').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
    if (rangeType === 3) return [today.subtract(29, 'day').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
    if (rangeType === 4) return [today.startOf('week').format('YYYY-MM-DD'), today.endOf('week').format('YYYY-MM-DD')]
    if (rangeType === 5) return [today.startOf('month').format('YYYY-MM-DD'), today.endOf('month').format('YYYY-MM-DD')]
    return [today.subtract(6, 'day').format('YYYY-MM-DD'), today.format('YYYY-MM-DD')]
  }, [rangeType])

  useEffect(() => {
    ;(async () => {
      try {
        const [t, u, o, top] = await Promise.all([
          getTurnoverStatistics({ begin, end }),
          getUserStatistics({ begin, end }),
          getOrderStatistics({ begin, end }),
          getTop10({ begin, end }),
        ])

        if (String(t.data?.code) === '1') {
          const d = t.data?.data || {}
          setTurnover({
            dateList: splitCsv(d.dateList),
            turnoverList: splitCsv(d.turnoverList).map((n: any) => Number(n)),
          })
        }
        if (String(u.data?.code) === '1') {
          const d = u.data?.data || {}
          setUsers({
            dateList: splitCsv(d.dateList),
            totalUserList: splitCsv(d.totalUserList).map((n: any) => Number(n)),
            newUserList: splitCsv(d.newUserList).map((n: any) => Number(n)),
          })
        }
        if (String(o.data?.code) === '1') {
          const d = o.data?.data || {}
          setOrders({
            dateList: splitCsv(d.dateList),
            orderCountList: splitCsv(d.orderCountList).map((n: any) => Number(n)),
            validOrderCountList: splitCsv(d.validOrderCountList).map((n: any) => Number(n)),
            totalOrderCount: d.totalOrderCount,
            validOrderCount: d.validOrderCount,
            orderCompletionRate: d.orderCompletionRate,
          })
        }
        if (String(top.data?.code) === '1') {
          const d = top.data?.data || {}
          setTop10({
            nameList: splitCsv(d.nameList).reverse(),
            numberList: splitCsv(d.numberList).reverse().map((n: any) => Number(n)),
          })
        }
      } catch (e: any) {
        if (isRequestCanceled(e)) return
        message.error(`请求出错了：${e?.message || '未知错误'}`)
      }
    })()
  }, [begin, end])

  const turnoverOption: EChartsOption = useMemo(
    () => ({
      tooltip: { trigger: 'axis' },
      grid: { top: '5%', left: 10, right: 50, bottom: '12%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: turnover?.dateList || [] },
      yAxis: [{ type: 'value', min: 0 }],
      series: [
        {
          name: '营业额',
          type: 'line',
          smooth: false,
          showSymbol: false,
          itemStyle: { color: '#F29C1B' },
          data: turnover?.turnoverList || [],
        },
      ],
    }),
    [turnover],
  )

  const userOption: EChartsOption = useMemo(
    () => ({
      tooltip: { trigger: 'axis' },
      grid: { top: '5%', left: 20, right: 50, bottom: '12%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: users?.dateList || [] },
      yAxis: [{ type: 'value', min: 0 }],
      series: [
        { name: '用户总量', type: 'line', smooth: false, showSymbol: false, data: users?.totalUserList || [] },
        { name: '新增用户', type: 'line', smooth: false, showSymbol: false, data: users?.newUserList || [] },
      ],
    }),
    [users],
  )

  const orderOption: EChartsOption = useMemo(
    () => ({
      tooltip: { trigger: 'axis' },
      grid: { top: '5%', left: 20, right: 50, bottom: '12%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: orders?.dateList || [] },
      yAxis: [{ type: 'value', min: 0 }],
      series: [
        { name: '订单总量', type: 'line', smooth: false, showSymbol: false, data: orders?.orderCountList || [] },
        { name: '有效订单', type: 'line', smooth: false, showSymbol: false, data: orders?.validOrderCountList || [] },
      ],
    }),
    [orders],
  )

  const top10Option: EChartsOption = useMemo(
    () => ({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 20, right: 20, top: 10, bottom: 10, containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: top10?.nameList || [] },
      series: [{ type: 'bar', data: top10?.numberList || [], itemStyle: { color: '#FFC200' } }],
    }),
    [top10],
  )

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <Card>
        <Space>
          <Typography.Text strong>时间范围</Typography.Text>
          <Select
            value={rangeType}
            style={{ width: 220 }}
            options={[
              { value: 1, label: '本月(至今)' },
              { value: 2, label: '近7天' },
              { value: 3, label: '近30天' },
              { value: 4, label: '本周' },
              { value: 5, label: '本月' },
            ]}
            onChange={(v) => setRangeType(v)}
          />
          <Typography.Text type="secondary">
            {begin} 至 {end}
          </Typography.Text>
        </Space>
      </Card>

      <Row gutter={16}>
        <Col span={12}>
          <Card title="营业额统计">
            <ReactECharts option={turnoverOption} style={{ height: 320 }} />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="用户统计">
            <ReactECharts option={userOption} style={{ height: 320 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={12}>
          <Card title="订单统计">
            <ReactECharts option={orderOption} style={{ height: 320 }} />
          </Card>
        </Col>
        <Col span={12}>
          <Card title="销量排名TOP10">
            <ReactECharts option={top10Option} style={{ height: 320 }} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
