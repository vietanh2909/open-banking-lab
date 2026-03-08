import React, { useMemo, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import "./Login.css";
import { login } from "../services/authService";

export default function Login() {
  const nav = useNavigate();
  const location = useLocation();

  const from = useMemo(() => {
    // nếu bạn dùng PrivateRoute redirect sang /login, nó thường gửi state.from
    return location.state?.from?.pathname || "/dashboard";
  }, [location.state]);

  const [username, setUsername] = useState("anhnv");
  const [password, setPassword] = useState("123456");
  const [remember, setRemember] = useState(true);
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errMsg, setErrMsg] = useState("");

  const onSubmit = async (e) => {
    e.preventDefault();
    setErrMsg("");
    setLoading(true);
    try {
      await login({ username, password, remember });
      nav(from, { replace: true });
    } catch (e2) {
      if (e2?.code === "INVALID_CREDENTIALS") {
        setErrMsg("Sai username/password. Thử lại nhé.");
      } else {
        setErrMsg("Login lỗi. Vui lòng thử lại.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fw-login-page bg-background-light dark:bg-background-dark text-slate-900 dark:text-slate-100 min-h-screen">
      <div className="flex min-h-screen">
        {/* Left (only >= lg) */}
        <div className="hidden lg:flex lg:w-1/2 fw-login-left items-center justify-center p-12 relative overflow-hidden">
          <div className="absolute top-10 left-10 flex items-center gap-2">
            <div className="size-8 bg-primary rounded-lg flex items-center justify-center text-white">
              <span className="material-symbols-outlined">account_balance_wallet</span>
            </div>
            <h2 className="text-xl font-bold tracking-tight">FintechWallet</h2>
          </div>

          <div className="z-10 text-center max-w-md">
            <div className="mb-8 flex justify-center">
              <div className="relative w-64 h-64 bg-primary/20 rounded-full flex items-center justify-center">
                <img
                  className="w-48 h-48 object-contain"
                  alt="wallet illustration"
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuA98IA5GKtAJlfnkY0YUAWCjWVSmYB7BnErhPowRSU81ogUaOz0yDPyoJpMgTMLH1KoqFRCPw7dCnBERSShCYA06Jk0b9yeH9pZOp7NTN3t1vLi5fRU8G3XCxwBX4AHWtgYC504Q40_0yRvsCq-QaBOAYMF2EdMWbh-qRjMCoAC3NqBy517bOt34LBedkYcjqhlcTaetbzIelx6gyBYy9gxjpvaMHHV4YdS8AMaRX2zwoNM8E69nm48YaVlR5On6NqhB6t0DuAIwP8"
                />
              </div>
            </div>

            <h1 className="text-4xl font-extrabold mb-4 leading-tight">
              Secure Your Future with Our Digital Wallet
            </h1>
            <p className="text-slate-600 dark:text-slate-400 text-lg">
              Manage your finances, track spending, and save more with our intuitive platform.
            </p>
          </div>

          <div className="absolute -bottom-20 -left-20 w-64 h-64 bg-primary/10 rounded-full blur-3xl" />
          <div className="absolute -top-20 -right-20 w-80 h-80 bg-primary/10 rounded-full blur-3xl" />
        </div>

        {/* Right */}
        <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-white dark:bg-background-dark">
          <div className="w-full max-w-md space-y-8">
            {/* Mobile logo */}
            <div className="lg:hidden flex items-center gap-2 mb-8">
              <div className="size-8 bg-primary rounded-lg flex items-center justify-center text-white">
                <span className="material-symbols-outlined">account_balance_wallet</span>
              </div>
              <h2 className="text-xl font-bold tracking-tight">FintechWallet</h2>
            </div>

            <div>
              <h2 className="text-3xl font-bold tracking-tight">Welcome Back</h2>
              <p className="mt-2 text-slate-500 dark:text-slate-400">
                Please enter your details to sign in to your account.
              </p>
            </div>

            <form className="mt-8 space-y-6" onSubmit={onSubmit}>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
                    Username or Email
                  </label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">
                      <span className="material-symbols-outlined text-sm">mail</span>
                    </span>
                    <input
                      className="fw-input"
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      placeholder="name@company.com"
                      autoComplete="username"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">
                    Password
                  </label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">
                      <span className="material-symbols-outlined text-sm">lock</span>
                    </span>
                    <input
                      className="fw-input pr-12"
                      type={showPw ? "text" : "password"}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      autoComplete="current-password"
                      required
                    />
                    <button
                      className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-400 hover:text-primary transition-colors"
                      type="button"
                      onClick={() => setShowPw((v) => !v)}
                      aria-label="toggle password"
                    >
                      <span className="material-symbols-outlined">
                        {showPw ? "visibility_off" : "visibility"}
                      </span>
                    </button>
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <input
                    className="h-4 w-4 text-primary focus:ring-primary border-slate-300 rounded cursor-pointer"
                    id="remember-me"
                    type="checkbox"
                    checked={remember}
                    onChange={(e) => setRemember(e.target.checked)}
                  />
                  <label className="ml-2 block text-sm text-slate-700 dark:text-slate-300 cursor-pointer" htmlFor="remember-me">
                    Remember me
                  </label>
                </div>

                <div className="text-sm">
                  <a className="font-semibold text-primary hover:text-primary/80 transition-colors" href="#">
                    Forgot password?
                  </a>
                </div>
              </div>

              {errMsg && (
                <div className="text-sm font-semibold text-red-600 bg-red-50 dark:bg-red-900/20 border border-red-100 dark:border-red-900/40 rounded-xl px-4 py-3">
                  {errMsg}
                </div>
              )}

              <button
                className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-bold rounded-xl text-slate-900 bg-primary hover:bg-primary/90 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary transition-all disabled:opacity-60"
                type="submit"
                disabled={loading}
              >
                {loading ? "Signing In..." : "Sign In"}
              </button>
            </form>

            <div className="mt-6">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-slate-200 dark:border-slate-700" />
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-white dark:bg-background-dark text-slate-500">Or continue with</span>
                </div>
              </div>

              <div className="mt-6 grid grid-cols-2 gap-4">
                <button className="fw-social-btn" type="button">
                  <img
                    alt="Google logo"
                    className="h-5 w-5"
                    src="https://lh3.googleusercontent.com/aida-public/AB6AXuCpg9MMul1S8bva9K4YbrNS3iSc2fcKBR-N12DNvpTCHksckyzVz2Vj-ohvQDQfD7aAZiRhOqyMMiMnfT6GNQRXpJUP-FqqLnNGNwk9QSQDeoA69sf5NCiH7tC8cHfFn0EwjUP_HE3rOWnXKcuQn7Xgj0RK-Rc65AnDQw1efbD2F1-kPddtvJKPJpb8k-oAMbmUyI8Zb96l2Oq6x840NcYbqEYQyIXyUuKqrOsqqWJHsi1CMW9bSDLu9XZhFTm2w1xwCNqC4T59oIg"
                  />
                  <span>Google</span>
                </button>
                <button className="fw-social-btn" type="button">
                  <img
                    alt="Apple logo"
                    className="h-5 w-5"
                    src="https://lh3.googleusercontent.com/aida-public/AB6AXuA5khZr6S66WCBz7MLJVjoSdBETEtfWIcmRgtC8IqrBdbpoVKfpjlNx6NTdkRUl4xl2RFJJGcA_ie4nJA7C2FHK1fP8u_QInJC7R7FBH6RWRjMr-0D4X03Au5dXXMZyvdShh4aaR6IOLLfahXBH8xPGi631xU2WXIRYcY7YaHtE645Z4WFOAGqXy22SEJO1Dcrb4cTvJEpm2ku815Map-nRkqQqtbhlgqDo-E_PdWFxL-xNVRzT0VY2AXu3ouG-jOSNnYlyXGMnamc"
                  />
                  <span>Apple</span>
                </button>
              </div>
            </div>

            <p className="mt-10 text-center text-sm text-slate-500">
              Don&apos;t have an account?{" "}
              <a className="font-bold text-primary hover:text-primary/80 transition-colors" href="#">
                Sign Up for free
              </a>
            </p>

            <div className="text-xs text-slate-400 text-center">
              Demo creds: <span className="font-semibold">anhnv / 123456</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}