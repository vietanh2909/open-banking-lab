import React from "react";
import "./BalanceCard.css";
import Icon from "../common/Icon";

export default function BalanceCard({ title, amount, syncText, onTopUpClick }) {
  return (
    <div className="fw-balance">
      <div className="fw-balance__watermark">
        <Icon name="payments" className="text-[120px]" />
      </div>

      <div className="relative z-10">
        <p className="text-white/80 font-medium mb-1">{title}</p>
        <h1 className="text-5xl font-extrabold tracking-tight mb-2">{amount}</h1>
        <div className="flex items-center gap-2 text-white/90 text-sm">
          <Icon name="sync" className="text-sm" />
          <span>{syncText}</span>
        </div>
      </div>

      <div className="flex gap-4 mt-10 relative z-10">
        <button className="fw-balance__btn fw-balance__btn--primary" onClick={onTopUpClick}>
          <Icon name="add_circle" />
          Top-up
        </button>
        <button className="fw-balance__btn fw-balance__btn--ghost">
          <Icon name="account_balance_wallet" />
          Withdraw
        </button>
      </div>
    </div>
  );
}
