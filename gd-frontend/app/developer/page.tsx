"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import styles from "../portal.module.css";
import { apiFetch } from "../lib/api";
import { clearSession, loadSession, type Session } from "../lib/session";

type Game = {
  id: string;
  developerId: string;
  developerName?: string | null;
  name: string;
  status: string;
  settlementCurrency: string;
  allowedCountries?: string[] | null;
  rejectionReason?: string | null;
};

type Rate = {
  id: string;
  gameId: string;
  ugcPerMgc: number;
  status: string;
  rejectionReason?: string | null;
  createdAt?: string | null;
};

type Receivable = {
  id: string;
  status: string;
  gameId: string;
  amountDue: number;
  settlementCurrency: string;
};

type Settlement = {
  id: string;
  developerId: string;
  status: string;
  requestedAt?: string | null;
};

type DeveloperProfile = {
  id: string;
  userId?: string | null;
  name: string;
  settlementCurrency: string;
  bankAccountRef: string;
  status: string;
  rejectionReason?: string | null;
};

export default function DeveloperPortal() {
  const router = useRouter();
  const [session, setSession] = useState<Session | null>(null);
  const [profile, setProfile] = useState<DeveloperProfile | null>(null);
  const [games, setGames] = useState<Game[]>([]);
  const [rates, setRates] = useState<Rate[]>([]);
  const [receivables, setReceivables] = useState<Receivable[]>([]);
  const [settlements, setSettlements] = useState<Settlement[]>([]);
  const [statusMessage, setStatusMessage] = useState("");

  const [studioName, setStudioName] = useState("Portal Dev Studio");
  const [bankAccountRef, setBankAccountRef] = useState("BANK-DEV-REQUEST");
  const [gameName, setGameName] = useState("New Game");
  const [settlementCurrency, setSettlementCurrency] = useState("USD");
  const [allowedCountries, setAllowedCountries] = useState("IN,US");

  const [rateGameId, setRateGameId] = useState("");
  const [ugcPerMgc, setUgcPerMgc] = useState("75");

  useEffect(() => {
    const stored = loadSession();
    if (!stored) {
      router.replace("/login");
      return;
    }
    if (stored.role !== "DEVELOPER") {
      router.replace("/dashboard");
      return;
    }
    setSession(stored);
  }, [router]);

  const developerId = useMemo(
    () => profile?.id || games[0]?.developerId || "",
    [profile, games]
  );
  const activeGames = useMemo(() => games.filter((game) => game.status === "ACTIVE"), [games]);
  const isActive = profile?.status === "ACTIVE";
  const needsProfile = profile === null || profile.status === "REJECTED";

  const loadDeveloperData = async (activeSession: Session) => {
    try {
      let profilePayload: DeveloperProfile | null = null;
      try {
        profilePayload = (await apiFetch(activeSession, "/v1/developer/profile")) as DeveloperProfile;
      } catch (_) {
        profilePayload = null;
      }

      setProfile(profilePayload);
      if (!profilePayload || profilePayload.status === "REJECTED") {
        setGames([]);
        setRates([]);
        return;
      }

      const [gamePayload, ratePayload] = await Promise.all([
        apiFetch(activeSession, "/v1/developer/games"),
        apiFetch(activeSession, "/v1/developer/rates/mgc-ugc"),
      ]);
      setGames(gamePayload as Game[]);
      setRates(ratePayload as Rate[]);
    } catch (error) {
      setStatusMessage(`Load error: ${String(error)}`);
    }
  };

  useEffect(() => {
    if (!session) return;
    loadDeveloperData(session);
  }, [session]);

  const handleCreateGame = async () => {
    if (!session) return;
    if (!isActive) {
      setStatusMessage("Developer profile must be approved before creating games.");
      return;
    }
    setStatusMessage("");
    try {
      const payload = await apiFetch(session, "/v1/developer/games", {
        method: "POST",
        body: JSON.stringify({
          name: gameName,
          settlementCurrency,
          allowedCountries: allowedCountries
            .split(",")
            .map((country) => country.trim().toUpperCase())
            .filter(Boolean),
        }),
      });
      setStatusMessage(`Game submitted: ${payload.name}`);
      loadDeveloperData(session);
    } catch (error) {
      setStatusMessage(`Game creation failed: ${String(error)}`);
    }
  };

  const handleProposeRate = async () => {
    if (!session) return;
    if (!isActive) {
      setStatusMessage("Developer profile must be approved before proposing rates.");
      return;
    }
    setStatusMessage("");
    try {
      const payload = await apiFetch(session, "/v1/developer/rates/mgc-ugc", {
        method: "POST",
        body: JSON.stringify({
          gameId: rateGameId,
          ugcPerMgc: Number(ugcPerMgc),
        }),
      });
      setStatusMessage(`Rate proposed: ${payload.ugcPerMgc} UGC per MGC`);
      loadDeveloperData(session);
    } catch (error) {
      setStatusMessage(`Rate proposal failed: ${String(error)}`);
    }
  };

  const handleSettlementRequest = async () => {
    if (!session) return;
    if (!isActive) {
      setStatusMessage("Developer profile must be approved before requesting settlement.");
      return;
    }
    setStatusMessage("");
    try {
      const payload = await apiFetch(session, "/v1/developer/settlements/request", {
        method: "POST",
        body: JSON.stringify({}),
      });
      setStatusMessage(`Settlement requested: ${payload.status}`);
    } catch (error) {
      setStatusMessage(`Settlement request failed: ${String(error)}`);
    }
  };

  const loadReceivables = async () => {
    if (!session || !developerId) {
      setStatusMessage("Create a game first to load receivables.");
      return;
    }
    try {
      const payload = await apiFetch(
        session,
        `/v1/developer/receivables?developerId=${developerId}`
      );
      setReceivables(payload as Receivable[]);
    } catch (error) {
      setStatusMessage(`Receivables load failed: ${String(error)}`);
    }
  };

  const loadSettlements = async () => {
    if (!session || !developerId) {
      setStatusMessage("Create a game first to load settlement history.");
      return;
    }
    try {
      const payload = await apiFetch(
        session,
        `/v1/developer/settlements?developerId=${developerId}`
      );
      setSettlements(payload as Settlement[]);
    } catch (error) {
      setStatusMessage(`Settlement history failed: ${String(error)}`);
    }
  };

  const signOut = () => {
    clearSession();
    router.replace("/login");
  };

  const requestProfile = async () => {
    if (!session) return;
    setStatusMessage("");
    try {
      const payload = await apiFetch(session, "/v1/developer/profile/request", {
        method: "POST",
        body: JSON.stringify({
          name: studioName,
          settlementCurrency,
          bankAccountRef,
        }),
      });
      setProfile(payload as DeveloperProfile);
      setStatusMessage("Developer profile submitted for approval.");
    } catch (error) {
      setStatusMessage(`Profile request failed: ${String(error)}`);
    }
  };

  return (
    <div className={styles.page}>
      <section className={styles.heroCard}>
        <div className={styles.sectionHeader}>
          <div>
            <span className={styles.tag}>Developer workspace</span>
            <h2>Game operations</h2>
            <p>Submit games, propose rates, and request settlement payouts.</p>
          </div>
          <button className={styles.secondaryButton} onClick={signOut}>
            Sign out
          </button>
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Developer profile</h2>
          <p>Onboarding is admin‑approved before you can publish games.</p>
        </div>
        {needsProfile ? (
          <>
            {profile?.rejectionReason ? (
              <p className={styles.error}>Rejected: {profile.rejectionReason}</p>
            ) : null}
            <div className={styles.formGrid}>
              <label>
                Studio name
                <input value={studioName} onChange={(event) => setStudioName(event.target.value)} />
              </label>
              <label>
                Settlement currency
                <input
                  value={settlementCurrency}
                  onChange={(event) => setSettlementCurrency(event.target.value.toUpperCase())}
                />
              </label>
              <label>
                Bank account ref
                <input
                  value={bankAccountRef}
                  onChange={(event) => setBankAccountRef(event.target.value)}
                />
              </label>
            </div>
            <button className={styles.primaryButton} onClick={requestProfile}>
              Submit developer profile
            </button>
          </>
        ) : (
          <div className={styles.card}>
            <div className={styles.cardTitle}>{profile?.name}</div>
            <span className={styles.muted}>
              {profile?.settlementCurrency} · {profile?.bankAccountRef}
            </span>
            <span className={styles.muted}>Status: {profile?.status}</span>
            {profile?.rejectionReason ? (
              <span className={styles.error}>Rejected: {profile.rejectionReason}</span>
            ) : null}
          </div>
        )}
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Game requests</h2>
          <p>Create a new game and define allowed countries.</p>
        </div>
        <div className={styles.formGrid}>
          <label>
            Game name
            <input value={gameName} onChange={(event) => setGameName(event.target.value)} />
          </label>
          <label>
            Settlement currency
            <input
              value={settlementCurrency}
              onChange={(event) => setSettlementCurrency(event.target.value.toUpperCase())}
            />
          </label>
          <label>
            Allowed countries
            <input
              value={allowedCountries}
              onChange={(event) => setAllowedCountries(event.target.value)}
            />
          </label>
        </div>
        <button className={styles.primaryButton} onClick={handleCreateGame} disabled={!isActive}>
          Submit game request
        </button>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Your games</h2>
          <p>Track approvals and rejection reasons.</p>
        </div>
        <div className={styles.grid}>
          {games.map((game) => (
            <div key={game.id} className={styles.card}>
              <div className={styles.cardTitle}>{game.name}</div>
              <span className={styles.muted}>Status: {game.status}</span>
              <span className={styles.muted}>
                Allowed: {(game.allowedCountries || []).join(", ") || "—"}
              </span>
              {game.rejectionReason ? (
                <span className={styles.error}>Rejected: {game.rejectionReason}</span>
              ) : null}
            </div>
          ))}
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Propose MGC → UGC rate</h2>
          <p>Submit new pricing for a game. Requires admin approval.</p>
        </div>
        <div className={styles.formGrid}>
          <label>
            Game
            <select value={rateGameId} onChange={(event) => setRateGameId(event.target.value)}>
              <option value="">Select game</option>
              {activeGames.length === 0 ? (
                <option value="" disabled>
                  No approved games yet
                </option>
              ) : null}
              {activeGames.map((game) => (
                <option key={game.id} value={game.id}>
                  {game.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            UGC per MGC
            <input value={ugcPerMgc} onChange={(event) => setUgcPerMgc(event.target.value)} />
          </label>
          <button
            className={styles.primaryButton}
            onClick={handleProposeRate}
            disabled={!rateGameId || !isActive}
          >
            Submit rate proposal
          </button>
        </div>
        <div className={styles.table}>
          {rates.map((rate) => (
            <div key={rate.id} className={styles.tableRow}>
              <div>{rate.gameId}</div>
              <div>{rate.ugcPerMgc} UGC/MGC</div>
              <div>{rate.status}</div>
              <div>{rate.rejectionReason || "—"}</div>
            </div>
          ))}
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Settlement</h2>
          <p>Request payout batches and review receivables.</p>
        </div>
        <div className={styles.actionRow}>
          <button className={styles.primaryButton} onClick={handleSettlementRequest}>
            Request settlement
          </button>
          <button className={styles.secondaryButton} onClick={loadReceivables}>
            Load receivables
          </button>
          <button className={styles.secondaryButton} onClick={loadSettlements}>
            Load settlement history
          </button>
        </div>
        <div className={styles.grid}>
          {receivables.map((rec) => (
            <div key={rec.id} className={styles.card}>
              <div className={styles.cardTitle}>{rec.status}</div>
              <span className={styles.muted}>{rec.amountDue} {rec.settlementCurrency}</span>
              <span className={styles.muted}>Game: {rec.gameId}</span>
            </div>
          ))}
        </div>
        <div className={styles.table}>
          {settlements.map((batch) => (
            <div key={batch.id} className={styles.tableRow}>
              <div>{batch.id}</div>
              <div>{batch.status}</div>
              <div>{batch.developerId}</div>
              <div>{batch.requestedAt || "—"}</div>
            </div>
          ))}
        </div>
      </section>

      {statusMessage ? <p className={styles.muted}>{statusMessage}</p> : null}
    </div>
  );
}
