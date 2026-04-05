# EKS Deployment — Spring Boot Microservices on AWS

## Project Overview

This project containerizes and deploys a **Spring Boot microservices architecture** to **Amazon EKS (Elastic Kubernetes Service)**. It covers the full DevOps pipeline — from writing Dockerfiles and pushing images to ECR, to deploying workloads on Kubernetes with an internet-facing Application Load Balancer.

---

## Architecture

```
Internet
   │
   ▼
Application Load Balancer (AWS ALB)    ← Provisioned via AWS Load Balancer Controller
   │
   ▼
gateway-service  (port 8080)           ← Spring Cloud Gateway — routes all traffic
   │
   ├──▶  user-service     (port 8081)  ─┐
   ├──▶  product-service  (port 8082)   ├─ ClusterIP Services (internal only)
   └──▶  order-service    (port 8083)  ─┘
```

### Key Design Decisions
- **Only the gateway is exposed publicly** — backend services are ClusterIP (internal DNS only)
- **Spring profile `k8s`** overrides gateway routes from `localhost` to Kubernetes DNS names (e.g. `http://user-service:8081`)
- **Non-root containers** — all Dockerfiles create a `spring` user and run as non-root (security best practice)
- **Multi-stage Dockerfiles** — Stage 1 compiles Java with Maven, Stage 2 produces a minimal JRE-only runtime image

---

## Technology Stack

| Layer | Technology |
|---|---|
| Application | Spring Boot 3, Spring Cloud Gateway |
| Containerization | Docker (multi-stage builds) |
| Container Registry | Amazon ECR (Elastic Container Registry) |
| Orchestration | Amazon EKS (Kubernetes 1.32) |
| Node Management | eksctl with Managed Node Groups |
| Load Balancing | AWS Application Load Balancer (ALB) |
| Ingress Controller | AWS Load Balancer Controller (Helm) |
| IAM Integration | IRSA (IAM Roles for Service Accounts) |
| Monitoring | AWS CloudWatch (cluster logging) |
| Scripting | PowerShell automation scripts |

---

## Repository Structure

```
Microservices/
├── gateway-service/           # Spring Cloud Gateway — public entry point
│   ├── Dockerfile             # Multi-stage build
│   └── src/main/resources/
│       ├── application.yml         # Default config (localhost routes)
│       └── application-k8s.yml    # K8s profile (DNS-based routes)
├── user-service/              # User management microservice (port 8081)
├── product-service/           # Product catalog microservice (port 8082)
├── order-service/             # Order processing microservice (port 8083)
├── k8s/                       # All Kubernetes manifests
│   ├── cluster.yml            # EKS cluster definition (eksctl)
│   ├── namespace.yml          # microservices namespace
│   ├── ingress.yml            # ALB Ingress (internet-facing)
│   ├── gateway-service/
│   │   ├── deployment.yml
│   │   └── service.yml
│   ├── user-service/
│   │   ├── deployment.yml
│   │   └── service.yml
│   ├── product-service/
│   │   ├── deployment.yml
│   │   └── service.yml
│   └── order-service/
│       ├── deployment.yml
│       └── service.yml
├── push-to-ecr.ps1            # Builds images and pushes to ECR
└── deploy-eks.ps1             # Creates EKS cluster and deploys all services
```

---

## Infrastructure Details

### EKS Cluster (`k8s/cluster.yml`)

```yaml
metadata:
  name: microservices-cluster
  region: us-east-1
  version: "1.32"
```

- **Kubernetes version:** 1.32
- **Region:** us-east-1
- **Availability Zones:** us-east-1a, us-east-1b, us-east-1c (3 AZs for HA and capacity)
- **Node group:** Managed Node Group with `t3.micro` / `t2.micro` instances
- **Node count:** min 3, max 5, desired 3
- **AMI:** AmazonLinux2023
- **Add-ons installed:** vpc-cni, coredns, kube-proxy
- **CloudWatch logging:** api, audit, authenticator log types enabled

**Why 3 AZs?** EC2 capacity is not guaranteed in any single AZ. Using 3 AZs ensures the node group can find capacity even if one AZ is constrained.

**Why `instanceTypes` list instead of single `instanceType`?** EKS managed node groups support multiple instance types for capacity flexibility — it picks whichever is available.

---

### Docker Images & ECR

Each service has its own **multi-stage Dockerfile**:

**Stage 1 — Builder:**
```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
```

