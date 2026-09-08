import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Products, Cart, ASSETS, productImage } from "../lib/api";

// Fallback specs when backend specifications are not seeded yet.
function fallbackSpecs(p) {
  const base = { Brand: "Shopora", Model: p.name, Category: p.categoryName || "-", Price: `₹${p.price}` };
  const cat = (p.categoryName || "").toLowerCase();
  if (cat.includes("electronic")) return { ...base, Warranty: "1 Year", "In-box": `1 x ${p.name}` };
  if (cat.includes("cloth")) return { ...base, Fabric: "Premium blend", Fit: "Regular", Care: "Machine wash", Sizes: "S to XXL" };
  if (cat.includes("grocer")) return { ...base, Storage: "Cool, dry place", "Shelf Life": "See pack" };
  return base;
}

export default function ProductDetail() {
  const { id } = useParams();
  const [p, setP] = useState(null);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");
  const [pin, setPin] = useState("");
  const [delivery, setDelivery] = useState("");

  useEffect(() => { Products.get(id).then(setP).catch((e) => setError(e.message)); }, [id]);
  if (error) return <p className="p-10 text-red-600">{error}</p>;
  if (!p) return <p className="p-10">Loading atelier piece...</p>;

  const specs = p.specifications && Object.keys(p.specifications).length > 0
    ? p.specifications : fallbackSpecs(p);
  const entries = Object.entries(specs);
  const highlights = entries.slice(0, 5);
  const outOfStock = (p.stock || 0) <= 0;

  const checkPin = (e) => {
    e.preventDefault();
    if (!/^\d{6}$/.test(pin)) { setDelivery("Enter a valid 6-digit pincode"); return; }
    const d = new Date(Date.now() + 4 * 864e5).toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short" });
    setDelivery(`Delivery to ${pin} by ${d} · Free above ₹499 · COD available`);
  };

  return (
    <div className="max-w-6xl mx-auto px-6 py-10">
      <div className="grid lg:grid-cols-2 gap-10">
        <div>
          <img src={productImage(p, Number(id) || 0)} alt={p.name}
            className="rounded-xl w-full h-[28rem] object-cover"
            onError={(e) => { e.currentTarget.src = ASSETS.product(2); }} />
          <div className="grid grid-cols-3 gap-3 mt-3">
            {[3, 4, 5].map((n) => (
              <img key={n} src={ASSETS.product(n)} alt="Atelier detail" className="rounded-lg h-28 w-full object-cover" />
            ))}
          </div>
        </div>
        <div>
          <p className="text-[11px] tracking-widest uppercase text-gray-500">{p.categoryName}</p>
          <h1 className="font-[Noto_Serif] text-4xl mt-2">{p.name}</h1>
          <p className="text-2xl mt-3">₹{p.price}</p>
          <p className={`text-sm mt-1 ${outOfStock ? "text-red-600 font-medium" : "text-gray-500"}`}>
            {outOfStock ? "Out of stock" : `Stock: ${p.stock} · ${p.active ? "Available" : "Inactive"}`}
          </p>
          <p className="text-gray-600 mt-4">{p.description}</p>

          <div className="flex gap-3 mt-6">
            <button
              disabled={outOfStock}
              className="px-8 py-3 bg-black text-white rounded-full text-sm tracking-widest uppercase disabled:opacity-40"
              onClick={async () => {
                try { await Cart.add(p.id, 1); setMsg("Added to bag"); }
                catch (e) { setMsg(e.message); }
              }}>
              Add to bag
            </button>
          </div>
          {msg && <p className="mt-3 text-sm">{msg}</p>}

          {/* Delivery checker */}
          <form onSubmit={checkPin} className="flex gap-2 mt-6 max-w-sm">
            <input value={pin} onChange={(e) => setPin(e.target.value)} placeholder="Delivery pincode"
              className="border rounded-full px-4 py-2 text-sm w-40" maxLength={6} />
            <button className="text-sm underline">Check</button>
          </form>
          {delivery && <p className="text-sm mt-2 text-gray-600">{delivery}</p>}

          {/* Highlights */}
          <div className="mt-8">
            <h2 className="font-[Noto_Serif] text-xl mb-3">Highlights</h2>
            <ul className="list-disc ml-5 text-sm text-gray-700 space-y-1">
              {highlights.map(([k, v]) => <li key={k}><b>{k}:</b> {v}</li>)}
            </ul>
          </div>
        </div>
      </div>

      {/* Specifications */}
      <div className="mt-12 bg-white border border-[#e3e2e3] rounded-xl overflow-hidden">
        <h2 className="font-[Noto_Serif] text-2xl px-6 pt-6 pb-2">Specifications</h2>
        <table className="w-full text-sm">
          <tbody>
            {entries.map(([k, v], i) => (
              <tr key={k} className={i % 2 ? "bg-[#faf9fa]" : ""}>
                <td className="px-6 py-2.5 text-gray-500 w-1/3">{k}</td>
                <td className="px-6 py-2.5">{v}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Product description */}
      <div className="mt-8">
        <h2 className="font-[Noto_Serif] text-2xl mb-3">Product Description</h2>
        <p className="text-gray-600 text-sm leading-relaxed max-w-3xl">
          {p.description} Crafted and quality-checked by the Shopora atelier before dispatch.
          Easy 7-day replacement for manufacturing defects. Prices inclusive of all taxes.
        </p>
      </div>
    </div>
  );
}
