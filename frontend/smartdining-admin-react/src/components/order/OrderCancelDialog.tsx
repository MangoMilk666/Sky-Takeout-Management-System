import { useMemo, useState } from 'react'
import { orderCancel, orderReject } from '@/api/order'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElDialog } from '@/components/legacy-vue/ElDialog'
import { ElSelect } from '@/components/legacy-vue/ElSelect'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { message } from '@/lib/ui/message'

type Mode = '取消' | '拒绝'

export function OrderCancelDialog({
  open,
  mode,
  orderId,
  onClose,
  onDone,
}: {
  open: boolean
  mode: Mode
  orderId: string
  onClose: () => void
  onDone: () => void
}) {
  const [reason, setReason] = useState<string>('')
  const [remark, setRemark] = useState<string>('')
  const [saving, setSaving] = useState(false)

  const title = useMemo(() => `${mode}订单`, [mode])

  const reasonOptions = useMemo(
    () => [
      { value: '订单信息有误', label: '订单信息有误' },
      { value: '库存不足', label: '库存不足' },
      { value: '商家忙碌', label: '商家忙碌' },
      { value: '自定义原因', label: '自定义原因' },
    ],
    [],
  )

  const confirm = async () => {
    if (!orderId) return
    if (!reason) {
      message.error(`请选择${mode}原因`)
      return
    }
    if (reason === '自定义原因' && !remark.trim()) {
      message.error(`请输入${mode}原因`)
      return
    }

    const reasonValue = reason === '自定义原因' ? remark.trim() : reason
    setSaving(true)
    try {
      const res =
        mode === '取消'
          ? await orderCancel({ id: orderId, cancelReason: reasonValue })
          : await orderReject({ id: orderId, rejectionReason: reasonValue })
      if (String(res.data?.code) === '1') {
        message.success('操作成功')
        onDone()
        return
      }
      message.error(res.data?.msg || '操作失败')
    } catch (e: any) {
      if (isRequestCanceled(e)) return
      message.error(e?.message || '操作失败')
    } finally {
      setSaving(false)
    }
  }

  return (
    <ElDialog
      open={open}
      title={title}
      width="30%"
      onClose={() => {
        if (saving) return
        onClose()
      }}
      footer={
        <span className="dialog-footer">
          <ElButton
            size="medium"
            onClick={() => {
              if (saving) return
              onClose()
            }}
          >
            取 消
          </ElButton>
          <ElButton elType="primary" size="medium" className="continue" onClick={confirm}>
            确 定
          </ElButton>
        </span>
      }
    >
      <div className="el-form demo-form-inline">
        <div className="el-form-item">
          <label className="el-form-item__label" style={{ width: 100 }}>
            {mode}原因：
          </label>
          <div className="el-form-item__content" style={{ marginLeft: 100 }}>
            <ElSelect<string>
              value={reason || undefined}
              options={reasonOptions}
              onChange={(v) => {
                setReason(v)
                if (v !== '自定义原因') setRemark('')
              }}
            />
          </div>
        </div>

        {reason === '自定义原因' ? (
          <div className="el-form-item">
            <label className="el-form-item__label" style={{ width: 100 }}>
              备注：
            </label>
            <div className="el-form-item__content" style={{ marginLeft: 100 }}>
              <div className="el-textarea">
                <textarea
                  className="el-textarea__inner"
                  rows={3}
                  value={remark}
                  placeholder={`请输入${mode}原因`}
                  onChange={(e) => setRemark(e.target.value)}
                />
              </div>
            </div>
          </div>
        ) : null}
      </div>
    </ElDialog>
  )
}

