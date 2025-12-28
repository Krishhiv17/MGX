import { apiFetch } from "./client";

export interface Game {
  id: string;
  developerId: string;
  developerName?: string | null;
  name: string;
  status: "ACTIVE" | "INACTIVE";
  settlementCurrency: string;
  createdAt: string;
}

export async function listGames() {
  return apiFetch<Game[]>("/v1/games", { method: "GET" });
}

export async function getGame(gameId: string) {
  return apiFetch<Game>(`/v1/games/${gameId}`, { method: "GET" });
}
