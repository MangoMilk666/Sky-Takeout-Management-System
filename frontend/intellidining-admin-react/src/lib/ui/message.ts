type MessageType = 'success' | 'warning' | 'info' | 'error'

type MessageOptions = {
  duration?: number
}

function ensureHost() {
  const id = '__el_message_host__'
  const existing = document.getElementById(id)
  if (existing) return existing
  const host = document.createElement('div')
  host.id = id
  host.style.position = 'fixed'
  host.style.left = '50%'
  host.style.top = '16px'
  host.style.transform = 'translateX(-50%)'
  host.style.zIndex = '9999'
  host.style.pointerEvents = 'none'
  document.body.appendChild(host)
  return host
}

function render(type: MessageType, text: string, opts?: MessageOptions) {
  if (typeof document === 'undefined') return
  const host = ensureHost()

  const el = document.createElement('div')
  el.className = `el-message el-message--${type}`
  el.style.marginTop = '10px'
  el.style.pointerEvents = 'auto'

  const content = document.createElement('p')
  content.className = 'el-message__content'
  content.textContent = text
  el.appendChild(content)

  host.appendChild(el)

  const duration = opts?.duration ?? 2000
  if (duration > 0) {
    window.setTimeout(() => {
      el.remove()
    }, duration)
  }
}

export const message = {
  success: (text: string, opts?: MessageOptions) => render('success', text, opts),
  warning: (text: string, opts?: MessageOptions) => render('warning', text, opts),
  info: (text: string, opts?: MessageOptions) => render('info', text, opts),
  error: (text: string, opts?: MessageOptions) => render('error', text, opts),
}

