"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import ProtectedRoute from "../../../../components/ProtectedRoute";
import PurchaseForm from "../../../../components/PurchaseForm";
import { getGame, type Game } from "../../../../lib/api/games";

export default function PurchasePage() {
  const params = useParams();
  const gameId = params?.gameId as string;
  const [game, setGame] = useState<Game | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!gameId) {
      return;
    }
    let active = true;
    getGame(gameId)
      .then((data) => {
        if (active) {
          setGame(data);
        }
      })
      .catch(() => {
        if (active) {
          setGame(null);
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
  }, [gameId]);

  return (
    <ProtectedRoute>
      <main className="min-h-screen bg-transparent px-6 py-16">
        <div className="mx-auto w-full max-w-3xl rounded-3xl border border-black/10 bg-white p-8 shadow-sm dark:border-white/10 dark:bg-zinc-950">
          {loading ? (
            <p className="text-sm text-zinc-500">Loading game...</p>
          ) : game ? (
            <>
              <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Purchase</p>
              <h1 className="mt-3 text-3xl font-semibold text-zinc-900 dark:text-white">
                {game.name}
              </h1>
              <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
                Developer: {game.developerName ?? game.developerId}
              </p>
              <div className="mt-8">
                <PurchaseForm gameId={game.id} />
              </div>
            </>
          ) : (
            <p className="text-sm text-zinc-500">Game not found.</p>
          )}
        </div>
      </main>
    </ProtectedRoute>
  );
}
