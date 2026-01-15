"use client";

import { useEffect, useMemo, useState } from "react";

type Game = {
  id: string;
  name: string;
  developerName?: string | null;
  allowedCountries?: string[] | null;
};

type PointsRate = {
  pointsPerMgc: number;
};

type MgcRate = {
  ugcPerMgc: number;
};

type OtpResponse = {
  sessionId: string;
  debugCode?: string;
  expiresAt?: string;
};

type LogEntry = {
  title: string;
  payload: unknown;
  time: string;
};

const STORAGE_KEY = "mgx-bank-mock-config";
const steps = [
  {
    id: "setup",
    title: "Connect",
    description: "Set the private API base URL, API key, and target country.",
  },
  {
    id: "discover",
    title: "Discover",
    description: "Fetch games and exchange rates for the selected country.",
  },
  {
    id: "otp",
    title: "Verify",
    description: "Request and validate the OTP session for the bank flow.",
  },
  {
    id: "link",
    title: "Link",
    description: "Link the bank reference to the MGX user account.",
  },
  {
    id: "purchase",
    title: "Purchase",
    description: "Send the private purchase request with MGC or UGC.",
  },
  {
    id: "review",
    title: "Review",
    description: "Audit the latest responses and payloads.",
  },
];

export default function BankMockPage() {
  const [activeStep, setActiveStep] = useState(0);
  const [baseUrl, setBaseUrl] = useState("http://localhost:8081");
  const [apiKey, setApiKey] = useState("");
  const [country, setCountry] = useState("IN");
  const [games, setGames] = useState<Game[]>([]);
  const [selectedGameId, setSelectedGameId] = useState("");
  const [selectedGame, setSelectedGame] = useState<Game | null>(null);
  const [pointsRate, setPointsRate] = useState<PointsRate | null>(null);
  const [mgcRate, setMgcRate] = useState<MgcRate | null>(null);
  const [phone, setPhone] = useState("");
  const [otpPurpose, setOtpPurpose] = useState("BANK_PURCHASE");
  const [otpSession, setOtpSession] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [userToken, setUserToken] = useState("");
  const [bankRef, setBankRef] = useState("BANK-001");
  const [linkOtpSession, setLinkOtpSession] = useState("");
  const [purchaseGameId, setPurchaseGameId] = useState("");
  const [mgcAmount, setMgcAmount] = useState("1");
  const [ugcAmount, setUgcAmount] = useState("");
  const [purchaseOtpSession, setPurchaseOtpSession] = useState("");
  const [idempotencyKey, setIdempotencyKey] = useState("bank-purchase-001");
  const [logEntries, setLogEntries] = useState<LogEntry[]>([]);

  const hasConfig = Boolean(baseUrl && apiKey && country);
  const hasGame = Boolean(selectedGameId);
  const hasOtp = Boolean(otpSession);
  const hasBankLink = Boolean(userToken && linkOtpSession && bankRef);
  const hasPurchaseReady = Boolean(purchaseGameId && (mgcAmount || ugcAmount));

  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (!saved) {
      return;
    }
    try {
      const parsed = JSON.parse(saved) as {
        baseUrl?: string;
        apiKey?: string;
        country?: string;
      };
      if (parsed.baseUrl) setBaseUrl(parsed.baseUrl);
      if (parsed.apiKey) setApiKey(parsed.apiKey);
      if (parsed.country) setCountry(parsed.country);
    } catch (_) {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  useEffect(() => {
    if (!selectedGameId) {
      setSelectedGame(null);
      return;
    }
    const match = games.find((game) => game.id === selectedGameId) || null;
    setSelectedGame(match);
    setPurchaseGameId(selectedGameId);
  }, [selectedGameId, games]);

  const logText = useMemo(() => {
    if (!logEntries.length) {
      return "Waiting for actions...";
    }
    return logEntries
      .map((entry) => {
        const header = `[${entry.time}] ${entry.title}`;
        return `${header}\n${JSON.stringify(entry.payload, null, 2)}`;
      })
      .join("\n\n");
  }, [logEntries]);

  const log = (title: string, payload: unknown) => {
    setLogEntries((entries) => [
      {
        title,
        payload,
        time: new Date().toLocaleTimeString(),
      },
      ...entries,
    ]);
  };

  const getHeaders = () => {
    return {
      "Content-Type": "application/json",
      "X-API-Key": apiKey,
    };
  };

  const saveConfig = () => {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        baseUrl,
        apiKey,
        country,
      })
    );
    log("Saved config", { baseUrl, country });
  };

  const loadConfig = () => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (!saved) {
      log("Load config", { message: "No saved config found" });
      return;
    }
    const parsed = JSON.parse(saved) as {
      baseUrl?: string;
      apiKey?: string;
      country?: string;
    };
    if (parsed.baseUrl) setBaseUrl(parsed.baseUrl);
    if (parsed.apiKey) setApiKey(parsed.apiKey);
    if (parsed.country) setCountry(parsed.country);
    log("Loaded config", parsed);
  };

  const fetchGames = async () => {
    try {
      const response = await fetch(
        `${baseUrl}/v1/private/games?country=${encodeURIComponent(country)}`,
        {
          headers: getHeaders(),
        }
      );
      const payload = await response.json();
      if (!response.ok) {
        log("Fetch games failed", payload);
        return;
      }
      setGames(payload as Game[]);
      if (payload.length) {
        setSelectedGameId(payload[0].id);
      }
      log("Fetched games", payload);
    } catch (error) {
      log("Fetch games error", { error: String(error) });
    }
  };

  const fetchPointsRate = async () => {
    try {
      const response = await fetch(`${baseUrl}/v1/private/points-mgc-rate`, {
        headers: getHeaders(),
      });
      const payload = await response.json();
      if (!response.ok) {
        log("Points rate failed", payload);
        return;
      }
      setPointsRate(payload as PointsRate);
      log("Points rate", payload);
    } catch (error) {
      log("Points rate error", { error: String(error) });
    }
  };

  const fetchMgcRate = async () => {
    if (!selectedGameId) {
      log("MGC rate", { message: "Select a game first" });
      return;
    }
    try {
      const response = await fetch(
        `${baseUrl}/v1/private/rates?gameId=${selectedGameId}&country=${encodeURIComponent(
          country
        )}`,
        {
          headers: getHeaders(),
        }
      );
      const payload = await response.json();
      if (!response.ok) {
        log("MGC rate failed", payload);
        return;
      }
      setMgcRate(payload as MgcRate);
      log("MGC rate", payload);
    } catch (error) {
      log("MGC rate error", { error: String(error) });
    }
  };

  const requestOtp = async () => {
    if (!phone) {
      log("OTP request", { message: "Phone number required" });
      return;
    }
    try {
      const response = await fetch(`${baseUrl}/v1/otp/request`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          phoneNumber: phone,
          purpose: otpPurpose,
        }),
      });
      const payload = (await response.json()) as OtpResponse;
      if (!response.ok) {
        log("OTP request failed", payload);
        return;
      }
      setOtpSession(payload.sessionId);
      setOtpCode(payload.debugCode || "");
      setPurchaseOtpSession(payload.sessionId);
      setLinkOtpSession(payload.sessionId);
      log("OTP requested", payload);
    } catch (error) {
      log("OTP request error", { error: String(error) });
    }
  };

  const verifyOtp = async () => {
    if (!otpSession || !otpCode) {
      log("OTP verify", { message: "Session and code required" });
      return;
    }
    try {
      const response = await fetch(`${baseUrl}/v1/otp/verify`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          sessionId: otpSession,
          code: otpCode,
        }),
      });
      const payload = await response.json();
      log("OTP verify", payload);
    } catch (error) {
      log("OTP verify error", { error: String(error) });
    }
  };

  const createBankLink = async () => {
    if (!userToken) {
      log("Bank link", { message: "User token required" });
      return;
    }
    if (!phone || !linkOtpSession) {
      log("Bank link", { message: "Phone and OTP session required" });
      return;
    }
    try {
      const response = await fetch(`${baseUrl}/v1/bank-links`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${userToken}`,
        },
        body: JSON.stringify({
          bankRef,
          phoneNumber: phone,
          otpSessionId: linkOtpSession,
        }),
      });
      const payload = await response.json();
      if (!response.ok) {
        log("Bank link failed", payload);
        return;
      }
      log("Bank link created", payload);
    } catch (error) {
      log("Bank link error", { error: String(error) });
    }
  };

  const submitPurchase = async () => {
    if (!purchaseGameId) {
      log("Purchase", { message: "Game ID required" });
      return;
    }
    if (!phone || !purchaseOtpSession) {
      log("Purchase", { message: "Phone and OTP session required" });
      return;
    }
    if (!apiKey) {
      log("Purchase", { message: "API key required" });
      return;
    }
    const mgcValue = mgcAmount ? Number(mgcAmount) : null;
    const ugcValue = ugcAmount ? Number(ugcAmount) : null;
    if (mgcValue && ugcValue) {
      log("Purchase", { message: "Provide either MGC or UGC, not both" });
      return;
    }
    if (!mgcValue && !ugcValue) {
      log("Purchase", { message: "Provide MGC or UGC amount" });
      return;
    }

    const payload = {
      gameId: purchaseGameId,
      mgcAmount: mgcValue || undefined,
      ugcAmount: ugcValue || undefined,
      phoneNumber: phone,
      bankRef,
      otpSessionId: purchaseOtpSession,
    };

    try {
      const response = await fetch(`${baseUrl}/v1/private/purchase`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-API-Key": apiKey,
          "Idempotency-Key": idempotencyKey,
        },
        body: JSON.stringify(payload),
      });
      const result = await response.json();
      log("Purchase result", result);
    } catch (error) {
      log("Purchase error", { error: String(error) });
    }
  };

  const goNext = () => {
    setActiveStep((step) => Math.min(step + 1, steps.length - 1));
  };

  const goBack = () => {
    setActiveStep((step) => Math.max(step - 1, 0));
  };

  const renderStep = () => {
    switch (steps[activeStep].id) {
      case "setup":
        return (
          <>
            <h2>Connection setup</h2>
            <p className="hint">
              These values persist in local storage so you can resume quickly.
            </p>
            <div className="field">
              <label htmlFor="baseUrl">Backend base URL</label>
              <input
                id="baseUrl"
                type="text"
                value={baseUrl}
                onChange={(event) => setBaseUrl(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="apiKey">API key (X-API-Key)</label>
              <input
                id="apiKey"
                type="password"
                placeholder="Paste API key"
                value={apiKey}
                onChange={(event) => setApiKey(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="country">Country code</label>
              <input
                id="country"
                type="text"
                value={country}
                maxLength={2}
                onChange={(event) => setCountry(event.target.value.toUpperCase())}
              />
            </div>
            <div className="actions">
              <button className="primary" onClick={saveConfig}>
                Save config
              </button>
              <button className="ghost" onClick={loadConfig}>
                Load saved
              </button>
            </div>
            <p className="hint">Serve this UI on port 3000 to match backend CORS.</p>
          </>
        );
      case "discover":
        return (
          <>
            <h2>Discover games</h2>
            <p className="hint">
              Pull the approved catalog and rates before moving forward.
            </p>
            <div className="actions">
              <button className="primary" onClick={fetchGames}>
                Fetch games
              </button>
              <button className="ghost" onClick={fetchPointsRate}>
                Points → MGC rate
              </button>
              <button className="ghost" onClick={fetchMgcRate}>
                MGC → UGC rate
              </button>
            </div>
            <div className="field">
              <label htmlFor="gameSelect">Games in country</label>
              <select
                id="gameSelect"
                value={selectedGameId}
                onChange={(event) => setSelectedGameId(event.target.value)}
              >
                <option value="">Select a game</option>
                {games.map((game) => (
                  <option key={game.id} value={game.id}>
                    {game.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="inline">
              Selected:{" "}
              {selectedGame ? `${selectedGame.name} · ${selectedGame.id}` : "None"}
            </div>
            <div className="inline">
              Points rate: {pointsRate ? pointsRate.pointsPerMgc : "-"}
            </div>
            <div className="inline">
              MGC rate: {mgcRate ? mgcRate.ugcPerMgc : "-"}
            </div>
          </>
        );
      case "otp":
        return (
          <>
            <h2>OTP verification</h2>
            <p className="hint">
              Required for both bank linking and purchase confirmations.
            </p>
            <div className="field">
              <label htmlFor="phone">Phone number</label>
              <input
                id="phone"
                type="text"
                placeholder="+919999000003"
                value={phone}
                onChange={(event) => setPhone(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="otpPurpose">Purpose</label>
              <input
                id="otpPurpose"
                type="text"
                value={otpPurpose}
                onChange={(event) => setOtpPurpose(event.target.value)}
              />
            </div>
            <div className="actions">
              <button className="primary" onClick={requestOtp}>
                Request OTP
              </button>
              <button className="ghost" onClick={verifyOtp}>
                Verify OTP
              </button>
            </div>
            <div className="field">
              <label htmlFor="otpSession">OTP session ID</label>
              <input
                id="otpSession"
                type="text"
                value={otpSession}
                onChange={(event) => setOtpSession(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="otpCode">OTP code</label>
              <input
                id="otpCode"
                type="text"
                value={otpCode}
                onChange={(event) => setOtpCode(event.target.value)}
              />
            </div>
            <p className="hint">Debug OTP code is returned by the mock backend.</p>
          </>
        );
      case "link":
        return (
          <>
            <h2>Bank link</h2>
            <p className="hint">
              Link the user’s MGX account to their bank reference.
            </p>
            <div className="field">
              <label htmlFor="userToken">User JWT</label>
              <input
                id="userToken"
                type="password"
                placeholder="Paste USER token"
                value={userToken}
                onChange={(event) => setUserToken(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="bankRef">Bank reference</label>
              <input
                id="bankRef"
                type="text"
                value={bankRef}
                onChange={(event) => setBankRef(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="linkOtpSession">OTP session ID</label>
              <input
                id="linkOtpSession"
                type="text"
                value={linkOtpSession}
                onChange={(event) => setLinkOtpSession(event.target.value)}
              />
            </div>
            <div className="actions">
              <button className="primary" onClick={createBankLink}>
                Create bank link
              </button>
            </div>
          </>
        );
      case "purchase":
        return (
          <>
            <h2>Purchase UGC</h2>
            <p className="hint">
              Use either MGC or UGC and keep the idempotency key unique.
            </p>
            <div className="field">
              <label htmlFor="purchaseGameId">Game ID</label>
              <input
                id="purchaseGameId"
                type="text"
                value={purchaseGameId}
                onChange={(event) => setPurchaseGameId(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="mgcAmount">MGC amount</label>
              <input
                id="mgcAmount"
                type="number"
                step="0.01"
                value={mgcAmount}
                onChange={(event) => setMgcAmount(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="ugcAmount">UGC amount (optional)</label>
              <input
                id="ugcAmount"
                type="number"
                step="0.01"
                value={ugcAmount}
                onChange={(event) => setUgcAmount(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="purchaseOtpSession">OTP session ID</label>
              <input
                id="purchaseOtpSession"
                type="text"
                value={purchaseOtpSession}
                onChange={(event) => setPurchaseOtpSession(event.target.value)}
              />
            </div>
            <div className="field">
              <label htmlFor="idempotencyKey">Idempotency key</label>
              <input
                id="idempotencyKey"
                type="text"
                value={idempotencyKey}
                onChange={(event) => setIdempotencyKey(event.target.value)}
              />
            </div>
            <div className="actions">
              <button className="primary" onClick={submitPurchase}>
                Submit purchase
              </button>
            </div>
          </>
        );
      case "review":
        return (
          <>
            <h2>Review</h2>
            <p className="hint">
              Validate the audit trail before running another transaction.
            </p>
            <div className="summary-grid">
              <div>
                <span className="summary-label">Country</span>
                <span className="summary-value">{country}</span>
              </div>
              <div>
                <span className="summary-label">Selected game</span>
                <span className="summary-value">
                  {selectedGame?.name || "None"}
                </span>
              </div>
              <div>
                <span className="summary-label">Points rate</span>
                <span className="summary-value">
                  {pointsRate?.pointsPerMgc ?? "—"}
                </span>
              </div>
              <div>
                <span className="summary-label">MGC rate</span>
                <span className="summary-value">
                  {mgcRate?.ugcPerMgc ?? "—"}
                </span>
              </div>
              <div>
                <span className="summary-label">OTP session</span>
                <span className="summary-value">
                  {otpSession ? "Ready" : "Missing"}
                </span>
              </div>
              <div>
                <span className="summary-label">Bank link</span>
                <span className="summary-value">
                  {linkOtpSession && userToken ? "Ready" : "Missing"}
                </span>
              </div>
            </div>
          </>
        );
      default:
        return null;
    }
  };

  return (
    <>
      <div className="ambient" />
      <header className="hero">
        <div>
          <p className="eyebrow">Bank Mock</p>
          <h1>MGX Bank Flow Studio</h1>
          <p className="sub">
            Walk through the private API flow step-by-step. Each stage builds the
            audit trail your bank integration expects.
          </p>
        </div>
        <div className="status-card">
          <span className="status-label">Step</span>
          <span className="status-value">
            {activeStep + 1} / {steps.length}
          </span>
        </div>
      </header>

      <main className="layout">
        <section className="card stepper">
          <div className="stepper-header">
            <h2>Flow steps</h2>
            <span className="hint">Complete each stage before moving on.</span>
          </div>
          <ol className="step-list">
            {steps.map((step, index) => {
              const isActive = index === activeStep;
              const isDone =
                (step.id === "setup" && hasConfig) ||
                (step.id === "discover" && hasGame) ||
                (step.id === "otp" && hasOtp) ||
                (step.id === "link" && hasBankLink) ||
                (step.id === "purchase" && hasPurchaseReady);
              return (
                <li key={step.id}>
                  <button
                    className={`step-button${isActive ? " active" : ""}`}
                    onClick={() => setActiveStep(index)}
                  >
                    <span className={`step-index${isDone ? " done" : ""}`}>
                      {isDone ? "✓" : index + 1}
                    </span>
                    <span className="step-copy">
                      <span className="step-title">{step.title}</span>
                      <span className="step-desc">{step.description}</span>
                    </span>
                  </button>
                </li>
              );
            })}
          </ol>
          <div className="stepper-footer">
            <button className="ghost" onClick={goBack} disabled={activeStep === 0}>
              Back
            </button>
            <button
              className="primary"
              onClick={goNext}
              disabled={activeStep === steps.length - 1}
            >
              Continue
            </button>
          </div>
        </section>

        <section className="card stage">
          <div className="stage-meta">
            <span className="stage-label">{steps[activeStep].title}</span>
            <span className="stage-status">
              {activeStep === 0 && (hasConfig ? "Configured" : "Pending")}
              {activeStep === 1 && (hasGame ? "Game selected" : "Pending")}
              {activeStep === 2 && (hasOtp ? "OTP ready" : "Pending")}
              {activeStep === 3 && (hasBankLink ? "Ready" : "Pending")}
              {activeStep === 4 && (hasPurchaseReady ? "Ready" : "Pending")}
              {activeStep === 5 && "Review"}
            </span>
          </div>
          {renderStep()}
        </section>

        <section className="card log">
          <h2>Response log</h2>
          <pre>{logText}</pre>
        </section>
      </main>

      <footer className="footer">MGX Bank Mock UI · Private API testing only</footer>
    </>
  );
}
