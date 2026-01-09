"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import styles from "../portal.module.css";
import { clearSession, loadSession, saveSession } from "../lib/session";

type Role = "USER" | "DEVELOPER";

export default function LoginPage() {
  const router = useRouter();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [baseUrl, setBaseUrl] = useState("http://localhost:8081");
  const [email, setEmail] = useState("user@mgx.local");
  const [password, setPassword] = useState("User123!");
  const [role, setRole] = useState<Role>("USER");
  const [phoneNumber, setPhoneNumber] = useState("+919999000001");
  const [countryCode, setCountryCode] = useState("IN");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const session = loadSession();
    if (!session) return;
    if (session.role === "DEVELOPER") {
      router.replace("/developer");
    } else {
      router.replace("/dashboard");
    }
  }, [router]);

  const handleAuth = async () => {
    setLoading(true);
    setError("");
    try {
      const endpoint = mode === "login" ? "/v1/auth/login" : "/v1/auth/register";
      const payload =
        mode === "login"
          ? { email, password }
          : { email, password, role, phoneNumber, countryCode };

      const response = await fetch(`${baseUrl}${endpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const isJson = response.headers.get("content-type")?.includes("application/json");
      const body = isJson ? await response.json() : await response.text();
      if (!response.ok) {
        throw new Error(typeof body === "string" ? body : body?.message || "Request failed");
      }

      if (body.role === "ADMIN") {
        clearSession();
        throw new Error("Admin accounts must use the admin portal.");
      }

      saveSession({
        baseUrl,
        token: body.token,
        role: body.role,
        email: body.email,
        userId: body.userId,
      });

      if (body.role === "DEVELOPER") {
        router.replace("/developer");
      } else {
        router.replace("/dashboard");
      }
    } catch (err) {
      clearSession();
      setError(err instanceof Error ? err.message : "Authentication failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroCard}>
          <span className={styles.tag}>MGX Portal</span>
          <h1>Welcome back</h1>
          <p>
            Sign in to access wallets, games, rates, and settlement tooling. Your
            role determines the dashboard you see.
          </p>
          <div className={styles.actionRow}>
            <button
              className={mode === "login" ? styles.primaryButton : styles.secondaryButton}
              onClick={() => setMode("login")}
            >
              Sign in
            </button>
            <button
              className={mode === "register" ? styles.primaryButton : styles.secondaryButton}
              onClick={() => setMode("register")}
            >
              Create account
            </button>
          </div>
        </div>

        <div className={styles.heroCard}>
          <div className={styles.formGrid}>
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
            {mode === "register" && (
              <>
                <label>
                  Role
                  <select value={role} onChange={(event) => setRole(event.target.value as Role)}>
                    <option value="USER">Gamer</option>
                    <option value="DEVELOPER">Developer</option>
                  </select>
                </label>
                <label>
                  Phone number
                  <input
                    value={phoneNumber}
                    onChange={(event) => setPhoneNumber(event.target.value)}
                  />
                </label>
                <label>
                  Country
                  <input
                    value={countryCode}
                    onChange={(event) => setCountryCode(event.target.value.toUpperCase())}
                  />
                </label>
              </>
            )}
          </div>
          <div className={styles.actionRow}>
            <button className={styles.primaryButton} onClick={handleAuth} disabled={loading}>
              {loading ? "Working..." : mode === "login" ? "Sign in" : "Create account"}
            </button>
          </div>
          {error ? <p className={styles.error}>{error}</p> : null}
        </div>
      </section>
    </div>
  );
}
