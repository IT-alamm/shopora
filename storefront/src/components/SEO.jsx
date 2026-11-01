import { Helmet } from "react-helmet-async";
const SITE_NAME = "Shopora";
const DEFAULT_DESCRIPTION = "Shop thoughtfully curated clothing, accessories, and everyday essentials at Shopora. Explore our collections and shop online.";
export default function SEO({ title, description = DEFAULT_DESCRIPTION, path, noindex = false, image, type = "website", structuredData }) {
  const siteUrl = (import.meta.env.VITE_SITE_URL || window.location.origin).replace(/\/$/, "");
  const canonical = path ? `${siteUrl}${path}` : siteUrl;
  const fullTitle = title ? `${title} | ${SITE_NAME}` : `${SITE_NAME} | Curated Fashion & Everyday Essentials`;
  return <Helmet prioritizeSeoTags>
    <title>{fullTitle}</title>
    <meta name="description" content={description} />
    <meta name="robots" content={noindex ? "noindex, nofollow" : "index, follow, max-image-preview:large"} />
    <link rel="canonical" href={canonical} />
    <meta property="og:site_name" content={SITE_NAME} />
    <meta property="og:type" content={type} />
    <meta property="og:title" content={fullTitle} />
    <meta property="og:description" content={description} />
    <meta property="og:url" content={canonical} />
    {image && <meta property="og:image" content={image.startsWith("http") ? image : `${siteUrl}${image}`} />}
    <meta name="twitter:card" content={image ? "summary_large_image" : "summary"} />
    <meta name="twitter:title" content={fullTitle} />
    <meta name="twitter:description" content={description} />
    {image && <meta name="twitter:image" content={image.startsWith("http") ? image : `${siteUrl}${image}`} />}
    {structuredData && <script type="application/ld+json">{JSON.stringify(structuredData)}</script>}
  </Helmet>;
}
