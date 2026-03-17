import { useNavigate } from "react-router-dom";
import { brand } from "../config/brand";
import { apiFetch } from "../lib/api";
import { clearSession, getSession } from "../lib/session";

export default function TopBar() {
  const nav = useNavigate();
  const session = getSession();

  async function onLogout() {
    try {
      await apiFetch<{ message: string }>("/auth/logout", { method: "POST", auth: true });
    } catch {
      // If logout fails (expired session), still clear client-side.
    } finally {
      clearSession();
      nav("/login", { replace: true });
    }
  }

  return (
    <div className="topBar">
      <div className="topBarLeft" onClick={() => nav(session ? "/dashboard" : "/login")}>
        <img className="topBarLogo" src={brand.logoPath} alt="logo" />
        <div className="topBarName">
          <div className="topBarApp">{brand.appName}</div>
          <div className="topBarTag">Pitch + Neon</div>
        </div>
      </div>

      <div className="topBarRight">
        {session?.userName ? <div className="chip">@{session.userName}</div> : null}
        {session ? (
          <button className="btn btnDanger" onClick={onLogout}>
            Logout
          </button>
        ) : null}
      </div>
    </div>
  );
}