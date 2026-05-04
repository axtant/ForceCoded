"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { getUsername, removeToken, saveToken } from "@/lib/auth";

interface AuthContextType {
  username: string | null;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
  username: null,
  login: () => {},
  logout: () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [username, setUsername] = useState<string | null>(null);

  useEffect(() => {
    setUsername(getUsername());
  }, []);

  function login(token: string) {
    saveToken(token);
    setUsername(getUsername());
  }

  function logout() {
    removeToken();
    setUsername(null);
  }

  return (
    <AuthContext.Provider value={{ username, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
