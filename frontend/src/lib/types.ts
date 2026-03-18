export type MetaOption = {
  id: number;
  label: string;
  description?: string | null;
};

export type PlayerProfileDraft = {
  aliasName: string;
  jerseyNumber: string;
  battingHandId: number | null;
  bowlingHandId: number | null;
  battingStyleId: number | null;
  battingPositionId: number | null;
  bowlingStyleId: number | null;
  bowlingTacticalRoleId: number | null;
  bowlingPreferenceId: number | null;
  playerRoleTypeId: number | null;
  battingIntentId: number | null;
  favouritePlayer: string;
  favouriteTeam: string;
  description: string;
};