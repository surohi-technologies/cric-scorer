import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import NeonCard from "../components/NeonCard";
import TopBar from "../components/TopBar";
import { apiFetch } from "../lib/api";

type DialCodeRow = {
  id: number;
  countryName: string;
  dialCode: string;
  iso2: string;
};

type RegisterResponse = {
  message: string;
  userId: number;
  userName: string;
  uniqueIdentifier: string;
};

type FieldErrors = Record<string, string>;

function pad2(n: number) {
  return n < 10 ? `0${n}` : String(n);
}

export default function RegisterPage() {
  const nav = useNavigate();
  const [dialCodes, setDialCodes] = useState<DialCodeRow[]>([]);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [gender, setGender] = useState("MALE");
  const [emailId, setEmailId] = useState("");
  const [phoneCountryCode, setPhoneCountryCode] = useState("+91");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [password, setPassword] = useState("");

  // Custom DOB picker (same palette, more consistent than the native date input)
  const nowYear = new Date().getFullYear();
  const [dobYear, setDobYear] = useState<number>(nowYear - 18);
  const [dobMonth, setDobMonth] = useState<number>(1);
  const [dobDay, setDobDay] = useState<number>(1);

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [created, setCreated] = useState<RegisterResponse | null>(null);

  const helper = useMemo(() => {
    return "Register with mobile, email, or both.";
  }, []);

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
    // If month/year changed and the selected day is now out of range, clamp it.
    const maxDay = new Date(dobYear, dobMonth, 0).getDate();
    if (dobDay > maxDay) setDobDay(maxDay);
  }, [dobYear, dobMonth, dobDay]);

  useEffect(() => {
    (async () => {
      try {
        const rows = await apiFetch<DialCodeRow[]>("/meta/dial-codes", { method: "GET" });
        setDialCodes(rows);
        if (rows.length > 0 && !rows.some((r) => r.dialCode === phoneCountryCode)) {
          setPhoneCountryCode(rows[0].dialCode);
        }
      } catch {
        // If meta fails, keep a default +91.
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function setErr(field: string, msg: string, next: FieldErrors) {
    next[field] = msg;
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setCreated(null);

    const nextErrors: FieldErrors = {};

    if (!firstName.trim()) setErr("firstName", "First name is required", nextErrors);
    if (!lastName.trim()) setErr("lastName", "Last name is required", nextErrors);

    const email = emailId.trim();
    const phone = phoneNumber.trim();

    if (!email && !phone) {
      setErr("emailId", "Either email or mobile is required", nextErrors);
      setErr("phoneNumber", "Either email or mobile is required", nextErrors);
    }

    if (phone) {
      if (!phoneCountryCode) {
        setErr("phoneCountryCode", "Country code is required when mobile is provided", nextErrors);
      }
    }

    if (!password || password.length < 6) {
      setErr("password", "Password must be at least 6 characters", nextErrors);
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
          password
        })
      });
      setCreated(res);
      setFieldErrors({});
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

  return (
    <div className="page">
      <TopBar />
      <main className="container">
        <NeonCard title="Create Account" subtitle={helper}>
          <form className="form" onSubmit={onSubmit}>
            {error ? <div className="alert alertErr">{error}</div> : null}

            <div className="grid2">
              <label className="field">
                <span className="label">First name</span>
                <input className="input" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
                {fieldErrors.firstName ? <div className="fieldErr">{fieldErrors.firstName}</div> : null}
              </label>
              <label className="field">
                <span className="label">Last name</span>
                <input className="input" value={lastName} onChange={(e) => setLastName(e.target.value)} />
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
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </label>
            </div>

            <label className="field">
              <span className="label">Email (optional)</span>
              <input
                className="input"
                value={emailId}
                onChange={(e) => setEmailId(e.target.value)}
                placeholder="name@example.com"
              />
              {fieldErrors.emailId ? <div className="fieldErr">{fieldErrors.emailId}</div> : null}
            </label>

            <div className="gridPhone">
              <label className="field">
                <span className="label">Country code</span>
                <select className="input" value={phoneCountryCode} onChange={(e) => setPhoneCountryCode(e.target.value)}>
                  <option value="+91">+91 (India)</option>
                  {dialCodes.map((d) => (
                    <option key={d.id} value={d.dialCode}>
                      {d.dialCode} ({d.countryName})
                    </option>
                  ))}
                </select>
                {fieldErrors.phoneCountryCode ? <div className="fieldErr">{fieldErrors.phoneCountryCode}</div> : null}
              </label>
              <label className="field">
                <span className="label">Mobile (optional)</span>
                <input
                  className="input"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="9999999999"
                />
                {fieldErrors.phoneNumber ? <div className="fieldErr">{fieldErrors.phoneNumber}</div> : null}
              </label>
            </div>

            <label className="field">
              <span className="label">Password</span>
              <input
                className="input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Minimum 6 characters"
                autoComplete="new-password"
              />
              {fieldErrors.password ? <div className="fieldErr">{fieldErrors.password}</div> : null}
            </label>

            {created ? (
              <div className="alert alertOk">
                <div>{created.message}</div>
                <div className="muted">
                  Your username: <b>{created.userName}</b>
                </div>
                <button type="button" className="btn btnPrimary" onClick={() => nav("/login")}>
                  Go to login
                </button>
              </div>
            ) : null}

            <button className="btn btnPrimary" disabled={busy}>
              {busy ? "Creating..." : "Register"}
            </button>

            <div className="muted">
              Already have an account? <Link to="/login">Login</Link>
            </div>
          </form>
        </NeonCard>
      </main>
    </div>
  );
}