**Stage 2 — Runtime:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=builder /app/target/*.jar app.jar
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**Why multi-stage?** The final image only contains the JRE and the JAR — no Maven, no source code. This reduces image size and attack surface.

**`-XX:+UseContainerSupport`** — tells the JVM to read memory limits from the container cgroups instead of the host machine. Without this, the JVM would size its heap based on the EC2 node's total RAM, causing OOM kills.

**ECR Repositories created:**
- `113725941421.dkr.ecr.us-east-1.amazonaws.com/gateway-service`
- `113725941421.dkr.ecr.us-east-1.amazonaws.com/user-service`
- `113725941421.dkr.ecr.us-east-1.amazonaws.com/product-service`
- `113725941421.dkr.ecr.us-east-1.amazonaws.com/order-service`

All repositories have **image scanning on push** enabled for vulnerability detection.

---

### Kubernetes Manifests

#### Namespace
All workloads run in a dedicated `microservices` namespace, isolated from system pods in `kube-system`.

#### Deployments
Each service has a `Deployment` with:
- **Readiness probe** — K8s only sends traffic once `/actuator/health` returns 200. Prevents requests hitting an app still starting up.
- **Liveness probe** — K8s restarts the pod if `/actuator/health` stops responding. Self-healing.
- **Resource requests & limits** — Scheduler uses `requests` to place pods on nodes; `limits` cap container resource usage.

```yaml
resources:
  requests:
    memory: "128Mi"
    cpu: "100m"
  limits:
    memory: "256Mi"
    cpu: "300m"
```

**Why separate requests and limits?** Requests guarantee resources; limits prevent a single pod from starving others. Setting limits too high on `t3.micro` (1GB RAM) nodes would cause pods to fail scheduling.

#### Services (ClusterIP)
Backend services use `ClusterIP` — they are only reachable by name **within the cluster**. Kubernetes CoreDNS resolves `user-service` → `user-service.microservices.svc.cluster.local`.

```yaml
spec:
  type: ClusterIP
  ports:
    - port: 8081
      targetPort: 8081
```

#### Gateway — Spring Profile Override
The gateway's default `application.yml` routes to `localhost` (works for local docker-compose). In EKS, a separate profile `application-k8s.yml` overrides this:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://user-service:8081   # Kubernetes DNS name
```

The deployment sets `SPRING_PROFILES_ACTIVE=k8s` so this profile is automatically loaded.

#### Ingress
```yaml
spec:
  ingressClassName: alb
  rules:
    - http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: gateway-service
                port:
                  number: 8080
```

- `ingressClassName: alb` — tells the AWS Load Balancer Controller to create a real ALB
- `scheme: internet-facing` — ALB gets a public IP/DNS
- `target-type: ip` — ALB routes directly to pod IPs (not node ports), more efficient

---

### AWS Load Balancer Controller

The **AWS Load Balancer Controller** is a Kubernetes controller that watches `Ingress` resources and provisions real AWS ALBs automatically.

**Installation method:** Helm chart from the `eks` repo

**IAM Integration (IRSA):**
The controller needs AWS permissions to create ALBs, target groups, and security groups. This is done via **IRSA (IAM Roles for Service Accounts)** — the pod's ServiceAccount is annotated with an IAM role ARN. The pod assumes this role without needing static credentials.

```
eksctl create iamserviceaccount \
  --cluster microservices-cluster \
  --namespace kube-system \
  --name aws-load-balancer-controller \
  --attach-policy-arn arn:aws:iam::113725941421:policy/AWSLoadBalancerControllerIAMPolicy
```

**Why not use node-level IAM?** IRSA gives least-privilege access — only the specific pod that needs AWS permissions gets them, not every pod on the node.

---

## Deployment Pipeline

### Phase 1 — Build & Push to ECR (`push-to-ecr.ps1`)

1. Validates Docker is running
2. Resolves AWS Account ID via `aws sts get-caller-identity`
3. Authenticates Docker to ECR using short-lived token (`aws ecr get-login-password`)
4. Creates ECR repositories if they don't exist (idempotent)
5. Builds each Docker image from local source using the service's Dockerfile
6. Tags each image with the ECR URI
7. Pushes to ECR
8. Patches the ECR account ID into all Kubernetes `deployment.yml` files

### Phase 2 — Deploy to EKS (`deploy-eks.ps1`)

1. Creates EKS cluster via `eksctl` using `k8s/cluster.yml`
2. Switches `kubectl` context to the EKS cluster (prevents accidental apply to local Docker Desktop)
3. Downloads and applies the AWS Load Balancer Controller IAM policy
4. Creates IAM service account (IRSA) for the controller
5. Installs AWS Load Balancer Controller via Helm
6. Waits for the controller pod to be `Ready` and the webhook server to initialise
7. Creates the `microservices` namespace
8. Deploys services in order: backends first, gateway last
9. Applies the ALB Ingress
10. Polls for the ALB DNS hostname and prints the public URL

---

## Challenges Faced & Solutions

| Challenge | Root Cause | Solution |
|---|---|---|
| Node group failed to create | `t3.medium` not Free Tier eligible | Switched to `t3.micro`/`t2.micro` |
| Webhook connection refused | LB controller pod not ready before manifests applied | Added `kubectl wait pod --for=condition=Ready` + 30s buffer |
| Services applied to wrong cluster | `kubectl` context pointed to Docker Desktop, not EKS | Added `aws eks update-kubeconfig` + context validation in script |
| ECR repo check crashed script | `$ErrorActionPreference=Stop` treated native `aws` CLI non-zero exit as terminating error | Used `$ErrorActionPreference=SilentlyContinue` scope + `$LASTEXITCODE` check |
| Deprecated ingress annotation | `kubernetes.io/ingress.class` removed in K8s 1.18+ | Migrated to `spec.ingressClassName: alb` |
| Gateway routing to `localhost` in K8s | Spring profile not overriding gateway routes | Created `application-k8s.yml` with Kubernetes DNS names, activated via `SPRING_PROFILES_ACTIVE=k8s` |
| Single AZ capacity failure | EC2 capacity unavailable in us-east-1a | Added us-east-1b and us-east-1c to `availabilityZones` |

---

## Common kubectl Commands

```bash
# Check cluster nodes
kubectl get nodes

# Check all pods in microservices namespace
kubectl get pods -n microservices

# Get all services
kubectl get svc -n microservices

# Get ALB URL
kubectl get ingress microservices-ingress -n microservices

# Stream logs from a service
kubectl logs -f deployment/gateway-service -n microservices

# Describe a pod (diagnose failures)
kubectl describe pod <pod-name> -n microservices

# Check recent events (image pull errors, scheduling issues)
kubectl get events -n microservices --sort-by='.lastTimestamp'

# Check LB controller logs
kubectl logs -f deployment/aws-load-balancer-controller -n kube-system

# Switch kubectl context back to local Docker Desktop
kubectl config use-context docker-desktop

# Switch back to EKS
aws eks update-kubeconfig --name microservices-cluster --region us-east-1
```

---

## Test Endpoints

After deployment, all traffic enters via the ALB URL:

```bash
# Health check
curl http://<ALB-URL>/actuator/health

# User service
curl http://<ALB-URL>/api/users

# Product service
curl http://<ALB-URL>/api/products

# Order service
curl http://<ALB-URL>/api/orders
```

---

## Teardown

```powershell
eksctl delete cluster --name microservices-cluster --region us-east-1
```

This deletes: EKS cluster, EC2 nodes, VPC, subnets, ALB, security groups, and CloudFormation stacks. ECR images remain in the registry.

---

## Interview Talking Points

**Q: Why EKS over ECS?**
EKS gives full Kubernetes compatibility — portable manifests, a huge ecosystem of tooling (Helm, kubectl, KEDA, Argo CD), and no vendor lock-in. ECS is simpler but proprietary to AWS.

**Q: What is the role of the AWS Load Balancer Controller?**
It's a Kubernetes controller that watches `Ingress` and `Service` resources and translates them into real AWS infrastructure (ALBs, Target Groups, Listeners, Security Groups). Without it, EKS has no native way to provision an ALB.

**Q: How does service-to-service communication work inside the cluster?**
Via Kubernetes CoreDNS. Each `Service` gets a stable DNS name (`<service-name>.<namespace>.svc.cluster.local`). Pods call `http://user-service:8081` and CoreDNS resolves it to the service's ClusterIP, which load balances to the pod IPs.

**Q: How do pods pull images from ECR without storing credentials?**
EKS nodes have an IAM instance role with `AmazonEC2ContainerRegistryReadOnly` policy. The `kubelet` on each node uses this role to authenticate to ECR automatically. No secrets needed.

**Q: What is IRSA and why did you use it?**
IRSA (IAM Roles for Service Accounts) allows specific Kubernetes pods to assume specific IAM roles without node-level permissions. The AWS Load Balancer Controller needs IAM permissions to create ALBs — IRSA gives only that pod those permissions, following the principle of least privilege.

**Q: How did you handle environment-specific config in Spring Boot?**
Spring Boot profiles. The default `application.yml` has localhost routes for local development. `application-k8s.yml` overrides the gateway routes with Kubernetes DNS names. The deployment sets `SPRING_PROFILES_ACTIVE=k8s` so the right config loads in the cluster.

**Q: Why use multi-stage Docker builds?**
The builder stage needs Maven and the full JDK. The runtime stage only needs the JRE and the compiled JAR. Multi-stage builds discard the build tools from the final image — smaller image size, fewer vulnerabilities, and no source code exposure.
