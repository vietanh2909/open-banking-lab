import React from "react";

export default function Badge({ tone = "green", children }) {
  const toneClass =
    tone === "green"
      ? "bg-green-100 dark:bg-green-900/30 text-green-600"
      : tone === "yellow"
        ? "bg-yellow-100 dark:bg-yellow-900/30 text-yellow-600"
        : "bg-slate-100 dark:bg-slate-800 text-slate-500";

  return (
    <span className={`px-2 py-1 text-[10px] font-bold uppercase rounded ${toneClass}`}>
      {children}
    </span>
  );
}