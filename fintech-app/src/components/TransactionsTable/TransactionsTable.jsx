import React from "react";
import "./TransactionsTable.css";
import Badge from "../common/Badge";
import Icon from "../common/Icon";

function formatTransactionAmount(amount) {
  const rawText = String(amount ?? "").trim();
  if (!rawText) return "$10.00";

  const sign = rawText.startsWith("-") ? "-" : rawText.startsWith("+") ? "+" : "";
  const numericText = rawText.replace(/[^\d.]/g, "");
  const numericValue = Number(numericText);
  if (!Number.isFinite(numericValue)) return "$10.00";

  const formatted = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(numericValue);

  return `${sign}$${formatted}`;
}

export default function TransactionsTable({ transactions }) {
  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100">Recent Transactions</h3>

        <div className="flex gap-2">
          <button className="fw-table__filterBtn">
            <Icon name="filter_list" className="text-sm" />
            Filter
          </button>
          <button className="text-slate-500 text-sm font-bold flex items-center gap-1 hover:text-primary transition-colors px-3">
            See All
            <Icon name="arrow_forward" className="text-lg" />
          </button>
        </div>
      </div>

      <div className="fw-table">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 dark:bg-slate-800/50">
                <th className="px-6 py-4 fw-th">Transaction</th>
                <th className="px-6 py-4 fw-th">Date</th>
                <th className="px-6 py-4 fw-th">Status</th>
                <th className="px-6 py-4 fw-th text-right">Amount</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-50 dark:divide-slate-800">
              {transactions.map((t, idx) => (
                <tr
                  key={`${t.title}-${idx}`}
                  className="hover:bg-slate-50/50 dark:hover:bg-slate-800/50 transition-colors"
                >
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className={`size-10 rounded-lg flex items-center justify-center ${t.iconWrap}`}>
                        <Icon name={t.icon} />
                      </div>
                      <div>
                        <p className="font-bold text-slate-900 dark:text-slate-100">{t.title}</p>
                        <p className="text-xs text-slate-400">{t.subtitle}</p>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-4 text-sm text-slate-500">{t.date}</td>

                  <td className="px-6 py-4">
                    <Badge tone={t.status.tone}>{t.status.label}</Badge>
                  </td>

                  <td
                    className={`px-6 py-4 text-right font-bold ${
                      t.amountTone === "green" ? "text-green-600" : "text-slate-900 dark:text-slate-100"
                    }`}
                  >
                    {formatTransactionAmount(t.amount)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
