import type { CSSProperties, InputHTMLAttributes, ReactNode } from 'react'

export function ElInput({
  value,
  onChange,
  onClear,
  clearable,
  prefix,
  style,
  className,
  ...rest
}: Omit<InputHTMLAttributes<HTMLInputElement>, 'style'> & {
  onClear?: () => void
  clearable?: boolean
  prefix?: ReactNode
  style?: CSSProperties
  className?: string
}) {
  const hasValue = value !== undefined && value !== null && String(value) !== ''
  const showClear = Boolean(clearable && hasValue && onClear)

  const wrapperClass = [
    'el-input',
    'el-input--medium',
    prefix ? 'el-input--prefix' : '',
    showClear ? 'el-input--suffix' : '',
    className || '',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={wrapperClass} style={style}>
      {prefix ? <span className="el-input__prefix">{prefix}</span> : null}
      <input className="el-input__inner" value={value as any} onChange={onChange} {...rest} />
      {showClear ? (
        <span className="el-input__suffix">
          <span className="el-input__suffix-inner">
            <i
              className="el-input__icon el-icon-circle-close"
              role="button"
              tabIndex={0}
              onClick={(e) => {
                e.preventDefault()
                onClear?.()
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') onClear?.()
              }}
            />
          </span>
        </span>
      ) : null}
    </div>
  )
}

