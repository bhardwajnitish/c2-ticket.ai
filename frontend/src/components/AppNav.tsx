import Link from "next/link";

export function AppNav() {
  return (
    <header className="app-header">
      <h1 className="app-title">C2 Ticket AI</h1>
      <nav className="app-nav" aria-label="Main">
        <Link href="/tickets">Tickets</Link>
        <Link href="/ask">Ask</Link>
      </nav>
    </header>
  );
}