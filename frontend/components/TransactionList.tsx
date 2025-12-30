import { useMemo, useState } from "react";
import { type PurchaseResponse } from "../lib/api/purchase";
import { type TopupResponse } from "../lib/api/topup";

export type TransactionItem =
  | ({ kind: "TOPUP" } & TopupResponse)
  | ({ kind: "PURCHASE" } & PurchaseResponse);

export default function TransactionList({
  items,
  gameNameById,
}: {
  items: TransactionItem[];
  gameNameById: Record<string, string>;
}) {
  const [openId, setOpenId] = useState<string | null>(null);

  const sorted = useMemo(
    () => [...items].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()),
    [items]
  );

  if (!items.length) {
    return (
      <div className="rounded-2xl border border-dashed border-black/20 bg-white p-6 text-sm text-zinc-500 dark:border-white/20 dark:bg-zinc-950">
        No transactions yet.
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {sorted.map((item) => {
        const isOpen = openId === item.id;
        const gameName =
          item.kind === "PURCHASE" ? gameNameById[item.gameId] ?? "Unknown game" : null;
        return (
        <div
          key={`${item.kind}-${item.id}`}
          className="rounded-2xl border border-black/10 bg-white p-4 shadow-sm dark:border-white/10 dark:bg-zinc-950"
        >
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-sm font-semibold text-zinc-900 dark:text-white">
                {item.kind === "TOPUP" ? "Top-up" : "Purchase"}
              </p>
              {item.kind === "PURCHASE" ? (
                <p className="text-xs text-zinc-500">Game: {gameName}</p>
              ) : null}
              <p className="text-xs text-zinc-500">{new Date(item.createdAt).toLocaleString()}</p>
            </div>
            <div className="flex items-center gap-3">
              <span className="text-xs uppercase tracking-[0.2em] text-zinc-400">{item.status}</span>
              <button
                className="rounded-full border border-black/20 px-3 py-1 text-xs font-semibold text-black transition hover:bg-black hover:text-white dark:border-white/20 dark:text-white dark:hover:bg-white dark:hover:text-black"
                type="button"
                onClick={() => setOpenId(isOpen ? null : item.id)}
              >
                {isOpen ? "Hide details" : "View details"}
              </button>
            </div>
          </div>
          <div className="mt-3 text-sm text-zinc-600 dark:text-zinc-400">
            {item.kind === "TOPUP" ? (
              <p>
                Points debited: {item.pointsDebited} • MGC credited: {item.mgcCredited}
              </p>
            ) : (
              <p>
                MGC spent: {item.mgcSpent} • UGC credited: {item.ugcCredited}
              </p>
            )}
          </div>
          {isOpen ? (
            <div className="mt-4 rounded-xl border border-black/5 bg-zinc-50 p-4 text-xs text-zinc-600 dark:border-white/10 dark:bg-black dark:text-zinc-400">
              {item.kind === "TOPUP" ? (
                <div className="grid gap-2">
                  <div>Transaction ID: {item.id}</div>
                  <div>User ID: {item.userId}</div>
                  <div>Status: {item.status}</div>
                  <div>Points debited: {item.pointsDebited}</div>
                  <div>MGC credited: {item.mgcCredited}</div>
                  <div>Rate snapshot: {item.ratePointsPerMgcSnapshot} points/MGC</div>
                  <div>Rate ID: {item.rateId ?? "N/A"}</div>
                  <div>Idempotency key: {item.idempotencyKey}</div>
                  <div>Created at: {new Date(item.createdAt).toLocaleString()}</div>
                </div>
              ) : (
                <div className="grid gap-2">
                  <div>Transaction ID: {item.id}</div>
                  <div>User ID: {item.userId}</div>
                  <div>Game ID: {item.gameId}</div>
                  <div>Game: {gameName}</div>
                  <div>Status: {item.status}</div>
                  <div>MGC spent: {item.mgcSpent}</div>
                  <div>UGC credited: {item.ugcCredited}</div>
                  <div>Rate snapshot: {item.rateUgcPerMgcSnapshot} UGC/MGC</div>
                  <div>Rate ID: {item.rateId ?? "N/A"}</div>
                  <div>Idempotency key: {item.idempotencyKey}</div>
                  <div>Created at: {new Date(item.createdAt).toLocaleString()}</div>
                </div>
              )}
            </div>
          ) : null}
        </div>
      );
      })}
    </div>
  );
}
