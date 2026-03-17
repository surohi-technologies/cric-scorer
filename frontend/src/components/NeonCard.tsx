import type { ReactNode } from "react";

export default function NeonCard({
  title,
  subtitle,
  children
}: {
  title: string;
  subtitle?: string;
  children: ReactNode;
}) {
  return (
    <section className="neonCard">
      <header className="neonCardHeader">
        <h2 className="neonCardTitle">{title}</h2>
        {subtitle ? <p className="neonCardSubtitle">{subtitle}</p> : null}
      </header>
      <div className="neonCardBody">{children}</div>
    </section>
  );
}