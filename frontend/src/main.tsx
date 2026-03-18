import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import "./styles/reset.css";
import "./styles/theme.css";
import { brand } from "./config/brand";

function applyBrandTheme() {
  const r = document.documentElement;
  const t = brand.theme;
  r.style.setProperty("--bg0", t.bg0);
  r.style.setProperty("--bg1", t.bg1);
  r.style.setProperty("--neonA", t.neonA);
  r.style.setProperty("--neonB", t.neonB);
  r.style.setProperty("--neonC", t.neonC);
  r.style.setProperty("--dangerA", t.dangerA);
  r.style.setProperty("--dangerB", t.dangerB);
  r.style.setProperty("--text0", t.text0);
  r.style.setProperty("--text1", t.text1);
  r.style.setProperty("--card", t.card);
  r.style.setProperty("--border", t.border);
}

applyBrandTheme();

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);