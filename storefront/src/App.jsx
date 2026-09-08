import { BrowserRouter, Routes, Route } from "react-router-dom";
import Layout from "./components/Layout.jsx";
import Home from "./pages/Home.jsx";
import Collections from "./pages/Collections.jsx";
import AuthPage from "./pages/Auth.jsx";
import ProductDetail from "./pages/ProductDetail.jsx";
import CartPage from "./pages/CartPage.jsx";
import Checkout from "./pages/Checkout.jsx";
import OrdersPage from "./pages/OrdersPage.jsx";
import OrderDetail from "./pages/OrderDetail.jsx";
import AddressesPage from "./pages/Addresses.jsx";

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/collections" element={<Collections />} />
          <Route path="/auth" element={<AuthPage />} />
          <Route path="/product/:id" element={<ProductDetail />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/checkout" element={<Checkout />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/:id" element={<OrderDetail />} />
          <Route path="/addresses" element={<AddressesPage />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}
