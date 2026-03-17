import type { PlayerProfileDraft } from "./types";

export function getDraftKey(userId: number) {
  return `cric:profileDraft:${userId}`;
}

export function loadDraft(userId: number): PlayerProfileDraft | null {
  try {
    const raw = localStorage.getItem(getDraftKey(userId));
    if (!raw) return null;
    return JSON.parse(raw) as PlayerProfileDraft;
  } catch {
    return null;
  }
}

export function saveDraft(userId: number, draft: PlayerProfileDraft) {
  localStorage.setItem(getDraftKey(userId), JSON.stringify(draft));
}

export function clearDraft(userId: number) {
  localStorage.removeItem(getDraftKey(userId));
}