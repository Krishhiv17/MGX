import Link from "next/link";
import type { Game } from "../lib/api/games";

export default function GameCard({ game }: { game: Game }) {
  return (
    <div className="rounded-2xl border border-black/10 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-zinc-950">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-zinc-900 dark:text-white">{game.name}</h3>
        <span className="rounded-full border border-black/10 px-3 py-1 text-xs uppercase tracking-[0.2em] text-zinc-500 dark:border-white/10">
          {game.status}
        </span>
      </div>
      <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-400">
        Developer: {game.developerName ?? game.developerId}
      </p>
      <div className="mt-4 flex items-center justify-between">
        <span className="text-xs uppercase tracking-[0.2em] text-zinc-400">{game.settlementCurrency}</span>
        <Link
          className="rounded-full border border-black/20 px-4 py-1.5 text-xs font-semibold transition hover:bg-black hover:text-white dark:border-white/20 dark:hover:bg-white dark:hover:text-black"
          href={`/games/${game.id}/purchase`}
        >
          Purchase
        </Link>
      </div>
    </div>
  );
}
