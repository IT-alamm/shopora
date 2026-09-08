import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Products, Cart, ASSETS, productImage } from "../lib/api";

// Collections: category-first browsing, no hero banner.
export default function Collections() {
  const [data, setData] = useState(null);
  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState(null);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");

  const load = async (cat = categoryId) => {
    setError("");
    try { setData(await Products.list({ size: 24, categoryId: cat })); }
    catch (e) { setError(e.message); }
  };
  useEffect(() => {
    Products.categories().then(setCategories).catch(() => {});
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const [params] = useSearchParams();
  useEffect(() => {
    const cid = params.get("category");
    const id = cid ? Number(cid) : null;
    setCategoryId(id);
    load(id);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params]);

  const pickCategory = (id) => {
    setCategoryId(id);
    load(id);
  };

  const items = data?.content ?? [];
  const activeName = categoryId === null ? "All" :
    (categories.find((c) => c.id === categoryId)?.name || "Collection");

  return (
    <div className="max-w-7xl mx-auto px-6 py-12">
      <p className="text-xs tracking-[0.25em] uppercase text-gray-500">Collections</p>
      <h1 className="font-[Noto_Serif] text-4xl mt-2 mb-8">{activeName}</h1>
      {error && <p className="text-red-600 mb-4">{error} — is gateway :8080 running?</p>}
      {msg && (
        <div className="fixed top-24 left-1/2 -translate-x-1/2 z-50 bg-black text-white text-sm px-6 py-3 rounded-full shadow-lg">
          {msg} — <Link to="/cart" className="underline">View Cart</Link>
        </div>
      )}
      <div className="flex flex-wrap gap-2 mb-8">
        <button
          onClick={() => pickCategory(null)}
          className={`px-5 py-2 rounded-full text-xs tracking-widest uppercase border ${categoryId === null ? "bg-black text-white border-black" : "bg-white text-gray-600 border-[#e3e2e3] hover:border-black"}`}>
          All
        </button>
        {categories.map((c) => (
          <button
            key={c.id}
            onClick={() => pickCategory(c.id)}
            className={`px-5 py-2 rounded-full text-xs tracking-widest uppercase border ${categoryId === c.id ? "bg-black text-white border-black" : "bg-white text-gray-600 border-[#e3e2e3] hover:border-black"}`}>
            {c.name}
          </button>
        ))}
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {items.map((p, i) => (
          <div key={p.id} className="bg-white border border-[#e3e2e3] rounded-xl overflow-hidden shadow-sm">
            <img src={productImage(p, i)} alt={p.name}
              className="h-64 w-full object-cover"
              onError={(e) => { e.currentTarget.src = ASSETS.home(4 + (i % 8)); }} />
            <div className="p-5">
              <p className="text-[11px] tracking-widest uppercase text-gray-500">{p.categoryName} · Stock {p.stock}</p>
              <h2 className="font-[Noto_Serif] text-xl mt-1">{p.name}</h2>
              <p className="text-lg mt-2">₹{p.price}</p>
              <div className="flex gap-2 mt-4">
                <Link to={`/product/${p.id}`} className="px-4 py-2 border rounded-full text-sm">View</Link>
                <button
                  className="px-4 py-2 bg-black text-white rounded-full text-sm"
                  onClick={async () => {
                    try { await Cart.add(p.id, 1); setMsg(`${p.name} added to bag`); }
                    catch (e) { setMsg(e.message); }
                  }}>
                  Add to bag
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
      {items.length === 0 && !error && <p className="text-gray-500">No products in this collection yet.</p>}
    </div>
  );
}
