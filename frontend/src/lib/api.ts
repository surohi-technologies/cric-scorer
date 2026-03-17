import { clearSession, getSession } from "./session";

type ApiError = {
  message?: string;
  errors?: Array<{ field: string; message: string }>;
};

function apiBase() {
  return (import.meta.env.VITE_API_BASE as string | undefined) ?? "/newEra/crick-scorer";
}

export async function apiFetch<T>(
  path: string,
  init?: RequestInit & { auth?: boolean }
): Promise<T> {
  const url = `${apiBase()}${path.startsWith("/") ? path : `/${path}`}`;
  const headers = new Headers(init?.headers ?? {});
  headers.set("Content-Type", "application/json");

  if (init?.auth) {
    const s = getSession();
    if (s?.sessionKey) {
      headers.set("X-Session-Key", s.sessionKey);
    }
  }

  const res = await fetch(url, { ...init, headers });
  if (res.status === 401) {
    clearSession();
  }

  if (!res.ok) {
    let body: ApiError | null = null;
    try {
      body = (await res.json()) as ApiError;
    } catch {
      // ignore
    }
    const msg = body?.message || `Request failed (${res.status})`;
    const e = new Error(msg) as Error & { details?: ApiError; status?: number };
    e.details = body ?? undefined;
    e.status = res.status;
    throw e;
  }

  return (await res.json()) as T;
}