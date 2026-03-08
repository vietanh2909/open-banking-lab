import React, { useState } from "react";
import Header from "../components/Header/Header";
import BalanceCard from "../components/BalanceCard/BalanceCard";
import LinkedBankCard from "../components/LinkedBankCard/LinkedBankCard";
import SubAccounts from "../components/SubAccounts/SubAccounts";
import CurrentViewDetails from "../components/CurrentViewDetails/CurrentViewDetails";
import TransactionsTable from "../components/TransactionsTable/TransactionsTable";
import TopUpModal from "../components/TopUpModal/TopUpModal";
import { getAisAccounts } from "../services/aisService";

import { navItems, subAccounts, transactions, balanceCard } from "../data/mockData";

function parseCurrencyText(value) {
  const numeric = Number(String(value ?? "").replace(/[^\d.-]/g, ""));
  return Number.isFinite(numeric) ? numeric : 0;
}

function formatUsdAmount(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
}

function formatCurrencyAmount(value, currency = "USD") {
  const amount = Number(value);
  if (!Number.isFinite(amount)) return "$0.00";

  try {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amount);
  } catch {
    return formatUsdAmount(amount);
  }
}

function mapAisAccountToSubAccount(account, index) {
  const typeCode = String(account?.type ?? "").trim().toUpperCase();
  const typeLabelByCode = {
    SVGS: "Savings Account",
    CACC: "Business Account",
    LOAN: "Shared Account",
  };

  const statusRaw = String(account?.status ?? "").trim().toLowerCase();
  const statusLabel = statusRaw ? `${statusRaw[0].toUpperCase()}${statusRaw.slice(1)}` : "Active";
  const statusToneByValue = {
    active: "green",
    inactive: "yellow",
    pending: "yellow",
    blocked: "red",
  };

  return {
    type: typeLabelByCode[typeCode] ?? "Bank Account",
    name: account?.name ?? `Account ${index + 1}`,
    amount: formatCurrencyAmount(account?.balance ?? 0, account?.currency ?? "USD"),
    id: String(account?.accountId ?? `ACC-${index + 1}`),
    status: {
      label: statusLabel,
      tone: statusToneByValue[statusRaw] ?? "yellow",
    },
    selected: index === 0,
    amountTone: index === 0 ? "primary" : "default",
  };
}

export default function App() {
  const [accounts, setAccounts] = useState(subAccounts);
  const [isReloadingAccounts, setIsReloadingAccounts] = useState(false);
  const [isTopUpModalOpen, setIsTopUpModalOpen] = useState(false);
  const [totalBalance, setTotalBalance] = useState(parseCurrencyText(balanceCard.amount));

  const handleReloadAccounts = async () => {
    setIsReloadingAccounts(true);
    try {
      const aisAccounts = await getAisAccounts();
      const mappedAccounts = aisAccounts.map(mapAisAccountToSubAccount);
      setAccounts(mappedAccounts);
    } catch (error) {
      console.error("Reload AIS accounts error:", error);
    } finally {
      setIsReloadingAccounts(false);
    }
  };

  const selectedSourceAccount = accounts.find((acc) => acc.selected) ?? accounts[0] ?? null;
  const handleTopUpConfirm = ({ amount }) => {
    setTotalBalance((prev) => prev + amount);
  };

  return (
    <div className="bg-background-light dark:bg-background-dark font-display text-slate-900 dark:text-slate-100 min-h-screen">
      <div className="relative flex min-h-screen w-full flex-col overflow-x-hidden">
        <div className="layout-container flex h-full grow flex-col">
          <Header navItems={navItems} />

          <main className="flex flex-1 justify-center py-8 px-4 lg:px-20">
            <div className="layout-content-container flex flex-col max-w-[1200px] flex-1 gap-8">
              {/* Top */}
              <section className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                <div className="lg:col-span-8">
                  <BalanceCard
                    {...balanceCard}
                    amount={formatUsdAmount(totalBalance)}
                    onTopUpClick={() => setIsTopUpModalOpen(true)}
                  />
                </div>
                <div className="lg:col-span-4">
                  <LinkedBankCard />
                </div>
              </section>

              {/* Sub accounts */}
              <SubAccounts
                accounts={accounts}
                onReload={handleReloadAccounts}
                isReloading={isReloadingAccounts}
              />

              {/* Details + Recent */}
              <section className="grid grid-cols-1 lg:grid-cols-4 gap-8">
                <div className="lg:col-span-1">
                  <CurrentViewDetails />
                </div>
                <div className="lg:col-span-3">
                  <TransactionsTable transactions={transactions} />
                </div>
              </section>
            </div>
          </main>

          <footer className="mt-12 border-t border-primary/10 py-10 px-6 lg:px-20 bg-white dark:bg-slate-900/30">
            <div className="max-w-[1200px] mx-auto flex flex-col md:flex-row justify-between items-center gap-6">
              <div className="flex items-center gap-3 text-slate-400">
                <span className="material-symbols-outlined">security</span>
                <p className="text-sm">Vietcombank linked session is encrypted for your safety.</p>
              </div>
              <div className="flex gap-8 text-sm font-medium text-slate-500">
                <a className="hover:text-primary transition-colors" href="#">Privacy Policy</a>
                <a className="hover:text-primary transition-colors" href="#">Terms of Service</a>
                <a className="hover:text-primary transition-colors" href="#">Support Center</a>
              </div>
            </div>
          </footer>
        </div>
      </div>
      <TopUpModal
        open={isTopUpModalOpen}
        onClose={() => setIsTopUpModalOpen(false)}
        sourceAccount={selectedSourceAccount}
        accounts={accounts}
        onConfirm={handleTopUpConfirm}
      />
    </div>
  );
}
