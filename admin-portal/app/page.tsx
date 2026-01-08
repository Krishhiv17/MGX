"use client";

import { useEffect, useMemo, useState } from "react";
import styles from "./page.module.css";

type Developer = {
  id: string;
  userId?: string | null;
  name: string;
  settlementCurrency: string;
  bankAccountRef: string;
  status: string;
  approvedBy?: string | null;
  approvedAt?: string | null;
  rejectionReason?: string | null;
};

type DeveloperSummary = Developer & {
  unsettledTotal?: number | string | null;
};

type Game = {
  id: string;
  developerId: string;
  developerName?: string | null;
  name: string;
  status: string;
  allowedCountries?: string[] | null;
  settlementCurrency: string;
  approvedBy?: string | null;
  approvedAt?: string | null;
  rejectionReason?: string | null;
};

type MgcRate = {
  id: string;
  gameId: string;
  ugcPerMgc: number;
  status: string;
  activeFrom?: string | null;
  activeTo?: string | null;
  createdBy?: string | null;
  approvedBy?: string | null;
  approvedAt?: string | null;
  rejectionReason?: string | null;
  createdAt?: string | null;
};

type ApiKey = {
  id: string;
  ownerName: string;
  scopes: string;
  status: string;
  createdAt?: string | null;
  key?: string | null;
};

type Transaction = {
  id: string;
  type: string;
  userId?: string | null;
  developerId?: string | null;
  gameId?: string | null;
  pointsDebited?: number | null;
  mgcCredited?: number | null;
  mgcSpent?: number | null;
  ugcCredited?: number | null;
  amountDue?: number | null;
  settlementCurrency?: string | null;
  status: string;
  createdAt?: string | null;
};

type Receivable = {
  id: string;
  developerId: string;
  gameId: string;
  status: string;
  amountDue: number;
  settlementCurrency: string;
  fxWindowId?: string | null;
  fxRateUsed?: number | null;
  createdAt?: string | null;
};

type LogEntry = {
  time: string;
  title: string;
  payload: unknown;
};

const STORAGE_KEY = "mgx-admin-portal";

