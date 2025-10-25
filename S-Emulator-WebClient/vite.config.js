import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/login': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      },
      '/userslist': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      },
      '/programs': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      },
      '/exec': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      },
      '/api/chat': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      },
      '/api': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      },
      '/executionHistory': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      },
      '/credits': {
        target: process.env.VITE_SERVER_BASE_URL || 'http://localhost:8080/S_Emulator_Server',
        changeOrigin: true,
        cookieDomainRewrite: 'localhost',
        cookiePathRewrite: '/'
      }
    }
  }
})

