export default function ProgressBar({ percent }: { percent: number }) {
  const p = Math.max(0, Math.min(100, Math.round(percent)));
  return (
    <div className="progressWrap" aria-label="Profile completion">
      <div className="progressHead">
        <span>Profile completion</span>
        <span className="progressPct">{p}%</span>
      </div>
      <div className="progressTrack">
        <div className="progressFill" style={{ width: `${p}%` }} />
      </div>
    </div>
  );
}