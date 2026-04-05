# Build All Microservices Script

$Green = 'Green'
$Yellow = 'Yellow'
$Red = 'Red'

Write-Host "=============================================" -ForegroundColor $Green
Write-Host " Building All Microservices" -ForegroundColor $Green
Write-Host "=============================================" -ForegroundColor $Green

$services = @("user-service", "product-service", "order-service", "gateway-service")

foreach ($service in $services) {
    Write-Host "`nBuilding $service..." -ForegroundColor $Yellow
    Set-Location "$PSScriptRoot\$service"
    mvn clean install -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[SUCCESS] $service built successfully" -ForegroundColor $Green
    } else {
        Write-Host "[FAILED] $service build failed" -ForegroundColor $Red
    }
}

Set-Location $PSScriptRoot
Write-Host "`n========================================" -ForegroundColor $Green
Write-Host " Build Complete!" -ForegroundColor $Green
Write-Host "========================================" -ForegroundColor $Green
