import { apiFetch } from "./client";

export interface TopupResponse {
  id: string;
  userId: string;
  pointsDebited: string;
  mgcCredited: string;
  ratePointsPerMgcSnapshot: string;
  status: "INITIATED" | "COMPLETED" | "FAILED";
  idempotencyKey: string;
  createdAt: string;
}

export async function topup(pointsAmount?: number, mgcAmount?: number) {
  const idempotencyKey = crypto.randomUUID();
  return apiFetch<TopupResponse>("/v1/topups", {
    method: "POST",
    headers: { "Idempotency-Key": idempotencyKey },
    body: {
      pointsAmount: pointsAmount ?? null,
      mgcAmount: mgcAmount ?? null,
    },
  });
}

export async function listTopups() {
  return apiFetch<TopupResponse[]>("/v1/topups", { method: "GET" });
}
