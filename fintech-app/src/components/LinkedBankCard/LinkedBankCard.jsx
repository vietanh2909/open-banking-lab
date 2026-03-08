import React, { useEffect, useState } from "react";
import "./LinkedBankCard.css";
import Icon from "../common/Icon";
import AddAnotherBankCard from "./AddAnotherBankCard";
import { getLinkedBank, linkBank, unlinkBank } from "../../services/bankLinkService";
import { startCiamLogin } from "../../auth/ciamAuth";

export default function LinkedBankCard() {
  const [loading, setLoading] = useState(true);
  const [bank, setBank] = useState(null);

  // Load initial state
  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const b = await getLinkedBank();
        if (mounted) setBank(b);
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const handleUnlink = async () => {
    setLoading(true);
    try {
      await unlinkBank();
      setBank(null); // UI chuyển sang AddAnotherBank
    } finally {
      setLoading(false);
    }
  };

  // const handleAdd = async () => {
  //   setLoading(true);
  //   try {
  //     // Sau này bạn thay bằng payload thật từ OIDC callback / chọn bank / API.
  //     const b = await linkBank({
  //       name: "Vietcombank",
  //       masked: "**** **** **** 4590",
  //       exp: "12/26",
  //       tag: "Primary",
  //     });
  //     setBank(b); // UI hiện bank card
  //   } finally {
  //     setLoading(false);
  //   }
  // };

  // Add another bank -> redirect sang CIAM
  const handleAdd = async () => {
    await startCiamLogin({
      returnTo: "/dashboard",
      scope: "openid profile email ais",
      flow: "AIS_LINK",
    });
  };


  return (
    <div className="flex flex-col h-full">
      <h5 className="text-lg font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
        <Icon name="link" className="text-primary" />
        Linked Bank
      </h5>

      {loading ? (
        <div className="fw-linked flex items-center justify-center text-slate-400">
          Loading...
        </div>
      ) : bank ? (
        <div className="fw-linked">
          <div className="flex justify-between items-start mb-6">
            <div className="fw-linked__iconWrap">
              <Icon name="account_balance" className="text-blue-700 text-4xl" />
            </div>
            <span className="px-2 py-1 bg-green-100 dark:bg-green-900/30 text-green-600 text-[10px] font-bold uppercase rounded">
              {bank.tag}
            </span>
          </div>

          <div>
            <h4 className="text-xl font-bold text-slate-900 dark:text-slate-100 leading-tight">
              {bank.name}
            </h4>
            <p className="text-slate-500 text-sm mt-1">{bank.masked}</p>
          </div>

          <div className="mt-8 flex items-center justify-between pt-4 border-t border-slate-50 dark:border-slate-800">
            <div className="text-xs text-slate-400">Exp: {bank.exp}</div>
            <button className="fw-linked__unlink" onClick={handleUnlink}>
              Unlink Bank
            </button>
          </div>
        </div>
      ) : (
        <AddAnotherBankCard onAdd={handleAdd} />
      )}
    </div>
  );
}