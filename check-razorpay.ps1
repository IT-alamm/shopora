# Usage: powershell -ExecutionPolicy Bypass -File check-razorpay.ps1
# Prints MASKED diagnostics only (secret value never printed).
. (Join-Path $PSScriptRoot "local-env.ps1")

$id = $env:RAZORPAY_KEY_ID
$sec = $env:RAZORPAY_KEY_SECRET

Write-Host ("key_id_prefix=[" + $id.Substring(0, [Math]::Min(13, $id.Length)) + "] key_id_len=[" + $id.Length + "]") -ForegroundColor Cyan
Write-Host ("secret_len=[" + $sec.Length + "]") -ForegroundColor Cyan
if ($id -notlike "rzp_test_*") { Write-Host "WARN: key is NOT a test key (should start with rzp_test_)" -ForegroundColor Yellow }

# Live auth test: create Rs 1.00 test order directly against Razorpay (no service needed)
$pair = "$id`:$sec"
$basic = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes($pair))
try {
    $res = Invoke-RestMethod -Method Post -Uri "https://api.razorpay.com/v1/orders" `
        -Headers @{ Authorization = "Basic $basic" } `
        -ContentType "application/json" `
        -Body '{"amount":100,"currency":"INR","receipt":"diag-test"}'
    Write-Host ("RAZORPAY_AUTH_OK order_id=[" + $res.id + "]") -ForegroundColor Green
} catch {
    $msg = $_.Exception.Message
    if ($_.ErrorDetails) { $msg += " | " + $_.ErrorDetails.Message.Substring(0, [Math]::Min(200, $_.ErrorDetails.Message.Length)) }
    Write-Host ("RAZORPAY_AUTH_FAIL " + $msg) -ForegroundColor Red
    Write-Host "Fix: dashboard Test Mode keys re-copy karo, local-env.ps1 update karo, payment-service restart karo." -ForegroundColor Yellow
}
