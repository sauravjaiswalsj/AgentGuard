import "./styles.css";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Formal Agent Kernel",
  description: "Runtime contract enforcement console for AI agent actions"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
