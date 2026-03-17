export type Session = {
  sessionKey: string;
  idleTimeoutSeconds: number;
  userId: number;
  userName: string | null;
  profileCompleted: boolean;
  firstTimeLogin: boolean;
  nextAction: "COMPLETE_PROFILE" | "NONE" | string;
};

const KEY = "cric:session";

export function getSession(): Session | null {
  try {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    return JSON.parse(raw) as Session;
  } catch {
    return null;
  }
}

export function setSession(s: Session) {
  localStorage.setItem(KEY, JSON.stringify(s));
}

export function clearSession() {
  localStorage.removeItem(KEY);
}