"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

const STORAGE_KEY = "mgx-admin-portal";

export default function AdminPortalEntry() {
  const router = useRouter();

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (!saved) {
      router.replace("/login");
      return;
    }
    try {
      const parsed = JSON.parse(saved) as { token?: string | null };
      if (parsed?.token) {
        router.replace("/dashboard");
      } else {
        router.replace("/login");
      }
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      router.replace("/login");
    }
  }, [router]);

  return <div style={{ padding: "2rem" }}>Redirecting...</div>;
}
