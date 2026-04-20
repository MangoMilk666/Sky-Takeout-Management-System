import { useEffect, useMemo, useRef, useState } from 'react'

type Option<T extends string | number> = { value: T; label: string }

export function ElSelect<T extends string | number>({
  value,
  options,
  placeholder = '请选择',
  clearable,
  style,
  onChange,
  onClear,
}: {
  value: T | undefined
  options: Option<T>[]
  placeholder?: string
  clearable?: boolean
  style?: React.CSSProperties
  onChange: (value: T) => void
  onClear?: () => void
}) {
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (!open) return
      const el = rootRef.current
      if (!el) return
      if (e.target instanceof Node && el.contains(e.target)) return
      setOpen(false)
    }
    document.addEventListener('click', onDocClick)
    return () => document.removeEventListener('click', onDocClick)
  }, [open])

  const label = useMemo(() => options.find((o) => o.value === value)?.label || '', [options, value])
  const showClear = Boolean(clearable && value !== undefined && onClear)

  return (
    <div ref={rootRef} className="el-select" style={style}>
      <div className="el-input el-input--suffix" onClick={() => setOpen((v) => !v)} role="button" tabIndex={0}>
        <input className="el-input__inner" readOnly value={label} placeholder={placeholder} />
        <span className="el-input__suffix">
          <span className="el-input__suffix-inner">
            {showClear ? (
              <i
                className="el-input__icon el-icon-circle-close"
                role="button"
                tabIndex={0}
                onClick={(e) => {
                  e.preventDefault()
                  e.stopPropagation()
                  onClear?.()
                  setOpen(false)
                }}
              />
            ) : (
              <i className="el-select__caret el-input__icon el-icon-arrow-up" />
            )}
          </span>
        </span>
      </div>

      {open ? (
        <div className="el-select-dropdown el-popper">
          <div className="el-scrollbar">
            <div className="el-select-dropdown__wrap el-scrollbar__wrap">
              <ul className="el-scrollbar__view el-select-dropdown__list">
                {options.map((opt) => (
                  <li
                    key={String(opt.value)}
                    className={[
                      'el-select-dropdown__item',
                      opt.value === value ? 'selected' : '',
                    ]
                      .filter(Boolean)
                      .join(' ')}
                    onClick={() => {
                      onChange(opt.value)
                      setOpen(false)
                    }}
                  >
                    <span>{opt.label}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}

