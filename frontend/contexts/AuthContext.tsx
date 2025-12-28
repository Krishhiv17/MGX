"use client";

import { createContext, useContext } from "react";
import type { AuthResponse, CurrentUserResponse, UserRole } from "../lib/api/types";

export interface AuthContextValue {
  user: CurrentUserResponse | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<AuthResponse>;
  register: (email: string, password: string, role: UserRole) => Promise<AuthResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

export default AuthContext;
