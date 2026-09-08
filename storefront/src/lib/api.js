// Backend client -> Spring Gateway. Base from VITE_API_BASE (default localhost:8080).
const API_BASE = (import.meta.env.VITE_API_BASE || "http://localhost:8080").replace(/\/$/, "");

export const ASSETS = {
  logo: "/assets/home_catalog/img_1.jpg",
  homeHero: "/assets/home_catalog/img_3.jpg",
  home: (n) => `/assets/home_catalog/img_${n}.jpg`,
  product: (n) => `/assets/product_detail_sculpted_trench/img_${n}.jpg`,
  authSide: "/assets/authentication_sign_in_register/img_2.jpg",
  cart: (n) => `/assets/shopping_bag_cart/img_${n}.jpg`,
  checkout: (n) => `/assets/checkout_secure_payment/img_${n}.jpg`,
  orders: (n) => `/assets/customer_account_orders/img_${n}.jpg`,
};

// Backend products carry placeholder imageUrls (example.com) — fall back to local atelier shots.
export function productImage(p, i = 0) {
  if (p?.imageUrl && !p.imageUrl.includes("example.com")) return p.imageUrl;
  return ASSETS.home(2 + (i % 12));
}

function notifyAuth() {
  window.dispatchEvent(new Event("shopora-auth"));
}

export const store = {
  get token() { return localStorage.getItem("JWT"); },
  set token(t) {
    t ? localStorage.setItem("JWT", t) : localStorage.removeItem("JWT");
    notifyAuth();
  },
  get email() { return localStorage.getItem("SHOPORA_EMAIL"); },
  set email(e) {
    e ? localStorage.setItem("SHOPORA_EMAIL", e) : localStorage.removeItem("SHOPORA_EMAIL");
    notifyAuth();
  },
  get loggedIn() { return !!this.token; },
  authHeaders(extra = {}) {
    const h = { "Content-Type": "application/json", ...extra };
    if (this.token) h["Authorization"] = "Bearer " + this.token;
    return h;
  },
};

async function api(path, options = {}) {
  const res = await fetch(API_BASE + path, options);
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (!res.ok) throw new Error((data && data.message) || `HTTP ${res.status}`);
  return data;
}

export const Auth = {
  async register({ name, email, password, phone }) {
    const d = await api("/api/auth/register", {
      method: "POST", headers: store.authHeaders(), body: JSON.stringify({ name, email, password, phone }),
    });
    if (d.token) { store.token = d.token; store.email = d.email || email; }
    return d;
  },
  async login(email, password) {
    const d = await api("/api/auth/login", {
      method: "POST", headers: store.authHeaders(), body: JSON.stringify({ email, password }),
    });
    if (d.token) { store.token = d.token; store.email = d.email || email; }
    return d;
  },
  logout() { store.token = null; store.email = null; },
};

export const Products = {
  list({ keyword = "", page = 0, size = 12, sort = "id,asc", categoryId = null } = {}) {
    const q = new URLSearchParams({ page, size, sort });
    if (keyword) q.set("keyword", keyword);
    if (categoryId) q.set("categoryId", categoryId);
    return api(`/api/products?${q}`);
  },
  get: (id) => api(`/api/products/${id}`),
  categories: () => api("/api/categories"),
};

function notifyCart() {
  window.dispatchEvent(new Event("shopora-cart"));
}

export const Cart = {
  add: async (productId, quantity = 1) => {
    const r = await api("/api/cart/items", {
      method: "POST", headers: store.authHeaders(), body: JSON.stringify({ productId, quantity }),
    });
    notifyCart();
    return r;
  },
  view: () => api("/api/cart", { headers: store.authHeaders() }),
  update: async (itemId, quantity) => {
    const r = await api(`/api/cart/items/${itemId}`, {
      method: "PUT", headers: store.authHeaders(), body: JSON.stringify({ quantity }),
    });
    notifyCart();
    return r;
  },
  remove: async (itemId) => {
    const r = await api(`/api/cart/items/${itemId}`, { method: "DELETE", headers: store.authHeaders() });
    notifyCart();
    return r;
  },
  count: async () => {
    try {
      const c = await api("/api/cart", { headers: store.authHeaders() });
      return (c.items || []).reduce((s, i) => s + (i.quantity || 0), 0);
    } catch { return 0; }
  },
};

export const Orders = {
  create: (items, shippingAddress) => api("/api/orders", {
    method: "POST", headers: store.authHeaders(), body: JSON.stringify({ items, shippingAddress }),
  }),
  list: () => api("/api/orders", { headers: store.authHeaders() }),
  get: (id) => api(`/api/orders/${id}`, { headers: store.authHeaders() }),
};

export const Addresses = {
  list: () => api("/api/users/addresses", { headers: store.authHeaders() }),
  create: (a) => api("/api/users/addresses", {
    method: "POST", headers: store.authHeaders(), body: JSON.stringify(a),
  }),
  update: (id, a) => api(`/api/users/addresses/${id}`, {
    method: "PUT", headers: store.authHeaders(), body: JSON.stringify(a),
  }),
  remove: (id) => api(`/api/users/addresses/${id}`, { method: "DELETE", headers: store.authHeaders() }),
};

// Flipkart-style single-line address for orders + invoice
export function formatAddress(a) {
  if (!a) return "";
  return `${a.name}, ${a.addressLine}, ${a.city}, ${a.state} ${a.pincode}, ${a.country || "India"} | Phone: ${a.phone}${a.type ? ` (${a.type})` : ""}`;
}

export const Payments = {
  createOrder: (orderId, amount) => api("/api/payments/create-order", {
    method: "POST", headers: store.authHeaders(), body: JSON.stringify({ orderId, amount }),
  }),
  verify: (razorpayOrderId, razorpayPaymentId, razorpaySignature) => api("/api/payments/verify", {
    method: "POST", headers: store.authHeaders(),
    body: JSON.stringify({ razorpayOrderId, razorpayPaymentId, razorpaySignature }),
  }),
  pay({ keyId, razorpayOrderId, amount, onSuccess, onFail }) {
    if (typeof window.Razorpay === "undefined") throw new Error("checkout.js not loaded");
    const rzp = new window.Razorpay({
      key: keyId,
      order_id: razorpayOrderId,
      amount: Math.round(amount * 100),
      currency: "INR",
      name: "Shopora",
      handler: (resp) => onSuccess && onSuccess(resp),
      modal: { ondismiss: () => onFail && onFail(new Error("Payment dismissed")) },
    });
    rzp.on("payment.failed", (e) => onFail && onFail(e));
    rzp.open();
  },
};
