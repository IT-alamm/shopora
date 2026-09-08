import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { Orders, Products, store } from "../lib/api";

const STEPS = ["PAYMENT_PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED"];

export default function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [names, setNames] = useState({});
  const [error, setError] = useState("");

  useEffect(() => {
    Orders.get(id)
      .then(async (o) => {
        setOrder(o);
        // Enrich product names best-effort
        const missing = [...new Set(o.items.filter((i) => !i.productName).map((i) => i.productId))];
        const entries = await Promise.allSettled(missing.map((pid) => Products.get(pid)));
        const map = {};
        entries.forEach((r, idx) => {
          if (r.status === "fulfilled") map[missing[idx]] = r.value.name;
        });
        setNames(map);
      })
      .catch((e) => setError(e.message));
  }, [id]);

  const downloadInvoice = () => {
    if (!order) return;
    const rows = order.items.map((i) => {
      const name = i.productName || names[i.productId] || `Product ${i.productId}`;
      return `<tr><td>${name}</td><td>${i.quantity}</td><td>Rs.${i.price}</td><td>Rs.${i.lineTotal}</td></tr>`;
    }).join("");
    const html = `<!DOCTYPE html><html><head><meta charset="utf-8"><title>Invoice - Order ${order.id}</title>
      <style>body{font-family:Arial,sans-serif;max-width:700px;margin:40px auto;padding:0 20px;color:#111}
      h1{font-size:28px}table{width:100%;border-collapse:collapse;margin:20px 0}
      th,td{border:1px solid #ccc;padding:8px;text-align:left}th{background:#f5f5f5}
      .meta{color:#555;font-size:14px}.total{text-align:right;font-size:18px;margin-top:10px}</style></head>
      <body><h1>Shopora — Tax Invoice</h1>
      <p class="meta">Order ID: ${order.id}<br>Status: ${order.status}<br>
      Customer: ${store.email || ""}<br>Ship to: ${order.shippingAddress || ""}<br>
      Generated: ${new Date().toLocaleString("en-IN")}</p>
      <table><tr><th>Item</th><th>Qty</th><th>Price</th><th>Total</th></tr>${rows}</table>
      <p class="total"><b>Grand Total: Rs.${order.totalAmount}</b></p>
      <p class="meta">Prices inclusive of all taxes. 7-day replacement for manufacturing defects.</p>
      <script>window.onload=()=>window.print()</script></body></html>`;
    const w = window.open("", "_blank");
    w.document.write(html);
    w.document.close();
  };

  if (error) return <p className="p-10 text-red-600">{error} — <Link className="underline" to="/orders">back</Link></p>;
  if (!order) return <p className="p-10">Loading order...</p>;

  const stepIdx = order.status === "CANCELLED" ? -1 : STEPS.indexOf(order.status);

  return (
    <div className="max-w-3xl mx-auto px-6 py-10">
      <Link to="/orders" className="text-sm underline text-gray-500">← All orders</Link>
      <div className="flex items-start justify-between mt-3 mb-6">
        <div>
          <h1 className="font-[Noto_Serif] text-3xl">Order {order.id}</h1>
          <p className="text-sm text-gray-500 mt-1">Ship to: {order.shippingAddress}</p>
          {store.email && <p className="text-sm text-gray-500">Account: {store.email}</p>}
        </div>
        <span className="px-4 py-1.5 rounded-full text-xs tracking-widest uppercase bg-[#e7ebff] text-[#094cb2]">
          {order.status}
        </span>
      </div>

      {/* Status timeline */}
      {stepIdx >= 0 ? (
        <div className="flex items-center gap-1 mb-8">
          {STEPS.map((s, i) => (
            <div key={s} className="flex-1">
              <div className={`h-1.5 rounded-full ${i <= stepIdx ? "bg-[#094cb2]" : "bg-gray-200"}`} />
              <p className={`text-[10px] mt-1 ${i <= stepIdx ? "text-black" : "text-gray-400"}`}>{s}</p>
            </div>
          ))}
        </div>
      ) : (
        <p className="mb-8 px-4 py-2 rounded-full text-xs bg-red-100 text-red-700 inline-block">CANCELLED</p>
      )}

      {/* Items */}
      <div className="bg-white border border-[#e3e2e3] rounded-xl overflow-hidden">
        {order.items.map((i, idx) => (
          <div key={idx} className="flex justify-between px-5 py-4 border-b last:border-0 border-[#eee]">
            <div>
              <Link to={`/product/${i.productId}`} className="font-medium hover:underline">
                {i.productName || names[i.productId] || `Product ${i.productId}`}
              </Link>
              <p className="text-sm text-gray-500">Qty {i.quantity} × ₹{i.price}</p>
            </div>
            <p>₹{i.lineTotal}</p>
          </div>
        ))}
        <div className="px-5 py-4 flex justify-between font-medium text-lg bg-[#faf9fa]">
          <span>Total</span><span>₹{order.totalAmount}</span>
        </div>
      </div>

      <button onClick={downloadInvoice}
        className="mt-6 px-8 py-2.5 bg-black text-white rounded-full text-sm tracking-widest uppercase">
        Download invoice
      </button>
    </div>
  );
}
