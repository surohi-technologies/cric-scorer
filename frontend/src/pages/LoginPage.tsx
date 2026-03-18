import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import NeonCard from "../components/NeonCard";
import TopBar from "../components/TopBar";
import { brand } from "../config/brand";
import { apiFetch } from "../lib/api";
import { setSession } from "../lib/session";

type LoginResponse = {
  message: string;
  sessionKey: string;
  idleTimeoutSeconds: number;
  userId: number;
  userName: string | null;
  profileCompleted: boolean;
  firstTimeLogin: boolean;
  nextAction: string;
};

const REMEMBER_KEY = "cric:rememberLogin";
const FLASH_KEY = "cric:flash";

export default function LoginPage() {
  const nav = useNavigate();
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [remember, setRemember] = useState(false);
  const [busy, setBusy] = useState(false);
  const [info, setInfo] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(REMEMBER_KEY);
      if (!raw) return;
      const obj = JSON.parse(raw) as { loginId?: string };
      if (obj.loginId) {
        setLoginId(obj.loginId);
        setRemember(true);
      }
    } catch {
      // ignore
    }
  }, []);

  useEffect(() => {
    const flash = sessionStorage.getItem(FLASH_KEY);
    if (flash) {
      setInfo(flash);
      sessionStorage.removeItem(FLASH_KEY);
    }
  }, []);

  const helper = useMemo(() => {
    return "Use your username, mobile number, or email ID.";
  }, []);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setInfo(null);
    setError(null);

    if (remember) {
      localStorage.setItem(REMEMBER_KEY, JSON.stringify({ loginId }));
    } else {
      localStorage.removeItem(REMEMBER_KEY);
    }

    setBusy(true);
    try {
      const res = await apiFetch<LoginResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ loginId, password })
      });

      setSession({
        sessionKey: res.sessionKey,
        idleTimeoutSeconds: res.idleTimeoutSeconds,
        userId: res.userId,
        userName: res.userName,
        profileCompleted: res.profileCompleted,
        firstTimeLogin: res.firstTimeLogin,
        nextAction: res.nextAction
      });

      // Trigger the dashboard welcome animation once per login.
      sessionStorage.setItem("cric:welcome", "1");

      if (res.nextAction === "COMPLETE_PROFILE" || !res.profileCompleted) {
        nav("/profile", { replace: true });
      } else {
        nav("/dashboard", { replace: true });
      }
    } catch (err: any) {
      setError(err?.message ?? "Login failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="page pageAuth pageLogin">
      <TopBar />
      <main className="container">
        <div className="hero">
          <div className="heroTitle">{brand.appName}</div>
          <div className="heroMsg">{brand.heroMessage}</div>
          <div className="heroMeta">
            Support: <a href={`mailto:${brand.supportEmail}`}>{brand.supportEmail}</a>
          </div>
        </div>

        <NeonCard title="Login" subtitle={helper}>
          <form className="form" onSubmit={onSubmit}>
            <label className="field">
              <span className="label">Login ID</span>
              <input
                className="input"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                placeholder="username / phone / email"
                autoComplete="username"
              />
            </label>

            <label className="field">
              <span className="label">Password</span>
              <input
                className="input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Your password"
                autoComplete="current-password"
              />
            </label>

            <div className="rowSplit">
              <label className="checkRow">
                <input type="checkbox" checked={remember} onChange={(e) => setRemember(e.target.checked)} />
                <span>Remember me (prefill login ID next time)</span>
              </label>
              <Link className="link" to="/forgot-password">
                Forgot password?
              </Link>
            </div>

            {info ? <div className="alert alertOk">{info}</div> : null}
            {error ? <div className="alert alertErr">{error}</div> : null}

            <button className="btn btnPrimary" disabled={busy}>
              {busy ? "Signing in..." : "Login"}
            </button>

            <div className="muted">
              New here? <Link to="/register">Create an account</Link>
            </div>
          </form>
        </NeonCard>
      </main>
    </div>
  );
}