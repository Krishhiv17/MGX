import type { Wallet } from "../lib/api/wallet";

export default function WalletCard({ wallet }: { wallet: Wallet }) {
  const label = wallet.type === "UGC" && wallet.gameName
    ? `UGC - ${wallet.gameName}`
    : wallet.type.replace("_", " ");

  return (
    <div className="rounded-2xl border border-black/10 bg-white p-5 shadow-sm dark:border-white/10 dark:bg-zinc-950">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-zinc-600 dark:text-zinc-400">{label}</p>
        <span className="text-xs uppercase tracking-[0.2em] text-zinc-400">Wallet</span>
      </div>
      <p className="mt-4 text-2xl font-semibold text-zinc-900 dark:text-white">
        {Number(wallet.balance).toFixed(2)}
      </p>
      {wallet.gameName ? (
        <p className="mt-1 text-xs text-zinc-500">Game ID: {wallet.gameId}</p>
      ) : null}
    </div>
  );
}
