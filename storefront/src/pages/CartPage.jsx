import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Cart } from "../lib/api";

export default function CartPage() {
  const nav = useNavigate();
  const [cart, setCart] = useState(null);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");
  const [busyId, setBusyId] = useState(null);

  const load = () => Cart.view().then(setCart).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const changeQty = async (item, delta) => {
    setMsg("");
    setBusyId(item.id);
    try {
      const next = item.quantity + delta;
      if (next < 1) {
        await Cart.remove(item.id);
      } else {
        await Cart.update(item.id, next);
      }
      await load();
    } catch (e) { setMsg(e.message); }
    finally { setBusyId(null); }
  };

  const removeOne = async (item) => {
    setMsg("");
    setBusyId(item.id);
    try {
      await Cart.remove(item.id);
      await load();
    } catch (e) { setMsg(e.message); }
    finally { setBusyId(null); }
  };

  const buyNow = (item) => {
    nav("/checkout", { state: { items: [{ productId: item.productId, quantity: item.quantity }] } });
  };

  if (error) return <p className="p-10 text-red-600">{error} — <Link className="underline" to="/auth">sign in first</Link></p>;
  if (!cart) return <p className="p-10">Loading shopping bag...</p>;

  return (
    <div className="max-w-3xl mx-auto px-6 py-10">
      <h1 className="font-[Noto_Serif] text-3xl mb-6">Shopping bag</h1>
      {msg && <p className="text-sm mb-4 text-red-600">{msg}</p>}
      {cart.items.map((i) => (
        <div key={i.id} className="border-b border-[#e3e2e3] py-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">{i.productName || `Product ${i.productId}`}</p>
              <p className="text-sm text-gray-500">₹{i.price} each</p>
            </div>
            <p className="font-medium">₹{i.lineTotal}</p>
          </div>
          <div className="flex items-center gap-2 mt-3">
            <div className="flex items-center border rounded-full">
              <button disabled={busyId === i.id}
                className="px-3 py-1 text-lg disabled:opacity-40"
                onClick={() => changeQty(i, -1)}>−</button>
              <span className="w-8 text-center text-sm">{i.quantity}</span>
              <button disabled={busyId === i.id}
                className="px-3 py-1 text-lg disabled:opacity-40"
                onClick={() => changeQty(i, 1)}>+</button>
            </div>
            <button disabled={busyId === i.id}
              className="text-red-600 text-sm underline disabled:opacity-40"
              onClick={() => removeOne(i)}>Remove</button>
            <button disabled={busyId === i.id}
              className="ml-auto px-4 py-1.5 bg-black text-white rounded-full text-xs tracking-widest uppercase disabled:opacity-40"
              onClick={() => buyNow(i)}>Buy now</button>
          </div>
        </div>
      ))}
      {cart.items.length === 0 && <p className="text-gray-500">Your bag is empty.</p>}
      <p className="text-xl mt-6">Total: ₹{cart.total}</p>
      {cart.items.length > 0 && (
        <Link to="/checkout" className="inline-block mt-4 px-8 py-2.5 bg-[#094cb2] text-white rounded-full text-sm tracking-widest uppercase">
          Secure checkout
        </Link>
      )}
    </div>
  );
}
