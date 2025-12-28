import { apiFetch, setToken } from "./client";
import type { AuthResponse, CurrentUserResponse, UserRole } from "./types";

export async function login(email: string, password: string): Promise<AuthResponse> {
  const response = await apiFetch<AuthResponse>("/v1/auth/login", {
    method: "POST",
    body: { email, password },
  });
  setToken(response.token);
  return response;
}

export async function register(
  email: string,
  password: string,
  role: UserRole
): Promise<AuthResponse> {
  const response = await apiFetch<AuthResponse>("/v1/auth/register", {
    method: "POST",
    body: { email, password, role },
  });
  setToken(response.token);
  return response;
}

export async function getCurrentUser(): Promise<CurrentUserResponse> {
  return apiFetch<CurrentUserResponse>("/v1/auth/me", { method: "GET" });
}

export function logout() {
  setToken(null);
}
