# Usage: powershell -ExecutionPolicy Bypass -File run-service.ps1 order-service
#        powershell -ExecutionPolicy Bypass -File run-service.ps1 product-service
# Always loads local-env.ps1 from this script's folder first, so no more
# "Access denied" due to forgotten / wrong-folder env loading.
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("eureka-server", "api-gateway", "auth-service", "product-service", "cart-service", "order-service", "payment-service", "user-service")]
    [string]$Service
)

$ErrorActionPreference = "Stop"

# 1. Load env from repo root (where this script lives)
. (Join-Path $PSScriptRoot "local-env.ps1")

# 2. Masked verification (never prints the password itself)
if ([string]::IsNullOrEmpty($env:MYSQL_USER)) { throw "MYSQL_USER is empty after loading local-env.ps1" }
if ([string]::IsNullOrEmpty($env:MYSQL_PASSWORD)) { throw "MYSQL_PASSWORD is empty - edit local-env.ps1 first" }
Write-Host ("DB user=[" + $env:MYSQL_USER + "] service=[" + $Service + "]") -ForegroundColor Cyan

# 3. TCP check for MySQL
$tcp = Test-NetConnection -ComputerName "localhost" -Port 3306 -WarningAction SilentlyContinue
if (-not $tcp.TcpTestSucceeded) { throw "MySQL is not reachable on localhost:3306. Start MySQL80 service first." }
Write-Host "MySQL port 3306 reachable." -ForegroundColor Green

# 4. Run the service
Set-Location (Join-Path $PSScriptRoot $Service)
mvn spring-boot:run
