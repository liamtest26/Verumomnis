import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    strictPort: false,
    open: true,
  },
  build: {
    outDir: 'dist',
    sourcemap: process.env.NODE_ENV === 'production' ? false : true,
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['react', 'react-dom', 'react-router-dom'],
          'capacitor': ['@capacitor/core', '@capacitor/android'],
          'forensic': ['tesseract.js', 'pdfkit', 'jspdf'],
        }
      }
    }
  },
  define: {
    __BUILD_TIME__: JSON.stringify(new Date().toISOString()),
    __VERSION__: JSON.stringify('5.2.7'),
    __APK_HASH__: JSON.stringify('56937d92ecf2f23bb9f11dbd619c3ce13f324ead1765311fccd18b6dbf209466'),
  },
  preview: {
    port: 5000,
    strictPort: false,
  }
})
