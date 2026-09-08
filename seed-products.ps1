# Seed categories + products into product-service (direct :8082).
# Usage: powershell -ExecutionPolicy Bypass -File seed-products.ps1
$ErrorActionPreference = "Continue"
$BASE = "http://localhost:8082"

function Post-Json($url, $body) {
  try {
    return Invoke-RestMethod -Method Post -Uri $url -ContentType "application/json" -Body ($body | ConvertTo-Json -Depth 4)
  } catch {
    $msg = $_.Exception.Message
    if ($_.ErrorDetails) { $msg += " | " + $_.ErrorDetails.Message }
    Write-Host "POST $url FAILED: $msg" -ForegroundColor Yellow
    return $null
  }
}

# 1. Categories (skip if exists)
$cats = Invoke-RestMethod -Uri "$BASE/api/categories" -TimeoutSec 10
$byName = @{}
foreach ($c in $cats) { $byName[$c.name] = $c.id }
Write-Host "Existing categories: $($byName.Keys -join ', ')" -ForegroundColor Cyan

foreach ($c in @(
  @{ name = "electronics"; description = "Phones, laptops, audio & wearables" },
  @{ name = "clothing"; description = "Apparel, footwear & fashion" },
  @{ name = "grocery"; description = "Pantry staples & everyday essentials" }
)) {
  if (-not $byName.ContainsKey($c.name)) {
    $created = Post-Json "$BASE/api/categories" $c
    if ($created) { $byName[$c.name] = $created.id; Write-Host "Created category $($c.name) id=$($created.id)" -ForegroundColor Green }
  }
}

# 2. Products (imageUrl omitted -> storefront shows local atelier shots)
$products = @(
  # electronics
  @{ name = "Laptop Pro 14"; description = "16GB RAM, 512GB SSD laptop"; price = 74999; stock = 5; categoryId = $byName["electronics"] },
  @{ name = "Wireless Earbuds"; description = "ANC wireless earbuds, 30h case"; price = 1999; stock = 50; categoryId = $byName["electronics"] },
  @{ name = "Smart Watch S2"; description = "AMOLED smartwatch, SpO2 + GPS"; price = 8999; stock = 20; categoryId = $byName["electronics"] },
  @{ name = "Bluetooth Speaker"; description = "20W portable speaker, IPX5"; price = 3499; stock = 30; categoryId = $byName["electronics"] },
  # clothing
  @{ name = "Sculpted Trench Coat"; description = "Tailored trench, water-resistant"; price = 12999; stock = 15; categoryId = $byName["clothing"] },
  @{ name = "Cotton Kurta"; description = "Handloom cotton kurta"; price = 1499; stock = 40; categoryId = $byName["clothing"] },
  @{ name = "Denim Jacket"; description = "Classic denim jacket"; price = 2999; stock = 25; categoryId = $byName["clothing"] },
  @{ name = "Running Shoes"; description = "Lightweight running shoes"; price = 4999; stock = 30; categoryId = $byName["clothing"] },
  # grocery
  @{ name = "Basmati Rice 5kg"; description = "Premium long-grain basmati"; price = 749; stock = 100; categoryId = $byName["grocery"] },
  @{ name = "Mustard Oil 1L"; description = "Cold-pressed mustard oil"; price = 299; stock = 80; categoryId = $byName["grocery"] },
  @{ name = "Masala Combo Pack"; description = "6 essential spice blends"; price = 499; stock = 60; categoryId = $byName["grocery"] },
  @{ name = "Organic Honey 500g"; description = "Raw unprocessed honey"; price = 399; stock = 70; categoryId = $byName["grocery"] }
)

$ok = 0
foreach ($p in $products) {
  $r = Post-Json "$BASE/api/products" $p
  if ($r) { $ok++; Write-Host "Created: $($r.name) (id=$($r.id), $($r.categoryName), Rs.$($r.price))" -ForegroundColor Green }
}

# 3. Verify
$all = Invoke-RestMethod -Uri "$BASE/api/products?page=0&size=50" -TimeoutSec 10
Write-Host "SEED_DONE products_created=$ok total_in_db=$($all.totalElements)" -ForegroundColor Cyan
$all.content | ForEach-Object { Write-Host " - [$($_.id)] $($_.name) | $($_.categoryName) | Rs.$($_.price) | stock $($_.stock)" }
