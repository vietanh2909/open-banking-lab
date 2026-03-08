
const FINTECH_BASE = import.meta.env.FINTECH_SERVICE_URL;

/**
 * Gọi API lấy danh sách tài khoản AIS
 * @returns {Promise<Array>} danh sách accounts
 */
export async function getAisAccounts() {
  try {
    const response = await fetch("/api/ais/accounts", {
      method: "GET",
      headers: {
        "Accept": "application/json",
        "X-Subject": "navitagi"
        // Sau này nếu cần access token:
        // "Authorization": `Bearer ${localStorage.getItem("access_token")}`
      },
    });

    if (!response.ok) {
      const errorText = await response.text().catch(() => "");
      throw new Error(`HTTP ${response.status} ${response.statusText} ${errorText}`);
    }

    const data = await response.json();

    if (!Array.isArray(data?.accounts)) {
      throw new Error("Invalid response format");
    }

    return data.accounts;

  } catch (error) {
    console.error("AIS getAccounts error:", error)
    throw error;
  }
}