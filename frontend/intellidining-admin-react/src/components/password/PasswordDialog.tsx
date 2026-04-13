import { useMemo, useState } from 'react'
import { editPassword } from '@/api/users'
import { ElButton } from '@/components/legacy-vue/ElButton'
import { ElDialog } from '@/components/legacy-vue/ElDialog'
import { ElInput } from '@/components/legacy-vue/ElInput'
import { isRequestCanceled } from '@/lib/http/isCanceled'
import { message } from '@/lib/ui/message'

const passwordReg = /^[0-9A-Za-z]{6,20}$/

export function PasswordDialog({
  open,
  onClose,
}: {
  open: boolean
  onClose: () => void
}) {
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [affirmPassword, setAffirmPassword] = useState('')
  const [touched, setTouched] = useState({ old: false, next: false, affirm: false })
  const [saving, setSaving] = useState(false)

  const oldError = useMemo(() => {
    if (!touched.old) return ''
    if (!oldPassword) return '请输入'
    if (!passwordReg.test(oldPassword)) return '6 - 20位密码，数字或字母，区分大小写'
    return ''
  }, [oldPassword, touched.old])

  const nextError = useMemo(() => {
    if (!touched.next) return ''
    if (!newPassword) return '请输入'
    if (!passwordReg.test(newPassword)) return '6 - 20位密码，数字或字母，区分大小写'
    return ''
  }, [newPassword, touched.next])

  const affirmError = useMemo(() => {
    if (!touched.affirm) return ''
    if (!affirmPassword) return '请再次输入密码'
    if (affirmPassword !== newPassword) return '密码不一致，请重新输入密码'
    return ''
  }, [affirmPassword, newPassword, touched.affirm])

  const footer = (
    <span className="dialog-footer">
      <ElButton
        onClick={() => {
          if (saving) return
          onClose()
        }}
      >
        取 消
      </ElButton>
      <ElButton
        elType="primary"
        className="continue"
        disabled={saving}
        onClick={async () => {
          if (saving) return
          setTouched({ old: true, next: true, affirm: true })
          const hasError = Boolean(oldError || nextError || affirmError)
          if (hasError) return

          setSaving(true)
          try {
            const res = await editPassword({ oldPassword, newPassword })
            if (String(res.data?.code) === '1') {
              message.success('操作成功')
              setOldPassword('')
              setNewPassword('')
              setAffirmPassword('')
              setTouched({ old: false, next: false, affirm: false })
              onClose()
              return
            }
            message.error(res.data?.msg || '操作失败')
          } catch (e: any) {
            if (isRequestCanceled(e)) return
            message.error(e?.message || '操作失败')
          } finally {
            setSaving(false)
          }
        }}
      >
        保 存
      </ElButton>
    </span>
  )

  return (
    <ElDialog
      open={open}
      title="修改密码"
      width="568px"
      dialogClassName="pwdCon"
      onClose={() => {
        if (saving) return
        onClose()
      }}
      footer={footer}
    >
      <div className="el-form" style={{ width: '100%' }}>
        <div className={['el-form-item', oldError ? 'is-error' : ''].filter(Boolean).join(' ')}>
          <label className="el-form-item__label" style={{ width: 85 }}>
            原始密码：
          </label>
          <div className="el-form-item__content" style={{ marginLeft: 85 }}>
            <ElInput
              type="password"
              placeholder="请输入"
              value={oldPassword}
              onChange={(e) => setOldPassword(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, old: true }))}
            />
            {oldError ? <div className="el-form-item__error">{oldError}</div> : null}
          </div>
        </div>

        <div className={['el-form-item', nextError ? 'is-error' : ''].filter(Boolean).join(' ')}>
          <label className="el-form-item__label" style={{ width: 85 }}>
            新密码：
          </label>
          <div className="el-form-item__content" style={{ marginLeft: 85 }}>
            <ElInput
              type="password"
              placeholder="6 - 20位密码，数字或字母，区分大小写"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, next: true }))}
            />
            {nextError ? <div className="el-form-item__error">{nextError}</div> : null}
          </div>
        </div>

        <div className={['el-form-item', affirmError ? 'is-error' : ''].filter(Boolean).join(' ')}>
          <label className="el-form-item__label" style={{ width: 85 }}>
            确认密码：
          </label>
          <div className="el-form-item__content" style={{ marginLeft: 85 }}>
            <ElInput
              type="password"
              placeholder="请输入"
              value={affirmPassword}
              onChange={(e) => setAffirmPassword(e.target.value)}
              onBlur={() => setTouched((t) => ({ ...t, affirm: true }))}
            />
            {affirmError ? <div className="el-form-item__error">{affirmError}</div> : null}
          </div>
        </div>
      </div>
    </ElDialog>
  )
}

