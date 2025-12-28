import { apiFetch } from "./client";

export interface Wallet {
  id: string;
  type: "REWARD_POINTS" | "MGC" | "UGC";
  gameId: string | null;
  gameName?: string | null;
  balance: string;
  createdAt: string;
}

export async function getWallets() {
  return apiFetch<Wallet[]>("/v1/wallets", { method: "GET" });
}
