# deploy-eks.ps1
# Creates the EKS cluster and deploys all microservices. Just for practice
# Usage: .\deploy-eks.ps1 -Region us-east-1

param(
    [string]$Region = "us-east-1"
)

$ErrorActionPreference = "Stop"
$Root = $PSScriptRoot

# --- Step 1: Create EKS cluster ---
Write-Host "Creating EKS cluster (this takes ~15 minutes)..."
eksctl create cluster -f "$Root\k8s\cluster.yml"

# --- Step 2: Switch kubectl context to EKS ---
Write-Host "Switching kubectl context to EKS cluster..."
aws eks update-kubeconfig --name microservices-cluster --region $Region
$currentContext = kubectl config current-context
Write-Host "Active context: $currentContext"
if ($currentContext -notmatch "microservices-cluster") {
    Write-Error "kubectl is not pointing to the EKS cluster. Current context: $currentContext"
    exit 1
}

# --- Step 3: Verify kubectl context ---
Write-Host "Verifying kubectl (must show EC2 nodes, not docker-desktop)..."
kubectl get nodes

# --- Step 3: Install AWS Load Balancer Controller ---
Write-Host "Installing AWS Load Balancer Controller..."

$AccountId = $(aws sts get-caller-identity --query Account --output text)

# Download IAM policy
Invoke-WebRequest `
    -Uri "https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.7.0/docs/install/iam_policy.json" `
    -OutFile "$env:TEMP\iam_policy.json"

$ErrorActionPreference = "SilentlyContinue"
aws iam create-policy `
    --policy-name AWSLoadBalancerControllerIAMPolicy `
    --policy-document file://$env:TEMP\iam_policy.json 2>&1 | Out-Null
$ErrorActionPreference = "Stop"
Write-Host "IAM policy ready."

# --- Associate OIDC provider (required for IRSA) ---
Write-Host "Associating IAM OIDC provider with cluster..."
eksctl utils associate-iam-oidc-provider `
    --region $Region `
    --cluster microservices-cluster `
    --approve

eksctl create iamserviceaccount `
    --cluster microservices-cluster `
    --namespace kube-system `
    --name aws-load-balancer-controller `
    --attach-policy-arn "arn:aws:iam::${AccountId}:policy/AWSLoadBalancerControllerIAMPolicy" `
    --approve --override-existing-serviceaccounts `
    --region $Region

helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm install aws-load-balancer-controller eks/aws-load-balancer-controller `
    -n kube-system `
    --set clusterName=microservices-cluster `
    --set serviceAccount.create=false `
    --set serviceAccount.name=aws-load-balancer-controller

# --- Wait for LB controller pods to be fully ready (webhook must be live) ---
Write-Host "Waiting for AWS Load Balancer Controller deployment to be available..."
kubectl rollout status deployment/aws-load-balancer-controller -n kube-system --timeout=180s

Write-Host "Waiting for LB controller pod to be Ready (webhook listener)..."
kubectl wait pod `
    -n kube-system `
    -l app.kubernetes.io/name=aws-load-balancer-controller `
    --for=condition=Ready `
    --timeout=120s

# Buffer for the webhook HTTPS server inside the pod to start accepting connections
Write-Host "Giving webhook server 30s to initialise..."
Start-Sleep -Seconds 30

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
    $ErrorActionPreference = "SilentlyContinue"
    $AlbUrl = kubectl get ingress microservices-ingress -n microservices `
        -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>&1
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -eq 0 -and $AlbUrl -and (-not $AlbUrl.ToString().StartsWith('Error'))) {
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
