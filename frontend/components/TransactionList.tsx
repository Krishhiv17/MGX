import { type PurchaseResponse } from "../lib/api/purchase";
import { type TopupResponse } from "../lib/api/topup";

export type TransactionItem =
  | ({ kind: "TOPUP" } & TopupResponse)
  | ({ kind: "PURCHASE" } & PurchaseResponse);

export default function TransactionList({ items }: { items: TransactionItem[] }) {
  if (!items.length) {
    return (
      <div className="rounded-2xl border border-dashed border-black/20 bg-white p-6 text-sm text-zinc-500 dark:border-white/20 dark:bg-zinc-950">
        No transactions yet.
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {items.map((item) => (
        <div
          key={`${item.kind}-${item.id}`}
          className="rounded-2xl border border-black/10 bg-white p-4 shadow-sm dark:border-white/10 dark:bg-zinc-950"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-zinc-900 dark:text-white">
                {item.kind === "TOPUP" ? "Top-up" : "Purchase"}
              </p>
              <p className="text-xs text-zinc-500">{new Date(item.createdAt).toLocaleString()}</p>
            </div>
            <span className="text-xs uppercase tracking-[0.2em] text-zinc-400">{item.status}</span>
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
        </div>
      ))}
    </div>
  );
}
