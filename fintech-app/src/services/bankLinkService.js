// src/services/bankLinkService.js
// Service layer: hiện mock localStorage, sau này thay bằng fetch/axios call backend.

const STORAGE_KEY = "finwallet_linked_bank";

/** Bank model ví dụ */
function getDefaultLinkedBank() {
  return {
    name: "Vietcombank",
    masked: "**** **** **** 4590",
    exp: "12/26",
    tag: "Primary",
    linkedAt: new Date().toISOString(),
  };
}

/** Simulate latency để UI giống gọi API */
function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

/** Lấy bank đã link (null nếu chưa link) */
export async function getLinkedBank() {
  await sleep(150);
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch {
    // dữ liệu lỗi -> xoá
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

/** Link bank (demo). Sau này thay bằng call backend, trả về bank vừa link */
export async function linkBank(payload = {}) {
  await sleep(250);

  // Payload bạn sẽ truyền từ luồng OIDC / callback thực tế.
  // Ở đây mock: nếu payload không đủ thì dùng default.
  const bank = {
    ...payload,
    linkedAt: new Date().toISOString(),
  };

  localStorage.setItem(STORAGE_KEY, JSON.stringify(bank));
  return bank;
}

/** Unlink bank */
export async function unlinkBank() {
  await sleep(200);
  localStorage.removeItem(STORAGE_KEY);
  return { success: true };
}