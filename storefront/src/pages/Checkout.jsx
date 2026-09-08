import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { Cart, Orders, Payments, Addresses, formatAddress, ASSETS } from "../lib/api";

export default function Checkout() {
  const location = useLocation();
  const directItems = location.state?.items || null; // Buy Now: [{productId, quantity}]
  const [saved, setSaved] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [manual, setManual] = useState("Mumbai 400001");
  const [useManual, setUseManual] = useState(false);
  const [status, setStatus] = useState("");

  useEffect(() => {
    Addresses.list()
      .then((a) => {
        setSaved(a);
        const def = a.find((x) => x.isDefault) || a[0];
        if (def) setSelectedId(def.id);
        else setUseManual(true);
      })
      .catch(() => setUseManual(true));
  }, []);

  const chosenAddress = () => {
    if (useManual || !saved.length) return manual;
    const a = saved.find((x) => x.id === selectedId) || saved[0];
    return formatAddress(a);
  };

  const checkout = async () => {
    setStatus(directItems ? "Creating order (stock reserve)..." : "Loading bag...");
    try {
      let items = directItems;
      if (!items) {
        const cart = await Cart.view();
        if (!cart.items.length) { setStatus("Bag is empty"); return; }
        items = cart.items.map((i) => ({ productId: i.productId, quantity: i.quantity }));
        setStatus("Creating order (stock reserve)...");
      }
      const order = await Orders.create(items, chosenAddress());
      setStatus(`Order ${order.id} PAYMENT_PENDING — creating Razorpay TEST order...`);
      const pay = await Payments.createOrder(order.id, order.totalAmount);
      setStatus("Opening Razorpay Checkout (TEST mode, no real money)...");
      Payments.pay({
        keyId: pay.keyId,
        razorpayOrderId: pay.razorpayOrderId,
        amount: order.totalAmount,
        onSuccess: async (resp) => {
          setStatus("Verifying signature on backend...");
          const v = await Payments.verify(resp.razorpay_order_id, resp.razorpay_payment_id, resp.razorpay_signature);
          setStatus(v.success
            ? `Payment ${v.status} ✓ — order ${order.id} CONFIRMED automatically. See Orders page.`
            : "Signature invalid — payment rejected.");
        },
        onFail: (e) => setStatus("Payment failed/dismissed: " + (e?.message || e?.description || e)),
      });
    } catch (e) { setStatus("Error: " + e.message); }
  };

  return (
    <div className="max-w-6xl mx-auto px-6 py-10 grid lg:grid-cols-2 gap-10">
      <div>
        <h1 className="font-[Noto_Serif] text-3xl mb-6">Secure payment</h1>
        {directItems && (
          <p className="text-sm bg-white border rounded-lg p-3 mb-4">
            Buy Now — {directItems.length} item(s), qty {directItems[0]?.quantity} (cart untouched)
          </p>
        )}
        <label className="text-sm font-medium">Delivery address</label>
        {saved.length > 0 && !useManual ? (
          <div className="space-y-2 mt-2 mb-3">
            {saved.map((a) => (
              <label key={a.id}
                className={`flex gap-3 border rounded-lg p-3 text-sm cursor-pointer ${selectedId === a.id ? "border-black bg-white" : "bg-white/60"}`}>
                <input type="radio" name="addr" checked={selectedId === a.id} onChange={() => setSelectedId(a.id)} />
                <span>
                  <b>{a.name}</b> <span className="text-gray-500">({a.type})</span><br />
                  {a.addressLine}, {a.city}, {a.state} {a.pincode}<br />
                  <span className="text-gray-500">Phone: {a.phone}</span>
                  {a.isDefault && <span className="ml-2 text-[11px] text-[#094cb2]">DEFAULT</span>}
                </span>
              </label>
            ))}
            <button className="text-sm underline" onClick={() => setUseManual(true)}>+ Use a different address</button>
          </div>
        ) : (
          <div className="mt-2 mb-3">
            <textarea className="border rounded-lg w-full px-3 py-2 text-sm" rows={3}
              value={manual} onChange={(e) => setManual(e.target.value)}
              placeholder="Name, address, city, state, pincode, phone" />
            {saved.length > 0 && (
              <button className="text-sm underline mt-1" onClick={() => setUseManual(false)}>Choose saved address</button>
            )}
            {saved.length === 0 && (
              <Link to="/addresses" className="text-sm underline mt-1 inline-block">Save addresses for faster checkout</Link>
            )}
          </div>
        )}
        <button onClick={checkout}
          className="mt-4 px-8 py-2.5 bg-[#094cb2] text-white rounded-full text-sm tracking-widest uppercase">
          Pay with Razorpay (TEST)
        </button>
        {status && <p className="mt-4 text-sm bg-white border rounded-lg p-3">{status}</p>}
        <p className="text-xs text-gray-500 mt-3">Secret key never leaves the backend — frontend only gets keyId + order id.</p>
      </div>
      <img src={ASSETS.checkout(1)} alt="Secure payment" className="rounded-xl object-cover w-full hidden lg:block"
        onError={(e) => { e.currentTarget.style.display = "none"; }} />
    </div>
  );
}
