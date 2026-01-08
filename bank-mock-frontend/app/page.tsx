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

export default function BankMockPage() {
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

  return (
    <>
      <div className="ambient" />
      <header>
        <div>
          <p className="eyebrow">Bank Mock</p>
          <h1>MGX Private API Console</h1>
          <p className="sub">
            Simulate the bank experience: discover games, request OTPs, and submit
            private purchases.
          </p>
        </div>
        <div className="badge">Phase F</div>
      </header>

      <main>
        <section className="card">
          <h2>Connection</h2>
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
        </section>

        <section className="card">
          <h2>Discover games</h2>
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
          <div className="field">
            <label>Selected game</label>
            <div className="inline">
              {selectedGame ? `${selectedGame.name} (${selectedGame.id})` : "None"}
            </div>
          </div>
          <div className="inline">
            Points rate: {pointsRate ? pointsRate.pointsPerMgc : "-"}
          </div>
          <div className="inline">
            MGC rate: {mgcRate ? mgcRate.ugcPerMgc : "-"}
          </div>
        </section>

        <section className="card">
          <h2>OTP session</h2>
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
        </section>

        <section className="card">
          <h2>Bank link (testing only)</h2>
          <p className="small">
            This simulates the MGX user portal linking their bank account.
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
        </section>

        <section className="card">
          <h2>Purchase</h2>
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
        </section>

        <section className="card wide">
          <h2>Response log</h2>
          <pre>{logText}</pre>
        </section>
      </main>

      <footer className="footer">MGX Bank Mock UI · Private API testing only</footer>
    </>
  );
}
