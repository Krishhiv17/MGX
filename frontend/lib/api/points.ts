import { apiFetch } from "./client";

export interface PointsBalance {
  userId: string;
  pointsAvailable: string;
}

export async function getPointsBalance() {
  return apiFetch<PointsBalance>("/v1/points/balance", { method: "GET" });
}
