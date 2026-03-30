# deploy-eks.ps1
# Creates the EKS cluster and deploys all microservices.
# Usage: .\deploy-eks.ps1 -Region us-east-1

param(
    [string]$Region = "us-east-1"
)

$ErrorActionPreference = "Stop"
$Root = $PSScriptRoot

# --- Step 1: Create EKS cluster ---
Write-Host "Creating EKS cluster (this takes ~15 minutes)..."
eksctl create cluster -f "$Root\k8s\cluster.yml"

# --- Step 2: Verify kubectl context ---
Write-Host "Verifying kubectl..."
kubectl get nodes

# --- Step 3: Install AWS Load Balancer Controller ---
Write-Host "Installing AWS Load Balancer Controller..."

$AccountId = $(aws sts get-caller-identity --query Account --output text)

# Download IAM policy
Invoke-WebRequest `
    -Uri "https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.7.0/docs/install/iam_policy.json" `
    -OutFile "$env:TEMP\iam_policy.json"

aws iam create-policy `
    --policy-name AWSLoadBalancerControllerIAMPolicy `
    --policy-document file://$env:TEMP\iam_policy.json 2>$null

eksctl create iamserviceaccount `
    --cluster microservices-cluster `
    --namespace kube-system `
    --name aws-load-balancer-controller `
    --attach-policy-arn "arn:aws:iam::${AccountId}:policy/AWSLoadBalancerControllerIAMPolicy" `
    --approve `
    --region $Region

helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm install aws-load-balancer-controller eks/aws-load-balancer-controller `
    -n kube-system `
    --set clusterName=microservices-cluster `
    --set serviceAccount.create=false `
    --set serviceAccount.name=aws-load-balancer-controller

# --- Step 4: Deploy namespace ---
Write-Host "Creating namespace..."
kubectl apply -f "$Root\k8s\namespace.yml"

# --- Step 5: Deploy services (backends first, gateway last) ---
$DeployOrder = @("user-service", "product-service", "order-service", "gateway-service")

foreach ($Service in $DeployOrder) {
    Write-Host "Deploying $Service..."
    kubectl apply -f "$Root\k8s\$Service\deployment.yml"
    kubectl apply -f "$Root\k8s\$Service\service.yml"
}

# --- Step 6: Apply Ingress ---
Write-Host "Applying Ingress..."
kubectl apply -f "$Root\k8s\ingress.yml"

# --- Step 7: Wait and print ALB URL ---
Write-Host ""
Write-Host "Waiting for ALB to be provisioned (up to 3 minutes)..."
Start-Sleep -Seconds 30

for ($i = 0; $i -lt 12; $i++) {
    $AlbUrl = kubectl get ingress microservices-ingress -n microservices `
        -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>$null
    if ($AlbUrl) {
        Write-Host ""
        Write-Host "Deployment complete!"
        Write-Host "Gateway URL: http://$AlbUrl"
        Write-Host ""
        Write-Host "Test endpoints:"
        Write-Host "  http://$AlbUrl/api/users"
        Write-Host "  http://$AlbUrl/api/products"
        Write-Host "  http://$AlbUrl/api/orders"
        exit 0
    }
    Write-Host "Waiting for ALB... ($( ($i+1)*15 )s)"
    Start-Sleep -Seconds 15
}

Write-Host "ALB not ready yet. Check with: kubectl get ingress -n microservices"
