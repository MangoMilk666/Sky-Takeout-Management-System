import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'normalize.css'
import './styles/element-ui/index.css'
import 'nprogress/nprogress.css'
import './styles/legacy-vue/index.scss'
import './styles/legacy-vue/home.scss'
import './styles/legacy-vue/newRJWMsystem.scss'
import './styles/legacy-vue/icon/iconfont.css'
import './styles/legacy-vue/layout-components.scss'
import './styles/compat.scss'
import App from './App.tsx'

createRoot(document.getElementById('app')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
