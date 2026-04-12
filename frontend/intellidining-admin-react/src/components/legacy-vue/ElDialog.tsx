import type { ReactNode } from 'react'

export function ElDialog({
  open,
  title,
  width = '30%',
  onClose,
  children,
  footer,
  showClose = true,
  wrapperClassName,
  dialogClassName,
}: {
  open: boolean
  title: string
  width?: string
  onClose: () => void
  children: ReactNode
  footer?: ReactNode
  showClose?: boolean
  wrapperClassName?: string
  dialogClassName?: string
}) {
  if (!open) return null

  return (
    <div
      className={['el-dialog__wrapper', wrapperClassName].filter(Boolean).join(' ')}
      role="dialog"
      aria-modal="true"
    >
      <div className="v-modal" onClick={onClose} />
      <div
        className={['el-dialog', dialogClassName].filter(Boolean).join(' ')}
        style={{ width }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="el-dialog__header">
          <span className="el-dialog__title">{title}</span>
          {showClose ? (
            <button type="button" className="el-dialog__headerbtn" onClick={onClose}>
              <i className="el-dialog__close el-icon el-icon-close" />
            </button>
          ) : null}
        </div>
        <div className="el-dialog__body">{children}</div>
        {footer !== undefined ? <div className="el-dialog__footer">{footer}</div> : null}
      </div>
    </div>
  )
}
