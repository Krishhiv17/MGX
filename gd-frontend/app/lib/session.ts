export type Session = {
  baseUrl: string;
  token: string;
  role: "USER" | "DEVELOPER" | "ADMIN" | string;
  email: string;
  userId: string;
};

export const STORAGE_KEY = "mgx-portal-session";

export const loadSession = (): Session | null => {
  if (typeof window === "undefined") return null;
  const saved = localStorage.getItem(STORAGE_KEY);
  if (!saved) return null;
  try {
    return JSON.parse(saved) as Session;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
};

export const saveSession = (session: Session) => {
  if (typeof window === "undefined") return;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
};

export const clearSession = () => {
  if (typeof window === "undefined") return;
  localStorage.removeItem(STORAGE_KEY);
};
