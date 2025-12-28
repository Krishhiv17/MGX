export default function Home() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-transparent px-6 py-24">
      <div className="w-full max-w-3xl rounded-3xl border border-black/10 bg-white p-10 shadow-sm dark:border-white/10 dark:bg-zinc-950">
        <p className="text-xs uppercase tracking-[0.2em] text-zinc-500">Mastercard Game Exchange</p>
        <h1 className="mt-4 text-3xl font-semibold text-zinc-900 dark:text-white">
          MGX demo environment
        </h1>
        <p className="mt-3 max-w-xl text-base text-zinc-600 dark:text-zinc-400">
          Use the auth pages to sign in, then continue testing wallets, top-ups, purchases, and
          settlement.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <a
            className="rounded-full bg-black px-5 py-2 text-sm font-semibold text-white transition hover:bg-zinc-800"
            href="/login"
          >
            Login
          </a>
          <a
            className="rounded-full border border-black/20 px-5 py-2 text-sm font-semibold text-black transition hover:bg-black hover:text-white dark:border-white/20 dark:text-white dark:hover:bg-white dark:hover:text-black"
            href="/register"
          >
            Register
          </a>
          <a
            className="rounded-full border border-black/20 px-5 py-2 text-sm font-semibold text-black transition hover:bg-black hover:text-white dark:border-white/20 dark:text-white dark:hover:bg-white dark:hover:text-black"
            href="/dashboard"
          >
            Dashboard
          </a>
        </div>
      </div>
    </main>
  );
}
