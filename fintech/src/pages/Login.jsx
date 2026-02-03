import React from "react";
import { useNavigate } from "react-router-dom";
import { loginLocal } from "../auth/localAuth";

export default function Login() {
  const nav = useNavigate();

const onSubmit = (e) => {
  e.preventDefault();
  const u = e.currentTarget.username.value.trim();
  const p = e.currentTarget.password.value;

  const session = loginLocal(u, p);
  if (!session) return alert("Sai username/password");

  nav("/dashboard");
};

  return (
    <div className="login-page">
      <main className="login-card" role="main" aria-label="Fintech login">
        <h1 className="login-title">WELCOME BACK</h1>

        <form onSubmit={onSubmit}>
          <div className="login-field">
            <input name="username" className="login-input" type="text" placeholder="Username" autoComplete="username"  />
            <span className="login-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path d="M12 12a4.5 4.5 0 1 0-4.5-4.5A4.5 4.5 0 0 0 12 12Zm0 2c-4.1 0-7.5 2.2-7.5 5v1h15v-1c0-2.8-3.4-5-7.5-5Z"/>
              </svg>
            </span>
          </div>

          <div className="login-field">
            <input name="password" className="login-input" type="password" placeholder="Password" autoComplete="current-password" />
            <span className="login-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path d="M17 9h-1V7a4 4 0 0 0-8 0v2H7a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2Zm-7-2a2 2 0 0 1 4 0v2h-4V7Zm3 8.73V18h-2v-2.27a2 2 0 1 1 2 0Z"/>
              </svg>
            </span>
          </div>

          <div className="login-meta">
            <a href="#" onClick={(e) => e.preventDefault()}>Lost Password?</a>
          </div>

          <button className="login-btn" type="submit">Login</button>
        </form>
      </main>
    </div>
  );
}
