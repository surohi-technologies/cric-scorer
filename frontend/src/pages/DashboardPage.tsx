import { useEffect, useMemo, useRef, useState } from "react";
import NeonCard from "../components/NeonCard";
import TopBar from "../components/TopBar";
import { fallbackQuotes } from "../config/quotes";
import { apiFetch } from "../lib/api";

type DashboardSummary = {
  welcomeName: string;
  userName: string | null;
  aliasName: string | null;
  jerseyNumber: number | null;
  roleLine: string | null;
  teamName: string | null;
  teamLogoUrl: string | null;
  performanceLevel: number;
  formPercent: number;
  last5FormColors: string[];
};

type TrumpTab = "batting" | "bowling" | "fielding";

type TrumpRow = { k: string; v: string | number };

function chipColorClass(c: string) {
  const v = (c || "").toUpperCase();
  if (v === "GREEN") return "chipGood";
  if (v === "LIME") return "chipGood2";
  if (v === "YELLOW") return "chipMid";
  if (v === "ORANGE") return "chipWarn";
  if (v === "RED") return "chipBad";
  if (v === "BLUE") return "chipInfo";
  return "chipNone";
}

function clamp01(n: number) {
  if (!Number.isFinite(n)) return 0;
  return Math.max(0, Math.min(1, n));
}

function AvatarBack({ jersey }: { jersey: number | null }) {
  const j = jersey && jersey > 0 ? jersey : 0;
  return (
    <svg viewBox="0 0 200 200" width="100%" height="100%" role="img" aria-label="avatar">
      <defs>
        <linearGradient id="a" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="var(--neonA)" stopOpacity="0.9" />
          <stop offset="1" stopColor="var(--neonB)" stopOpacity="0.9" />
        </linearGradient>
        <radialGradient id="b" cx="50%" cy="40%" r="70%">
          <stop offset="0" stopColor="#0E2230" />
          <stop offset="1" stopColor="#050B10" />
        </radialGradient>
      </defs>
      <rect x="0" y="0" width="200" height="200" rx="28" fill="url(#b)" />
      <path
        d="M100 36c14 0 25 11 25 25 0 9-4 16-11 21 18 6 34 18 43 35 4 8 2 16-6 20-16 9-34 14-51 14s-35-5-51-14c-8-4-10-12-6-20 9-17 25-29 43-35-7-5-11-12-11-21 0-14 11-25 25-25Z"
        fill="#0A1016"
        opacity="0.75"
      />
      <path
        d="M56 124c12-20 30-32 44-32s32 12 44 32c2 3 1 6-2 8-14 8-28 12-42 12s-28-4-42-12c-3-2-4-5-2-8Z"
        fill="url(#a)"
        opacity="0.16"
      />
      <path
        d="M72 96c10-8 20-12 28-12s18 4 28 12"
        stroke="url(#a)"
        strokeWidth="3"
        opacity="0.55"
        strokeLinecap="round"
      />
      <text
        x="100"
        y="132"
        textAnchor="middle"
        fontFamily="Space Grotesk, system-ui, -apple-system, Segoe UI, Arial"
        fontWeight="800"
        fontSize="42"
        fill="url(#a)"
        opacity="0.95"
      >
        {j}
      </text>
      <rect x="18" y="18" width="164" height="164" rx="24" fill="none" stroke="url(#a)" strokeOpacity="0.22" />
    </svg>
  );
}

function TeamBadge({ teamName, teamLogoUrl }: { teamName: string | null; teamLogoUrl: string | null }) {
  return (
    <div className="teamBadge">
      {teamName ? (
        <div className="teamRow">
          {teamLogoUrl ? <img className="teamLogo" src={teamLogoUrl} alt="team" /> : null}
          <span>{teamName}</span>
        </div>
      ) : (
        <div className="teamRow teamRowEmpty">
          <span>No Team</span>
          <span className="teamHint">Create or Join</span>
        </div>
      )}
    </div>
  );
}

