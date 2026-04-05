# push-to-ecr.ps1
# Builds all microservice images, tags, and pushes them to AWS ECR.
# Usage: .\push-to-ecr.ps1 -Region us-east-1

param(
    [string]$Region = "us-east-1"
)

$ErrorActionPreference = "Stop"

# --- Check Docker is running ---
Write-Host "Checking Docker is running..."
try {
    docker info | Out-Null
} catch {
    Write-Error "Docker is not running. Start Docker Desktop and wait ~30 seconds, then retry."
    exit 1
}
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker is not running. Start Docker Desktop and wait ~30 seconds, then retry."
    exit 1
}
Write-Host "Docker is running."
Write-Host ""

# Resolve AWS account ID
$AccountId = $(aws sts get-caller-identity --query Account --output text)
if (-not $AccountId) { Write-Error "Could not resolve AWS Account ID. Run 'aws configure' first."; exit 1 }

$EcrBase = "$AccountId.dkr.ecr.$Region.amazonaws.com"

Write-Host "Account : $AccountId"
Write-Host "Region  : $Region"
Write-Host "ECR Base: $EcrBase"
Write-Host ""

# --- Authenticate Docker to ECR ---
Write-Host "Authenticating Docker to ECR..."
aws ecr get-login-password --region $Region | docker login --username AWS --password-stdin $EcrBase

# --- Create repos if they don't exist ---
$Services = @("gateway-service", "user-service", "product-service", "order-service")

foreach ($Service in $Services) {
    $ErrorActionPreference = "SilentlyContinue"
    aws ecr describe-repositories --repository-names $Service --region $Region 2>&1 | Out-Null
    $repoExists = ($LASTEXITCODE -eq 0)
    $ErrorActionPreference = "Stop"

    if (-not $repoExists) {
        Write-Host "Creating ECR repository: $Service"
        aws ecr create-repository `
            --repository-name $Service `
            --region $Region `
            --image-scanning-configuration scanOnPush=true `
            --encryption-configuration encryptionType=AES256
        if ($LASTEXITCODE -ne 0) { Write-Error "Failed to create ECR repository: $Service"; exit 1 }
    } else {
        Write-Host "ECR repository already exists: $Service"
    }
}

# --- Build, Tag, Push ---
$Root = $PSScriptRoot

foreach ($Service in $Services) {
    Write-Host ""
    Write-Host "=== $Service ==="

    $ServicePath = Join-Path $Root $Service

    docker build -t $Service "$ServicePath"
    if ($LASTEXITCODE -ne 0) { Write-Error "docker build failed for $Service"; exit 1 }

    docker tag "${Service}:latest" "$EcrBase/${Service}:latest"
    if ($LASTEXITCODE -ne 0) { Write-Error "docker tag failed for $Service"; exit 1 }

    docker push "$EcrBase/${Service}:latest"
    if ($LASTEXITCODE -ne 0) { Write-Error "docker push failed for $Service"; exit 1 }

    Write-Host "$Service pushed successfully."
}

# --- Patch account ID into K8s deployment manifests ---
Write-Host ""
Write-Host "Patching account ID into k8s deployment manifests..."
Get-ChildItem -Path (Join-Path $Root "k8s") -Recurse -Filter "deployment.yml" | ForEach-Object {
    (Get-Content $_.FullName) -replace '<ACCOUNT_ID>', $AccountId | Set-Content $_.FullName
}

Write-Host ""
Write-Host "Done! All images pushed to ECR and manifests patched."
Write-Host "Next: run .\deploy-eks.ps1 to deploy to EKS."
