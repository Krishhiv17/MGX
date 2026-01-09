import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "MGX Portal",
  description: "Unified gamer and developer portal for MGX",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
