import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import NeonCard from "../components/NeonCard";
import TopBar from "../components/TopBar";
import { apiFetch } from "../lib/api";

type DialCodeRow = {
  id: number;
  label: string; // ex: "+91 India"
  description?: string; // iso2
};

type RegisterResponse = {
  message: string;
  userId: number;
  userName: string;
  uniqueIdentifier: string;
  verificationRequired: boolean;
  requiredChannels: string[];
};

type RegistrationOtpResponse = {
  message: string;
  verified: boolean;
  active: boolean;
  requiredChannels: string[];
  verifiedChannels: string[];
};

type FieldErrors = Record<string, string>;

type ContactMode = "MOBILE" | "EMAIL" | "BOTH";

function pad2(n: number) {
  return n < 10 ? `0${n}` : String(n);
}

function dialFromLabel(label: string) {
  const s = (label || "").trim();
  if (!s) return "";
  return s.split(/\s+/)[0] || "";
}

function countryFromLabel(label: string) {
  const s = (label || "").trim();
  if (!s) return "";
  const parts = s.split(/\s+/);
  parts.shift();
  return parts.join(" ").trim();
}

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

export default function RegisterPage() {
  const nav = useNavigate();
  const [dialCodes, setDialCodes] = useState<DialCodeRow[]>([]);
  const [dialLoadFailed, setDialLoadFailed] = useState(false);

  const [step, setStep] = useState<"form" | "otp">("form");

  const [contactMode, setContactMode] = useState<ContactMode>("BOTH");

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [gender, setGender] = useState("");
  const [emailId, setEmailId] = useState("");
  const [phoneCountryCode, setPhoneCountryCode] = useState("+91");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const nowYear = new Date().getFullYear();
  const [dobYear, setDobYear] = useState<number>(nowYear - 18);
  const [dobMonth, setDobMonth] = useState<number>(1);
  const [dobDay, setDobDay] = useState<number>(1);

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  const [created, setCreated] = useState<RegisterResponse | null>(null);
  const [otpByChannel, setOtpByChannel] = useState<Record<string, string>>({});
  const [otpState, setOtpState] = useState<RegistrationOtpResponse | null>(null);

  const helper = useMemo(() => {
    return step === "form"
      ? "Register with mobile, email, or both."
      : "Enter the OTP to activate your account.";
  }, [step]);

  const dob = useMemo(() => {
    return `${dobYear}-${pad2(dobMonth)}-${pad2(dobDay)}`;
  }, [dobYear, dobMonth, dobDay]);

  const years = useMemo(() => {
    const min = 1950;
    const max = nowYear;
    const arr: number[] = [];
    for (let y = max; y >= min; y--) arr.push(y);
    return arr;
  }, [nowYear]);

  const days = useMemo(() => {
    const daysInMonth = new Date(dobYear, dobMonth, 0).getDate();
    const arr: number[] = [];
    for (let d = 1; d <= daysInMonth; d++) arr.push(d);
    return arr;
  }, [dobYear, dobMonth]);

  useEffect(() => {
    const maxDay = new Date(dobYear, dobMonth, 0).getDate();
    if (dobDay > maxDay) setDobDay(maxDay);
  }, [dobYear, dobMonth, dobDay]);

  useEffect(() => {
    (async () => {
      try {
        const rows = await apiFetch<DialCodeRow[]>("/meta/dial-codes", { method: "GET" });
        setDialCodes(rows);
        setDialLoadFailed(false);

        if (rows.length > 0) {
          const codes = rows.map((r) => dialFromLabel(r.label)).filter(Boolean);
          if (phoneCountryCode && codes.includes(phoneCountryCode)) return;
          if (codes[0]) setPhoneCountryCode(codes[0]);
        }
      } catch {
        setDialCodes([]);
        setDialLoadFailed(true);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const dialOptions = useMemo(() => {
    const opts = dialCodes
      .map((d) => {
        const dial = dialFromLabel(d.label);
        const country = countryFromLabel(d.label);
        const label = dial && country ? `${dial} (${country})` : d.label;
        return { id: d.id, dial, label };
      })
      .filter((o) => o.dial);

    // If API failed, show only a safe fallback so form still works.
    if (opts.length === 0) {
      return [{ id: -91, dial: "+91", label: "+91 (India)" }];
    }

    // De-dup by dial (DB might have +1 twice for US/CA)
    const seen = new Set<string>();
    const uniq: Array<{ id: number; dial: string; label: string }> = [];
    for (const o of opts) {
      if (seen.has(o.dial)) continue;
      seen.add(o.dial);
      uniq.push(o);
    }
    return uniq;
  }, [dialCodes]);

  const pwdRules = useMemo(() => {
    return [
      { k: "len", ok: password.length >= 8, t: "Password should be 8 char" },
      {
        k: "case",
        ok: hasLower(password) && hasUpper(password),
        t: "Must have Cpital Letters and Small Letters at leat 1"
      },
      { k: "dig", ok: hasDigit(password), t: "Must have atleast 1 digit" },
      { k: "spec", ok: hasSpecial(password), t: "Must have atleast 1 special char (!,$,#,@,&,*)" }
    ];
  }, [password]);

  const allPwdOk = useMemo(() => pwdRules.every((r) => r.ok), [pwdRules]);

  function setErr(field: string, msg: string, next: FieldErrors) {
    next[field] = msg;
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setCreated(null);
    setOtpState(null);

    const nextErrors: FieldErrors = {};

    if (!firstName.trim()) setErr("firstName", "First name is required", nextErrors);
    if (!lastName.trim()) setErr("lastName", "Last name is required", nextErrors);
    if (!gender) setErr("gender", "Please select gender", nextErrors);

    const email = emailId.trim();
    const phone = phoneNumber.trim();

    if (contactMode === "EMAIL") {
      if (!email) setErr("emailId", "Email is required", nextErrors);
    } else if (contactMode === "MOBILE") {
      if (!phone) setErr("phoneNumber", "Mobile number is required", nextErrors);
    } else {
      // BOTH: at least one
      if (!email && !phone) {
        setErr("emailId", "Either email or mobile is required", nextErrors);
        setErr("phoneNumber", "Either email or mobile is required", nextErrors);
      }
    }

    if (phone) {
      if (!phoneCountryCode) {
        setErr("phoneCountryCode", "Country code is required when mobile is provided", nextErrors);
      }
    }

    if (!password || !allPwdOk) {
      setErr("password", "Password must satisfy all rules below", nextErrors);
    }

    if (!confirmPassword) {
      setErr("confirmPassword", "Confirm password is required", nextErrors);
    } else if (password !== confirmPassword) {
      setErr("confirmPassword", "Confirm password must match password", nextErrors);
    }

    setFieldErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    setBusy(true);
    try {
      const res = await apiFetch<RegisterResponse>("/user/register", {
        method: "POST",
        body: JSON.stringify({
          firstName: firstName.trim(),
          lastName: lastName.trim(),
          dob,
          gender,
          emailId: email || null,
          phoneCountryCode: phone ? phoneCountryCode : null,
          phoneNumber: phone || null,
          password,
          confirmPassword
        })
      });

      setCreated(res);
      setFieldErrors({});

      if (res.verificationRequired) {
        setStep("otp");
        const init: Record<string, string> = {};
        for (const ch of res.requiredChannels || []) init[ch] = "";
        setOtpByChannel(init);
      }
    } catch (err: any) {
      const details = err?.details;
      if (details?.errors && Array.isArray(details.errors)) {
        const apiErrors: FieldErrors = {};
        for (const e of details.errors) {
          if (e?.field && e?.message && !apiErrors[e.field]) apiErrors[e.field] = e.message;
        }
        setFieldErrors(apiErrors);
      }
      setError(err?.message ?? "Registration failed");
    } finally {
      setBusy(false);
    }
  }

  async function verifyChannel(ch: string) {
    if (!created?.userId) return;
    setError(null);
    try {
      const otp = (otpByChannel[ch] || "").trim();
      const res = await apiFetch<RegistrationOtpResponse>("/auth/registration/verify-otp", {
        method: "POST",
        body: JSON.stringify({ userId: created.userId, channel: ch, otp })
      });
      setOtpState(res);
    } catch (err: any) {
      setError(err?.message ?? "OTP verification failed");
    }
  }

  async function resend(ch?: string) {
    if (!created?.userId) return;
    setError(null);
    try {
      const res = await apiFetch<RegistrationOtpResponse>("/auth/registration/resend-otp", {
        method: "POST",
        body: JSON.stringify({ userId: created.userId, channel: ch || null })
      });
      setOtpState(res);
    } catch (err: any) {
      setError(err?.message ?? "Resend failed");
    }
  }

  return (
    <div className="page pageAuth pageRegister">
      <TopBar />
      <main className="container">
        <NeonCard title={step === "form" ? "Registration" : "OTP Verification"} subtitle={helper}>
          {step === "form" ? (
            <form className="form" onSubmit={onSubmit}>
              <div className="segRow" role="tablist" aria-label="contact mode">
                <button
                  type="button"
                  className={`segBtn ${contactMode === "MOBILE" ? "segBtnActive" : ""}`}
                  onClick={() => setContactMode("MOBILE")}
                >
                  Mobile
                </button>
                <button
                  type="button"
                  className={`segBtn ${contactMode === "EMAIL" ? "segBtnActive" : ""}`}
                  onClick={() => setContactMode("EMAIL")}
                >
                  Email
                </button>
                <button
                  type="button"
                  className={`segBtn ${contactMode === "BOTH" ? "segBtnActive" : ""}`}
                  onClick={() => setContactMode("BOTH")}
                >
                  Both
                </button>
              </div>
              <div className="muted" style={{ marginTop: 2 }}>
                {contactMode === "BOTH" ? "Provide either one, or both." : `Provide your ${contactMode.toLowerCase()} to register.`}
              </div>

              <div className="grid2" style={{ marginTop: 12 }}>
                <label className="field">
                  <span className="label">First name</span>
                  <input
                    className="input"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value.replace(/[^a-zA-Z]/g, ""))}
                  />
                  {fieldErrors.firstName ? <div className="fieldErr">{fieldErrors.firstName}</div> : null}
                </label>
                <label className="field">
                  <span className="label">Last name</span>
                  <input
                    className="input"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value.replace(/[^a-zA-Z]/g, ""))}
                  />
                  {fieldErrors.lastName ? <div className="fieldErr">{fieldErrors.lastName}</div> : null}
                </label>
              </div>

              <div className="grid2">
                <div className="field">
                  <span className="label">Date of birth</span>
                  <div className="dobGrid">
                    <select className="input" value={dobDay} onChange={(e) => setDobDay(Number(e.target.value))}>
                      {days.map((d) => (
                        <option key={d} value={d}>
                          {d}
                        </option>
                      ))}
                    </select>
                    <select className="input" value={dobMonth} onChange={(e) => setDobMonth(Number(e.target.value))}>
                      {Array.from({ length: 12 }).map((_, i) => {
                        const m = i + 1;
                        return (
                          <option key={m} value={m}>
                            {m}
                          </option>
                        );
                      })}
                    </select>
                    <select className="input" value={dobYear} onChange={(e) => setDobYear(Number(e.target.value))}>
                      {years.map((y) => (
                        <option key={y} value={y}>
                          {y}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <label className="field">
                  <span className="label">Gender</span>
                  <select className="input" value={gender} onChange={(e) => setGender(e.target.value)}>
                  <option value="" disabled>Select gender</option>
                  <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                  {fieldErrors.gender ? <div className="fieldErr">{fieldErrors.gender}</div> : null}
                </label>
              </div>

              {contactMode !== "MOBILE" ? (
                <label className="field">
                  <span className="label">Email {contactMode === "EMAIL" ? "" : "(optional)"}</span>
                  <input
                    className="input"
                    value={emailId}
                    onChange={(e) => setEmailId(e.target.value)}
                    placeholder="surohitech@gmail.com"
                  />
                  {fieldErrors.emailId ? <div className="fieldErr">{fieldErrors.emailId}</div> : null}
                </label>
              ) : null}

              {contactMode !== "EMAIL" ? (
                <div className="gridPhone">
                  <label className="field">
                    <span className="label">Country code</span>
                    <select className="input" value={phoneCountryCode} onChange={(e) => setPhoneCountryCode(e.target.value)}>
                      {dialOptions.map((d) => (
                        <option key={d.id} value={d.dial}>
                          {d.label}
                        </option>
                      ))}
                    </select>
                    {dialLoadFailed ? <div className="muted">Dial codes could not load, using fallback.</div> : null}
                    {fieldErrors.phoneCountryCode ? <div className="fieldErr">{fieldErrors.phoneCountryCode}</div> : null}
                  </label>
                  <label className="field">
                    <span className="label">Mobile {contactMode === "MOBILE" ? "" : "(optional)"}</span>
                    <input
                      className="input"
                      value={phoneNumber}
                      onChange={(e) => setPhoneNumber(e.target.value.replace(/\D/g, ""))}
                      placeholder="9999999999"
                    />
                    {fieldErrors.phoneNumber ? <div className="fieldErr">{fieldErrors.phoneNumber}</div> : null}
                  </label>
                </div>
              ) : null}

              <label className="field">
                <span className="label">Password</span>
                <input
                  className="input"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Min 8 chars, Aa1!"
                  autoComplete="new-password"
                />
                {fieldErrors.password ? <div className="fieldErr">{fieldErrors.password}</div> : null}

                {!allPwdOk ? (
                  <div className="pwdRules">
                    {pwdRules.map((r) => (
                      <div key={r.k} className={`pwdRule ${r.ok ? "pwdRuleOk" : "pwdRuleBad"}`}>
                        {r.t}
                      </div>
                    ))}
                  </div>
                ) : null}
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
                {fieldErrors.confirmPassword ? <div className="fieldErr">{fieldErrors.confirmPassword}</div> : null}
              </label>

              {error ? <div className="alert alertErr">{error}</div> : null}

              <button className="btn btnPrimary" disabled={busy}>
                {busy ? "Creating..." : "Register"}
              </button>

              <div className="muted">
                Already have an account? <Link to="/login">Login</Link>
              </div>
            </form>
          ) : (
            <div className="stack">
              <div className="alert alertOk">
                <div>{created?.message}</div>
                <div className="muted">
                  Your username: <b>{created?.userName}</b>
                </div>
              </div>

              {error ? <div className="alert alertErr">{error}</div> : null}

              {(created?.requiredChannels || []).map((ch) => (
                <div key={ch} className="stack" style={{ gap: 8 }}>
                  <div className="label">OTP ({ch})</div>
                  <div className="rowSplit">
                    <input
                      className="input"
                      value={otpByChannel[ch] || ""}
                      onChange={(e) => setOtpByChannel((p) => ({ ...p, [ch]: e.target.value }))}
                      placeholder="6-digit OTP"
                      inputMode="numeric"
                    />
                    <button type="button" className="btn btnGhost" onClick={() => resend(ch)}>
                      Resend
                    </button>
                    <button type="button" className="btn btnPrimary" onClick={() => verifyChannel(ch)}>
                      Verify
                    </button>
                  </div>
                </div>
              ))}

              {otpState ? <div className="muted">{otpState.message}</div> : null}

              {otpState?.verified ? (
                <div className="alert alertOk">
                  Verified. You can login now.
                  <button type="button" className="btn btnPrimary" onClick={() => nav("/login")}>
                    Login
                  </button>
                </div>
              ) : (
                <div className="muted">After verification, your account will be activated.</div>
              )}
            </div>
          )}
        </NeonCard>
      </main>
    </div>
  );
}