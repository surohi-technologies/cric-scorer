import { Link, useNavigate } from "react-router-dom";
import NeonCard from "../components/NeonCard";
import TopBar from "../components/TopBar";
import { getSession } from "../lib/session";

export default function DashboardPage() {
  const nav = useNavigate();
  const s = getSession();

  return (
    <div className="page">
      <TopBar />
      <main className="container">
        <NeonCard title="Dashboard" subtitle="This is the landing area after successful login.">
          <div className="stack">
            <div className="muted">
              Logged in as <b>{s?.userName ? `@${s.userName}` : `userId=${s?.userId}`}</b>
            </div>
            <div className="grid2">
              <button className="btn btnGhost" onClick={() => nav("/profile")}>
                View / Edit profile
              </button>
              <Link className="btn btnGhost" to="/login">
                Back to login
              </Link>
            </div>
            <div className="muted">
              Next: we can build scoring screens, team management, match creation, and player search.
            </div>
          </div>
        </NeonCard>
      </main>
    </div>
  );
}