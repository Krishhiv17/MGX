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

export async function purchase(
  gameId: string,
  mgcAmount?: number,
  ugcAmount?: number,
  idempotencyKey?: string
) {
  const key = idempotencyKey ?? crypto.randomUUID();
  return apiFetch<PurchaseResponse>("/v1/purchases", {
    method: "POST",
    headers: { "Idempotency-Key": key },
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

export function parseApiError(error: unknown) {
  if (!error) {
    return "Purchase failed. Check balance and try again.";
  }
  if (error instanceof Error) {
    try {
      const parsed = JSON.parse(error.message);
      if (parsed && typeof parsed.message === "string") {
        return parsed.message;
      }
    } catch {
      // fall through
    }
    return error.message;
  }
  return "Purchase failed. Check balance and try again.";
}
