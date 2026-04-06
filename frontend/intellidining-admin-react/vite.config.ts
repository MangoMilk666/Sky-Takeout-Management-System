import path from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTargetRaw = env.VITE_PROXY_TARGET || 'http://localhost:8080/admin'
  const proxyUrl = new URL(proxyTargetRaw)
  const proxyOrigin = `${proxyUrl.protocol}//${proxyUrl.host}`
  const proxyBasePath = proxyUrl.pathname.replace(/\/$/, '')

  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "@/styles/legacy-vue/_variables.scss" as *;\n@use "@/styles/legacy-vue/_mixins.scss" as *;\n`,
          silenceDeprecations: ['import'],
          quietDeps: true,
        },
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: proxyOrigin,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/api/, proxyBasePath),
        },
      },
    },
  }
})
