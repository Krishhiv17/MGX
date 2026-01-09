import type { Session } from "./session";

export async function apiFetch(
  session: Session,
  path: string,
  options: RequestInit = {}
) {
  const headers = new Headers(options.headers || {});
  if (!headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (session.token) {
    headers.set("Authorization", `Bearer ${session.token}`);
  }

  const response = await fetch(`${session.baseUrl}${path}`, {
    ...options,
    headers,
  });
  const isJson = response.headers.get("content-type")?.includes("application/json");
  const payload = isJson ? await response.json() : await response.text();
  if (!response.ok) {
    const message =
      typeof payload === "string" ? payload : payload?.message || "Request failed";
    throw new Error(message);
  }
  return payload;
}
