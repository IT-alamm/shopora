import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Auth, ASSETS, store } from "../lib/api";

export default function AuthPage() {
  const nav = useNavigate();
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "test1@test.com", password: "pass123", phone: "" });
  const [error, setError] = useState("");
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      if (mode === "login") await Auth.login(form.email, form.password);
      else await Auth.register(form);
      nav("/");
    } catch (err) { setError(err.message); }
  };

  return (
    <div className="max-w-6xl mx-auto px-6 py-12 grid lg:grid-cols-2 gap-10 items-stretch">
      <img src={ASSETS.authSide} alt="Maison archives" className="rounded-xl object-cover w-full hidden lg:block" />
      <div className="bg-white border border-[#e3e2e3] rounded-xl p-8 shadow-sm">
        <h1 className="font-[Noto_Serif] text-3xl mb-1">{mode === "login" ? "Sign in" : "Register"}</h1>
        <p className="text-sm text-gray-500 mb-6">Maison authentication — JWT issued by auth-service</p>
        <form onSubmit={submit} className="flex flex-col gap-3">
          {mode === "register" && (
            <>
              <input className="border rounded px-3 py-2.5 text-sm" placeholder="Full name" value={form.name} onChange={set("name")} required />
              <input className="border rounded px-3 py-2.5 text-sm" placeholder="Phone" value={form.phone} onChange={set("phone")} />
            </>
          )}
          <input className="border rounded px-3 py-2.5 text-sm" placeholder="Email" value={form.email} onChange={set("email")} required />
          <input className="border rounded px-3 py-2.5 text-sm" placeholder="Password (min 6 chars)" type="password" value={form.password} onChange={set("password")} required />
          {error && <p className="text-red-600 text-sm">{error}</p>}
          <button className="bg-[#094cb2] text-white py-2.5 rounded text-sm tracking-widest uppercase">
            {mode === "login" ? "Sign in" : "Create account"}
          </button>
        </form>
        <button className="underline text-sm mt-4" onClick={() => setMode(mode === "login" ? "register" : "login")}>
          {mode === "login" ? "New here? Create an account" : "Have an account? Sign in"}
        </button>
        {store.token && <p className="text-green-700 text-xs mt-3 break-all">JWT saved ✓</p>}
      </div>
    </div>
  );
}