export default function AdminPortalPage() {
  const [baseUrl, setBaseUrl] = useState("http://localhost:8081");
  const [email, setEmail] = useState("admin.in@mgx.local");
  const [password, setPassword] = useState("Admin123!");
  const [token, setToken] = useState("");
  const [activeTab, setActiveTab] = useState("approvals");
  const [logEntries, setLogEntries] = useState<LogEntry[]>([]);

  const [developers, setDevelopers] = useState<Developer[]>([]);
  const [developerSummaries, setDeveloperSummaries] = useState<DeveloperSummary[]>([]);
  const [games, setGames] = useState<Game[]>([]);
  const [rates, setRates] = useState<MgcRate[]>([]);
  const [apiKeys, setApiKeys] = useState<ApiKey[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [receivables, setReceivables] = useState<Receivable[]>([]);

  const [rejectReason, setRejectReason] = useState<Record<string, string>>({});
  const [apiKeyOwner, setApiKeyOwner] = useState("Bank Mock");
  const [apiKeyScopes, setApiKeyScopes] = useState("private");
  const [newApiKey, setNewApiKey] = useState<ApiKey | null>(null);

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (!saved) return;
    try {
      const parsed = JSON.parse(saved) as {
        baseUrl?: string;
        email?: string;
        token?: string;
      };
      if (parsed.baseUrl) setBaseUrl(parsed.baseUrl);
      if (parsed.email) setEmail(parsed.email);
      if (parsed.token) setToken(parsed.token);
    } catch (_) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  useEffect(() => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        baseUrl,
        email,
        token,
      })
    );
  }, [baseUrl, email, token]);

  const logText = useMemo(() => {
    if (!logEntries.length) return "No events yet.";
    return logEntries
      .map((entry) => `[#${entry.time}] ${entry.title}\n${JSON.stringify(entry.payload, null, 2)}`)
      .join("\n\n");
  }, [logEntries]);

  const log = (title: string, payload: unknown) => {
    setLogEntries((entries) => [
      { title, payload, time: new Date().toLocaleTimeString() },
      ...entries,
    ]);
  };

  const apiFetch = async (path: string, options: RequestInit = {}) => {
    const headers = new Headers(options.headers || {});
    if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json");
    if (token) headers.set("Authorization", `Bearer ${token}`);

    const response = await fetch(`${baseUrl}${path}`, {
      ...options,
      headers,
    });
    const isJson = response.headers.get("content-type")?.includes("application/json");
    const payload = isJson ? await response.json() : await response.text();
    if (!response.ok) {
      log(`Request failed: ${path}`, payload);
      throw new Error(typeof payload === "string" ? payload : payload?.message || "Request failed");
    }
    return payload;
  };

  const login = async () => {
    try {
      const payload = await apiFetch("/v1/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      setToken(payload.token || "");
      log("Login success", payload);
    } catch (error) {
      log("Login error", { error: String(error) });
    }
  };

  const loadApprovals = async () => {
    try {
      const [developerList, gameList, rateList] = await Promise.all([
        apiFetch("/v1/admin/developers"),
        apiFetch("/v1/admin/games"),
        apiFetch("/v1/admin/rates/mgc-ugc?status=PENDING"),
      ]);
      setDevelopers(developerList as Developer[]);
      setGames(gameList as Game[]);
      setRates(rateList as MgcRate[]);
      log("Loaded approvals", {
        developers: (developerList as Developer[]).length,
        games: (gameList as Game[]).length,
        rates: (rateList as MgcRate[]).length,
      });
    } catch (error) {
      log("Load approvals error", { error: String(error) });
    }
  };

  const loadSummaries = async () => {
    try {
      const payload = await apiFetch("/v1/admin/developers/summary");
      setDeveloperSummaries(payload as DeveloperSummary[]);
      log("Loaded developer summaries", payload);
    } catch (error) {
      log("Load summaries error", { error: String(error) });
    }
  };

  const loadTransactions = async () => {
    try {
      const payload = await apiFetch("/v1/admin/transactions");
      setTransactions(payload as Transaction[]);
      log("Loaded transactions", payload);
    } catch (error) {
      log("Load transactions error", { error: String(error) });
    }
  };

  const loadReceivables = async () => {
    try {
      const payload = await apiFetch("/v1/admin/receivables");
      setReceivables(payload as Receivable[]);
      log("Loaded receivables", payload);
    } catch (error) {
      log("Load receivables error", { error: String(error) });
    }
  };

  const loadApiKeys = async () => {
    try {
      const payload = await apiFetch("/v1/admin/api-keys");
      setApiKeys(payload as ApiKey[]);
      log("Loaded API keys", payload);
    } catch (error) {
      log("Load API keys error", { error: String(error) });
    }
  };

  const approveDeveloper = async (id: string) => {
    await apiFetch(`/v1/admin/developers/${id}/approve`, { method: "POST" });
    log("Developer approved", { id });
    loadApprovals();
  };

  const rejectDeveloper = async (id: string) => {
    const reason = rejectReason[`dev-${id}`];
    if (!reason?.trim()) {
      log("Rejection requires reason", { id });
      return;
    }
    await apiFetch(`/v1/admin/developers/${id}/reject`, {
      method: "POST",
      body: JSON.stringify({ reason }),
    });
    log("Developer rejected", { id, reason });
    loadApprovals();
  };

  const approveGame = async (id: string) => {
    await apiFetch(`/v1/admin/games/${id}/approve`, { method: "POST" });
    log("Game approved", { id });
    loadApprovals();
  };

  const rejectGame = async (id: string) => {
    const reason = rejectReason[`game-${id}`];
    if (!reason?.trim()) {
      log("Rejection requires reason", { id });
      return;
    }
    await apiFetch(`/v1/admin/games/${id}/reject`, {
      method: "POST",
      body: JSON.stringify({ reason }),
    });
    log("Game rejected", { id, reason });
    loadApprovals();
  };

  const approveRate = async (id: string) => {
    await apiFetch(`/v1/admin/rates/mgc-ugc/${id}/approve`, { method: "POST" });
    log("Rate approved", { id });
    loadApprovals();
  };

  const rejectRate = async (id: string) => {
    const reason = rejectReason[`rate-${id}`];
    if (!reason?.trim()) {
      log("Rejection requires reason", { id });
      return;
    }
    await apiFetch(`/v1/admin/rates/mgc-ugc/${id}/reject`, {
      method: "POST",
      body: JSON.stringify({ reason }),
    });
    log("Rate rejected", { id, reason });
    loadApprovals();
  };

  const createApiKey = async () => {
    try {
      const payload = await apiFetch("/v1/admin/api-keys", {
        method: "POST",
        body: JSON.stringify({
          ownerName: apiKeyOwner,
          scopes: apiKeyScopes.split(",").map((scope) => scope.trim()).filter(Boolean),
        }),
      });
      setNewApiKey(payload as ApiKey);
      log("API key created", payload);
      loadApiKeys();
    } catch (error) {
      log("Create API key error", { error: String(error) });
    }
  };

  const pendingDevelopers = developers.filter((dev) => dev.status === "PENDING_APPROVAL");
  const pendingGames = games.filter((game) => game.status === "PENDING_APPROVAL");
  const pendingRates = rates.filter((rate) => rate.status === "PENDING");

  const gameNameById = useMemo(() => {
    const map = new Map<string, string>();
    games.forEach((game) => map.set(game.id, game.name));
    return map;
  }, [games]);

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <div>
          <p className={styles.kicker}>MGX Admin Portal</p>
          <h1>Operational control center</h1>
          <p className={styles.subtitle}>
            Approve onboarding, rates, and monitor every transaction across the platform.
          </p>
        </div>
        <div className={styles.pill}>Secure Admin</div>
      </header>

      <section className={styles.authCard}>
        <div>
          <h2>Admin login</h2>
          <p>Use an admin account to unlock approvals and monitoring data.</p>
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
          <button className={styles.primaryButton} onClick={login}>
            Sign in
          </button>
          <div className={styles.tokenBadge}>
            <span>Status</span>
            <strong>{token ? "Authenticated" : "No token"}</strong>
          </div>
        </div>
      </section>

      <nav className={styles.nav}>
        {[
          { id: "approvals", label: "Approvals" },
          { id: "summaries", label: "Developer totals" },
          { id: "transactions", label: "Transactions" },
          { id: "receivables", label: "Receivables" },
          { id: "keys", label: "API keys" },
        ].map((tab) => (
          <button
            key={tab.id}
            className={activeTab === tab.id ? styles.navActive : styles.navButton}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
        <button className={styles.secondaryButton} onClick={loadApprovals}>
          Refresh approvals
        </button>
      </nav>

      {activeTab === "approvals" && (
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2>Pending approvals</h2>
            <p>Review incoming developers, games, and rate changes.</p>
          </div>

          <div className={styles.grid}>
            <div className={styles.panel}>
              <div className={styles.panelHeader}>
                <h3>Developers</h3>
                <span>{pendingDevelopers.length} pending</span>
              </div>
              {pendingDevelopers.length === 0 && (
                <p className={styles.muted}>No developer approvals pending.</p>
              )}
              {pendingDevelopers.map((dev) => (
                <div key={dev.id} className={styles.card}>
                  <div>
                    <strong>{dev.name}</strong>
                    <p>{dev.settlementCurrency} · {dev.bankAccountRef}</p>
                    <p className={styles.muted}>User: {dev.userId || "Unlinked"}</p>
                  </div>
                  <div className={styles.actions}>
                    <input
                      placeholder="Rejection reason"
                      value={rejectReason[`dev-${dev.id}`] || ""}
                      onChange={(event) =>
                        setRejectReason((prev) => ({
                          ...prev,
                          [`dev-${dev.id}`]: event.target.value,
                        }))
                      }
                    />
                    <div className={styles.actionRow}>
                      <button className={styles.successButton} onClick={() => approveDeveloper(dev.id)}>
                        Approve
                      </button>
                      <button className={styles.dangerButton} onClick={() => rejectDeveloper(dev.id)}>
                        Reject
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className={styles.panel}>
              <div className={styles.panelHeader}>
                <h3>Games</h3>
                <span>{pendingGames.length} pending</span>
              </div>
              {pendingGames.length === 0 && (
                <p className={styles.muted}>No game approvals pending.</p>
              )}
              {pendingGames.map((game) => (
                <div key={game.id} className={styles.card}>
                  <div>
                    <strong>{game.name}</strong>
                    <p>Developer: {game.developerName || game.developerId}</p>
                    <p className={styles.muted}>Allowed: {(game.allowedCountries || []).join(", ")}</p>
                  </div>
                  <div className={styles.actions}>
                    <input
                      placeholder="Rejection reason"
                      value={rejectReason[`game-${game.id}`] || ""}
                      onChange={(event) =>
                        setRejectReason((prev) => ({
                          ...prev,
                          [`game-${game.id}`]: event.target.value,
                        }))
                      }
                    />
                    <div className={styles.actionRow}>
                      <button className={styles.successButton} onClick={() => approveGame(game.id)}>
                        Approve
                      </button>
                      <button className={styles.dangerButton} onClick={() => rejectGame(game.id)}>
                        Reject
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className={styles.panel}>
              <div className={styles.panelHeader}>
                <h3>MGC → UGC rates</h3>
                <span>{pendingRates.length} pending</span>
              </div>
              {pendingRates.length === 0 && (
                <p className={styles.muted}>No rate approvals pending.</p>
              )}
              {pendingRates.map((rate) => (
                <div key={rate.id} className={styles.card}>
                  <div>
                    <strong>{gameNameById.get(rate.gameId) || rate.gameId}</strong>
                    <p>{rate.ugcPerMgc} UGC per 1 MGC</p>
                    <p className={styles.muted}>Status: {rate.status}</p>
                  </div>
                  <div className={styles.actions}>
                    <input
                      placeholder="Rejection reason"
                      value={rejectReason[`rate-${rate.id}`] || ""}
                      onChange={(event) =>
                        setRejectReason((prev) => ({
                          ...prev,
                          [`rate-${rate.id}`]: event.target.value,
                        }))
                      }
                    />
                    <div className={styles.actionRow}>
                      <button className={styles.successButton} onClick={() => approveRate(rate.id)}>
                        Approve
                      </button>
                      <button className={styles.dangerButton} onClick={() => rejectRate(rate.id)}>
                        Reject
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>
      )}

      {activeTab === "summaries" && (
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2>Developer exposure</h2>
            <p>Monitor unsettled totals for each developer.</p>
            <button className={styles.secondaryButton} onClick={loadSummaries}>
              Refresh summary
            </button>
          </div>
          <div className={styles.list}>
            {developerSummaries.map((dev) => (
              <div key={dev.id} className={styles.listRow}>
                <div>
                  <strong>{dev.name}</strong>
                  <p className={styles.muted}>{dev.settlementCurrency} · {dev.bankAccountRef}</p>
                </div>
                <div className={styles.value}>
                  <span>Unsettled</span>
                  <strong>{dev.unsettledTotal ?? 0}</strong>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {activeTab === "transactions" && (
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2>Global transactions</h2>
            <p>All topups, purchases, and receivables in a single stream.</p>
            <button className={styles.secondaryButton} onClick={loadTransactions}>
              Refresh transactions
            </button>
          </div>
          <div className={styles.table}>
            {transactions.map((tx) => (
              <div key={tx.id} className={styles.tableRow}>
                <div>
                  <strong>{tx.type}</strong>
                  <p className={styles.muted}>{tx.status}</p>
                </div>
                <div className={styles.muted}>{tx.createdAt || ""}</div>
                <div className={styles.muted}>{tx.userId || tx.developerId || ""}</div>
                <div>
                  {tx.pointsDebited ? `Points ${tx.pointsDebited}` : null}
                  {tx.mgcCredited ? `MGC +${tx.mgcCredited}` : null}
                  {tx.mgcSpent ? `MGC -${tx.mgcSpent}` : null}
                  {tx.ugcCredited ? `UGC +${tx.ugcCredited}` : null}
                  {tx.amountDue ? `${tx.amountDue} ${tx.settlementCurrency}` : null}
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {activeTab === "receivables" && (
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2>Receivables overview</h2>
            <p>Track outstanding amounts by developer and FX window.</p>
            <button className={styles.secondaryButton} onClick={loadReceivables}>
              Refresh receivables
            </button>
          </div>
          <div className={styles.table}>
            {receivables.map((rec) => (
              <div key={rec.id} className={styles.tableRow}>
                <div>
                  <strong>{rec.status}</strong>
                  <p className={styles.muted}>{rec.createdAt || ""}</p>
                </div>
                <div className={styles.muted}>{rec.developerId}</div>
                <div className={styles.muted}>{rec.gameId}</div>
                <div>
                  {rec.amountDue} {rec.settlementCurrency}
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {activeTab === "keys" && (
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2>API keys</h2>
            <p>Create and monitor API keys for partner integrations.</p>
            <button className={styles.secondaryButton} onClick={loadApiKeys}>
              Refresh keys
            </button>
          </div>
          <div className={styles.keyGrid}>
            <div className={styles.panel}>
              <h3>Create new key</h3>
              <label>
                Owner name
                <input value={apiKeyOwner} onChange={(event) => setApiKeyOwner(event.target.value)} />
              </label>
              <label>
                Scopes (comma separated)
                <input value={apiKeyScopes} onChange={(event) => setApiKeyScopes(event.target.value)} />
              </label>
              <button className={styles.primaryButton} onClick={createApiKey}>
                Generate key
              </button>
              {newApiKey?.key && (
                <div className={styles.keyResult}>
                  <p>New key</p>
                  <code>{newApiKey.key}</code>
                </div>
              )}
            </div>
            <div className={styles.panel}>
              <h3>Active keys</h3>
              <div className={styles.list}>
                {apiKeys.map((key) => (
                  <div key={key.id} className={styles.listRow}>
                    <div>
                      <strong>{key.ownerName}</strong>
                      <p className={styles.muted}>{key.scopes}</p>
                    </div>
                    <span className={styles.badge}>{key.status}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>
      )}

      <section className={styles.logSection}>
        <div className={styles.sectionHeader}>
          <h2>Activity log</h2>
          <p>Every request from this portal is logged here.</p>
        </div>
        <pre className={styles.log}>{logText}</pre>
      </section>
    </div>
  );
}
