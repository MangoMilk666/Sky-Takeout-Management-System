import { useMemo } from 'react'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElDialog } from '@/components/legacy-vue/ElDialog'

type OrderRow = {
  id: string
  number?: string
  status?: number
  consignee?: string
  phone?: string
  address?: string
  orderTime?: string
  amount?: number
}

function safeText(v: unknown) {
  const s = String(v ?? '')
  return s.trim() ? s : '-'
}

function formatMoney(v: unknown) {
  const n = Number(v)
  if (!Number.isFinite(n)) return '-'
  return `￥${n.toFixed(2)}`
}

function getOrderType(status?: number) {
  if (status === 1) return '待付款'
  if (status === 2) return '待接单'
  if (status === 3) return '待派送'
  if (status === 4) return '派送中'
  if (status === 5) return '已完成'
  if (status === 6) return '已取消'
  return '退款'
}

type OrderDetail = Record<string, any> | null

type DishItem = {
  name?: string
  dishName?: string
  number?: number
  copies?: number
  amount?: number
  dishFlavor?: string
  flavors?: string
  image?: string
}

function pickItems(detail: OrderDetail): DishItem[] {
  if (!detail) return []
  const raw =
    (detail as any).orderDetailList ||
    (detail as any).orderDetails ||
    (detail as any).details ||
    (detail as any).orderDishes ||
    (detail as any).dishList
  if (!raw) return []
  if (Array.isArray(raw)) return raw as any
  return []
}

export function OrderDetailDialog({
  open,
  title = '订单详情',
  loading,
  detail,
  row,
  onClose,
  onAccept,
  onReject,
  onCancel,
  onDelivery,
  onComplete,
}: {
  open: boolean
  title?: string
  loading: boolean
  detail: OrderDetail
  row: OrderRow | null
  onClose: () => void
  onAccept: () => void
  onReject: () => void
  onCancel: () => void
  onDelivery: () => void
  onComplete: () => void
}) {
  const status = Number((detail as any)?.status ?? row?.status ?? 0)
  const header = useMemo(() => {
    const number = (detail as any)?.number ?? row?.number
    return number ? `${title}（${number}）` : title
  }, [detail, row?.number, title])

  const items = useMemo(() => pickItems(detail), [detail])
  const base = {
    number: (detail as any)?.number ?? row?.number,
    status,
    orderTime: (detail as any)?.orderTime ?? row?.orderTime,
    payMethod: (detail as any)?.payMethod,
    amount: (detail as any)?.amount ?? row?.amount,
    remark: (detail as any)?.remark,
    cancelReason: (detail as any)?.cancelReason,
    rejectionReason: (detail as any)?.rejectionReason,
    consignee: (detail as any)?.consignee ?? row?.consignee,
    phone: (detail as any)?.phone ?? row?.phone,
    address: (detail as any)?.address ?? row?.address,
  }

  const footer = (
    <span className="dialog-footer">
      <ElButton size="medium" onClick={onClose}>
        关 闭
      </ElButton>
      {status === 2 ? (
        <>
          <ElButton size="medium" className="delBut" onClick={onCancel}>
            取 消
          </ElButton>
          <ElButton size="medium" className="delBut" onClick={onReject}>
            拒 单
          </ElButton>
          <ElButton elType="primary" size="medium" className="continue" onClick={onAccept}>
            接 单
          </ElButton>
        </>
      ) : null}
      {status === 3 ? (
        <ElButton elType="primary" size="medium" className="continue" onClick={onDelivery}>
          派 送
        </ElButton>
      ) : null}
      {status === 4 ? (
        <ElButton elType="primary" size="medium" className="continue" onClick={onComplete}>
          完 成
        </ElButton>
      ) : null}
    </span>
  )

  return (
    <ElDialog open={open} title={header} width="52%" onClose={onClose} footer={footer}>
      {loading ? (
        <div style={{ padding: '10px 0' }}>加载中...</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          <div>
            <div className="tableBar" style={{ marginBottom: 10 }}>
              <span style={{ fontSize: 14, fontWeight: 600 }}>基本信息</span>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              <div>订单号：{safeText(base.number)}</div>
              <div>订单状态：{getOrderType(status)}</div>
              <div>下单时间：{safeText(base.orderTime)}</div>
              <div>支付方式：{safeText(base.payMethod)}</div>
              <div>订单金额：{formatMoney(base.amount)}</div>
              <div>备注：{safeText(base.remark)}</div>
              {base.cancelReason ? <div>取消原因：{safeText(base.cancelReason)}</div> : null}
              {base.rejectionReason ? <div>拒单原因：{safeText(base.rejectionReason)}</div> : null}
            </div>
          </div>

          <div>
            <div className="tableBar" style={{ marginBottom: 10 }}>
              <span style={{ fontSize: 14, fontWeight: 600 }}>收货信息</span>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              <div>收货人：{safeText(base.consignee)}</div>
              <div>手机号：{safeText(base.phone)}</div>
              <div style={{ gridColumn: '1 / -1' }}>地址：{safeText(base.address)}</div>
            </div>
          </div>

          <div>
            <div className="tableBar" style={{ marginBottom: 10 }}>
              <span style={{ fontSize: 14, fontWeight: 600 }}>菜品明细</span>
            </div>

            <div className="el-table tableBox el-table--fit el-table--striped el-table--enable-row-hover order-detail-items-table">
              <div className="el-table__header-wrapper">
                <table className="el-table__header">
                  <colgroup>
                    <col style={{ width: '38%' }} />
                    <col style={{ width: '32%' }} />
                    <col style={{ width: '15%' }} />
                    <col style={{ width: '15%' }} />
                  </colgroup>
                  <thead>
                    <tr>
                      <th className="el-table__cell">
                        <div className="cell">菜品</div>
                      </th>
                      <th className="el-table__cell">
                        <div className="cell">口味/规格</div>
                      </th>
                      <th className="el-table__cell is-center">
                        <div className="cell">数量</div>
                      </th>
                      <th className="el-table__cell is-right">
                        <div className="cell">小计</div>
                      </th>
                    </tr>
                  </thead>
                </table>
              </div>

              <div className="el-table__body-wrapper">
                <table className="el-table__body">
                  <colgroup>
                    <col style={{ width: '38%' }} />
                    <col style={{ width: '32%' }} />
                    <col style={{ width: '15%' }} />
                    <col style={{ width: '15%' }} />
                  </colgroup>
                  <tbody>
                    {items.length ? (
                      items.map((it, idx) => {
                        const name = it.name || it.dishName
                        const flavor = it.dishFlavor || it.flavors
                        const qty = Number(it.number ?? it.copies)
                        const amount = it.amount
                        return (
                          <tr key={`${idx}-${String(name || '')}`} className="el-table__row">
                            <td className="el-table__cell">
                              <div className="cell" title={safeText(name)}>
                                {safeText(name)}
                              </div>
                            </td>
                            <td className="el-table__cell">
                              <div className="cell" title={safeText(flavor)}>
                                {safeText(flavor)}
                              </div>
                            </td>
                            <td className="el-table__cell is-center">
                              <div className="cell">{Number.isFinite(qty) && qty > 0 ? qty : '-'}</div>
                            </td>
                            <td className="el-table__cell is-right">
                              <div className="cell">{formatMoney(amount)}</div>
                            </td>
                          </tr>
                        )
                      })
                    ) : (
                      <tr>
                        <td className="el-table__cell" colSpan={4}>
                          <div className="el-table__empty-block">
                            <span className="el-table__empty-text">暂无数据</span>
                          </div>
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}
    </ElDialog>
  )
}
