# Seed Flipkart-style specifications for existing products (PUT /api/products/{id}).
# Run AFTER restarting product-service with the new specifications column.
# Usage: powershell -ExecutionPolicy Bypass -File seed-specs.ps1
$ErrorActionPreference = "Continue"
$BASE = "http://localhost:8082"

$specs = @{
  1  = @{ Brand = "Shopora"; Model = "Phone X"; Display = '6.5" AMOLED, 120Hz'; RAM = "8 GB"; Storage = "128 GB"; "Rear Camera" = "50MP + 12MP"; "Front Camera" = "16MP"; Battery = "5000 mAh"; OS = "Android 14"; Warranty = "1 Year Manufacturer" }
  2  = @{ Brand = "Shopora"; Model = "Laptop Pro 14"; Processor = "Intel i7 13th Gen"; RAM = "16 GB DDR5"; Storage = "512 GB SSD"; Display = '14" 2.8K IPS'; Battery = "70 Wh, up to 12h"; OS = "Windows 11"; Weight = "1.4 kg"; Warranty = "1 Year Onsite" }
  3  = @{ Brand = "Shopora"; Model = "Aria Buds"; Type = "True Wireless (TWS)"; ANC = "Hybrid Active Noise Cancellation"; "Battery Life" = "30h with charging case"; Bluetooth = "v5.3"; Driver = "11 mm"; Warranty = "1 Year" }
  4  = @{ Brand = "Shopora"; Model = "Pulse S2"; Display = '1.85" AMOLED'; GPS = "Built-in"; Sensors = "SpO2 + Heart-rate"; "Battery Life" = "Up to 7 days"; "Water Resistance" = "5 ATM"; Warranty = "1 Year" }
  5  = @{ Brand = "Shopora"; Model = "Boombox Mini"; Output = "20 W Stereo"; "Battery Life" = "Up to 12h"; Bluetooth = "v5.2"; Rating = "IPX5 splash-proof"; Warranty = "1 Year" }
  6  = @{ Brand = "Shopora Atelier"; Model = "Sculpted Trench"; Fabric = "Cotton-Gabardine, water-resistant"; Fit = "Regular tailored"; Closure = "Horn buttons"; Pockets = "4 (2 flap + 2 inner)"; Care = "Machine wash cold"; Sizes = "S to XXL" }
  7  = @{ Brand = "Shopora"; Model = "Banarasi Silk Saree"; Fabric = "Silk blend with zari border"; Length = "6.3 m with blouse piece"; Occasion = "Festive / Wedding"; Care = "Dry clean only"; Weave = "Banarasi" }
  8  = @{ Brand = "Shopora"; Model = "Rider Denim Jacket"; Fabric = "12 oz denim"; Fit = "Regular"; Closure = "Metal buttons"; Pockets = "4"; Care = "Machine wash" }
  9  = @{ Brand = "Shopora"; Model = "Strider Run"; Type = "Running shoes"; Upper = "Engineered mesh"; Sole = "EVA + rubber"; Weight = "280 g (UK 9)"; Care = "Wipe clean"; Sizes = "UK 6 to 11" }
  10 = @{ Brand = "Shopora Pantry"; Item = "Basmati Rice"; Weight = "5 kg"; Grain = "Extra-long, aged 2 years"; "Shelf Life" = "12 months"; Storage = "Airtight container"; Type = "Premium" }
  11 = @{ Brand = "Shopora Pantry"; Item = "Olive Oil"; Volume = "1 L"; Type = "Cold-pressed, extra-virgin"; "Shelf Life" = "12 months"; Storage = "Cool, dry place" }
  12 = @{ Brand = "Shopora Pantry"; Item = "Masala Combo"; Contents = "6 blends x 100 g (garam, chana, chai, pav bhaji, biryani, chaat)"; "Shelf Life" = "12 months"; Storage = "Airtight jars" }
  13 = @{ Brand = "Shopora Pantry"; Item = "Fresh Watermelon"; Weight = "Approx 2-3 kg"; Type = "Farm-fresh, whole fruit"; "Shelf Life" = "5 days refrigerated"; Storage = "Refrigerate after cutting" }
}

$ok = 0
foreach ($id in ($specs.Keys | Sort-Object)) {
  try {
    $body = @{ specifications = $specs[$id] } | ConvertTo-Json -Depth 4
    $r = Invoke-RestMethod -Method Put -Uri "$BASE/api/products/$id" -ContentType "application/json" -Body $body -TimeoutSec 10
    $ok++
    Write-Host "Specs saved: [$id] $($r.name) ($($r.specifications.Count) fields)" -ForegroundColor Green
  } catch {
    Write-Host "FAILED [$id]: $($_.Exception.Message) $($_.ErrorDetails)" -ForegroundColor Red
  }
}
echo "SPECS_DONE ok=$ok/13"
