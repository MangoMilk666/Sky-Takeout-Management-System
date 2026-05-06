import type { CSSProperties } from 'react'

type Props = {
  mini?: boolean
  style?: CSSProperties
  className?: string
}

const GRAD_ID = 'sdBadgeGrad'

const BadgeIcon = () => (
  <>
    {/* Soft outer glow */}
    <circle cx="34" cy="34" r="33" fill="#FF6600" fillOpacity="0.14" />

    {/* Main gradient circle */}
    <circle cx="34" cy="34" r="30" fill={`url(#${GRAD_ID})`} />

    {/* White outer border — separates badge from any background */}
    <circle cx="34" cy="34" r="30" fill="none" stroke="white" strokeWidth="2.5" strokeOpacity="0.55" />

    {/* Subtle inner ring */}
    <circle cx="34" cy="34" r="25.5" fill="none" stroke="white" strokeWidth="0.7" strokeOpacity="0.28" />

    {/* ─── Fork (centered at x=23) ─── */}
    {/* Tines */}
    <line x1="19" y1="13" x2="19" y2="24" stroke="white" strokeWidth="2.1" strokeLinecap="round" />
    <line x1="23" y1="13" x2="23" y2="24" stroke="white" strokeWidth="2.1" strokeLinecap="round" />
    <line x1="27" y1="13" x2="27" y2="24" stroke="white" strokeWidth="2.1" strokeLinecap="round" />
    {/* Tine base curve → handle */}
    <path
      d="M19 24 C19 29 23 30.5 23 30.5 C23 30.5 27 29 27 24"
      stroke="white" strokeWidth="2.1" fill="none" strokeLinecap="round" strokeLinejoin="round"
    />
    {/* Handle */}
    <line x1="23" y1="30.5" x2="23" y2="55" stroke="white" strokeWidth="2.3" strokeLinecap="round" />

    {/* ─── Spoon (centered at x=46) ─── */}
    {/* Spoon bowl */}
    <ellipse cx="46" cy="22" rx="5.5" ry="7.5" stroke="white" strokeWidth="2.1" fill="none" />
    {/* Handle */}
    <line x1="46" y1="29.5" x2="46" y2="55" stroke="white" strokeWidth="2.3" strokeLinecap="round" />

    {/* ─── Decorative accent dots ─── */}
    {/* Top sparkle */}
    <circle cx="34" cy="5" r="2.8" fill="white" fillOpacity="0.78" />
    {/* Upper-left & upper-right edge dots */}
    <circle cx="11" cy="15" r="1.7" fill="white" fillOpacity="0.52" />
    <circle cx="57" cy="15" r="1.7" fill="white" fillOpacity="0.52" />
    {/* Tiny secondary dots beside the top sparkle */}
    <circle cx="27" cy="7" r="1.2" fill="white" fillOpacity="0.38" />
    <circle cx="41" cy="7" r="1.2" fill="white" fillOpacity="0.38" />
  </>
)

export function LogoBadge({ mini = false, style, className }: Props) {
  const defs = (
    <defs>
      <linearGradient id={GRAD_ID} x1="4" y1="4" x2="64" y2="64" gradientUnits="userSpaceOnUse">
        <stop stopColor="#FF6800" />
        <stop offset="0.55" stopColor="#E03200" />
        <stop offset="1" stopColor="#BB1A00" />
      </linearGradient>
    </defs>
  )

  if (mini) {
    return (
      <svg
        width="36"
        height="36"
        viewBox="0 0 68 68"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={style}
        className={className}
        aria-hidden="true"
      >
        {defs}
        <BadgeIcon />
      </svg>
    )
  }

  return (
    <svg
      viewBox="0 0 272 68"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      style={{ width: '100%', height: 'auto', maxWidth: 272, display: 'block', ...style }}
      className={className}
      role="img"
      aria-label="Smart-Dining 点餐平台"
    >
      {defs}

      <BadgeIcon />

      {/* ─── Text Section ─── */}

      {/* Main title */}
      <text
        x="80"
        y="31"
        fontFamily="'Segoe UI', 'SF Pro Display', 'Helvetica Neue', 'PingFang SC', Arial, sans-serif"
        fontSize="21"
        fontWeight="800"
        fill="#7B3000"
        letterSpacing="-0.3"
      >
        Smart-Dining
      </text>

      {/* Thin accent divider */}
      <rect x="80" y="36.5" width="170" height="1.2" rx="0.6" fill="rgba(120,40,0,0.28)" />

      {/* Subtitle */}
      <text
        x="80"
        y="53"
        fontFamily="'PingFang SC', 'Noto Sans CJK SC', 'Microsoft YaHei', sans-serif"
        fontSize="13.5"
        fontWeight="500"
        fill="#9A4800"
        letterSpacing="3"
      >
        点餐平台
      </text>

      {/* ─── Decorative dot cluster (right edge) ─── */}
      <circle cx="255" cy="47" r="2.2" fill="#C06000" fillOpacity="0.65" />
      <circle cx="261" cy="41" r="1.6" fill="#C06000" fillOpacity="0.45" />
      <circle cx="265" cy="53" r="1.3" fill="#C06000" fillOpacity="0.35" />

      {/* Small four-point star accent beside title */}
      <path
        d="M257 27 L258.5 24.5 L260 27 L262.5 28.5 L260 30 L258.5 32.5 L257 30 L254.5 28.5 Z"
        fill="#C06000"
        fillOpacity="0.55"
      />
    </svg>
  )
}
