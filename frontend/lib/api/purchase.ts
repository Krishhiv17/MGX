import { apiFetch } from "./client";

export interface PurchaseResponse {
  id: string;
  userId: string;
  gameId: string;
  mgcSpent: string;
  ugcCredited: string;
  rateUgcPerMgcSnapshot: string;
  status: "INITIATED" | "COMPLETED" | "FAILED";
  idempotencyKey: string;
  createdAt: string;
}

export async function purchase(gameId: string, mgcAmount?: number, ugcAmount?: number) {
  const idempotencyKey = crypto.randomUUID();
  return apiFetch<PurchaseResponse>("/v1/purchases", {
    method: "POST",
    headers: { "Idempotency-Key": idempotencyKey },
    body: {
      gameId,
      mgcAmount: mgcAmount ?? null,
      ugcAmount: ugcAmount ?? null,
    },
  });
}

export async function listPurchases() {
  return apiFetch<PurchaseResponse[]>("/v1/purchases", { method: "GET" });
}
