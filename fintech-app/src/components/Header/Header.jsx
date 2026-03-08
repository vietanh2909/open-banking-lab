import React, { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Header.css";
import Icon from "../common/Icon";
import { logout, getSession } from "../../services/authService";

export default function Header({ navItems }) {
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  const session = getSession();
  const displayName = session?.username || "Alex Johnson";

  // click outside => close
  useEffect(() => {
    const onDocClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, []);

  // ESC => close
  useEffect(() => {
    const onEsc = (e) => {
      if (e.key === "Escape") setOpen(false);
    };
    document.addEventListener("keydown", onEsc);
    return () => document.removeEventListener("keydown", onEsc);
  }, []);

  const handleLogout = () => {
    logout();
    setOpen(false);
    navigate("/login", { replace: true });
  };

  return (
    <header className="fw-header">
      <div className="flex items-center gap-8">
        <div className="flex items-center gap-4 text-primary">
          <div className="size-8 bg-primary rounded-lg flex items-center justify-center text-white">
            <Icon name="account_balance_wallet" />
          </div>
          <h2 className="text-slate-900 dark:text-slate-100 text-xl font-bold leading-tight tracking-[-0.015em]">
            FinWallet
          </h2>
        </div>

        <label className="hidden md:flex flex-col min-w-40 h-10 max-w-64">
          <div className="flex w-full flex-1 items-stretch rounded-xl h-full bg-primary/5 dark:bg-primary/10 border border-primary/10">
            <div className="text-slate-500 flex items-center justify-center pl-4">
              <Icon name="search" className="text-xl" />
            </div>
            <input
              className="form-input flex w-full min-w-0 flex-1 border-none bg-transparent focus:outline-0 focus:ring-0 h-full placeholder:text-slate-400 px-4 text-base font-normal"
              placeholder="Search transactions..."
              defaultValue=""
            />
          </div>
        </label>
      </div>

      <div className="flex flex-1 justify-end gap-6 items-center">
        <nav className="hidden xl:flex items-center gap-8">
          {navItems.map((it) => (
            <a
              key={it.label}
              href="#"
              className={
                it.active
                  ? "text-primary text-sm font-bold leading-normal border-b-2 border-primary pb-1"
                  : "text-slate-600 dark:text-slate-400 text-sm font-medium leading-normal hover:text-primary transition-colors"
              }
            >
              {it.label}
            </a>
          ))}
        </nav>

        <div className="flex gap-3">
          <button className="fw-icon-btn" type="button">
            <Icon name="notifications" />
          </button>
          <button className="fw-icon-btn" type="button">
            <Icon name="settings" />
          </button>
        </div>

        {/* ✅ Profile dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            type="button"
            onClick={() => setOpen((v) => !v)}
            className="bg-center bg-no-repeat aspect-square bg-cover rounded-full size-10 border-2 border-primary focus:outline-none cursor-pointer"
            style={{
              backgroundImage:
                'url("https://lh3.googleusercontent.com/aida-public/AB6AXuCQNh36dol4xUC9UrkiBA-u2Kw8BoflVDFjJmvegREKGAsEqBIrSKsUHeopwSCG6hoJ0qHRK74q-zMZTmoPndfHbPvAjydt4miRmPGZhfMyurK8A4FeQvivazWuu-Xyj_WXcU4rKXkoFme1k-0eWO2qGpeAKKNLvYD7Nu0C0KC9OGsChN5tUasjgF7MwUKbf0TIqyEd1EFQLBFD9LALBbjh86CEQkfdQapL0SHqdiD6JS4KmDDa-82Hlr_LOliSQVQRAZf6mlC6keg")',
            }}
            aria-haspopup="menu"
            aria-expanded={open}
          />

          {open && (
            <div className="profile-dropdown absolute right-0 mt-3 w-48 bg-white dark:bg-slate-900 rounded-xl shadow-xl border border-primary/10 py-2 z-[60]">
              <div className="px-4 py-2 border-b border-slate-50 dark:border-slate-800">
                <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Account
                </p>
                <p className="text-sm font-bold text-slate-900 dark:text-white truncate">
                  {displayName}
                </p>
              </div>

              <button
                type="button"
                onClick={() => setOpen(false)}
                className="flex w-full items-center gap-3 px-4 py-3 text-sm text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-left"
              >
                <span className="material-symbols-outlined text-xl">person</span>
                My Profile
              </button>

              <button
                type="button"
                onClick={() => setOpen(false)}
                className="flex w-full items-center gap-3 px-4 py-3 text-sm text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors text-left"
              >
                <span className="material-symbols-outlined text-xl">security</span>
                Security
              </button>

              <div className="mt-1 border-t border-slate-50 dark:border-slate-800 pt-1">
                <button
                  type="button"
                  onClick={handleLogout}
                  className="flex w-full items-center gap-3 px-4 py-3 text-sm font-bold text-primary hover:bg-orange-50 dark:hover:bg-primary/10 transition-colors text-left"
                >
                  <span className="material-symbols-outlined text-xl">
                    logout
                  </span>
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}