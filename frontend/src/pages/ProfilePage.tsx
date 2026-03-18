import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import NeonCard from "../components/NeonCard";
import ProgressBar from "../components/ProgressBar";
import TopBar from "../components/TopBar";
import { apiFetch } from "../lib/api";
import { clearDraft, loadDraft, saveDraft } from "../lib/draft";
import { getSession, setSession } from "../lib/session";
import type { MetaOption, PlayerProfileDraft } from "../lib/types";

const REMEMBER_KEY = "cric:rememberLogin";

type FieldErrors = Record<string, string>;

type MetaBundle = {
  arms: MetaOption[];
  battingStyles: MetaOption[];
  battingPositions: MetaOption[];
  bowlingStyles: MetaOption[];
  bowlingPrefs: MetaOption[];
  bowlingRoles: MetaOption[];
  playerRoles: MetaOption[];
  battingIntents: MetaOption[];
};

const emptyDraft: PlayerProfileDraft = {
  aliasName: "",
  jerseyNumber: "",
  battingHandId: null,
  bowlingHandId: null,
  battingStyleId: null,
  battingPositionId: null,
  bowlingStyleId: null,
  bowlingTacticalRoleId: null,
  bowlingPreferenceId: null,
  playerRoleTypeId: null,
  battingIntentId: null,
  favouritePlayer: "",
  favouriteTeam: "",
  description: ""
};

function getLabel(list: MetaOption[] | undefined | null, id: number | null) {
  if (!list || id == null) return null;
  return list.find((x) => x.id === id)?.label ?? null;
}

function isRememberEnabled() {
  try {
    return !!localStorage.getItem(REMEMBER_KEY);
  } catch {
    return false;
  }
}

function completionPercent(d: PlayerProfileDraft) {
  const required: Array<boolean> = [
    d.aliasName.trim().length > 0,
    Number(d.jerseyNumber) > 0,
    d.playerRoleTypeId != null,

    d.battingHandId != null,
    d.battingPositionId != null,
    d.battingIntentId != null,

    d.bowlingHandId != null,
    d.bowlingPreferenceId != null,
    d.bowlingTacticalRoleId != null,

    d.favouritePlayer.trim().length > 0,
    d.favouriteTeam.trim().length > 0,

    d.battingStyleId != null,
    d.bowlingStyleId != null
  ];
  const done = required.filter(Boolean).length;
  return (done / required.length) * 100;
}

type BatPosGroup = "" | "OPENER" | "TOP" | "MIDDLE" | "LOWER" | "TAIL";

type RoleOrder = "BATTING_FIRST" | "BOWLING_FIRST";

type DescMode = "AUTO" | "MANUAL";

function batGroupFromId(id: number | null): BatPosGroup {
  if (!id) return "";
  if (id === 1 || id === 2) return "OPENER";
  if (id === 3 || id === 4) return "TOP";
  if (id === 5 || id === 6 || id === 7) return "MIDDLE";
  if (id === 8) return "LOWER";
  if (id === 9 || id === 10 || id === 11) return "TAIL";
  return "";
}

function batOptionsForGroup(g: BatPosGroup): Array<{ id: number; label: string }> {
  if (g === "OPENER") {
    return [
      { id: 1, label: "First Ball Face (Striker)" },
      { id: 2, label: "Support Opener (Non-striker)" }
    ];
  }
  if (g === "TOP") {
    return [
      { id: 3, label: "One Down (Stabilizer)" },
      { id: 4, label: "Two Down (Stroke Builder)" }
    ];
  }
  if (g === "MIDDLE") {
    return [
      { id: 5, label: "Anchor (Innings Builder)" },
      { id: 6, label: "Finisher (Game Changer)" },
      { id: 7, label: "Utility Batter (All-Round Role)" }
    ];
  }
  if (g === "LOWER") {
    return [{ id: 8, label: "Support Finisher (Lower Order Hitter)" }];
  }
  if (g === "TAIL") {
    return [
      { id: 9, label: "Bowler Batter" },
      { id: 10, label: "Night Watch (Defender)" },
      { id: 11, label: "Last Man Standing" }
    ];
  }
  return [];
}

function batLabelById(id: number | null): string | null {
  if (!id) return null;
  const g = batGroupFromId(id);
  return batOptionsForGroup(g).find((x) => x.id === id)?.label ?? null;
}

function roleOrderFromLabel(label: string | null): RoleOrder {
  const v = (label || "").toLowerCase();
  if (v.includes("bowler") || v.includes("bowling all")) return "BOWLING_FIRST";
  return "BATTING_FIRST";
}