function buildTrumpRows(tab: TrumpTab): TrumpRow[] {
  // Placeholder until match tables exist.
  if (tab === "batting") {
    return [
      { k: "Matches", v: "-" },
      { k: "Innings", v: "-" },
      { k: "Runs", v: "-" },
      { k: "Best", v: "-" },
      { k: "100s", v: "-" },
      { k: "50s", v: "-" },
      { k: "4s", v: "-" },
      { k: "6s", v: "-" }
    ];
  }
  if (tab === "bowling") {
    return [
      { k: "Matches", v: "-" },
      { k: "Overs", v: "-" },
      { k: "Wickets", v: "-" },
      { k: "Best", v: "-" },
      { k: "Economy", v: "-" },
      { k: "SR", v: "-" },
      { k: "Dots%", v: "-" },
      { k: "Maidens", v: "-" }
    ];
  }
  return [
    { k: "Catches", v: "-" },
    { k: "Run-outs", v: "-" },
    { k: "Stumpings", v: "-" },
    { k: "Drops", v: "-" },
    { k: "Direct hits", v: "-" },
    { k: "Assists", v: "-" },
    { k: "Safe hands", v: "-" },
    { k: "Impact", v: "-" }
  ];
}

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [quote, setQuote] = useState<string>(fallbackQuotes[0]);
  const [quotes, setQuotes] = useState<string[]>(fallbackQuotes);

  const [showWelcome, setShowWelcome] = useState(false);
  const [welcomeEnd, setWelcomeEnd] = useState<{ x: number; y: number } | null>(null);
  const welcomeDockRef = useRef<HTMLDivElement | null>(null);

  const [tab, setTab] = useState<TrumpTab>("batting");

  useEffect(() => {
    (async () => {
      try {
        const res = await apiFetch<string[]>("/meta/quotes", { method: "GET" });
        if (Array.isArray(res) && res.length > 0) {
          setQuotes(res);
          setQuote(res[0]);
        }
      } catch {
        // keep fallback
      }
    })();
  }, []);

  useEffect(() => {
    const timer = window.setInterval(() => {
      setQuote((prev) => {
        const idx = quotes.indexOf(prev);
        const next = quotes[(idx + 1 + quotes.length) % quotes.length];
        return next || quotes[0];
      });
    }, 5000);
    return () => window.clearInterval(timer);
  }, [quotes]);

  useEffect(() => {
    (async () => {
      try {
        const res = await apiFetch<DashboardSummary>("/dashboard/summary", { method: "GET", auth: true });
        setSummary(res);
      } catch (e: any) {
        setError(e?.message ?? "Failed to load dashboard");
      }
    })();
  }, []);

  useEffect(() => {
    const flag = sessionStorage.getItem("cric:welcome");
    if (flag !== "1") return;
    if (!summary) return;

    sessionStorage.removeItem("cric:welcome");
    setShowWelcome(true);

    requestAnimationFrame(() => {
      const r = welcomeDockRef.current?.getBoundingClientRect();
      if (r) {
        setWelcomeEnd({ x: r.left + r.width / 2, y: r.top + r.height / 2 });
      } else {
        setWelcomeEnd({ x: 160, y: 120 });
      }
    });

    window.setTimeout(() => {
      setShowWelcome(false);
      setWelcomeEnd(null);
    }, 1400);
  }, [summary]);

  const perf = useMemo(() => {
    const p = summary?.performanceLevel ?? 0;
    return Math.max(0, Math.min(100, Math.round(p)));
  }, [summary]);

  const perfFrac = useMemo(() => clamp01(perf / 100), [perf]);
  const perfDeg = useMemo(() => Math.round(perfFrac * 360), [perfFrac]);

  const welcomeName = summary?.welcomeName || "Player";
  const alias = summary?.aliasName || summary?.userName || "Player";

  const rows = useMemo(() => buildTrumpRows(tab), [tab]);

  return (
    <div className="page pageDash">
      <TopBar />

      {showWelcome ? (
        <div
          className="welcomeFly"
          style={
            welcomeEnd
              ? ({
                  ["--welcome-end-x" as any]: `${welcomeEnd.x}px`,
                  ["--welcome-end-y" as any]: `${welcomeEnd.y}px`
                } as React.CSSProperties)
              : undefined
          }
          aria-hidden="true"
        >
          <div className="welcomeFlyInner">
            <div className="welcomeFlyTitle">Welcome back</div>
            <div className="welcomeFlyName">{welcomeName}</div>
          </div>
        </div>
      ) : null}

      <main className="container">
        <div className="quoteBanner" aria-label="Cricket quote">
          <div className="quoteDot" />
          <div className="quoteText">{quote}</div>
        </div>

        {error ? <div className="alert alertErr">{error}</div> : null}

        <div className="dashGrid">
          <div className="dashLeft">
            <NeonCard title="Performance" subtitle={summary?.roleLine || "Complete your profile to unlock insights."}>
              <div className="dashTwoCol">
                <div className="perfBlock">
                  <div
                    className="perfRing"
                    style={{
                      background: `conic-gradient(from -90deg, #22C55E 0deg, #4ADE80 ${Math.max(
                        1,
                        Math.floor(perfDeg * 0.55)
                      )}deg, #16A34A ${perfDeg}deg, var(--ringTrack) ${perfDeg}deg 360deg)`
                    }}
                  >
                    <div className="perfRingInner">
                      <div className="perfNum">{perf}</div>
                      <div className="perfDen">/ 100</div>
                    </div>
                  </div>
                  <div className="perfLabel">Performance rating</div>
                </div>

                <div className="perfDetails">
                  <div ref={welcomeDockRef} className="welcomeDock">
                    <div className="welcomeDockHi">Welcome,</div>
                    <div className="welcomeDockName">{welcomeName}</div>
                  </div>

                  <div className="formBlock">
                    <div className="formHead">
                      <div className="formTitle">Current form</div>
                      <div className="formPct">{summary?.formPercent ?? 0}%</div>
                    </div>
                    <div className="formChips">
                      {(summary?.last5FormColors?.length ? summary.last5FormColors : ["GRAY", "GRAY", "GRAY", "GRAY", "GRAY"]).map(
                        (c, i) => (
                          <div key={i} className={`formChip ${chipColorClass(c)}`} title={c} />
                        )
                      )}
                    </div>
                    <div className="muted">Last 5 match colors will appear once match history is added.</div>
                  </div>
                </div>
              </div>
            </NeonCard>
          </div>

          <div className="dashMid">
            <NeonCard title="Player Trump Card" subtitle="Batting | Bowling | Fielding (auto-updates from match data)">
              <div className="trumpTabs">
                <button
                  type="button"
                  className={`trumpTab ${tab === "batting" ? "trumpTabActive" : ""}`}
                  onClick={() => setTab("batting")}
                >
                  Batting
                </button>
                <button
                  type="button"
                  className={`trumpTab ${tab === "bowling" ? "trumpTabActive" : ""}`}
                  onClick={() => setTab("bowling")}
                >
                  Bowling
                </button>
                <button
                  type="button"
                  className={`trumpTab ${tab === "fielding" ? "trumpTabActive" : ""}`}
                  onClick={() => setTab("fielding")}
                >
                  Fielding
                </button>
              </div>

              <div className="trumpCard">
                <div className="trumpHead">
                  <div>
                    <div className="trumpName">{alias}</div>
                    <div className="trumpMeta">{summary?.roleLine || "Player"}</div>
                  </div>
                  <div className="trumpBadge">
                    <div className="trumpJersey">#{summary?.jerseyNumber ?? "-"}</div>
                  </div>
                </div>

                <div className="trumpBody">
                  <div className="trumpGrid">
                    {rows.map((r) => (
                      <div key={r.k} className="trumpRow">
                        <span className="trumpKey">{r.k}</span>
                        <span className="trumpVal">{r.v}</span>
                      </div>
                    ))}
                  </div>
                  <div className="muted" style={{ marginTop: 10 }}>
                    This card will become powerful once we store match-by-match stats.
                  </div>
                </div>
              </div>
            </NeonCard>
          </div>

          <div className="dashRight">
            <NeonCard
              title={alias}
              subtitle={summary?.userName ? `@${summary.userName}` : ""}
              right={<TeamBadge teamName={summary?.teamName ?? null} teamLogoUrl={summary?.teamLogoUrl ?? null} />}
            >
              <div className="profileCard">
                <div className="avatarWrap">
                  <AvatarBack jersey={summary?.jerseyNumber ?? null} />
                </div>

                <div className="profileMeta">
                  <div className="metaLine">
                    <span className="metaKey">Jersey</span>
                    <span className="metaVal">{summary?.jerseyNumber ?? "-"}</span>
                  </div>
                  <div className="metaLine">
                    <span className="metaKey">Role</span>
                    <span className="metaVal">{summary?.roleLine ?? "-"}</span>
                  </div>
                  <div className="metaLine">
                    <span className="metaKey">Next</span>
                    <span className="metaVal">Create/Join team, then create a match</span>
                  </div>
                </div>
              </div>
            </NeonCard>
          </div>
        </div>
      </main>
    </div>
  );
}