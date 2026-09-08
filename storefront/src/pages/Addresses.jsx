import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Addresses } from "../lib/api";

const EMPTY = { name: "", phone: "", pincode: "", addressLine: "", city: "", state: "", type: "HOME", isDefault: false };

export default function AddressesPage() {
  const [list, setList] = useState([]);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");
  const [form, setForm] = useState(EMPTY);
  const [editingId, setEditingId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [locStatus, setLocStatus] = useState("");

  const useLiveLocation = () => {
    if (!navigator.geolocation) { setLocStatus("Geolocation not supported on this device/browser"); return; }
    setLocStatus("Fetching live location...");
    navigator.geolocation.getCurrentPosition(async (pos) => {
      const { latitude, longitude } = pos.coords;
      try {
        const r = await fetch(
          `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=en`);
        const g = await r.json();
        setForm((f) => ({
          ...f,
          city: g.city || g.locality || f.city,
          state: g.principalSubdivision || f.state,
          pincode: String(g.postcode || f.pincode || "").replace(/\D/g, "").slice(0, 6),
        }));
        setLocStatus(`Live location locked: ${latitude.toFixed(4)}, ${longitude.toFixed(4)} — verify & save`);
      } catch {
        setLocStatus(`Coords: ${latitude.toFixed(4)}, ${longitude.toFixed(4)} (lookup failed — fill manually)`);
      }
    }, () => setLocStatus("Location permission denied — allow location in the browser address bar"),
      { enableHighAccuracy: true, timeout: 15000 });
  };

  const load = () => Addresses.list().then(setList).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError(""); setMsg("");
    try {
      if (editingId) await Addresses.update(editingId, form);
      else await Addresses.create(form);
      setForm(EMPTY); setEditingId(null); setShowForm(false);
      setMsg("Address saved");
      await load();
    } catch (err) { setError(err.message); }
  };

  const startEdit = (a) => {
    setEditingId(a.id);
    setShowForm(true);
    setForm({ name: a.name, phone: a.phone, pincode: a.pincode, addressLine: a.addressLine, city: a.city, state: a.state, type: a.type || "HOME", isDefault: !!a.isDefault });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <div className="max-w-3xl mx-auto px-6 py-10">
      <h1 className="font-[Noto_Serif] text-3xl mb-6">My addresses</h1>
      {error && <p className="text-red-600 text-sm mb-3">{error} — <Link className="underline" to="/auth">sign in first</Link></p>}
      {msg && <p className="text-green-700 text-sm mb-3">{msg}</p>}

      {list.map((a) => (
        <div key={a.id} className="bg-white border border-[#e3e2e3] rounded-xl p-5 mb-3">
          <div className="flex items-center gap-2">
            <p className="font-medium">{a.name}</p>
            <span className="text-[11px] px-2 py-0.5 rounded-full bg-gray-100 text-gray-600">{a.type}</span>
            {a.isDefault && <span className="text-[11px] px-2 py-0.5 rounded-full bg-[#e7ebff] text-[#094cb2]">DEFAULT</span>}
          </div>
          <p className="text-sm text-gray-600 mt-1">{a.addressLine}, {a.city}, {a.state} {a.pincode}</p>
          <p className="text-sm text-gray-500">Phone: {a.phone}</p>
          <div className="flex gap-3 mt-2 text-sm">
            <button className="underline" onClick={() => startEdit(a)}>Edit</button>
            <button className="underline text-red-600" onClick={async () => { await Addresses.remove(a.id); load(); }}>Delete</button>
          </div>
        </div>
      ))}
      {list.length === 0 && !error && <p className="text-gray-500 text-sm mb-4">No addresses yet — add your first delivery address below.</p>}

      {!showForm && !editingId ? (
        <button onClick={() => setShowForm(true)}
          className="mt-8 px-8 py-2.5 bg-black text-white rounded-full text-sm tracking-widest uppercase">
          + Add new address
        </button>
      ) : (
      <>
      <h2 className="font-[Noto_Serif] text-xl mt-8 mb-4">{editingId ? "Edit address" : "Add a new address"}</h2>
      <button type="button" onClick={useLiveLocation}
        className="mb-3 px-5 py-2 border border-black rounded-full text-sm hover:bg-black hover:text-white transition-colors">
        📍 Use my live location
      </button>
      {locStatus && <p className="text-xs text-gray-600 mb-3">{locStatus}</p>}
      <form onSubmit={submit} className="bg-white border border-[#e3e2e3] rounded-xl p-5 grid grid-cols-1 sm:grid-cols-2 gap-3">
        <input className="border rounded px-3 py-2 text-sm" placeholder="Full name" value={form.name} onChange={set("name")} required />
        <input className="border rounded px-3 py-2 text-sm" placeholder="10-digit phone" value={form.phone} onChange={set("phone")} pattern="\d{10}" required />
        <input className="border rounded px-3 py-2 text-sm sm:col-span-2" placeholder="House no, street, area" value={form.addressLine} onChange={set("addressLine")} required />
        <input className="border rounded px-3 py-2 text-sm" placeholder="City" value={form.city} onChange={set("city")} required />
        <input className="border rounded px-3 py-2 text-sm" placeholder="State" value={form.state} onChange={set("state")} required />
        <input className="border rounded px-3 py-2 text-sm" placeholder="6-digit pincode" value={form.pincode} onChange={set("pincode")} pattern="\d{6}" required />
        <select className="border rounded px-3 py-2 text-sm" value={form.type} onChange={set("type")}>
          <option value="HOME">HOME</option>
          <option value="WORK">WORK</option>
        </select>
        <label className="text-sm flex items-center gap-2">
          <input type="checkbox" checked={!!form.isDefault} onChange={(e) => setForm({ ...form, isDefault: e.target.checked })} />
          Make default
        </label>
        <div className="sm:col-span-2 flex gap-3">
          <button className="px-6 py-2 bg-black text-white rounded-full text-sm">{editingId ? "Save changes" : "Add address"}</button>
          {editingId && <button type="button" className="underline text-sm" onClick={() => { setEditingId(null); setForm(EMPTY); setShowForm(false); }}>Cancel</button>}
        </div>
      </form>
      </>
      )}
    </div>
  );
}
