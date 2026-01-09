"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import styles from "../portal.module.css";
import { apiFetch } from "../lib/api";
import { clearSession, loadSession, type Session } from "../lib/session";

type Wallet = {
  id: string;
  type: string;
  gameId?: string | null;
  gameName?: string | null;
  balance: string;
};

type Game = {
  id: string;
  name: string;
  developerName?: string | null;
  allowedCountries?: string[] | null;
};

type Profile = {
  userId: string;
  email: string;
  role: string;
  phoneNumber?: string | null;
  countryCode?: string | null;
};

type Rate = {
  id: string;
  ugcPerMgc: number;
  status: string;
};

type Topup = {
  id: string;
  pointsDebited: number;
  mgcCredited: number;
  status: string;
  createdAt: string;
};

type Purchase = {
  id: string;
  gameId: string;
  mgcSpent: number;
  ugcCredited: number;
  status: string;
  createdAt: string;
};

type PointsBalance = {
  pointsAvailable: number;
};

const makeKey = (prefix: string) => {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return `${prefix}-${crypto.randomUUID()}`;
  }
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
};

export default function GamerDashboard() {
  const router = useRouter();
  const [session, setSession] = useState<Session | null>(null);
  const [profile, setProfile] = useState<Profile | null>(null);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [games, setGames] = useState<Game[]>([]);
  const [topups, setTopups] = useState<Topup[]>([]);
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [pointsBalance, setPointsBalance] = useState<PointsBalance | null>(null);
  const [rate, setRate] = useState<Rate | null>(null);
  const [rateError, setRateError] = useState("");
  const [statusMessage, setStatusMessage] = useState("");
  const [countryInput, setCountryInput] = useState("IN");

  const [topupPoints, setTopupPoints] = useState("1000");
  const [purchaseGameId, setPurchaseGameId] = useState("");
  const [purchaseMgc, setPurchaseMgc] = useState("1");

  useEffect(() => {
    const stored = loadSession();
    if (!stored) {
      router.replace("/login");
      return;
    }
    if (stored.role === "DEVELOPER") {
      router.replace("/developer");
      return;
    }
    setSession(stored);
  }, [router]);

  const loadDashboard = async (activeSession: Session) => {
    try {
      const [profilePayload, walletPayload, gamePayload, topupPayload, purchasePayload, pointsPayload] =
        await Promise.all([
          apiFetch(activeSession, "/v1/profile"),
          apiFetch(activeSession, "/v1/wallets"),
          apiFetch(activeSession, "/v1/games"),
          apiFetch(activeSession, "/v1/topups"),
          apiFetch(activeSession, "/v1/purchases"),
          apiFetch(activeSession, "/v1/points/balance"),
        ]);
      setProfile(profilePayload as Profile);
      setCountryInput(profilePayload.countryCode || "IN");
      setWallets(walletPayload as Wallet[]);
      setGames(gamePayload as Game[]);
      setTopups(topupPayload as Topup[]);
      setPurchases(purchasePayload as Purchase[]);
      setPointsBalance(pointsPayload as PointsBalance);
    } catch (error) {
      setStatusMessage(`Load error: ${String(error)}`);
    }
  };

  useEffect(() => {
    if (!session) return;
    loadDashboard(session);
  }, [session]);

  const refreshRate = async (gameId: string) => {
    if (!session || !gameId) return;
    setRateError("");
    try {
      const payload = await apiFetch(session, `/v1/rates/games/${gameId}/mgc-ugc`);
      setRate(payload as Rate);
    } catch (error) {
      setRate(null);
      setRateError(String(error));
    }
  };

  const handleTopup = async () => {
    if (!session) return;
    setStatusMessage("");
    try {
      const payload = await apiFetch(session, "/v1/topups", {
        method: "POST",
        headers: { "Idempotency-Key": makeKey("topup") },
        body: JSON.stringify({ pointsAmount: Number(topupPoints) }),
      });
      setStatusMessage(`Topup completed: ${payload.mgcCredited} MGC`);
      loadDashboard(session);
    } catch (error) {
      setStatusMessage(`Topup failed: ${String(error)}`);
    }
  };

  const handlePurchase = async () => {
    if (!session) return;
    setStatusMessage("");
    try {
      const payload = await apiFetch(session, "/v1/purchases", {
        method: "POST",
        headers: { "Idempotency-Key": makeKey("purchase") },
        body: JSON.stringify({ gameId: purchaseGameId, mgcAmount: Number(purchaseMgc) }),
      });
      setStatusMessage(`Purchased ${payload.ugcCredited} UGC for ${payload.mgcSpent} MGC`);
      loadDashboard(session);
    } catch (error) {
      setStatusMessage(`Purchase failed: ${String(error)}`);
    }
  };

  const handleCountryUpdate = async () => {
    if (!session) return;
    setStatusMessage("");
    try {
      const payload = await apiFetch(session, "/v1/profile", {
        method: "PUT",
        body: JSON.stringify({ countryCode: countryInput }),
      });
      setProfile(payload as Profile);
      setStatusMessage("Location updated.");
      loadDashboard(session);
    } catch (error) {
      setStatusMessage(`Update failed: ${String(error)}`);
    }
  };

  const signOut = () => {
    clearSession();
    router.replace("/login");
  };

  const mgcWallet = useMemo(
    () => wallets.find((wallet) => wallet.type === "MGC"),
    [wallets]
  );

  const rewardWallet = useMemo(
    () => wallets.find((wallet) => wallet.type === "REWARD_POINTS"),
    [wallets]
  );

  return (
    <div className={styles.page}>
      <section className={styles.heroCard}>
        <div className={styles.sectionHeader}>
          <div>
            <span className={styles.tag}>Gamer dashboard</span>
            <h2>Your MGX account</h2>
            <p>
              {profile?.email} · Country {profile?.countryCode || "—"}
            </p>
          </div>
          <button className={styles.secondaryButton} onClick={signOut}>
            Sign out
          </button>
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Balances</h2>
          <p>Live wallets scoped to your current country.</p>
        </div>
        <div className={styles.grid}>
          <div className={styles.card}>
            <span className={styles.muted}>Reward points wallet</span>
            <div className={styles.cardTitle}>{rewardWallet?.balance ?? "0.00"}</div>
            <span className={styles.muted}>Bank available: {pointsBalance?.pointsAvailable ?? "—"}</span>
          </div>
          <div className={styles.card}>
            <span className={styles.muted}>MGC wallet</span>
            <div className={styles.cardTitle}>{mgcWallet?.balance ?? "0.00"}</div>
            <span className={styles.muted}>Country scoped</span>
          </div>
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Top up</h2>
          <p>Convert reward points into MGC.</p>
        </div>
        <div className={styles.formGrid}>
          <label>
            Points amount
            <input value={topupPoints} onChange={(event) => setTopupPoints(event.target.value)} />
          </label>
          <button className={styles.primaryButton} onClick={handleTopup}>
            Submit topup
          </button>
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Purchase UGC</h2>
          <p>Choose a game and spend MGC for UGC.</p>
        </div>
        <div className={styles.formGrid}>
          <label>
            Game
            <select
              value={purchaseGameId}
              onChange={(event) => {
                setPurchaseGameId(event.target.value);
                refreshRate(event.target.value);
              }}
            >
              <option value="">Select a game</option>
              {games.map((game) => (
                <option key={game.id} value={game.id}>
                  {game.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            MGC amount
            <input value={purchaseMgc} onChange={(event) => setPurchaseMgc(event.target.value)} />
          </label>
          <button className={styles.primaryButton} onClick={handlePurchase} disabled={!purchaseGameId}>
            Purchase
          </button>
        </div>
        {rate ? (
          <p className={styles.muted}>
            Rate: 1 MGC = {rate.ugcPerMgc} UGC · Estimated UGC:{" "}
            {Number(purchaseMgc || 0) * rate.ugcPerMgc}
          </p>
        ) : null}
        {rateError ? <p className={styles.error}>{rateError}</p> : null}
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Location</h2>
          <p>Switch your active country (wallets remain isolated).</p>
        </div>
        <div className={styles.formGrid}>
          <label>
            Country code
            <input value={countryInput} onChange={(event) => setCountryInput(event.target.value)} />
          </label>
          <button className={styles.secondaryButton} onClick={handleCountryUpdate}>
            Update location
          </button>
        </div>
      </section>

      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2>Recent activity</h2>
          <p>Topups and purchases linked to your account.</p>
        </div>
        <div className={styles.table}>
          {topups.map((topup) => (
            <div key={topup.id} className={styles.tableRow}>
              <div>Topup</div>
              <div>{topup.pointsDebited} points</div>
              <div>+{topup.mgcCredited} MGC</div>
              <div>{topup.status}</div>
            </div>
          ))}
          {purchases.map((purchase) => (
            <div key={purchase.id} className={styles.tableRow}>
              <div>Purchase</div>
              <div>-{purchase.mgcSpent} MGC</div>
              <div>+{purchase.ugcCredited} UGC</div>
              <div>{purchase.status}</div>
            </div>
          ))}
        </div>
      </section>

      {statusMessage ? <p className={styles.muted}>{statusMessage}</p> : null}
    </div>
  );
}
