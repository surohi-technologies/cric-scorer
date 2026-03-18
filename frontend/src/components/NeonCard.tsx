import type { ReactNode } from "react";

export default function NeonCard({
  title,
  subtitle,
  right,
  children
}: {
  title: string;
  subtitle?: string;
  right?: ReactNode;
  children: ReactNode;
}) {
  return (
    <section className="neonCard">
      <header className="neonCardHeader">
        <div className="neonCardHeadRow">
          <h2 className="neonCardTitle">{title}</h2>
          {right ? <div className="neonCardRight">{right}</div> : null}
        </div>
        {subtitle ? <p className="neonCardSubtitle">{subtitle}</p> : null}
      </header>
      <div className="neonCardBody">{children}</div>
    </section>
  );
}