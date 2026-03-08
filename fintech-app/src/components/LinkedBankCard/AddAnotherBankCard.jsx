import React from "react";
import Icon from "../common/Icon";
import "./LinkedBankCard.css";

export default function AddAnotherBankCard({ onAdd }) {
  return (
    <button type="button" className="fw-addbank" onClick={onAdd}>
      <div className="fw-addbank__inner">
        <div className="fw-addbank__plus">
          <Icon name="add" className="text-primary text-3xl" />
        </div>
        <div className="fw-addbank__text">Add another bank</div>
      </div>
    </button>
  );
}