export default function ProfilePage() {
  const nav = useNavigate();
  const session = getSession();

  const [meta, setMeta] = useState<MetaBundle | null>(null);
  const [draft, setDraft] = useState<PlayerProfileDraft>(() => {
    if (!session) return emptyDraft;
    return loadDraft(session.userId) ?? emptyDraft;
  });

  const [batGroup, setBatGroup] = useState<BatPosGroup>(() => batGroupFromId(draft.battingPositionId));
  const [descMode, setDescMode] = useState<DescMode>("AUTO");

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [savedMsg, setSavedMsg] = useState<string | null>(null);

  const saveTimer = useRef<number | null>(null);
  useEffect(() => {
    if (!session) return;
    if (saveTimer.current) window.clearTimeout(saveTimer.current);
    saveTimer.current = window.setTimeout(() => {
      saveDraft(session.userId, draft);
      setSavedMsg("Draft saved");
      window.setTimeout(() => setSavedMsg(null), 900);
    }, 250);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draft]);

  useEffect(() => {
    (async () => {
      try {
        const [
          arms,
          battingStyles,
          battingPositions,
          bowlingStyles,
          bowlingPrefs,
          bowlingRoles,
          playerRoles,
          battingIntents
        ] = await Promise.all([
          apiFetch<MetaOption[]>("/meta/arms", { method: "GET" }),
          apiFetch<MetaOption[]>("/meta/batting-styles", { method: "GET" }),
          apiFetch<MetaOption[]>("/meta/batting-positions", { method: "GET" }),
          apiFetch<MetaOption[]>("/meta/bowling-styles", { method: "GET" }),
          apiFetch<MetaOption[]>("/meta/bowling-preferences", { method: "GET" }),
          apiFetch<MetaOption[]>("/meta/bowling-tactical-roles", { method: "GET" }),
          apiFetch<MetaOption[]>("/meta/player-role-types", { method: "GET" }),
          apiFetch<MetaOption[]>("/meta/batting-intents", { method: "GET" })
        ]);
        setMeta({
          arms,
          battingStyles,
          battingPositions,
          bowlingStyles,
          bowlingPrefs,
          bowlingRoles,
          playerRoles,
          battingIntents
        });
      } catch (e: any) {
        setError(e?.message ?? "Failed to load profile options");
      }
    })();
  }, []);

  // Remember-me smart defaults: prefill alias name from username.
  useEffect(() => {
    if (!session) return;
    if (!isRememberEnabled()) return;
    if (draft.aliasName.trim().length > 0) return;
    if (!session.userName) return;
    setDraft((d) => ({ ...d, aliasName: session.userName ?? d.aliasName }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session?.userName]);

  // Keep group in sync if user edits/loads draft.
  useEffect(() => {
    const g = batGroupFromId(draft.battingPositionId);
    if (g && g !== batGroup) setBatGroup(g);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draft.battingPositionId]);

  // Auto-fill batting style from batting hand
  useEffect(() => {
    if (!meta) return;
    if (!draft.battingHandId) return;
    if (draft.battingStyleId) return;

    const hand = getLabel(meta.arms, draft.battingHandId)?.toLowerCase() ?? "";
    const target = hand.includes("left") ? "left-hand" : "right-hand";
    const match = meta.battingStyles.find((x) => x.label.toLowerCase().includes(target));
    setDraft((d) => ({ ...d, battingStyleId: match?.id ?? meta.battingStyles[0]?.id ?? null }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [meta, draft.battingHandId]);

  // Auto-fill bowling style from preference
  useEffect(() => {
    if (!meta) return;
    if (!draft.bowlingPreferenceId) return;
    if (draft.bowlingStyleId) return;

    const pref = getLabel(meta.bowlingPrefs, draft.bowlingPreferenceId)?.toLowerCase() ?? "";

    const pick = () => {
      if (pref.includes("spin")) {
        return (
          meta.bowlingStyles.find((x) => x.label.toLowerCase().includes("finger")) ??
          meta.bowlingStyles.find((x) => x.label.toLowerCase().includes("spinner"))
        );
      }
      if (pref.includes("pace") || pref.includes("seam") || pref.includes("swing")) {
        return meta.bowlingStyles.find((x) => x.label.toLowerCase() === "fast") ?? meta.bowlingStyles[0];
      }
      return meta.bowlingStyles[0];
    };

    const chosen = pick();
    setDraft((d) => ({ ...d, bowlingStyleId: chosen?.id ?? null }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [meta, draft.bowlingPreferenceId]);

  const autoDescription = useMemo(() => {
    if (!meta) return "";
    const alias = draft.aliasName.trim() || (session?.userName ? session.userName : "Player");
    const jerseyNum = Number(draft.jerseyNumber);
    const role = getLabel(meta.playerRoles, draft.playerRoleTypeId);

    const batHand = getLabel(meta.arms, draft.battingHandId);
    const batPos = batLabelById(draft.battingPositionId);
    const batIntent = getLabel(meta.battingIntents, draft.battingIntentId);

    const bowlHand = getLabel(meta.arms, draft.bowlingHandId);
    const bowlPref = getLabel(meta.bowlingPrefs, draft.bowlingPreferenceId);
    const bowlRole = getLabel(meta.bowlingRoles, draft.bowlingTacticalRoleId);

    const favP = draft.favouritePlayer.trim();
    const favT = draft.favouriteTeam.trim();

    const sentences: string[] = [];

    let s1 = alias;
    if (Number.isFinite(jerseyNum) && jerseyNum > 0) s1 += ` wears jersey #${jerseyNum}`;
    if (role) s1 += ` and plays as a ${role}`;
    s1 += ".";
    sentences.push(s1);

    if (batHand || batPos || batIntent) {
      const a = batHand ? `${batHand}-hand` : "skilled";
      const p = batPos ? `as ${batPos}` : "across the order";
      const i = batIntent ? `with a ${batIntent} mindset` : "with smart intent";
      sentences.push(`With the bat, a ${a} batter who operates ${p}, ${i}.`);
    }

    if (bowlHand || bowlPref || bowlRole) {
      const h = bowlHand ? `${bowlHand}-arm` : "versatile";
      const pref = bowlPref ? `${bowlPref}` : "mixed skills";
      const r = bowlRole ? `often used as a ${bowlRole}` : "trusted in key phases";
      sentences.push(`With the ball, ${h} with ${pref} preference, ${r}.`);
    }

    if (favP || favT) {
      const fp = favP || "cricket greats";
      const ft = favT || "their team";
      sentences.push(`Inspired by ${fp} and backing ${ft}.`);
    }

    return sentences.join(" ");
  }, [
    meta,
    session?.userName,
    draft.aliasName,
    draft.jerseyNumber,
    draft.playerRoleTypeId,
    draft.battingHandId,
    draft.battingPositionId,
    draft.battingIntentId,
    draft.bowlingHandId,
    draft.bowlingPreferenceId,
    draft.bowlingTacticalRoleId,
    draft.favouritePlayer,
    draft.favouriteTeam
  ]);

  useEffect(() => {
    if (descMode !== "AUTO") return;
    if (!autoDescription) return;
    if (draft.description === autoDescription) return;
    setDraft((d) => ({ ...d, description: autoDescription }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [descMode, autoDescription]);

  const percent = useMemo(() => completionPercent(draft), [draft]);

  const roleLabel = useMemo(() => (meta ? getLabel(meta.playerRoles, draft.playerRoleTypeId) : null), [meta, draft.playerRoleTypeId]);
  const order = useMemo(() => roleOrderFromLabel(roleLabel), [roleLabel]);
  const showAfterRole = !!draft.playerRoleTypeId;

  function err(field: string) {
    return fieldErrors[field] ? <div className="fieldErr">{fieldErrors[field]}</div> : <div className="fieldErr fieldErrPlaceholder">.</div>;
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!session) return;

    setBusy(true);
    setError(null);
    setFieldErrors({});

    try {
      if (!draft.battingStyleId || !draft.bowlingStyleId) {
        setError("Please pick your hand and preferences first so we can auto-fill your style.");
        return;
      }

      await apiFetch<{ message: string }>("/profile/creation", {
        method: "POST",
        auth: true,
        body: JSON.stringify({
          aliasName: draft.aliasName.trim(),
          jerseyNumber: Number(draft.jerseyNumber),
          battingHandId: draft.battingHandId,
          bowlingHandId: draft.bowlingHandId,
          battingStyleId: draft.battingStyleId,
          battingPositionId: draft.battingPositionId,
          bowlingStyleId: draft.bowlingStyleId,
          bowlingTacticalRoleId: draft.bowlingTacticalRoleId,
          bowlingPreferenceId: draft.bowlingPreferenceId,
          playerRoleTypeId: draft.playerRoleTypeId,
          battingIntentId: draft.battingIntentId,
          favouritePlayer: draft.favouritePlayer.trim(),
          favouriteTeam: draft.favouriteTeam.trim(),
          autoDescription: descMode === "AUTO",
          description: draft.description.trim() || null
        })
      });

      clearDraft(session.userId);
      setSession({ ...session, profileCompleted: true, firstTimeLogin: false, nextAction: "NONE" });
      sessionStorage.setItem("cric:welcome", "1");
      nav("/dashboard", { replace: true });
    } catch (err: any) {
      if (err?.status === 409) {
        clearDraft(session.userId);
        setSession({ ...session, profileCompleted: true, firstTimeLogin: false, nextAction: "NONE" });
        sessionStorage.setItem("cric:welcome", "1");
        nav("/dashboard", { replace: true });
        return;
      }

      const details = err?.details;
      if (details?.errors && Array.isArray(details.errors)) {
        const apiErrors: FieldErrors = {};
        for (const e of details.errors) {
          if (e?.field && e?.message && !apiErrors[e.field]) apiErrors[e.field] = e.message;
        }
        setFieldErrors(apiErrors);
      }
      setError(err?.message ?? "Profile save failed");
    } finally {
      setBusy(false);
    }
  }

  if (!session) {
    return (
      <div className="page">
        <TopBar />
        <main className="container">
          <div className="alert alertErr">Session missing. Please login again.</div>
        </main>
      </div>
    );
  }

  const battingSection = (
    <div className="stack" style={{ gap: 12 }}>
      <div className="muted">Batting</div>

      <label className="field">
        <span className="label">Batting hand</span>
        <select
          className="input"
          value={draft.battingHandId ?? ""}
          onChange={(e) => setDraft({ ...draft, battingHandId: e.target.value ? Number(e.target.value) : null, battingStyleId: null })}
        >
          <option value="">Select</option>
          {meta?.arms.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
        {err("battingHandId")}
      </label>

      <label className="field">
        <span className="label">Batting position</span>
        <div className="grid2">
          <div className="field">
            <span className="label">Role group</span>
            <select
              className="input"
              value={batGroup}
              onChange={(e) => {
                const g = (e.target.value || "") as BatPosGroup;
                setBatGroup(g);
                setDraft({ ...draft, battingPositionId: null });
              }}
            >
              <option value="">Select</option>
              <option value="OPENER">Opener</option>
              <option value="TOP">Top Order (No. 3 & 4)</option>
              <option value="MIDDLE">Middle Order (No. 5-7)</option>
              <option value="LOWER">Lower Order (No. 8)</option>
              <option value="TAIL">Tailenders (No. 9-11)</option>
            </select>
          </div>

          <div className="field">
            <span className="label">Position</span>
            <select
              className="input"
              value={draft.battingPositionId ?? ""}
              onChange={(e) => setDraft({ ...draft, battingPositionId: e.target.value ? Number(e.target.value) : null })}
              disabled={!batGroup}
            >
              <option value="">Select</option>
              {batOptionsForGroup(batGroup).map((o) => (
                <option key={o.id} value={o.id}>
                  {o.label}
                </option>
              ))}
            </select>
          </div>
        </div>
        {err("battingPositionId")}
      </label>

      <label className="field">
        <span className="label">Batting intent</span>
        <select
          className="input"
          value={draft.battingIntentId ?? ""}
          onChange={(e) => setDraft({ ...draft, battingIntentId: e.target.value ? Number(e.target.value) : null })}
        >
          <option value="">Select</option>
          {meta?.battingIntents.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
        {err("battingIntentId")}
      </label>
    </div>
  );

  const bowlingSection = (
    <div className="stack" style={{ gap: 12 }}>
      <div className="muted">Bowling</div>

      <label className="field">
        <span className="label">Bowling hand</span>
        <select
          className="input"
          value={draft.bowlingHandId ?? ""}
          onChange={(e) => setDraft({ ...draft, bowlingHandId: e.target.value ? Number(e.target.value) : null })}
        >
          <option value="">Select</option>
          {meta?.arms.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
        {err("bowlingHandId")}
      </label>

      <label className="field">
        <span className="label">Bowling preferences</span>
        <select
          className="input"
          value={draft.bowlingPreferenceId ?? ""}
          onChange={(e) =>
            setDraft({
              ...draft,
              bowlingPreferenceId: e.target.value ? Number(e.target.value) : null,
              bowlingStyleId: null
            })
          }
        >
          <option value="">Select</option>
          {meta?.bowlingPrefs.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
        {err("bowlingPreferenceId")}
      </label>

      <label className="field">
        <span className="label">Bowling tactical role</span>
        <select
          className="input"
          value={draft.bowlingTacticalRoleId ?? ""}
          onChange={(e) => setDraft({ ...draft, bowlingTacticalRoleId: e.target.value ? Number(e.target.value) : null })}
        >
          <option value="">Select</option>
          {meta?.bowlingRoles.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
        {err("bowlingTacticalRoleId")}
      </label>

      <div className="muted">Bowling style is auto-filled from your preference to keep this quick.</div>
    </div>
  );

  return (
    <div className="page">
      <TopBar />
      <main className="container">
        <div className="stack">
          <ProgressBar percent={percent} />
          {savedMsg ? <div className="toast">{savedMsg}</div> : null}
        </div>

        <NeonCard title="Player Profile" subtitle="Choose your role first, then complete the details.">
          <form className="form" onSubmit={onSubmit}>
            {error ? <div className="alert alertErr">{error}</div> : null}

            <div className="grid2">
              <label className="field">
                <span className="label">Alias name</span>
                <input
                  className="input"
                  value={draft.aliasName}
                  onChange={(e) => setDraft({ ...draft, aliasName: e.target.value })}
                  placeholder="Your cricket name"
                />
                {err("aliasName")}
              </label>

              <label className="field">
                <span className="label">Jersey number</span>
                <input
                  className="input"
                  value={draft.jerseyNumber}
                  onChange={(e) => setDraft({ ...draft, jerseyNumber: e.target.value.replace(/\D/g, "") })}
                  placeholder="11"
                  inputMode="numeric"
                />
                {err("jerseyNumber")}
              </label>
            </div>

            <label className="field">
              <span className="label">Player role type</span>
              <select
                className="input"
                value={draft.playerRoleTypeId ?? ""}
                onChange={(e) => setDraft({ ...draft, playerRoleTypeId: e.target.value ? Number(e.target.value) : null })}
              >
                <option value="">Select</option>
                {meta?.playerRoles.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.label}
                  </option>
                ))}
              </select>
              {err("playerRoleTypeId")}
            </label>

            {showAfterRole ? (
              <>
                {order === "BATTING_FIRST" ? (
                  <>
                    {battingSection}
                    {bowlingSection}
                  </>
                ) : (
                  <>
                    {bowlingSection}
                    {battingSection}
                  </>
                )}

                <div className="grid2">
                  <label className="field">
                    <span className="label">Favourite player</span>
                    <input
                      className="input"
                      value={draft.favouritePlayer}
                      onChange={(e) => setDraft({ ...draft, favouritePlayer: e.target.value })}
                      placeholder="Enter your Fav Player name"
                    />
                    {err("favouritePlayer")}
                  </label>
                  <label className="field">
                    <span className="label">Favourite team</span>
                    <input
                      className="input"
                      value={draft.favouriteTeam}
                      onChange={(e) => setDraft({ ...draft, favouriteTeam: e.target.value })}
                      placeholder="Enter your fvc team"
                    />
                    {err("favouriteTeam")}
                  </label>
                </div>

                <label className="field">
                  <span className="label">About you (optional)</span>
                  <div className="rowSplit" style={{ alignItems: "center" }}>
                    <div className="muted">
                      {descMode === "AUTO"
                        ? "Auto-generated from your selections. Click Edit to customize."
                        : "Manual description. Click Auto to regenerate from your selections."}
                    </div>
                    <button
                      type="button"
                      className="btn btnGhost"
                      onClick={() => setDescMode(descMode === "AUTO" ? "MANUAL" : "AUTO")}
                    >
                      {descMode === "AUTO" ? "Edit" : "Auto"}
                    </button>
                  </div>
                  <textarea
                    className="input textarea"
                    value={draft.description}
                    readOnly={descMode === "AUTO"}
                    onChange={(e) => {
                      setDescMode("MANUAL");
                      setDraft({ ...draft, description: e.target.value });
                    }}
                    placeholder="Write a short description about your playing style"
                  />
                </label>
              </>
            ) : (
              <div className="alert" style={{ borderColor: "rgba(255,255,255,0.12)", background: "rgba(255,255,255,0.04)" }}>
                Select <b>Player role type</b> to continue.
              </div>
            )}

            <button className="btn btnPrimary" disabled={busy}>
              {busy ? "Saving..." : "Save profile"}
            </button>

            <div className="muted">Tip: Your draft is saved and will resume when you login again.</div>
          </form>
        </NeonCard>
      </main>
    </div>
  );
}