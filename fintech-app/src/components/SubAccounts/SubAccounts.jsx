import React from "react";
import "./SubAccounts.css";
import Badge from "../common/Badge";
import Icon from "../common/Icon";

export default function SubAccounts({ accounts = [], onReload, isReloading = false }) {
  return (
    <section>
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <h3 className="text-2xl font-bold text-slate-900 dark:text-slate-100">
            Sub-Accounts
          </h3>
          <span className="px-2 py-0.5 bg-primary/10 text-primary text-xs font-bold rounded-full">
            {accounts.length} Accounts
          </span>
        </div>

        <button
          className="text-primary font-bold text-sm flex items-center gap-2 px-4 py-2 bg-primary/10 rounded-xl hover:bg-primary/20 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          onClick={onReload}
          disabled={isReloading}
        >
          <span className="material-symbols-outlined text-lg">refresh</span>
          {isReloading ? "Reloading..." : "Reload"}
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {accounts.map((acc) => (
          <div
            key={acc.id}
            className={`fw-subcard group ${acc.selected ? "border-primary/20" : "border-primary/10"}`}
          >
            <div
              className={`absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity ${
                acc.selected ? "text-primary" : "text-slate-300"
              }`}
            >
              <Icon name={acc.selected ? "radio_button_checked" : "radio_button_unchecked"} />
            </div>

            <p className="text-slate-500 text-xs font-semibold uppercase tracking-wider mb-1">
              {acc.type}
            </p>
            <h4 className="text-lg font-bold text-slate-900 dark:text-slate-100">
              {acc.name}
            </h4>
            <p
              className={`text-2xl font-extrabold mt-2 ${
                acc.amountTone === "primary"
                  ? "text-primary"
                  : "text-slate-900 dark:text-slate-100"
              }`}
            >
              {acc.amount}
            </p>

            <div className="mt-4 flex gap-2">
              <span className="px-2 py-1 bg-slate-100 dark:bg-slate-800 text-slate-500 text-[10px] font-bold uppercase rounded">
                ID: {acc.id}
              </span>
              <Badge tone={acc.status.tone}>{acc.status.label}</Badge>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
