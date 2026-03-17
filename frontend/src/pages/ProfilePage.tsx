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

function completionPercent(d: PlayerProfileDraft) {
  // We keep the profile simple: 10 required items = 10% each.
  const required: Array<boolean> = [
    d.aliasName.trim().length > 0,
    Number(d.jerseyNumber) > 0,
    d.battingHandId != null,
    d.bowlingHandId != null,
    d.battingPositionId != null,
    d.bowlingTacticalRoleId != null,
    d.bowlingPreferenceId != null,
    d.playerRoleTypeId != null,
    d.battingStyleId != null, // auto-filled from batting hand
    d.bowlingStyleId != null // auto-filled from preference
  ];
  const done = required.filter(Boolean).length;
  return (done / required.length) * 100;
}

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

export default function ProfilePage() {
  const nav = useNavigate();
  const session = getSession();

  const [meta, setMeta] = useState<MetaBundle | null>(null);
  const [draft, setDraft] = useState<PlayerProfileDraft>(() => {
    if (!session) return emptyDraft;
    return loadDraft(session.userId) ?? emptyDraft;
  });
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

  // Auto-fill batting style from batting hand (Right -> Right-hand bat, Left -> Left-hand bat)
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

  // Auto-fill bowling style from preference (simple mapping).
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

  // Remember-me smart description: generate a short bio based on selections, only if description is empty.
  useEffect(() => {
    if (!meta) return;
    if (!session) return;
    if (!isRememberEnabled()) return;
    if (draft.description.trim().length > 0) return;

    const role = getLabel(meta.playerRoles, draft.playerRoleTypeId);
    const batPos = getLabel(meta.battingPositions, draft.battingPositionId);
    const bowlRole = getLabel(meta.bowlingRoles, draft.bowlingTacticalRoleId);

    if (!role && !batPos && !bowlRole) return;

    const parts = [role ? role : null, batPos ? `Bat: ${batPos}` : null, bowlRole ? `Bowl: ${bowlRole}` : null].filter(
      Boolean
    ) as string[];

    setDraft((d) => ({ ...d, description: parts.join(" | ") }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [meta, draft.playerRoleTypeId, draft.battingPositionId, draft.bowlingTacticalRoleId]);

  const percent = useMemo(() => completionPercent(draft), [draft]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!session) return;

    setBusy(true);
    setError(null);
    setFieldErrors({});

    try {
      // Final safety: ensure the hidden auto-fields are present.
      if (!draft.battingStyleId || !draft.bowlingStyleId) {
        setError("Please pick your hand and preferences first so we can auto-fill your style.");
        return;
      }

      await apiFetch<{ message: string }>("/profile/creation", {
        method: "POST",
        auth: true,
        body: JSON.stringify({
          aliasName: draft.aliasName,
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
          favouritePlayer: draft.favouritePlayer || null,
          favouriteTeam: draft.favouriteTeam || null,
          description: draft.description || null
        })
      });

      clearDraft(session.userId);
      setSession({ ...session, profileCompleted: true, firstTimeLogin: false, nextAction: "NONE" });
      nav("/dashboard", { replace: true });
    } catch (err: any) {
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

  return (
    <div className="page">
      <TopBar />
      <main className="container">
        <div className="stack">
          <ProgressBar percent={percent} />
          {savedMsg ? <div className="toast">{savedMsg}</div> : null}
        </div>

        <NeonCard title="Player Profile" subtitle="Complete this once to unlock your dashboard.">
          <form className="form" onSubmit={onSubmit}>
            {error ? <div className="alert alertErr">{error}</div> : null}

            <div className="grid2">
              <label className="field">
                <span className="label">Alias name</span>
                <input
                  className="input"
                  value={draft.aliasName}
                  onChange={(e) => setDraft({ ...draft, aliasName: e.target.value })}
                  placeholder="Your name on the scoreboard"
                />
                {fieldErrors.aliasName ? <div className="fieldErr">{fieldErrors.aliasName}</div> : null}
              </label>
              <label className="field">
                <span className="label">Jersey number</span>
                <input
                  className="input"
                  value={draft.jerseyNumber}
                  onChange={(e) => setDraft({ ...draft, jerseyNumber: e.target.value })}
                  placeholder="e.g. 7"
                  inputMode="numeric"
                />
                {fieldErrors.jerseyNumber ? <div className="fieldErr">{fieldErrors.jerseyNumber}</div> : null}
              </label>
            </div>

            <div className="grid2">
              <label className="field">
                <span className="label">Batting hand</span>
                <select
                  className="input"
                  value={draft.battingHandId ?? ""}
                  onChange={(e) =>
                    setDraft({
                      ...draft,
                      battingHandId: e.target.value ? Number(e.target.value) : null,
                      battingStyleId: null // re-pick automatically
                    })
                  }
                >
                  <option value="">Select</option>
                  {meta?.arms.map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.label}
                    </option>
                  ))}
                </select>
                {fieldErrors.battingHandId ? <div className="fieldErr">{fieldErrors.battingHandId}</div> : null}
              </label>
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
                {fieldErrors.bowlingHandId ? <div className="fieldErr">{fieldErrors.bowlingHandId}</div> : null}
              </label>
            </div>

            <div className="grid2">
              <label className="field">
                <span className="label">Batting position</span>
                <select
                  className="input"
                  value={draft.battingPositionId ?? ""}
                  onChange={(e) =>
                    setDraft({ ...draft, battingPositionId: e.target.value ? Number(e.target.value) : null })
                  }
                >
                  <option value="">Select</option>
                  {meta?.battingPositions.map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.label}
                    </option>
                  ))}
                </select>
                {fieldErrors.battingPositionId ? <div className="fieldErr">{fieldErrors.battingPositionId}</div> : null}
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
                      bowlingStyleId: null // re-pick automatically
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
                {fieldErrors.bowlingPreferenceId ? <div className="fieldErr">{fieldErrors.bowlingPreferenceId}</div> : null}
              </label>
            </div>

            <div className="grid2">
              <label className="field">
                <span className="label">Bowling tactical role</span>
                <select
                  className="input"
                  value={draft.bowlingTacticalRoleId ?? ""}
                  onChange={(e) =>
                    setDraft({ ...draft, bowlingTacticalRoleId: e.target.value ? Number(e.target.value) : null })
                  }
                >
                  <option value="">Select</option>
                  {meta?.bowlingRoles.map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.label}
                    </option>
                  ))}
                </select>
                {fieldErrors.bowlingTacticalRoleId ? <div className="fieldErr">{fieldErrors.bowlingTacticalRoleId}</div> : null}
              </label>
              <label className="field">
                <span className="label">Player role type</span>
                <select
                  className="input"
                  value={draft.playerRoleTypeId ?? ""}
                  onChange={(e) =>
                    setDraft({ ...draft, playerRoleTypeId: e.target.value ? Number(e.target.value) : null })
                  }
                >
                  <option value="">Select</option>
                  {meta?.playerRoles.map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.label}
                    </option>
                  ))}
                </select>
                {fieldErrors.playerRoleTypeId ? <div className="fieldErr">{fieldErrors.playerRoleTypeId}</div> : null}
              </label>
            </div>

            <label className="field">
              <span className="label">Batting intent (optional)</span>
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
            </label>

            <div className="grid2">
              <label className="field">
                <span className="label">Favourite player (optional)</span>
                <input
                  className="input"
                  value={draft.favouritePlayer}
                  onChange={(e) => setDraft({ ...draft, favouritePlayer: e.target.value })}
                  placeholder="Your inspiration"
                />
              </label>
              <label className="field">
                <span className="label">Favourite team (optional)</span>
                <input
                  className="input"
                  value={draft.favouriteTeam}
                  onChange={(e) => setDraft({ ...draft, favouriteTeam: e.target.value })}
                  placeholder="e.g. CSK, MI, India"
                />
              </label>
            </div>

            <label className="field">
              <span className="label">About you (optional)</span>
              <textarea
                className="input textarea"
                value={draft.description}
                onChange={(e) => setDraft({ ...draft, description: e.target.value })}
                placeholder="A short bio for your profile"
              />
            </label>

            <div className="muted">
              Batting style and bowling style are auto-filled from your hand and preference to keep things simple.
            </div>

            <button className="btn btnPrimary" disabled={busy}>
              {busy ? "Saving..." : "Save profile"}
            </button>

            <div className="muted">
              Tip: You can leave anytime. Your draft is saved and will resume when you login again.
            </div>
          </form>
        </NeonCard>
      </main>
    </div>
  );
}