const USERS = [
  { username: "navitagi", password: "123456", name: "Nguyen Viet Anh" }
];

export function loginLocal(username, password) {
  const u = USERS.find(x => x.username === username && x.password === password);
  if (!u) return null;

  // “token” giả lập
  const session = { username: u.username, name: u.name, loggedInAt: Date.now() };
  localStorage.setItem("fintech_session", JSON.stringify(session));
  return session;
}

export function logoutLocal() {
  localStorage.removeItem("fintech_session");
}

export function getSession() {
  const raw = localStorage.getItem("fintech_session");
  return raw ? JSON.parse(raw) : null;
}
