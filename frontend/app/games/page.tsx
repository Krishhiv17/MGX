"use client";

import { useEffect, useState } from "react";
import ProtectedRoute from "../../components/ProtectedRoute";
import GameCard from "../../components/GameCard";
import { listGames, type Game } from "../../lib/api/games";

export default function GamesPage() {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    listGames()
      .then((data) => {
        if (active) {
          setGames(data);
        }
      })
      .catch(() => {
        if (active) {
          setGames([]);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-transparent px-6 py-16">
        <div className="mx-auto w-full max-w-5xl space-y-8">
          <div className="rounded-3xl border border-black/10 bg-white p-8 shadow-sm dark:border-white/10 dark:bg-zinc-950">
            <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Games</p>
            <h1 className="mt-3 text-3xl font-semibold text-zinc-900 dark:text-white">
              Available titles
            </h1>
            <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
              Purchase UGC for any active game.
            </p>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            {loading ? (
              <div className="text-sm text-zinc-500">Loading games...</div>
            ) : games.length ? (
              games.map((game) => <GameCard key={game.id} game={game} />)
            ) : (
              <div className="rounded-2xl border border-dashed border-black/20 bg-white p-6 text-sm text-zinc-500 dark:border-white/20 dark:bg-zinc-950">
                No active games yet.
              </div>
            )}
          </div>
        </div>
      </main>
    </ProtectedRoute>
  );
}
