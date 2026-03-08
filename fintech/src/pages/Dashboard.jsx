import React, { useMemo, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { startCiamLogin } from "../auth/ciamAuth";
import { getAisAccounts } from "../services/aisService";

function formatVnd(n) {
  try {
    return new Intl.NumberFormat("vi-VN").format(n) + " VND";
  } catch {
    return `${n} VND`;
  }
}


function fmtTime(ms) {
  const d = new Date(ms);
  return d.toLocaleString("vi-VN");
}

export default function Dashboard() {
  const nav = useNavigate();
  const [loadingAccounts, setLoadingAccounts] = useState(false);
  const [accounts, setAccounts] = useState([]);
  const [accountsError, setAccountsError] = useState("");
  const hasAccounts = useMemo(() => Array.isArray(accounts) && accounts.length > 0, [accounts]);
  const [payState, setPayState] = useState({ status: "IDLE", message: "", ref: "" });
  const [bankLink, setBankLink] = useState(null);

  // giả lập dữ liệu hoá đơn + bank đã link
  const bill = useMemo(() => ({
    provider: "EVN (Điện lực)",
    customerId: "EVN-0123456789",
    period: "2026-01",
    amount: 450000,
    bank: { code: "MSB", name: "Ngân hàng MSB" },
    billId: "BILL-202601-0001",
  }), []);

  useEffect(() => {
      const raw = localStorage.getItem("bank_link_state");
      setBankLink(raw ? JSON.parse(raw) : null);
    }, []);

  const isLinked = !!bankLink?.linked;
  const isExpired = isLinked && bankLink.expiresAt && Date.now() >= bankLink.expiresAt;

  const onLink = () => {
      startCiamLogin({
        flow: "AIS_LINK",
        scope: "openid profile email ais",
        returnTo: "/dashboard"
      });
  };

  const onUnlink = () => {
    localStorage.removeItem("bank_link_state");
    setBankLink(null);
  };

  const fetchAccounts = async () => {
    setLoadingAccounts(true);
    setAccountsError("");

    try {
      const list = await getAisAccounts();
      setAccounts(list);
    } catch (e) {
      setAccounts([]);
      setAccountsError(e?.message || "Không truy vấn được danh sách tài khoản.");
    } finally {
      setLoadingAccounts(false);
    }
  };


  return (
    <div className="dash-page">
      <div className="dash-container">
        <header className="dash-header">
          <div className="dash-brand">
            <div className="dash-logo" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path d="M12 2l3 6 7 .9-5 4.8 1.4 7.3L12 17.9 5.6 21 7 13.7 2 8.9 9 8z"/>
              </svg>
            </div>
            <div>
              <h1 className="dash-h1">Dashboard</h1>
              <p className="dash-sub">Quản lý thông tin cá nhân & ngân hàng liên kết</p>
            </div>
          </div>

          <div className="dash-actions">
            <button className="dash-btn" onClick={() => window.location.reload()}>Tải lại</button>
            <button className="dash-btn dash-btn-primary" onClick={() => nav("/login")}>Đăng xuất</button>
          </div>
        </header>

        {/* Thông tin cá nhân */}
        <section className="dash-card">
          <div className="cardHeader">
            <div>
              <h2 className="cardTitle">Thông tin tài khoản</h2>
              <p className="cardDesc">Truy vấn và hiển thị danh sách tài khoản AIS</p>
            </div>

            <button
              className="btnPrimary"
              type="button"
              onClick={fetchAccounts}
              disabled={loadingAccounts}
              title="Truy vấn danh sách tài khoản"
            >
              {loadingAccounts ? "Đang truy vấn..." : "Truy vấn danh sách tài khoản"}
            </button>
          </div>

          {/* Error */}
          {accountsError && (
            <div className="errorCard">
              <div className="errorIcon">⚠</div>
              <div className="errorContent">
                <div className="errorTitle">
                  Không thể tải danh sách tài khoản
                </div>
                <div className="errorMessage">
                  Hệ thống không nhận được dữ liệu hợp lệ từ dịch vụ AIS.
                </div>

                <details className="errorDetail">
                  <summary>Xem chi tiết kỹ thuật</summary>
                  <pre>{accountsError}</pre>
                </details>
              </div>
            </div>
          )}

          {/* Empty state */}
          {!loadingAccounts && !accountsError && !hasAccounts ? (
            <div className="emptyState">
              Chưa có dữ liệu. Bấm <b>Truy vấn danh sách tài khoản</b> để tải dữ liệu.
            </div>
          ) : null}

          {/* Table */}
          {hasAccounts ? (
            <div className="tableWrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Account ID</th>
                    <th>Tên tài khoản</th>
                    <th>Loại</th>
                    <th>Tiền tệ</th>
                    <th>Mã ngân hàng</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {accounts.map((a, idx) => (
                    <tr key={`${a.accountId || "acc"}-${idx}`}>
                      <td className="mono">{a.accountId || "-"}</td>
                      <td>{a.name || "-"}</td>
                      <td className="pillCell">
                        <span className="pill">{a.type || "-"}</span>
                      </td>
                      <td className="pillCell">
                        <span className="pill">{a.currency || "-"}</span>
                      </td>
                      <td className="pillCell">
                        <span className="pill">{a.bankCode || "-"}</span>
                      </td>
                      <td className="pillCell">
                        <span className={`status ${String(a.status).toLowerCase() === "active" ? "ok" : "warn"}`}>
                          {a.status || "-"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>

        {/* Ngân hàng liên kết */}
        <section className="dash-card">
        <h2 className="dash-h2">Liên kết ngân hàng</h2>
        <p className="dash-hint">Quản lý các ngân hàng đã liên kết</p>

        <div className="dash-banks-row">
          {/* Ô liên kết */}
          <div className={`dash-add-tile ${isLinked ? "is-disabled" : ""}`}>
            <button
              className="dash-add-btn"
              type="button"
              onClick={onLink}
              disabled={isLinked}
              title={isLinked ? "Bạn đã liên kết, vui lòng hủy trước khi liên kết lại" : "Liên kết ngân hàng"}
            >
              <div className="dash-plus-icon" aria-hidden="true" />
              <div className="dash-add-text">
                {isLinked ? "Đã liên kết" : "Liên kết ngân hàng"}
              </div>
            </button>

            {/* Text trạng thái */}
            {isLinked && (
              <div className="dash-link-meta">
                <div className={`dash-link-pill ${isExpired ? "expired" : "active"}`}>
                  {isExpired ? "Hết hạn" : "Đang hiệu lực"}
                </div>
                <div className="dash-link-time">
                  Hết hạn lúc: <b>{fmtTime(bankLink.expiresAt)}</b>
                </div>

                <button
                  className="dash-btn dash-btn-outline"
                  type="button"
                  onClick={onUnlink}
                >
                  Hủy liên kết
                </button>
              </div>
            )}
          </div>
        </div>
      </section>

        <section className="dash-card">
          <div className="poc-head">
            <div>
              <h2 className="dash-h2">Thanh toán hóa đơn điện (PoC)</h2>
              <p className="dash-hint">Giả lập giao dịch thanh toán 1 hóa đơn đã sẵn sàng</p>
            </div>

            <button
              className={`dash-btn dash-btn-primary ${payState.status === "PENDING" ? "is-loading" : ""}`}
              type="button"
              //onClick={onPay}
              onClick={() => startCiamLogin({
                flow: "AIS_LINK",
                scope: "openid profile email pis",
                returnTo: "/dashboard"
              })}
              disabled={payState.status === "PENDING"}
            >
              {payState.status === "PENDING" ? "Đang thanh toán..." : "Thanh toán"}
            </button>
          </div>

          <div className="poc-grid-2">
            {/* Bill info */}
            <div className="poc-box">
              <div className="poc-title">Thông tin hóa đơn</div>

              <div className="poc-kv">
                <div className="poc-k">Nhà cung cấp</div>
                <div className="poc-v">{bill.provider}</div>

                <div className="poc-k">Mã khách hàng</div>
                <div className="poc-v">{bill.customerId}</div>

                <div className="poc-k">Kỳ hóa đơn</div>
                <div className="poc-v">{bill.period}</div>

                <div className="poc-k">Số tiền</div>
                <div className="poc-v">{formatVnd(bill.amount)}</div>

                <div className="poc-k">Ngân hàng</div>
                <div className="poc-v">{bill.bank.name} ({bill.bank.code})</div>

                <div className="poc-k">BillId</div>
                <div className="poc-v">{bill.billId}</div>
              </div>
            </div>

            {/* Result */}
            <div className="poc-result">
              <div className="poc-title">Kết quả</div>

              <div className={`poc-status ${payState.status.toLowerCase()}`}>
                <span className="poc-badge">
                  {payState.status === "IDLE" && "CHƯA THANH TOÁN"}
                  {payState.status === "PENDING" && "PENDING"}
                  {payState.status === "SUCCESS" && "SUCCESS"}
                  {payState.status === "FAIL" && "FAIL"}
                </span>

                <div className="poc-msg">
                  {payState.status === "IDLE" && "Nhấn “Thanh toán” để bắt đầu PoC."}
                  {payState.status !== "IDLE" && payState.message}
                </div>

                {payState.ref && (
                  <div className="poc-ref">
                    Mã giao dịch: <b>{payState.ref}</b>
                  </div>
                )}
              </div>

              <div className="poc-note">
                *PoC: Sau này bạn có thể thay phần random bằng gọi `fintech-services → PIS/Consent` và map trạng thái.
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
