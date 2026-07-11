import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: 'config/vite_config/dist_prod',
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

          if (primaryName.endsWith('.css')) {
            return 'assets/index-[hash].css';
          }

          //Forces fonts (.eot, .ttf, .woff, .woff2, .svg) into flat, predictable structure
          if(/\.(woff2?|ttf|eot|svg)$/i.test(primaryName)){
            return 'assets/[name]-[hash][extname]';
          }

          return 'assets/[name]-[hash][extname]';
        },
        // 5. Forces PrimeVue and all node_modules to collapse into the main entry point 
        manualChunks(id) {
          if(id.includes('node_modules')){
            // Forcing node modules to merge directly with the main file
            return 'index';
          }
        }
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})