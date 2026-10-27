$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
. (Join-Path $root 'local-env.ps1')
Set-Location (Join-Path $root 'monolith-backend')
mvn spring-boot:run
