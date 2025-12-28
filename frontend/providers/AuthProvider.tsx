"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import AuthContext from "../contexts/AuthContext";
import { getCurrentUser, login, logout, register } from "../lib/api/auth";
import { getToken, setToken } from "../lib/api/client";
import type { AuthResponse, CurrentUserResponse, UserRole } from "../lib/api/types";

export default function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<CurrentUserResponse | null>(null);
  const [token, setAuthToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const loadUser = useCallback(async () => {
    const storedToken = getToken();
    if (!storedToken) {
      setLoading(false);
      return;
    }

    setAuthToken(storedToken);
    try {
      const me = await getCurrentUser();
      setUser(me);
    } catch (error) {
      setToken(null);
      setAuthToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadUser();
  }, [loadUser]);

  useEffect(() => {
    const handleLogoutEvent = () => {
      setAuthToken(null);
      setUser(null);
    };

    window.addEventListener("mgx:auth:logout", handleLogoutEvent);
    return () => window.removeEventListener("mgx:auth:logout", handleLogoutEvent);
  }, []);

  const handleLogin = useCallback(async (email: string, password: string) => {
    const response: AuthResponse = await login(email, password);
    setAuthToken(response.token);
    setUser({ userId: response.userId, email: response.email, role: response.role });
    return response;
  }, []);

  const handleRegister = useCallback(
    async (email: string, password: string, role: UserRole) => {
      const response: AuthResponse = await register(email, password, role);
      setAuthToken(response.token);
      setUser({ userId: response.userId, email: response.email, role: response.role });
      return response;
    },
    []
  );

  const handleLogout = useCallback(() => {
    logout();
    setAuthToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      token,
      loading,
      login: handleLogin,
      register: handleRegister,
      logout: handleLogout,
    }),
    [user, token, loading, handleLogin, handleRegister, handleLogout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
