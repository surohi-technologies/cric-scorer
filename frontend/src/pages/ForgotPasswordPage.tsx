import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import NeonCard from "../components/NeonCard";
import TopBar from "../components/TopBar";
import { apiFetch } from "../lib/api";

type StartRes = {
  message: string;
  availableChannels: string[];
  sentChannel: string | null;
};

type VerifyRes = {
  message: string;
  resetToken: string;
  expiresInSeconds: number;
};

function hasLower(s: string) {
  return /[a-z]/.test(s);
}
function hasUpper(s: string) {
  return /[A-Z]/.test(s);
}
function hasDigit(s: string) {
  return /[0-9]/.test(s);
}
function hasSpecial(s: string) {
  return /[!$#@*&]/.test(s);
}

export default function ForgotPasswordPage() {
  const [step, setStep] = useState<"start" | "otp" | "reset">("start");

  const [loginId, setLoginId] = useState("");
  const [availableChannels, setAvailableChannels] = useState<string[]>([]);
  const [channel, setChannel] = useState<string>("EMAIL");

  const [otp, setOtp] = useState("");
  const [resetToken, setResetToken] = useState<string | null>(null);

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [ok, setOk] = useState<string | null>(null);

  const helper = useMemo(() => {
    if (step === "start") return "Enter your username, mobile number, or email ID.";
    if (step === "otp") return "Enter the OTP sent to your registered email/phone.";
    return "Set a new password (must not match your last 3 passwords).";
  }, [step]);

  async function start(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setOk(null);
    setBusy(true);
    try {
      const res = await apiFetch<StartRes>("/auth/forgot-password", {
        method: "POST",
        body: JSON.stringify({ loginId })
      });
      setOk(res.message);
      setAvailableChannels(res.availableChannels || []);
      const ch = res.sentChannel || (res.availableChannels?.[0] ?? "EMAIL");
      setChannel(ch);
      setStep("otp");
    } catch (err: any) {
      setError(err?.message ?? "Request failed");
    } finally {
      setBusy(false);
    }
  }

  async function verifyOtp(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setOk(null);
    setBusy(true);
    try {
      const res = await apiFetch<VerifyRes>("/auth/forgot-password/verify-otp", {
        method: "POST",
        body: JSON.stringify({ loginId, channel, otp })
      });
      setOk(res.message);
      setResetToken(res.resetToken);
      setStep("reset");
    } catch (err: any) {
      setError(err?.message ?? "OTP verification failed");
    } finally {
      setBusy(false);
    }
  }

  async function reset(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setOk(null);

    if (!newPassword || newPassword.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }
    if (!hasLower(newPassword) || !hasUpper(newPassword) || !hasDigit(newPassword) || !hasSpecial(newPassword)) {
      setError("Password must include lowercase, uppercase, digit, and one special from !$#@*&");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Confirm password must match");
      return;
    }

    if (!resetToken) {
      setError("Missing reset token. Please restart the flow.");
      return;
    }

    setBusy(true);
    try {
      const res = await apiFetch<{ message: string }>("/auth/forgot-password/reset", {
        method: "POST",
        headers: { "X-Reset-Token": resetToken },
        body: JSON.stringify({ newPassword, confirmPassword })
      });
      setOk(res.message);
    } catch (err: any) {
      setError(err?.message ?? "Reset failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="page">
      <TopBar />
      <main className="container">
        <NeonCard title="Forgot Password" subtitle={helper}>
          {step === "start" ? (
            <form className="form" onSubmit={start}>
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

              {ok ? <div className="alert alertOk">{ok}</div> : null}
              {error ? <div className="alert alertErr">{error}</div> : null}

              <button className="btn btnPrimary" disabled={busy}>
                {busy ? "Sending..." : "Send OTP"}
              </button>

              <div className="muted">
                Back to <Link to="/login">Login</Link>
              </div>
            </form>
          ) : null}

          {step === "otp" ? (
            <form className="form" onSubmit={verifyOtp}>
              <div className="grid2">
                <label className="field">
                  <span className="label">Channel</span>
                  <select className="input" value={channel} onChange={(e) => setChannel(e.target.value)}>
                    {(availableChannels.length ? availableChannels : ["EMAIL", "PHONE"]).map((ch) => (
                      <option key={ch} value={ch}>
                        {ch}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="field">
                  <span className="label">OTP</span>
                  <input
                    className="input"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    placeholder="6-digit OTP"
                    inputMode="numeric"
                  />
                </label>
              </div>

              {ok ? <div className="alert alertOk">{ok}</div> : null}
              {error ? <div className="alert alertErr">{error}</div> : null}

              <button className="btn btnPrimary" disabled={busy}>
                {busy ? "Verifying..." : "Verify OTP"}
              </button>

              <div className="muted">
                <button type="button" className="btn btnGhost" onClick={() => setStep("start")}>
                  Start over
                </button>
              </div>
            </form>
          ) : null}

          {step === "reset" ? (
            <form className="form" onSubmit={reset}>
              <label className="field">
                <span className="label">New password</span>
                <input
                  className="input"
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Min 8 chars, Aa1!"
                  autoComplete="new-password"
                />
              </label>
              <label className="field">
                <span className="label">Confirm password</span>
                <input
                  className="input"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Repeat password"
                  autoComplete="new-password"
                />
              </label>

              {ok ? <div className="alert alertOk">{ok}</div> : null}
              {error ? <div className="alert alertErr">{error}</div> : null}

              <button className="btn btnPrimary" disabled={busy}>
                {busy ? "Updating..." : "Update password"}
              </button>

              <div className="muted">
                Back to <Link to="/login">Login</Link>
              </div>
            </form>
          ) : null}
        </NeonCard>
      </main>
    </div>
  );
}