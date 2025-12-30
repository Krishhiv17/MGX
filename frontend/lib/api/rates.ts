import { apiFetch } from "./client";

export interface MgcUgcRate {
  id: string;
  gameId: string;
  ugcPerMgc: string;
  activeFrom: string;
  activeTo: string | null;
  createdBy: string | null;
  createdAt: string;
}

export async function getMgcUgcRate(gameId: string) {
  return apiFetch<MgcUgcRate>(`/v1/rates/games/${gameId}/mgc-ugc`, { method: "GET" });
}
