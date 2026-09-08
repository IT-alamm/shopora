import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ASSETS, Auth, Cart, store } from "../lib/api";

function ProfileMenu() {
  const nav = useNavigate();
  const [open, setOpen] = useState(false);
  const email = store.email || "Account";
  const initial = (email[0] || "A").toUpperCase();

  return (
    <div className="relative">
      <button
        title={email}
        onClick={() => setOpen((o) => !o)}
        className="w-9 h-9 rounded-full bg-black text-white text-sm font-semibold flex items-center justify-center">
        {initial}
      </button>
      {open && (
        <>
          <button aria-label="close menu" className="fixed inset-0 cursor-default" onClick={() => setOpen(false)} />
          <div className="absolute right-0 mt-2 w-56 bg-white border border-[#e3e2e3] rounded-xl shadow-lg p-2 z-50">
            <p className="px-3 py-2 text-xs text-gray-500 truncate">{email}</p>
            <Link to="/orders" onClick={() => setOpen(false)}
              className="block px-3 py-2 text-sm rounded-lg hover:bg-gray-100">My orders</Link>
            <Link to="/addresses" onClick={() => setOpen(false)}
              className="block px-3 py-2 text-sm rounded-lg hover:bg-gray-100">My addresses</Link>
            <button
              className="w-full text-left px-3 py-2 text-sm rounded-lg hover:bg-gray-100 text-red-600"
              onClick={() => { Auth.logout(); setOpen(false); nav("/auth"); }}>
              Logout
            </button>
          </div>
        </>
      )}
    </div>
  );
}

function HeaderSearch() {
  const nav = useNavigate();
  const [q, setQ] = useState("");
  return (
    <form
      className="hidden md:flex items-center gap-2 flex-1 max-w-md mx-4"
      onSubmit={(e) => { e.preventDefault(); nav(q.trim() ? `/?q=${encodeURIComponent(q.trim())}` : "/"); }}>
      <input value={q} onChange={(e) => setQ(e.target.value)} placeholder="Search the collection..."
        className="border border-gray-400 rounded-full px-4 py-2 w-full text-sm bg-white placeholder:text-gray-400 focus:outline-none" />
      <button className="bg-[#094cb2] text-white px-6 py-2 rounded-full text-sm shrink-0">Search</button>
    </form>
  );
}

export default function Layout({ children }) {
  const [loggedIn, setLoggedIn] = useState(store.loggedIn);
  const [cartCount, setCartCount] = useState(0);
  useEffect(() => {
    const sync = () => setLoggedIn(store.loggedIn);
    const syncCart = () => Cart.count().then(setCartCount).catch(() => {});
    syncCart();
    window.addEventListener("shopora-auth", sync);
    window.addEventListener("shopora-auth", syncCart);
    window.addEventListener("shopora-cart", syncCart);
    return () => {
      window.removeEventListener("shopora-auth", sync);
      window.removeEventListener("shopora-auth", syncCart);
      window.removeEventListener("shopora-cart", syncCart);
    };
  }, []);
  return (
    <div className="min-h-screen bg-[#faf9fa] font-[Inter] text-[#1b1c1d]">
      <div className="bg-[#e9e8e9] px-6 py-1.5 text-center text-[11px] tracking-[0.2em] uppercase text-[#434653]">
        Complimentary bespoke gift wrapping on orders over ₹25,000
      </div>
      <header className="sticky top-0 z-50 bg-[#faf9fa]/90 backdrop-blur border-b border-[#e3e2e3]">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between gap-6">
          <Link to="/" className="flex items-center gap-3">
            <img src={ASSETS.logo} alt="Shopora" className="h-9 w-auto object-contain" />
            <span className="flex flex-col leading-none">
              <span className="font-[Noto_Serif] font-semibold text-lg">Shopora</span>
              <span className="text-[10px] tracking-[0.25em] uppercase text-gray-500">Atelier & Goods</span>
            </span>
          </Link>
          <HeaderSearch />
          <nav className="hidden lg:flex items-center gap-7 text-xs tracking-widest uppercase text-gray-500">
            <Link to="/" className="hover:text-black">Shop All</Link>
            <Link to="/collections" className="hover:text-black">Collections</Link>
            <Link to="/orders" className="hover:text-black">Orders</Link>
          </nav>
          <div className="flex items-center gap-4">
            <Link to="/cart" className="hover:underline text-sm relative">
              Cart
              {cartCount > 0 && (
                <span className="absolute -top-2 -right-4 bg-[#094cb2] text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center">
                  {cartCount > 99 ? "99+" : cartCount}
                </span>
              )}
            </Link>
            {loggedIn ? <ProfileMenu /> : <Link to="/auth" className="hover:underline text-sm">Sign in</Link>}
          </div>
        </div>
      </header>
      <main>{children}</main>
      <footer className="border-t border-[#e3e2e3] mt-16 py-8 text-center text-xs text-gray-500">
        Shopora Atelier & Goods — Spring/Summer Craftsmanship Editions · Razorpay TEST mode
      </footer>
    </div>
  );
}
