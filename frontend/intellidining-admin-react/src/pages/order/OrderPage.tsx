import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd'
import type { Dayjs } from 'dayjs'
import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  completeOrder,
  deliveryOrder,
  getOrderDetailPage,
  orderAccept,
  orderCancel,
  orderReject,
  queryOrderDetailById,
} from '@/api/order'
import { usePageTitle } from '@/lib/ui/usePageTitle'

type OrderRow = {
  id: string
  number: string
  status: number
  orderDishes?: string
  consignee?: string
  phone?: string
  address?: string
  orderTime?: string
  amount?: number
}

function getOrderType(row: OrderRow) {
  if (row.status === 1) return '待付款'
  if (row.status === 2) return '待接单'
  if (row.status === 3) return '待派送'
  if (row.status === 4) return '派送中'
  if (row.status === 5) return '已完成'
  if (row.status === 6) return '已取消'
  return '退款'
}

export function OrderPage() {
  usePageTitle('IntelliDining - 订单管理')
  const [searchParams, setSearchParams] = useSearchParams()
  const [activeStatus, setActiveStatus] = useState<number>(() => {
    const raw = searchParams.get('status')
    const v = raw ? Number(raw) : 0
    const valid = [0, 2, 3, 4, 5, 6].includes(v)
    return valid ? v : 0
  })
  const [number, setNumber] = useState('')
  const [phone, setPhone] = useState('')
  const [timeRange, setTimeRange] = useState<[Dayjs, Dayjs] | null>(null)
  const [isSearch, setIsSearch] = useState(false)

  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [rows, setRows] = useState<OrderRow[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)

  const [detailOpen, setDetailOpen] = useState(false)
  const [detail, setDetail] = useState<any>(null)
  const [detailRow, setDetailRow] = useState<OrderRow | null>(null)

  const [cancelOpen, setCancelOpen] = useState(false)
  const [cancelTitle, setCancelTitle] = useState<'取消' | '拒绝'>('取消')
  const [cancelOrderRow, setCancelOrderRow] = useState<OrderRow | null>(null)
  const [cancelReason, setCancelReason] = useState<string>('')
  const [cancelRemark, setCancelRemark] = useState<string>('')

  const fetchList = async (opts?: { resetPage?: boolean; markSearch?: boolean; status?: number }) => {
    if (opts?.resetPage) setPage(1)
    if (opts?.markSearch !== undefined) setIsSearch(opts.markSearch)
    if (opts?.status !== undefined) setActiveStatus(opts.status)

    setLoading(true)
    try {
      const beginTime = timeRange ? timeRange[0].format('YYYY-MM-DD HH:mm:ss') : undefined
      const endTime = timeRange ? timeRange[1].format('YYYY-MM-DD HH:mm:ss') : undefined
      const res = await getOrderDetailPage({
        page: opts?.resetPage ? 1 : page,
        pageSize,
        number: number || undefined,
        phone: phone || undefined,
        beginTime,
        endTime,
        status: (opts?.status ?? activeStatus) || undefined,
      })
      if (String(res.data?.code) === '1') {
        setRows(res.data?.data?.records || [])
        setTotal(Number(res.data?.data?.total || 0))
      } else {
        message.error(res.data?.msg || '查询失败')
      }
    } catch (e: any) {
      message.error(`请求出错了：${e?.message || '未知错误'}`)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchList()
  }, [page, pageSize, activeStatus])

  const openDetailById = async (orderId: string, opts?: { syncUrl?: boolean }) => {
    const syncUrl = opts?.syncUrl !== false
    if (syncUrl) {
      const next = new URLSearchParams(searchParams)
      next.set('orderId', orderId)
      setSearchParams(next, { replace: true })
    }

    setDetailOpen(true)
    setDetailRow({ id: orderId, number: '', status: 0 })
    setDetail(null)
    try {
      const res = await queryOrderDetailById({ orderId })
      if (String(res.data?.code) === '1') {
        const data = res.data?.data || null
        setDetail(data)
        if (data && typeof data === 'object') {
          setDetailRow((prev) => ({
            id: String((data as any).id || prev?.id || orderId),
            number: String((data as any).number || prev?.number || ''),
            status: Number((data as any).status || prev?.status || 0),
            consignee: String((data as any).consignee || prev?.consignee || ''),
            phone: String((data as any).phone || prev?.phone || ''),
            address: String((data as any).address || prev?.address || ''),
            orderTime: String((data as any).orderTime || prev?.orderTime || ''),
            amount: Number((data as any).amount || prev?.amount || 0),
          }))
        }
        return
      }
      message.error(res.data?.msg || '查询失败')
    } catch (e: any) {
      message.error(`请求出错了：${e?.message || '未知错误'}`)
    }
  }

  const openDetail = async (row: OrderRow) => {
    const next = new URLSearchParams(searchParams)
    next.set('orderId', row.id)
    setSearchParams(next, { replace: true })

    setDetailOpen(true)
    setDetailRow(row)
    setDetail(null)
    try {
      const res = await queryOrderDetailById({ orderId: row.id })
      if (String(res.data?.code) === '1') {
        setDetail(res.data?.data || null)
        return
      }
      message.error(res.data?.msg || '查询失败')
    } catch (e: any) {
      message.error(`请求出错了：${e?.message || '未知错误'}`)
    }
  }

  useEffect(() => {
    const raw = searchParams.get('status')
    if (raw !== null) {
      const v = Number(raw)
      if ([0, 2, 3, 4, 5, 6].includes(v) && v !== activeStatus) {
        setActiveStatus(v)
        setPage(1)
      }
    } else if (activeStatus !== 0) {
      setActiveStatus(0)
      setPage(1)
    }

    const orderId = searchParams.get('orderId')
    if (orderId && orderId !== detailRow?.id) {
      void openDetailById(orderId, { syncUrl: false })
    }
  }, [searchParams])

  const openCancel = (row: OrderRow, mode: '取消' | '拒绝') => {
    setCancelTitle(mode)
    setCancelOrderRow(row)
    setCancelReason('')
    setCancelRemark('')
    setCancelOpen(true)
  }

  const confirmCancel = async () => {
    if (!cancelOrderRow) return
    if (!cancelReason) {
      message.error(`请选择${cancelTitle}原因`)
      return
    }
    if (cancelReason === '自定义原因' && !cancelRemark) {
      message.error(`请输入${cancelTitle}原因`)
      return
    }
    const reasonValue = cancelReason === '自定义原因' ? cancelRemark : cancelReason
    const api = cancelTitle === '取消' ? orderCancel : orderReject
    const payload: any =
      cancelTitle === '取消'
        ? { id: cancelOrderRow.id, cancelReason: reasonValue }
        : { id: cancelOrderRow.id, rejectionReason: reasonValue }

    const res = await api(payload)
    if (String(res.data?.code) === '1') {
      message.success('操作成功')
      setCancelOpen(false)
      await fetchList()
      return
    }
    message.error(res.data?.msg || '操作失败')
  }

  const columns = useMemo(
    () => [
      { title: '订单号', dataIndex: 'number', width: 220 },
      { title: '订单状态', dataIndex: 'status', render: (_: any, r: OrderRow) => <Tag>{getOrderType(r)}</Tag> },
      { title: '用户名', dataIndex: 'consignee' },
      { title: '手机号', dataIndex: 'phone', width: 140 },
      { title: '地址', dataIndex: 'address' },
      { title: '下单时间', dataIndex: 'orderTime', width: 180 },
      {
        title: '操作',
        key: 'actions',
        width: 320,
        render: (_: any, row: OrderRow) => {
          const actions: Array<{ key: string; label: string; onClick: () => void; danger?: boolean }> = []
          actions.push({ key: 'detail', label: '查看', onClick: () => openDetail(row) })
          if (row.status === 2) {
            actions.push({
              key: 'accept',
              label: '接单',
              onClick: async () => {
                const res = await orderAccept({ id: row.id })
                if (String(res.data?.code) === '1') {
                  message.success('操作成功')
                  await fetchList()
                  return
                }
                message.error(res.data?.msg || '操作失败')
              },
            })
            actions.push({ key: 'reject', label: '拒单', onClick: () => openCancel(row, '拒绝'), danger: true })
            actions.push({ key: 'cancel', label: '取消', onClick: () => openCancel(row, '取消'), danger: true })
          }
          if (row.status === 3) {
            actions.push({
              key: 'delivery',
              label: '派送',
              onClick: async () => {
                const res = await deliveryOrder({ id: row.id })
                if (String(res.data?.code) === '1') {
                  message.success('操作成功')
                  await fetchList()
                  return
                }
                message.error(res.data?.msg || '操作失败')
              },
            })
          }
          if (row.status === 4) {
            actions.push({
              key: 'complete',
              label: '完成',
              onClick: async () => {
                const res = await completeOrder({ id: row.id })
                if (String(res.data?.code) === '1') {
                  message.success('操作成功')
                  await fetchList()
                  return
                }
                message.error(res.data?.msg || '操作失败')
              },
            })
          }
          return (
            <Space>
              {actions.map((a) => (
                <Button key={a.key} type="link" danger={a.danger} onClick={a.onClick}>
                  {a.label}
                </Button>
              ))}
            </Space>
          )
        },
      },
    ],
    [activeStatus, number, phone, timeRange, page, pageSize],
  )

  return (
    <Card>
      <Tabs
        activeKey={String(activeStatus)}
        onChange={(k) => {
          const nextStatus = Number(k)
          setActiveStatus(nextStatus)
          const next = new URLSearchParams(searchParams)
          if (nextStatus === 0) next.delete('status')
          else next.set('status', String(nextStatus))
          next.delete('orderId')
          setSearchParams(next, { replace: true })

          setNumber('')
          setPhone('')
          setTimeRange(null)
          setPage(1)
        }}
        items={[
          { key: '0', label: '全部' },
          { key: '2', label: '待接单' },
          { key: '3', label: '待派送' },
          { key: '4', label: '派送中' },
          { key: '5', label: '已完成' },
          { key: '6', label: '已取消' },
        ]}
      />

      <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap', marginBottom: 16 }}>
        <Space>
          <span>订单号：</span>
          <Input
            value={number}
            placeholder="请填写订单号"
            style={{ width: 220 }}
            allowClear
            onChange={(e) => {
              const v = e.target.value
              setNumber(v)
              if (v === '') fetchList({ resetPage: true })
            }}
            onPressEnter={() => fetchList({ resetPage: true, markSearch: true })}
          />
        </Space>

        <Space>
          <span>手机号：</span>
          <Input
            value={phone}
            placeholder="请填写手机号"
            style={{ width: 180 }}
            allowClear
            onChange={(e) => {
              const v = e.target.value
              setPhone(v)
              if (v === '') fetchList({ resetPage: true })
            }}
            onPressEnter={() => fetchList({ resetPage: true, markSearch: true })}
          />
        </Space>

        <Space>
          <span>下单时间：</span>
          <DatePicker.RangePicker
            showTime
            value={timeRange}
            onChange={(v) => setTimeRange(v as any)}
            style={{ width: 360 }}
          />
        </Space>

        <Button onClick={() => fetchList({ resetPage: true, markSearch: true })}>查询</Button>
      </div>

      <Table<OrderRow>
        rowKey="id"
        loading={loading}
        dataSource={rows}
        columns={columns as any}
        pagination={
          total > 10
            ? {
                current: page,
                pageSize,
                total,
                showSizeChanger: true,
                onChange: (p, ps) => {
                  setPage(p)
                  setPageSize(ps)
                },
              }
            : false
        }
        locale={{ emptyText: isSearch ? '未搜索到相关订单' : '暂无数据' }}
      />

      <Modal
        open={detailOpen}
        title="订单详情"
        onCancel={() => {
          setDetailOpen(false)
          const next = new URLSearchParams(searchParams)
          next.delete('orderId')
          setSearchParams(next, { replace: true })
        }}
        footer={null}
        width={720}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <Typography.Text>订单号：{detailRow?.number}</Typography.Text>
          <Typography.Text>状态：{detailRow ? getOrderType(detailRow) : ''}</Typography.Text>
          <Typography.Text>用户：{detailRow?.consignee}</Typography.Text>
          <Typography.Text>手机号：{detailRow?.phone}</Typography.Text>
          <Typography.Text>地址：{detailRow?.address}</Typography.Text>
          <Typography.Text>下单时间：{detailRow?.orderTime}</Typography.Text>
          <pre
            style={{
              marginTop: 8,
              padding: 12,
              background: '#0b1020',
              color: '#cbd5e1',
              borderRadius: 8,
              maxHeight: 320,
              overflow: 'auto',
            }}
          >
            {detail ? JSON.stringify(detail, null, 2) : '加载中...'}
          </pre>
        </div>
      </Modal>

      <Modal
        open={cancelOpen}
        title={`${cancelTitle}订单`}
        onCancel={() => setCancelOpen(false)}
        onOk={confirmCancel}
        okText="确定"
        cancelText="取消"
      >
        <Form layout="vertical">
          <Form.Item label={`${cancelTitle}原因`} required>
            <Select
              value={cancelReason || undefined}
              placeholder={`请选择${cancelTitle}原因`}
              options={[
                { value: '订单信息有误', label: '订单信息有误' },
                { value: '库存不足', label: '库存不足' },
                { value: '商家忙碌', label: '商家忙碌' },
                { value: '自定义原因', label: '自定义原因' },
              ]}
              onChange={(v) => setCancelReason(v)}
            />
          </Form.Item>
          {cancelReason === '自定义原因' ? (
            <Form.Item label="备注" required>
              <Input.TextArea rows={3} value={cancelRemark} onChange={(e) => setCancelRemark(e.target.value)} />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>
    </Card>
  )
}
