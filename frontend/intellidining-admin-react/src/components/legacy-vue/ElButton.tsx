import type { ButtonHTMLAttributes, ReactNode } from 'react'

type ElButtonType = 'default' | 'primary' | 'text'
type ElButtonSize = 'medium' | 'small' | 'mini'

export function ElButton({
  children,
  elType = 'default',
  size = 'medium',
  className,
  ...rest
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  children: ReactNode
  elType?: ElButtonType
  size?: ElButtonSize
}) {
  const classes = [
    'el-button',
    `el-button--${elType}`,
    size ? `el-button--${size}` : '',
    className || '',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <button type="button" className={classes} {...rest}>
      <span>{children}</span>
    </button>
  )
}

