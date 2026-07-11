import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    host: 'localhost',
    port: 3000,
    base: '/saf-dashboard-bff/',
    proxy: {
      '/saf-dashboard-bff/api/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/saf-dashboard-bff\/api\/v1/, '\/auth'),
      },
    },
  },
  build: {
    outDir: 'config/vite_config/dist_hot',
    // 1. Disables CSS code splitting, forcing all Principles and app CSS into one file
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        // 2. Generates a single JS entry file with a hash (e.g. index-a1b2c3d4.js)
        entryFileNames: 'assets/index-[hash].js',
        // 3. Catches any dynamically split chunks if they still try to form 
        chunkFileNames: 'assets/[name]-[hash].js',
	      // 4. Forces the single combined CSS asset to use the same hashed style
	      assetFileNames: (assetInfo) => {
          const primaryName = assetInfo.names?.[0] || '';

		      if(primaryName.endsWith('.css')){
			      return 'assets/index-[hash].css';
		      }
		      return 'assets/[name]-[hash][extname]';
	      },        
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
