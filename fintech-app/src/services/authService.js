// src/services/authService.js

const SESSION_KEY = "fintech_session";

// default PoC creds
const DEFAULT_USER = "anhnv";
const DEFAULT_PASS = "123456";

export function getSession() {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export function isAuthenticated() {
  return !!getSession();
}

export async function login({ username, password, remember }) {
  // PoC: validate local
  if (username !== DEFAULT_USER || password !== DEFAULT_PASS) {
    const err = new Error("INVALID_CREDENTIALS");
    err.code = "INVALID_CREDENTIALS";
    throw err;
  }

  const session = {
    username,
    loggedInAt: Date.now(),
    remember: !!remember,
  };

  // PoC: remember = localStorage (đang dùng localStorage luôn)
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  return session;
}

export function logout() {
  localStorage.removeItem(SESSION_KEY);
}