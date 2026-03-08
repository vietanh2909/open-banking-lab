export const navItems = [
  { label: "Dashboard", active: true },
  { label: "Accounts", active: false },
  { label: "Transactions", active: false },
  { label: "Cards", active: false },
];

export const balanceCard = {
  title: "Total E-Wallet Balance",
  amount: "$12,450.00",
  syncText: "Last sync with Vietcombank: 2 minutes ago",
};

export const linkedBank = {
  name: "Vietcombank",
  masked: "**** **** **** 4590",
  exp: "12/26",
  tag: "Primary",
};

export const subAccounts = [
  {
    type: "Savings Account",
    name: "Personal Rainy Day",
    amount: "$8,240.50",
    id: "00921",
    status: { label: "Active", tone: "green" },
    selected: true,
    amountTone: "primary",
  },
  {
    type: "Business Account",
    name: "Freelance Income",
    amount: "$3,210.00",
    id: "00445",
    status: { label: "Active", tone: "green" },
    selected: false,
    amountTone: "default",
  },
  {
    type: "Shared Account",
    name: "Family Expenses",
    amount: "$999.50",
    id: "00120",
    status: { label: "In Review", tone: "yellow" },
    selected: false,
    amountTone: "default",
  },
];

export const transactions = [
  {
    icon: "shopping_cart",
    iconWrap: "bg-orange-100 dark:bg-orange-900/30 text-primary",
    title: "Coffee Shop",
    subtitle: "Personal Account",
    date: "Oct 24, 2023",
    status: { label: "Completed", tone: "green" },
    amount: "-$4.50",
    amountTone: "default",
  },
  {
    icon: "download",
    iconWrap: "bg-blue-100 dark:bg-blue-900/30 text-blue-600",
    title: "From Vietcombank",
    subtitle: "Bank Sync",
    date: "Oct 23, 2023",
    status: { label: "Completed", tone: "green" },
    amount: "+$500.00",
    amountTone: "green",
  },
  {
    icon: "subscriptions",
    iconWrap: "bg-slate-100 dark:bg-slate-800 text-slate-600",
    title: "Netflix",
    subtitle: "Subscription",
    date: "Oct 22, 2023",
    status: { label: "Pending", tone: "yellow" },
    amount: "-$15.99",
    amountTone: "default",
  },
  {
    icon: "upload",
    iconWrap: "bg-red-100 dark:bg-red-900/30 text-red-600",
    title: "Move to Wallet",
    subtitle: "Internal Transfer",
    date: "Oct 20, 2023",
    status: { label: "Completed", tone: "green" },
    amount: "-$200.00",
    amountTone: "default",
  },
];