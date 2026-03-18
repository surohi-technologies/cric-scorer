import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { apiFetch } from "./api";
import { clearSession, getSession } from "./session";

const FLASH_KEY = "cric:flash";

export function useIdleLogout() {
  const nav = useNavigate();
  const lastActivityRef = useRef<number>(Date.now());
  const firedRef = useRef(false);

  useEffect(() => {
    const s = getSession();
    if (!s) return;

    const idleSec = Math.max(1, Number(s.idleTimeoutSeconds || 60));
    const idleMs = idleSec * 1000;

    firedRef.current = false;
    lastActivityRef.current = Date.now();

    const touch = () => {
      lastActivityRef.current = Date.now();
    };

    const events: Array<keyof WindowEventMap> = [
      "mousemove",
      "mousedown",
      "keydown",
      "touchstart",
      "scroll",
      "click"
    ];

    events.forEach((ev) => window.addEventListener(ev, touch, { passive: true }));

    const t = window.setInterval(() => {
      if (firedRef.current) return;
      if (!getSession()) return;

      const idleFor = Date.now() - lastActivityRef.current;
      if (idleFor < idleMs) return;

      firedRef.current = true;
      (async () => {
        try {
          await apiFetch<{ message: string }>("/auth/logout", { method: "POST", auth: true });
        } catch {
          // ignore
        } finally {
          clearSession();
          sessionStorage.setItem(FLASH_KEY, "Session expired due to inactivity. Please login again.");
          nav("/login", { replace: true });
        }
      })();
    }, 1000);

    return () => {
      window.clearInterval(t);
      events.forEach((ev) => window.removeEventListener(ev, touch));
    };
  }, [nav]);
}