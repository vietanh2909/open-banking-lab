import React from "react";
import { Navigate } from "react-router-dom";
import { getSession } from "../auth/localAuth";

export default function PrivateRoute({ children }) {
  const session = getSession();
  if (!session) return <Navigate to="/login" replace />;
  return children;
}
