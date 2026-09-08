# Assign verified-correct photos to all products (PUT /api/products/{id}).
# Photo content was visually verified 1-by-1. Products 7/11/13 are renamed
# to match their photos (saree, olive oil, watermelon).
# Usage: powershell -ExecutionPolicy Bypass -File seed-photos.ps1
$ErrorActionPreference = "Continue"
$BASE = "http://localhost:8082"
$U = "https://images.unsplash.com"

$updates = @(
  @{ id = 1;  body = @{ imageUrl = "$U/photo-1511707171634-5f897ff02aa9?w=800&q=80" } },
  @{ id = 2;  body = @{ imageUrl = "$U/photo-1496181133206-80ce9b88a853?w=800&q=80" } },
  @{ id = 3;  body = @{ imageUrl = "$U/photo-1590658268037-6bf12165a8df?w=800&q=80" } },
  @{ id = 4;  body = @{ imageUrl = "$U/photo-1523275335684-37898b6baf30?w=800&q=80" } },
  @{ id = 5;  body = @{ imageUrl = "$U/photo-1545454675-3531b543be5d?w=800&q=80" } },
  @{ id = 6;  body = @{ imageUrl = "$U/photo-1539533018447-63fcce2678e3?w=800&q=80" } },
  @{ id = 7;  body = @{ name = "Banarasi Silk Saree"; description = "Traditional Banarasi silk saree with zari border, blouse piece included"; imageUrl = "$U/photo-1610030469983-98e550d6193c?w=800&q=80" } },
  @{ id = 8;  body = @{ imageUrl = "$U/photo-1544022613-e87ca75a784a?w=800&q=80" } },
  @{ id = 9;  body = @{ imageUrl = "$U/photo-1542291026-7eec264c27ff?w=800&q=80" } },
  @{ id = 10; body = @{ imageUrl = "$U/photo-1586201375761-83865001e31c?w=800&q=80" } },
  @{ id = 11; body = @{ name = "Olive Oil 1L"; description = "Cold-pressed olive oil, ideal for salads and cooking"; imageUrl = "$U/photo-1474979266404-7eaacbcd87c5?w=800&q=80" } },
  @{ id = 12; body = @{ imageUrl = "$U/photo-1596040033229-a9821ebd058d?w=800&q=80" } },
  @{ id = 13; body = @{ name = "Fresh Watermelon"; description = "Farm-fresh watermelon, approx 2-3 kg"; price = 99; imageUrl = "$U/photo-1587049352846-4a222e784d38?w=800&q=80" } }
)

$ok = 0
foreach ($u in $updates) {
  try {
    $r = Invoke-RestMethod -Method Put -Uri "$BASE/api/products/$($u.id)" -ContentType "application/json" -Body ($u.body | ConvertTo-Json -Depth 4) -TimeoutSec 10
    $ok++
    Write-Host "Photo set: [$($u.id)] $($r.name)" -ForegroundColor Green
  } catch {
    Write-Host "FAILED [$($u.id)]: $($_.Exception.Message) $($_.ErrorDetails)" -ForegroundColor Red
  }
}
echo "PHOTOS_DONE ok=$ok/13"
