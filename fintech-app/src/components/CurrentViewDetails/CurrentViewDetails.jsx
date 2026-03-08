import React from "react";
import "./CurrentViewDetails.css";
import Icon from "../common/Icon";

export default function CurrentViewDetails() {
  return (
    <div className="space-y-6">
      <h3 className="text-xl font-bold text-slate-900 dark:text-slate-100">Current View Details</h3>

      <div className="fw-details">
        <div className="space-y-4">
          <div>
            <p className="text-slate-400 text-xs font-semibold uppercase">Daily Limit</p>
            <div className="flex items-center gap-2 mt-1">
              <div className="flex-1 h-2 bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                <div className="h-full bg-primary w-2/3" />
              </div>
              <span className="text-xs font-bold">$2k/$3k</span>
            </div>
          </div>

          <div>
            <p className="text-slate-400 text-xs font-semibold uppercase">Verification</p>
            <div className="flex items-center gap-2 mt-1 text-green-600">
              <Icon name="verified_user" className="text-sm" />
              <span className="text-sm font-bold">Level 2 (Full)</span>
            </div>
          </div>

          <div>
            <p className="text-slate-400 text-xs font-semibold uppercase">Target Account</p>
            <p className="text-sm font-bold text-slate-900 dark:text-slate-100 mt-1">
              Personal Rainy Day (Sub)
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}