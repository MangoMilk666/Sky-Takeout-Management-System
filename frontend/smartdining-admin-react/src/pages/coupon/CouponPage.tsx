import { DatePicker } from 'antd'
import type { Dayjs } from 'dayjs'
import { useEffect, useMemo, useState } from 'react'
import './coupon.scss'
import { listCoupons, publishCoupon, type CouponRow } from '@/api/coupon'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElDialog } from '@/components/legacy-vue/ElDialog'
import { ElInput } from '@/components/legacy-vue/ElInput'
import { ElSelect } from '@/components/legacy-vue/ElSelect'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { usePageTitle } from '@/lib/ui/usePageTitle'

export function CouponPage() {
  usePageTitle('IntelliDining - 优惠券管理')
  const [rows, setRows] = useState<CouponRow[]>([])
  const [loading, setLoading] = useState(false)
  const [dialogVisible, setDialogVisible] = useState(false)

  const [name, setName] = useState('')
  const [discountType, setDiscountType] = useState<1 | 2>(2)
  const [discount, setDiscount] = useState('')
  const [totalCount, setTotalCount] = useState('')
  const [beginTime, setBeginTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [timeRange, setTimeRange] = useState<[Dayjs, Dayjs] | null>(null)

  const fetchList = async () => {
    setLoading(true)
    try {
      const res = await listCoupons()
      if (String(res.data?.code) === '1') {
        setRows(res.data?.data || [])
      } else {
        window.alert(res.data?.msg || '查询失败')
      }
    } catch (e: any) {
      if (isRequestCanceled(e)) return
      window.alert(`请求出错了：${e?.message || '未知错误'}`)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchList()
  }, [])

  const dialogTitle = useMemo(() => '发布优惠券', [])

  const validate = () => {
    if (!name.trim()) return '优惠券名称不能为空'
    if (!discount.trim()) return '优惠值不能为空'
    if (!totalCount.trim()) return '库存不能为空'
    if (!beginTime.trim()) return '有效期开始时间不能为空'
    if (!endTime.trim()) return '有效期结束时间不能为空'
    const count = Number(totalCount)
    if (!Number.isFinite(count) || count <= 0) return '库存必须为正数'
    const d = Number(discount)
    if (!Number.isFinite(d) || d <= 0) return '优惠值必须为正数'
    if (discountType === 1 && d > 1) return '折扣类型时优惠值应为0~1'
    return ''
  }

  const submit = async () => {
    const err = validate()
    if (err) {
      window.alert(err)
      return
    }
    try {
      const res = await publishCoupon({
        name: name.trim(),
        discountType,
        discount: Number(discount),
        totalCount: Number(totalCount),
        beginTime: beginTime.trim(),
        endTime: endTime.trim(),
      })
      if (String(res.data?.code) === '1') {
        window.alert('发布成功，已完成Redis预热')
        setDialogVisible(false)
        setName('')
        setDiscount('')
        setTotalCount('')
        setBeginTime('')
        setEndTime('')
        setTimeRange(null)
        await fetchList()
        return
      }
      window.alert(res.data?.msg || '发布失败')
    } catch (e: any) {
      if (isRequestCanceled(e)) return
      window.alert(`请求出错了：${e?.message || '未知错误'}`)
    }
  }

  return (
    <div className="dashboard-container">
      <div className="container">
        <div className="tableBar" style={{ display: 'inline-block', width: '100%' }}>
          <div style={{ float: 'right' }}>
            <ElButton elType="primary" className="continue" onClick={() => setDialogVisible(true)}>
              + 发布优惠券
            </ElButton>
          </div>
          <ElButton className="normal-btn continue" onClick={() => fetchList()}>
            刷新
          </ElButton>
        </div>

        <div className="el-table tableBox el-table--fit el-table--striped el-table--enable-row-hover">
          <div className="el-table__header-wrapper">
            <table className="el-table__header">
              <thead>
                <tr>
                  <th className="el-table__cell"><div className="cell">名称</div></th>
                  <th className="el-table__cell"><div className="cell">类型</div></th>
                  <th className="el-table__cell"><div className="cell">优惠</div></th>
                  <th className="el-table__cell"><div className="cell">库存(剩余/总)</div></th>
                  <th className="el-table__cell"><div className="cell">有效期</div></th>
                  <th className="el-table__cell"><div className="cell">更新时间</div></th>
                </tr>
              </thead>
            </table>
          </div>
          <div className="el-table__body-wrapper">
            <table className="el-table__body">
              <tbody>
                {rows.length ? (
                  rows.map((row) => (
                    <tr key={row.id} className="el-table__row">
                      <td className="el-table__cell"><div className="cell">{row.name}</div></td>
                      <td className="el-table__cell"><div className="cell">{row.discountType === 1 ? '折扣' : '直减'}</div></td>
                      <td className="el-table__cell"><div className="cell">{row.discountType === 1 ? `${row.discount}` : `¥${row.discount}`}</div></td>
                      <td className="el-table__cell"><div className="cell">{row.remainedCount}/{row.totalCount}</div></td>
                      <td className="el-table__cell"><div className="cell">{row.beginTime} ~ {row.endTime}</div></td>
                      <td className="el-table__cell"><div className="cell">{row.updateTime || '-'}</div></td>
                    </tr>
                  ))
                ) : (
                  <tr className="el-table__row">
                    <td className="el-table__cell" colSpan={6}>
                      <div className="cell">{loading ? '加载中...' : '暂无数据'}</div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        <ElDialog
          open={dialogVisible}
          title={dialogTitle}
          width="600px"
          dialogClassName="coupon-publish-dialog"
          onClose={() => setDialogVisible(false)}
          footer={
            <span className="dialog-footer">
              <ElButton onClick={() => setDialogVisible(false)}>取消</ElButton>
              <ElButton elType="primary" style={{ marginLeft: 12 }} onClick={submit}>
                发布
              </ElButton>
            </span>
          }
        >
          <div>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: 'inline-block', width: 110 }}>名称：</label>
              <ElInput value={name} placeholder="例如：满减券" style={{ width: '70%' }} onChange={(e) => setName(e.target.value)} />
            </div>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: 'inline-block', width: 110 }}>类型：</label>
              <ElSelect<1 | 2>
                value={discountType}
                options={[
                  { value: 2, label: '直减' },
                  { value: 1, label: '折扣' },
                ]}
                style={{ width: '70%' }}
                onChange={(v) => setDiscountType(v || 2)}
              />
            </div>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: 'inline-block', width: 110 }}>优惠值：</label>
              <ElInput
                value={discount}
                placeholder={discountType === 1 ? '0~1，例如 0.8' : '金额，例如 10'}
                style={{ width: '70%' }}
                onChange={(e) => setDiscount(e.target.value)}
              />
            </div>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: 'inline-block', width: 110 }}>库存：</label>
              <ElInput value={totalCount} placeholder="例如 100" style={{ width: '70%' }} onChange={(e) => setTotalCount(e.target.value)} />
            </div>
            <div style={{ marginBottom: 12 }}>
              <label style={{ display: 'inline-block', width: 110 }}>开始时间：</label>
              <DatePicker.RangePicker
                showTime
                value={timeRange}
                onChange={(v) => {
                  const next = (v as any) as [Dayjs, Dayjs] | null
                  setTimeRange(next)
                  setBeginTime(next ? next[0].format('YYYY-MM-DD HH:mm:ss') : '')
                  setEndTime(next ? next[1].format('YYYY-MM-DD HH:mm:ss') : '')
                }}
                style={{ width: '70%' }}
              />
            </div>
            <div style={{ marginBottom: 12, color: '#666', fontSize: 12, paddingLeft: 110 }}>
              {beginTime && endTime ? `${beginTime} ~ ${endTime}` : '请选择有效期（含时间）'}
            </div>
          </div>

        </ElDialog>
      </div>
    </div>
  )
}
