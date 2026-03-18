import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev setup:
// - Frontend runs at http://localhost:5173
// - Backend runs at http://localhost:2032
// - We proxy /newEra/* -> backend so we can call the API without CORS headaches.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      "/newEra": {
        target: "http://localhost:2032",
        changeOrigin: true
      }
    }
  }
});