import React from "react";
import { useNavigate } from "react-router-dom";
import { startCiamLogin } from "../auth/ciamAuth";

export default function Dashboard() {
  const nav = useNavigate();

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
          <h2 className="dash-h2">Thông tin cá nhân</h2>
          <p className="dash-hint">Thông tin định danh người dùng</p>

          <div className="dash-profile">
            <div className="dash-avatar" aria-hidden="true">
              <svg viewBox="0 0 24 24">
                <path d="M12 12a4.5 4.5 0 1 0-4.5-4.5A4.5 4.5 0 0 0 12 12Zm0 2c-4.1 0-7.5 2.2-7.5 5v1h15v-1c0-2.8-3.4-5-7.5-5Z"/>
              </svg>
            </div>

            <div className="dash-info">
              <div className="dash-field"><span>Họ tên</span><strong>Nguyễn Văn A</strong></div>
              <div className="dash-field"><span>SĐT</span><strong>090x xxx xxx</strong></div>
              <div className="dash-field"><span>Email</span><strong>user@mail.com</strong></div>
              <div className="dash-field"><span>CCCD</span><strong>0xxxxxxxxx</strong></div>
              <div className="dash-field"><span>KYC</span><strong>Đã xác minh</strong></div>
              <div className="dash-field"><span>Cập nhật</span><strong>29/01/2026</strong></div>
            </div>
          </div>
        </section>

        {/* Ngân hàng liên kết */}
        <section className="dash-card">
          <h2 className="dash-h2">Liên kết ngân hàng</h2>
          <p className="dash-hint">Quản lý các ngân hàng đã liên kết</p>

          <div className="dash-banks">
            {/* Add bank tile */}
            <button
              className="dash-add-bank"
              type="button"
               onClick={() => startCiamLogin("/dashboard")}
            >
              <div className="dash-plus-circle" aria-hidden="true" />
              <p>Liên kết ngân hàng</p>
            </button>

            {/* Demo tiles */}
            <div className="dash-bank">
              <div className="dash-bank-logo">MB</div>
              <h4>Ngân hàng MB</h4>
              <span>Đang hoạt động</span>
            </div>

            <div className="dash-bank">
              <div className="dash-bank-logo">VCB</div>
              <h4>Vietcombank</h4>
              <span>Token còn 12 ngày</span>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
