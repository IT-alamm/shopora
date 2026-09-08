import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Orders } from "../lib/api";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => { Orders.list().then(setOrders).catch((e) => setError(e.message)); }, []);

  return (
    <div className="max-w-3xl mx-auto px-6 py-10">
      <h1 className="font-[Noto_Serif] text-3xl mb-6">Account & orders</h1>
      {error && <p className="text-red-600">{error}</p>}
      {orders.map((o) => (
        <Link key={o.id} to={`/orders/${o.id}`}
          className="block bg-white border border-[#e3e2e3] rounded-xl p-5 mb-4 shadow-sm hover:shadow-md transition-shadow">
          <p className="font-medium">Order {o.id} · <span className="text-[#094cb2]">{o.status}</span> · ₹{o.totalAmount}</p>
          <p className="text-sm text-gray-500">{o.shippingAddress}</p>
          {o.items.map((i, idx) => (
            <p key={idx} className="text-sm mt-1">{i.productName || `Product ${i.productId}`} × {i.quantity} = ₹{i.lineTotal}</p>
          ))}
          <p className="text-xs text-gray-400 mt-2 underline">View details + invoice →</p>
        </Link>
      ))}
      {orders.length === 0 && !error && <p className="text-gray-500">No orders yet — checkout from your bag.</p>}
    </div>
  );
}
