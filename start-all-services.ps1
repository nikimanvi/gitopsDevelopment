# Microservices - Start All Services Script

# Colors for output
$Green = 'Green'
$Yellow = 'Yellow'
$Red = 'Red'

Write-Host "========================================" -ForegroundColor $Green
Write-Host " Starting All Microservices" -ForegroundColor $Green
Write-Host "========================================" -ForegroundColor $Green

# Start User Service (Port 8081)
Write-Host "`nStarting User Service on port 8081..." -ForegroundColor $Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\user-service'; mvn spring-boot:run"
Start-Sleep -Seconds 3

# Start Product Service (Port 8082)
Write-Host "Starting Product Service on port 8082..." -ForegroundColor $Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\product-service'; mvn spring-boot:run"
Start-Sleep -Seconds 3

# Start Order Service (Port 8083)
Write-Host "Starting Order Service on port 8083..." -ForegroundColor $Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\order-service'; mvn spring-boot:run"
Start-Sleep -Seconds 3

# Start API Gateway (Port 8080)
Write-Host "Starting API Gateway on port 8080..." -ForegroundColor $Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\gateway-service'; mvn spring-boot:run"

Write-Host "`n========================================" -ForegroundColor $Green
Write-Host " All Services Started!" -ForegroundColor $Green
Write-Host "========================================" -ForegroundColor $Green
Write-Host "`nServices:" -ForegroundColor $Yellow
Write-Host "  - API Gateway:     http://localhost:8080" -ForegroundColor $Green
Write-Host "  - User Service:    http://localhost:8081" -ForegroundColor $Green
Write-Host "  - Product Service: http://localhost:8082" -ForegroundColor $Green
Write-Host "  - Order Service:   http://localhost:8083" -ForegroundColor $Green
Write-Host "`nWait 30-60 seconds for all services to fully start..." -ForegroundColor $Yellow
