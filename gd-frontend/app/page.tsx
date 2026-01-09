"use client";

import Link from "next/link";
import styles from "./portal.module.css";

export default function LandingPage() {
  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.heroCard}>
          <span className={styles.tag}>Unified Portal</span>
          <h1>MGX gamer + developer workspace</h1>
          <p>
            Manage wallets, topups, and game purchases alongside developer game approvals
            and rate proposals. One portal, smart role awareness.
          </p>
          <div className={styles.actionRow}>
            <Link className={styles.primaryButton} href="/login">
              Sign in
            </Link>
            <Link className={styles.secondaryButton} href="/login">
              Create account
            </Link>
          </div>
          <div className={styles.chipRow}>
            <span className={styles.chip}>Country-isolated wallets</span>
            <span className={styles.chip}>Idempotent topups</span>
            <span className={styles.chip}>Audit-ready receipts</span>
          </div>
        </div>
        <div className={styles.heroCard}>
          <h2>Role-aware by design</h2>
          <p>
            Gamers see balances, purchases, and history. Developers see game
            creation, rate proposals, and settlement tooling. Your role decides
            the navigation automatically.
          </p>
          <div className={styles.grid}>
            <div className={styles.card}>
              <strong>Gamer suite</strong>
              <span className={styles.muted}>Wallets · Topups · Purchases</span>
            </div>
            <div className={styles.card}>
              <strong>Developer suite</strong>
              <span className={styles.muted}>Games · Rates · Settlements</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
