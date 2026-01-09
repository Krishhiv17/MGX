"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import styles from "../page.module.css";

const STORAGE_KEY = "mgx-admin-portal";

export default function AdminLoginPage() {
  const router = useRouter();
  const [baseUrl, setBaseUrl] = useState("http://localhost:8081");
  const [email, setEmail] = useState("admin.in@mgx.local");
  const [password, setPassword] = useState("Admin123!");
  const [token, setToken] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (!saved) return;
    try {
      const parsed = JSON.parse(saved) as {
        baseUrl?: string;
        email?: string;
        token?: string;
      };
      if (parsed?.token) {
        router.replace("/dashboard");
        return;
      }
      if (parsed.baseUrl) setBaseUrl(parsed.baseUrl);
      if (parsed.email) setEmail(parsed.email);
    } catch {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [router]);

  const login = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await fetch(`${baseUrl}/v1/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      const isJson = response.headers.get("content-type")?.includes("application/json");
      const payload = isJson ? await response.json() : await response.text();
      if (!response.ok) {
        throw new Error(typeof payload === "string" ? payload : payload?.message || "Login failed");
      }
      const nextToken = payload.token || "";
      const session = { baseUrl, email, token: nextToken };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      setToken(nextToken);
      router.replace("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div>
          <p className={styles.kicker}>MGX Admin Portal</p>
          <h1>Secure sign in</h1>
          <p className={styles.subtitle}>
            Authenticate with your admin account to access approvals and controls.
          </p>
        </div>
        <div className={styles.pill}>Private access</div>
      </header>

      <section className={styles.authCard}>
        <div>
          <h2>Admin login</h2>
          <p>Enter your admin credentials to continue.</p>
        </div>
        <div className={styles.authForm}>
          <label>
            API Base URL
            <input value={baseUrl} onChange={(event) => setBaseUrl(event.target.value)} />
          </label>
          <label>
            Email
            <input value={email} onChange={(event) => setEmail(event.target.value)} />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          <button className={styles.primaryButton} onClick={login} disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
          {error ? <p className={styles.error}>{error}</p> : null}
          <div className={styles.tokenBadge}>
            <span>Status</span>
            <strong>{token ? "Authenticated" : "No token"}</strong>
          </div>
        </div>
      </section>
    </div>
  );
}
