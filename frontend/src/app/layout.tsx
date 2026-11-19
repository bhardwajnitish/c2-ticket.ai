import type { Metadata } from "next";
import { AppNav } from "@/components/AppNav";
import "./globals.css";

export const metadata: Metadata = {
  title: "C2 Ticket AI",
  description: "Support tickets and AI-assisted search",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <div className="app-shell">
          <AppNav />
          <main>{children}</main>
        </div>
      </body>
    </html>
  );
}