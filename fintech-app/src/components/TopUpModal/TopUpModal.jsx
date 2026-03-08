import React, { useEffect, useMemo, useState } from "react";
import "./TopUpModal.css";
import Icon from "../common/Icon";

function formatAmountInput(value) {
  const cleaned = String(value ?? "").replace(/,/g, "").replace(/[^\d.]/g, "");
  const firstDotIndex = cleaned.indexOf(".");
  const normalized =
    firstDotIndex === -1
      ? cleaned
      : `${cleaned.slice(0, firstDotIndex + 1)}${cleaned.slice(firstDotIndex + 1).replace(/\./g, "")}`;

  const [intPartRaw = "", decPartRaw = ""] = normalized.split(".");
  const intPart = intPartRaw.replace(/^0+(?=\d)/, "") || "0";
  const intPartWithComma = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ",");

  if (normalized.includes(".")) {
    return `${intPartWithComma}.${decPartRaw.slice(0, 2)}`;
  }

  return intPartWithComma;
}

export default function TopUpModal({ open, onClose, sourceAccount, accounts = [], onConfirm }) {
  const [amount, setAmount] = useState("10");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedAccountId, setSelectedAccountId] = useState(sourceAccount?.id ?? null);

  useEffect(() => {
    if (!open) return;
    setSelectedAccountId(sourceAccount?.id ?? accounts[0]?.id ?? null);
    setIsDropdownOpen(false);
    setAmount("10");
  }, [open, sourceAccount?.id, accounts]);

  const selectedAccount = useMemo(() => {
    if (!selectedAccountId) return sourceAccount ?? accounts[0] ?? null;
    return accounts.find((acc) => String(acc.id) === String(selectedAccountId)) ?? sourceAccount ?? null;
  }, [accounts, selectedAccountId, sourceAccount]);

  const sourceName = selectedAccount?.name ?? "No account";
  const sourceBalance = selectedAccount?.amount ?? "$0.00";

  const normalizedAmount = useMemo(() => String(amount).replace(/,/g, ""), [amount]);

  if (!open) return null;

  const handleSubmit = () => {
    const amountNumber = Number(normalizedAmount);
    if (!Number.isFinite(amountNumber) || amountNumber <= 0) return;

    onConfirm?.({
      amount: amountNumber,
      sourceAccountId: selectedAccount?.id ?? null,
    });
    onClose?.();
  };

  return (
    <div className="fw-topup-overlay" onClick={onClose}>
      <div className="fw-topup-modal" onClick={(e) => e.stopPropagation()}>
        <div className="fw-topup-header">
          <div className="flex items-center gap-3">
            <div className="fw-topup-iconWrap">
              <Icon name="add_circle" />
            </div>
            <h2 className="fw-topup-title">Top-up Wallet</h2>
          </div>
          <button className="fw-topup-close" onClick={onClose} aria-label="Close top-up dialog">
            <Icon name="close" />
          </button>
        </div>

        <div className="fw-topup-body">
          <div>
            <label className="fw-topup-label">Select Source Account</label>
            <div className="fw-topup-sourceWrap">
              <button
                type="button"
                className="fw-topup-source"
                onClick={() => setIsDropdownOpen((prev) => !prev)}
              >
                <div className="flex items-center gap-3">
                  <div className="fw-topup-sourceIcon">
                    <Icon name="account_balance" />
                  </div>
                  <div>
                    <p className="fw-topup-sourceName">{sourceName}</p>
                    <p className="fw-topup-sourceBalance">Balance: {sourceBalance}</p>
                  </div>
                </div>
                <Icon name="expand_more" className="text-slate-400" />
              </button>

              {isDropdownOpen && (
                <div className="fw-topup-dropdown">
                  {accounts.map((acc) => (
                    <button
                      key={acc.id}
                      type="button"
                      className="fw-topup-dropdownItem"
                      onClick={() => {
                        setSelectedAccountId(acc.id);
                        setIsDropdownOpen(false);
                      }}
                    >
                      <div className="fw-topup-dropdownName">{acc.name}</div>
                      <div className="fw-topup-dropdownMeta">
                        {acc.type} | {acc.amount}
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div>
            <label className="fw-topup-label">Amount</label>
            <div className="fw-topup-inputWrap">
              <span className="fw-topup-currency">$</span>
              <input
                type="text"
                value={amount}
                onChange={(e) => setAmount(formatAmountInput(e.target.value))}
                className="fw-topup-input"
              />
            </div>
          </div>

          <div className="fw-topup-summary">
            <div className="fw-topup-row">
              <span>Processing Fee</span>
              <span>$0.00</span>
            </div>
            <div className="fw-topup-row">
              <span>Estimated Arrival</span>
              <span>Instant</span>
            </div>
          </div>
        </div>

        <div className="fw-topup-footer">
          <button className="fw-topup-confirm" onClick={handleSubmit}>
            Confirm Top-up
          </button>
        </div>
      </div>
    </div>
  );
}
