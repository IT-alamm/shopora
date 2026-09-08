import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Products, Cart, ASSETS, productImage } from "../lib/api";

const SLIDES = [
  {
    kicker: "Vol. XIV — Limited Release",
    titleA: "Spring/Summer ",
    accent: "Craftsmanship",
    titleB: " Editions",
    copy: "Hand-stitched Tuscan calfskin, bespoke tailoring, and kiln-fired vessels — live from our catalog service.",
    img: ASSETS.homeHero,
    alt: "Atelier craftsman",
    cta: "Explore curation",
    link: "/collections",
  },
  {
    kicker: "The Tailored Edit",
    titleA: "Dressed with ",
    accent: "Intent",
    titleB: "",
    copy: "Sculpted trench coats, handloom kurtas and sharp denim — clothing cut for every occasion.",
    img: ASSETS.home(10),
    alt: "Tailored blazer look",
    cta: "Shop clothing",
    link: "/collections?category=2",
  },
  {
    kicker: "Atelier Icons",
    titleA: "Leather goods, ",
    accent: "for life",
    titleB: "",
    copy: "Briefcases, journals and desk sets burnished by hand — icons that age beautifully with you.",
    img: ASSETS.home(12),
    alt: "Leather briefcase",
    cta: "Shop the icons",
    link: "/collections",
  },
];

function HeroCarousel() {
  const [i, setI] = useState(0);
  const [paused, setPaused] = useState(false);
  useEffect(() => {
    if (paused) return;
    const t = setInterval(() => setI((v) => (v + 1) % SLIDES.length), 6000);
    return () => clearInterval(t);
  }, [paused]);
  const s = SLIDES[i];
  return (
    <section className="bg-[#f5f3f4]"
      onMouseEnter={() => setPaused(true)} onMouseLeave={() => setPaused(false)}>
      <div className="max-w-7xl mx-auto px-6 py-14 grid lg:grid-cols-2 gap-10 items-center">
        <div key={"t" + i}>
          <p className="text-xs tracking-[0.25em] uppercase text-gray-500 mb-4">{s.kicker}</p>
          <h1 className="font-[Noto_Serif] text-5xl leading-tight mb-6">
            {s.titleA}<span className="italic text-[#094cb2]">{s.accent}</span>{s.titleB}
          </h1>
          <p className="text-gray-600 max-w-xl mb-8">{s.copy}</p>
          <Link to={s.link} className="inline-block bg-black text-white px-8 py-3 rounded-full text-sm tracking-widest uppercase">
            {s.cta}
          </Link>
        </div>
        <div className="relative" key={"m" + i}>
          <img src={s.img} alt={s.alt} className="rounded-xl object-cover w-full h-96" />
          <button aria-label="Previous slide"
            onClick={() => setI((i - 1 + SLIDES.length) % SLIDES.length)}
            className="absolute left-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/90 shadow text-lg">‹</button>
          <button aria-label="Next slide"
            onClick={() => setI((i + 1) % SLIDES.length)}
            className="absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/90 shadow text-lg">›</button>
          <div className="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-2">
            {SLIDES.map((_, d) => (
              <button key={d} aria-label={`Go to slide ${d + 1}`} onClick={() => setI(d)}
                className={`h-2 rounded-full transition-all ${d === i ? "w-6 bg-black" : "w-2 bg-black/30"}`} />
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

export default function Home() {
  const [data, setData] = useState(null);
  const [keyword, setKeyword] = useState("");
  const [categories, setCategories] = useState([]);
  const [categoryId, setCategoryId] = useState(null);
  const [error, setError] = useState("");
  const [msg, setMsg] = useState("");

  const load = async (kw = "", cat = categoryId) => {
    setError("");
    try { setData(await Products.list({ keyword: kw, size: 12, categoryId: cat })); }
    catch (e) { setError(e.message); }
  };
  useEffect(() => {
    Products.categories().then(setCategories).catch(() => {});
    load();
  }, []);

  // Header search (?q=) support
  const [params] = useSearchParams();
  useEffect(() => {
    const q = params.get("q") || "";
    setKeyword(q);
    load(q, categoryId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params]);

  const pickCategory = (id) => {
    setCategoryId(id);
    load(keyword, id);
  };

  const items = data?.content ?? [];
  return (
    <div>
      <HeroCarousel />

      <section className="max-w-7xl mx-auto px-6 py-12">
        {error && <p className="text-red-600 mb-4">{error} — is gateway :8080 running?</p>}
        {msg && (
        <div className="fixed top-24 left-1/2 -translate-x-1/2 z-50 bg-black text-white text-sm px-6 py-3 rounded-full shadow-lg">
          {msg} — <Link to="/cart" className="underline">View Cart</Link>
        </div>
      )}
        {/* Collections — category pills */}
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
        {items.length === 0 && !error && <p className="text-gray-500">No products yet — create some via product-service.</p>}
      </section>
    </div>
  );
}
