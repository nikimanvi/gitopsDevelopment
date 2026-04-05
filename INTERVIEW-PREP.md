# DevOps Interview Preparation Guide
## EKS Microservices Deployment — Explained in Simple Terms

> This document explains every concept used in this project in plain English.
> Use this as your study guide and reference before interviews.

---

## Table of Contents
1. [The Big Picture — What Did We Build?](#1-the-big-picture)
2. [Microservices — What and Why?](#2-microservices)
3. [Docker — Packing Your App into a Box](#3-docker)
4. [ECR — AWS's Private Docker Hub](#4-ecr)
5. [Kubernetes — The Manager of Your Boxes](#5-kubernetes)
6. [EKS — Kubernetes on AWS](#6-eks)
7. [Key Kubernetes Concepts Explained](#7-key-kubernetes-concepts)
8. [AWS Load Balancer Controller — The Traffic Cop](#8-aws-load-balancer-controller)
9. [IAM & IRSA — Security and Permissions](#9-iam--irsa)
10. [Spring Boot Profiles — Different Settings for Different Places](#10-spring-boot-profiles)
11. [The Deployment Flow — Step by Step](#11-the-deployment-flow)
12. [Tools Used and Why](#12-tools-used-and-why)
13. [Problems We Hit and How We Fixed Them](#13-problems-we-hit-and-how-we-fixed-them)
14. [Interview Questions with Strong Answers](#14-interview-questions-with-strong-answers)
15. [Cheat Sheet — Commands You Must Know](#15-cheat-sheet)

---

## 1. The Big Picture

### What Did We Build?

Imagine you run an online store. Your store has three departments:
- A **Users department** (handles login, profiles)
- A **Products department** (handles the product catalog)
- An **Orders department** (handles purchases)

Instead of one massive store building, we split them into **separate smaller shops** — each running independently. This is microservices.

We then:
1. **Packaged** each shop into a Docker container (like a shipping container — works anywhere)
2. **Stored** the container images in AWS ECR (like a warehouse)
3. **Deployed** them on AWS EKS (Kubernetes manages all shops automatically)
4. **Put a receptionist (gateway)** at the front door — all customers talk to the receptionist, who routes them to the right department

```
Customer (Internet)
       │
       ▼
  Receptionist         ← gateway-service (Spring Cloud Gateway)
  (Front Door)
       │
  ┌────┼────┐
  ▼    ▼    ▼
Users Products Orders  ← Internal departments (no direct public access)
```

---

## 2. Microservices

### What is a Microservice?

**Simple explanation:** Instead of building one giant application (called a "monolith"), you break it into small, focused services. Each service:
- Does ONE thing well
- Runs independently
- Can be updated without touching the others
- Can be scaled independently

### Our 4 Services

| Service | Port | Job |
|---|---|---|
| `gateway-service` | 8080 | Receives ALL requests from the internet. Routes them to the right service. |
| `user-service` | 8081 | Handles everything about users |
| `product-service` | 8082 | Handles everything about products |
| `order-service` | 8083 | Handles everything about orders |

### Why Gateway?

Think of a hotel. Guests don't wander into the kitchen or the laundry room directly. They go to the **reception desk**, which directs them to the right place.

- Customer calls `/api/users` → gateway sends them to `user-service`
- Customer calls `/api/products` → gateway sends them to `product-service`
- Customer calls `/api/orders` → gateway sends them to `order-service`

The backend services are **never directly exposed** to the internet. Only the gateway is public.

---

## 3. Docker

### What is Docker?

**Simple explanation:** Imagine you wrote an app on your laptop. It works perfectly. But when you deploy it on a server, it breaks because the server has a different Java version, different OS, different settings.

Docker solves this by creating a **"container"** — a self-contained box that includes:
- Your application JAR file
- The Java runtime (JRE)
- All dependencies
- The operating system libraries it needs

The box runs **identically** everywhere — your laptop, a test server, AWS.

### What is a Dockerfile?

It's a **recipe** for building the container. Like a cooking recipe: "start with this base, add this, configure that, run this command."

### Our Dockerfile Explained — Multi-Stage Build

```dockerfile
# ======= STAGE 1: BUILD =======
# Think of this as a construction site with all the tools
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
# Result: a compiled JAR file sits in /app/target/

# ======= STAGE 2: RUNTIME =======
# Think of this as the finished house — tools are gone, only what's needed remains
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Don't run as root (like not giving admin rights to a regular app)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the JAR from Stage 1 — not Maven, not source code
COPY --from=builder /app/target/*.jar app.jar

# Tell JVM to respect container memory limits
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**Why 2 stages?**
- Stage 1 is like a factory — it has heavy machinery (Maven, JDK) to build the product
- Stage 2 is the final product box — only contains what's needed to run
- The final Docker image is **much smaller** (no Maven, no source code, no JDK — just JRE)
- Smaller image = faster to download, fewer security vulnerabilities

**`-XX:+UseContainerSupport` explained:**
Without this, if your EC2 machine has 4GB RAM but your container is limited to 256MB, Java would still try to use 4GB and get killed. With this flag, Java reads the container's own memory limit and sizes itself correctly.

**Non-root user explained:**
By default, Docker containers run as `root` (admin). That's a security risk — if someone hacks your container, they'd have full admin access. Creating a `spring` user and running as that user limits the damage.

---

## 4. ECR

### What is ECR?

**ECR = Elastic Container Registry**

**Simple explanation:** It's like **Docker Hub** but private and inside AWS. After you build your Docker image (the container box), you need somewhere to store it so your EKS cluster can download and run it.

ECR is that storage place. It's private — only YOUR AWS account can access it.

### How It Works

```
Your Laptop                    AWS ECR                    EKS Cluster
    │                             │                             │
    │  docker build               │                             │
    │  (creates image)            │                             │
    │                             │                             │
    │  docker push ──────────────▶│  stores image               │
    │  (uploads to ECR)           │                             │
    │                             │                             │
    │                             │◀────── pulls image ─────────│
    │                             │       (when pod starts)     │
```

### Our 4 ECR Repositories

```
113725941421.dkr.ecr.us-east-1.amazonaws.com/gateway-service
113725941421.dkr.ecr.us-east-1.amazonaws.com/user-service
113725941421.dkr.ecr.us-east-1.amazonaws.com/product-service
113725941421.dkr.ecr.us-east-1.amazonaws.com/order-service
```

- `113725941421` = your AWS Account ID
- `us-east-1` = the AWS region
- `gateway-service` = the repository name

**Image scanning on push:** We enabled this. Every time you push an image, ECR automatically scans it for known security vulnerabilities. Like an antivirus for your container.

---

## 5. Kubernetes

### What is Kubernetes?

**Simple explanation:** Imagine you have 10 shops (containers) to run, spread across 3 buildings (servers). You need someone to:
- Make sure each shop is always running
- Restart a shop if it crashes
- If one building burns down, move the shops to other buildings
- Handle customer traffic and send them to the right shop

**Kubernetes** (also called "K8s") is that manager. It's a system that automatically manages your containers across multiple machines.

### Key Kubernetes Vocabulary

| Term | Simple Explanation | Real World Analogy |
|---|---|---|
| **Cluster** | The entire Kubernetes system — all machines together | The whole company |
| **Node** | A single machine (EC2 instance) in the cluster | One office building |
| **Pod** | The smallest unit — one running container | One employee's desk |
| **Deployment** | Instructions for how many pods to run and how to run them | HR policy for hiring |
| **Service** | A stable address for your pods | Company's phone number |
| **Namespace** | A logical grouping — like folders | Department |
| **Ingress** | Rules for how outside traffic enters the cluster | Front door + receptionist |

---

## 6. EKS

### What is EKS?

**EKS = Elastic Kubernetes Service**

**Simple explanation:** Running Kubernetes yourself is complicated — you have to manage the "control plane" (the brain of Kubernetes). EKS is AWS's managed Kubernetes service where **AWS runs the control plane for you** and you just manage your worker nodes (EC2 machines).

### Components of Our EKS Cluster

```
┌─────────────────────────────────────────────────────────┐
│                    EKS Cluster                          │
│                                                         │
│  ┌──────────────────────────────────────────────────┐   │
│  │          Control Plane (managed by AWS)          │   │
│  │  - API Server (kubectl talks to this)            │   │
│  │  - etcd (stores cluster state)                   │   │
│  │  - Scheduler (decides which node runs what)      │   │
│  └──────────────────────────────────────────────────┘   │
│                                                         │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐        │
│  │   Node 1   │  │   Node 2   │  │   Node 3   │        │
│  │ t3.micro   │  │ t3.micro   │  │ t3.micro   │        │
│  │ us-east-1a │  │ us-east-1b │  │ us-east-1c │        │
│  │            │  │            │  │            │        │
│  │ [pod] [pod]│  │ [pod] [pod]│  │ [pod]      │        │
│  └────────────┘  └────────────┘  └────────────┘        │
└─────────────────────────────────────────────────────────┘
```

### Our Cluster Configuration (`k8s/cluster.yml`)

```yaml
metadata:
  name: microservices-cluster
  region: us-east-1
  version: "1.32"           # Kubernetes version

availabilityZones:
  - us-east-1a              # 3 AZs for redundancy
  - us-east-1b
  - us-east-1c

managedNodeGroups:
  - name: microservices-nodes
    instanceTypes: ["t3.micro", "t2.micro"]  # Free tier eligible
    minSize: 3              # Minimum 3 nodes always running
    maxSize: 5              # Can scale up to 5 if needed
    desiredCapacity: 3      # Start with 3
```

**Why 3 Availability Zones?**
AZs are separate physical data centers within a region. If us-east-1a has a power outage, your app still runs in 1b and 1c. Also, EC2 capacity isn't always available in every AZ — using 3 gives more chances to find available machines.

**Why a list of instance types?**
`instanceTypes: ["t3.micro", "t2.micro"]` — EKS tries `t3.micro` first, if no capacity available it tries `t2.micro`. This prevents the "insufficient capacity" error.

**Managed Node Group explained:**
Instead of manually creating and managing EC2 instances, EKS "manages" them for you — automatic security patches, automatic replacement if a node dies, automatic scaling.

---

## 7. Key Kubernetes Concepts

### 7.1 Namespace

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: microservices
```

**Simple explanation:** Like folders on your computer. All our 4 services live in the `microservices` namespace. Kubernetes system components live in `kube-system`. They don't interfere with each other.

---

### 7.2 Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: microservices
spec:
  replicas: 1               # Run 1 copy of this pod
  selector:
    matchLabels:
      app: user-service     # Manage pods with this label
  template:                 # Blueprint for the pod
    spec:
      containers:
        - name: user-service
          image: 113725941421.dkr.ecr.us-east-1.amazonaws.com/user-service:latest
          ports:
            - containerPort: 8081
```

**Simple explanation:** A Deployment is like a job posting that says:
- "I need 1 person (pod) doing the job of user-service"
- "They should use this skill set (Docker image)"
- "They sit at desk 8081 (port)"

If that person quits (pod crashes), Kubernetes immediately hires a replacement (restarts the pod). That's **self-healing**.

---

### 7.3 Readiness and Liveness Probes

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8081
  initialDelaySeconds: 30    # Wait 30s before first check
  periodSeconds: 10          # Check every 10s

livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8081
  initialDelaySeconds: 45
  periodSeconds: 15
```

**Readiness Probe — "Are you ready to work?"**
Spring Boot takes time to start up (~20-30 seconds). Without a readiness probe, Kubernetes might send traffic to a pod that's still starting, causing errors. The readiness probe waits for the app to respond with HTTP 200 on `/actuator/health` before sending any traffic to it.

**Liveness Probe — "Are you still alive?"**
After a pod is running, it can sometimes get stuck (deadlock, out of memory). The liveness probe keeps checking every 15 seconds. If the app stops responding, Kubernetes **automatically restarts** the pod. This is why K8s apps are "self-healing."

---

### 7.4 Resource Requests and Limits

```yaml
resources:
  requests:
    memory: "128Mi"    # Minimum guaranteed memory
    cpu: "100m"        # Minimum guaranteed CPU (100 millicores = 0.1 CPU)
  limits:
    memory: "256Mi"    # Maximum memory allowed
    cpu: "300m"        # Maximum CPU allowed
```

**Requests — "What you're guaranteed"**
When Kubernetes decides which node to place a pod on, it looks at `requests`. It guarantees this amount is available on the node. Like reserving a seat at a restaurant.

**Limits — "What you can't exceed"**
If a pod tries to use more than `256Mi` memory, Kubernetes kills it (OOM kill) and restarts it. Prevents one runaway pod from crashing the entire node.

**Why did we reduce these?**
Our nodes are `t3.micro` with only 1GB RAM. System pods use ~400MB. So we only have ~600MB for our 4 services. Setting requests to 128Mi each = 512MB total, which fits.

---

### 7.5 Service (ClusterIP)

```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: microservices
spec:
  selector:
    app: user-service      # Routes to pods with this label
  ports:
    - port: 8081
      targetPort: 8081
  type: ClusterIP           # Only reachable INSIDE the cluster
```

**Simple explanation:** A Service is like a company's internal phone extension. 

- `user-service` gets the DNS name `user-service.microservices.svc.cluster.local`
- Any pod inside the cluster can call it using just `http://user-service:8081`
- **No one outside the cluster can reach it** — `ClusterIP` means internal only

**Why use a Service instead of Pod IP directly?**
Pods are temporary. When a pod restarts, it gets a NEW IP address. A Service has a **stable, permanent** address that never changes, even if the pods behind it get replaced.

---

### 7.6 Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: microservices-ingress
  namespace: microservices
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
spec:
  ingressClassName: alb      # Use AWS ALB
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

**Simple explanation:** An Ingress is the **front door rules** for your cluster. It says:
- "Anyone coming from the internet to any path (`/`) should be sent to `gateway-service`"

The `ingressClassName: alb` tells the AWS Load Balancer Controller: "Create a real AWS Application Load Balancer for this."

**`target-type: ip` explained:**
Traffic goes directly from the ALB to the pod IP. The older way was `instance` (traffic goes to the EC2 node first, then to the pod). `ip` is more direct and efficient.

---

## 8. AWS Load Balancer Controller

### What is it?

**Simple explanation:** Kubernetes has an `Ingress` resource, but Kubernetes itself doesn't know how to create an AWS ALB. The **AWS Load Balancer Controller** is a program running inside your cluster that:
1. Watches for `Ingress` resources
2. Calls AWS APIs to create a real Application Load Balancer
3. Keeps the ALB in sync with your Ingress config

Think of it as a translator between Kubernetes language and AWS language.

### How it's installed

```powershell
# Install via Helm (Helm is like apt-get but for Kubernetes)
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
    -n kube-system \
    --set clusterName=microservices-cluster
```

### The Webhook Problem (and Our Fix)

The controller registers a **webhook** — a security checkpoint that intercepts every `Service` and `Ingress` creation to validate it. The webhook runs inside the controller pod.

**The problem we hit:** We applied services too quickly after installing the controller. The Helm install finished but the pod's webhook wasn't listening yet. Result: "connection refused."

**Our fix:**
```powershell
# Wait for the pod itself to be Ready (not just the Deployment)
kubectl wait pod -n kube-system \
    -l app.kubernetes.io/name=aws-load-balancer-controller \
    --for=condition=Ready --timeout=120s

# Extra 30 seconds for the HTTPS webhook server inside the pod to start
Start-Sleep -Seconds 30
```

---

## 9. IAM & IRSA

### What is IAM?

**IAM = Identity and Access Management**

**Simple explanation:** IAM is AWS's permission system. It controls WHO can do WHAT in AWS.

- A **User** in IAM = a person with AWS login
- A **Role** in IAM = a set of permissions that can be assumed by services
- A **Policy** in IAM = a document listing what's allowed/denied

### What is IRSA?

**IRSA = IAM Roles for Service Accounts**

**The problem it solves:** Our AWS Load Balancer Controller needs permission to create ALBs in AWS. How do you give a Kubernetes pod AWS permissions without hard-coding credentials?

**Old (bad) way:** Put AWS Access Key and Secret Key in the pod. If someone hacks the pod, they get your AWS credentials.

**IRSA (good) way:**
1. Create an IAM Role with the needed permissions
2. Link a Kubernetes `ServiceAccount` to that IAM Role
3. The pod uses the `ServiceAccount`
4. AWS automatically gives the pod temporary credentials via a token

```
Pod (using aws-load-balancer-controller ServiceAccount)
  │
  │  "I am aws-load-balancer-controller SA in EKS cluster X"
  ▼
AWS STS (Security Token Service)
  │
  │  Verifies the pod's identity via OIDC
  ▼
IAM Role: AWSLoadBalancerControllerIAMPolicy
  │
  │  Issues temporary 1-hour credentials
  ▼
Pod can now call AWS APIs (create ALB, target groups, etc.)
```

**Why IRSA is important for interviews:** It's the correct answer to "how do you give AWS permissions to Kubernetes pods without storing credentials." Shows security awareness.

---

## 10. Spring Boot Profiles

### The Problem

Our gateway's default config routes to `localhost`:
```yaml
# application.yml (works on laptop)
uri: http://localhost:8081   # user-service
uri: http://localhost:8082   # product-service
```

In Kubernetes, `localhost` means only the pod itself. There's no user-service on localhost.

### The Solution — Spring Profiles

Spring Boot lets you have **different config files for different environments**:

```yaml
# application-k8s.yml (used when SPRING_PROFILES_ACTIVE=k8s)
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://user-service:8081      # Kubernetes DNS name
        - id: product-service
          uri: http://product-service:8082
        - id: order-service
          uri: http://order-service:8083
```

In the Kubernetes deployment:
```yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "k8s"               # Activates application-k8s.yml
```

**How Kubernetes DNS works:**
When you create a Service named `user-service` in namespace `microservices`, Kubernetes (CoreDNS) automatically creates a DNS entry: `user-service.microservices.svc.cluster.local`. Within the same namespace, you can just use `user-service` and it resolves automatically.

---

## 11. The Deployment Flow

Here's everything that happens when you run `.\push-to-ecr.ps1` and `.\deploy-eks.ps1`:

### Phase 1: push-to-ecr.ps1

```
Step 1: Check Docker Desktop is running
         ↓
Step 2: Get your AWS Account ID
        aws sts get-caller-identity → "113725941421"
         ↓
Step 3: Log Docker into ECR
        aws ecr get-login-password | docker login
         ↓
Step 4: For each service (gateway, user, product, order):
        - Create ECR repo if missing
        - docker build (compiles Java inside container using Maven)
        - docker tag (add ECR URL as the image name)
        - docker push (upload to ECR)
         ↓
Step 5: Replace <ACCOUNT_ID> in all deployment.yml files
        with real account ID "113725941421"
```

### Phase 2: deploy-eks.ps1

```
Step 1: eksctl create cluster
        - Creates VPC, subnets (AWS networking)
        - Creates EKS control plane (takes 10 min)
        - Creates 3× t3.micro EC2 nodes (takes 5 min)
         ↓
Step 2: aws eks update-kubeconfig
        - Updates ~/.kube/config so kubectl points to EKS (not Docker Desktop)
         ↓
Step 3: Install AWS Load Balancer Controller
        - Create IAM policy (permissions document)
        - Create IAM service account (IRSA link)
        - helm install (deploys controller pod into kube-system)
         ↓
Step 4: Wait for controller pod Ready + 30s
         ↓
Step 5: kubectl apply namespace.yml
        - Creates "microservices" namespace
         ↓
Step 6: Deploy each service (in order: backends first, gateway last)
        - kubectl apply deployment.yml → creates pods
        - kubectl apply service.yml → creates internal DNS name
         ↓
Step 7: kubectl apply ingress.yml
        - Controller sees the Ingress → calls AWS API → creates real ALB
         ↓
Step 8: Wait for ALB DNS to appear (2-3 min)
        - Print public URL: http://k8s-xxxx.us-east-1.elb.amazonaws.com
```

---

## 12. Tools Used and Why

| Tool | What It Does | Simple Analogy |
|---|---|---|
| **Docker** | Packages apps into containers | Packs your stuff into a shipping container |
| **ECR** | Stores Docker images in AWS | AWS's private Docker Hub warehouse |
| **kubectl** | Command-line tool to control Kubernetes | TV remote for your K8s cluster |
| **eksctl** | Creates/manages EKS clusters | One-click EKS cluster builder |
| **Helm** | Package manager for Kubernetes apps | apt-get / pip but for K8s |
| **AWS CLI** | Command-line tool for AWS | Remote control for all of AWS |
| **PowerShell** | Scripting to automate everything | Automation glue |
| **Spring Boot** | Java framework for microservices | Pre-built engine for Java web apps |
| **Spring Cloud Gateway** | API Gateway for routing | Hotel reception desk |
| **eksctl** | Manages EKS clusters via YAML config | Like terraform but only for EKS |
| **CloudFormation** | AWS's infrastructure-as-code (used by eksctl internally) | Blueprint all AWS resources |

---

## 13. Problems We Hit and How We Fixed Them

These are the best interview stories — real problems with real solutions.

---

### Problem 1: Docker Not Running

**Error:** `open //./pipe/dockerDesktopLinuxEngine: The system cannot find the file specified`

**What happened:** We ran the push script but Docker Desktop wasn't open.

**What it means:** Docker CLI communicates with Docker Desktop through a "pipe" (a connection channel). If Docker Desktop is closed, the pipe doesn't exist.

**Fix:** Start Docker Desktop, wait 30 seconds, then run the script. We also added a check at the start of the script:
```powershell
docker info | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Error "Docker is not running"; exit 1 }
```

---

### Problem 2: PowerShell ErrorActionPreference

**Error:** `NativeCommandError` when checking if ECR repo exists

**What happened:** We used `try/catch` to handle ECR repo not existing, but PowerShell's `$ErrorActionPreference = "Stop"` intercepts the AWS CLI's non-zero exit code BEFORE our code could check `$LASTEXITCODE`.

**What it means:** `try/catch` in PowerShell only catches PowerShell cmdlet errors, not errors from external programs like `aws`. For external programs, you check `$LASTEXITCODE`.

**Fix:**
```powershell
$ErrorActionPreference = "SilentlyContinue"   # Temporarily disable error stopping
aws ecr describe-repositories --repository-names $Service 2>&1 | Out-Null
$repoExists = ($LASTEXITCODE -eq 0)           # Check exit code manually
$ErrorActionPreference = "Stop"               # Re-enable
```

---

### Problem 3: EC2 Capacity — Not Free Tier Eligible

**Error:** `The specified instance type is not eligible for Free Tier`

**What happened:** We used `t3.medium` instances. AWS Free Tier only covers `t3.micro` and `t2.micro`.

**Fix:** Changed `instanceType: t3.medium` to `instanceTypes: ["t3.micro", "t2.micro"]` and increased node count from 2 to 3 (because micro instances have less RAM).

**Learn:** AWS Free Tier gives 750 hours/month of `t3.micro` EC2. EKS itself costs ~$0.10/hour for the control plane regardless.

---

### Problem 4: kubectl Pointing to Wrong Cluster

**Error:** No LB controller found, services applying to Docker Desktop

**What happened:** kubectl was configured to talk to local Docker Desktop instead of EKS. All our `kubectl apply` commands were running against the local K8s, not AWS.

**Fix:** After cluster creation, always run:
```powershell
aws eks update-kubeconfig --name microservices-cluster --region us-east-1
```
This updates `~/.kube/config` (kubectl's config file) to add EKS and set it as the active context.

**How to check which cluster kubectl is talking to:**
```powershell
kubectl config current-context
# Should show: arn:aws:eks:us-east-1:113725941421:cluster/microservices-cluster
# NOT: docker-desktop
```

---

### Problem 5: Webhook Connection Refused

**Error:** `failed calling webhook "mservice.elbv2.k8s.aws": connection refused`

**What happened:** The AWS Load Balancer Controller was installed, but we applied services before the controller's webhook server was ready. The webhook registers with Kubernetes immediately upon Helm install, but the actual HTTPS server inside the pod takes extra seconds to start accepting connections.

**Fix:**
```powershell
# Wait for the pod (not just the deployment) to be Ready
kubectl wait pod -n kube-system \
    -l app.kubernetes.io/name=aws-load-balancer-controller \
    --for=condition=Ready --timeout=120s

# Wait additional 30 seconds for the webhook HTTPS listener to start
Start-Sleep -Seconds 30
```

---

### Problem 6: EKS Version Not Supported

**Error:** `invalid version, 1.29 is no longer supported`

**AWS EKS supported versions (as of 2026):** 1.30, 1.31, 1.32, 1.33

**Fix:** Changed `version: "1.29"` to `version: "1.32"` in `k8s/cluster.yml`.

**Learn:** AWS regularly deprecates old Kubernetes versions. Always check the [EKS supported versions page](https://docs.aws.amazon.com/eks/latest/userguide/kubernetes-versions.html) before deploying.

---

### Problem 7: Deprecated Ingress Annotation

**Warning:** `annotation "kubernetes.io/ingress.class" is deprecated`

**What happened:** Older way of specifying ingress class was via annotation. Kubernetes 1.18+ uses `spec.ingressClassName` instead.

**Old way (deprecated):**
```yaml
annotations:
  kubernetes.io/ingress.class: alb
```

**New way (correct):**
```yaml
spec:
  ingressClassName: alb
```

---

## 14. Interview Questions with Strong Answers

---

**Q: Can you walk me through your EKS deployment architecture?**

> "We built a Spring Boot microservices application with 4 services — a gateway, user, product, and order service. We containerized each using multi-stage Dockerfiles to keep images small and secure. Images are stored in ECR. We deployed to EKS using eksctl with a managed node group of t3.micro instances across 3 availability zones for resilience. The gateway is the only public-facing service, exposed via an AWS ALB created by the AWS Load Balancer Controller. Backend services communicate via Kubernetes internal DNS using ClusterIP services."

---

**Q: What is the difference between a Deployment and a Service in Kubernetes?**

> "A Deployment manages how many pod replicas run and handles rolling updates and restarts. It's the 'what to run.' A Service provides a stable network endpoint (DNS name and ClusterIP) to reach those pods. It's the 'how to reach it.' Pods are temporary and get new IPs on restart — a Service's address never changes, so other services always know how to reach it."

---

**Q: What is IRSA and why did you use it?**

> "IRSA stands for IAM Roles for Service Accounts. It's the secure way to give AWS permissions to Kubernetes pods without storing credentials. The AWS Load Balancer Controller needs to call AWS APIs to create ALBs. Instead of hard-coding access keys, we create an IAM role with the required permissions and link it to a Kubernetes service account via OIDC. When the pod runs with that service account, AWS automatically issues temporary credentials. It follows the principle of least privilege — only that specific pod gets those permissions."

---

**Q: What is the purpose of liveness and readiness probes?**

> "Readiness probes prevent traffic from being sent to pods that aren't ready yet — for example, a Spring Boot app takes 30 seconds to start. Without readiness probes, Kubernetes might route requests to it during startup, causing errors. Liveness probes detect when a running pod becomes unresponsive or deadlocked and automatically restart it. Together they make the system self-healing."

---

**Q: Why did you use multi-stage Docker builds?**

> "To keep the final image small and secure. The build stage needs Maven and the full JDK — heavy tools not needed at runtime. The runtime stage copies only the compiled JAR and uses a minimal JRE image. The final image has no source code, no build tools, and no unnecessary packages — smaller attack surface, faster to pull, and fewer vulnerabilities."

---

**Q: What is the AWS Load Balancer Controller and why is it needed?**

> "Kubernetes has an Ingress resource for defining routing rules, but it doesn't know how to create cloud-provider-specific load balancers. The AWS Load Balancer Controller is a Kubernetes operator that watches Ingress and Service resources and translates them into real AWS Application Load Balancers and target groups. Without it, declaring an Ingress in EKS does nothing — no ALB gets created."

---

**Q: How does inter-service communication work in your cluster?**

> "Via Kubernetes DNS. Every ClusterIP Service gets a stable DNS name like `user-service.microservices.svc.cluster.local`. Within the same namespace, pods can use just the short name `user-service`. CoreDNS (built into EKS) resolves this to the Service's ClusterIP, which then load balances across the pod IPs. We override the gateway's Spring Boot config via a `k8s` profile to use these DNS names instead of localhost."

---

**Q: What happened when a node group failed to create and how did you debug it?**

> "The cluster creation timed out on the node group. I used CloudFormation to find the actual error: `aws cloudformation describe-stack-events --stack-name eksctl-microservices-cluster-nodegroup-... --query StackEvents[?ResourceStatus=='CREATE_FAILED']`. The error showed the instance type `t3.medium` was not Free Tier eligible. I switched to `t3.micro`, increased the node count from 2 to 3 to compensate for the smaller RAM, reduced container memory requests accordingly, and added 3 availability zones to avoid capacity issues in a single AZ."

---

**Q: How did you handle environment-specific configuration?**

> "Using Spring Boot profiles. The default `application.yml` has localhost routing for local development. `application-k8s.yml` overrides the gateway routes with Kubernetes DNS names. The Kubernetes deployment sets the environment variable `SPRING_PROFILES_ACTIVE=k8s`, which tells Spring Boot to load the k8s profile on top of defaults. This way the same Docker image works in both local docker-compose and EKS without any code changes."

---

## 15. Cheat Sheet

### AWS Commands
```powershell
# Who am I?
aws sts get-caller-identity

# Configure AWS credentials
aws configure

# List ECR repositories
aws ecr describe-repositories --region us-east-1

# Login Docker to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <ACCOUNT>.dkr.ecr.us-east-1.amazonaws.com

# Update kubectl to use EKS
aws eks update-kubeconfig --name microservices-cluster --region us-east-1

# List EKS clusters
eksctl get cluster --region us-east-1

# Create cluster from config file
eksctl create cluster -f k8s/cluster.yml

# Delete cluster (teardown)
eksctl delete cluster --name microservices-cluster --region us-east-1

# Create IAM service account (IRSA)
eksctl create iamserviceaccount --cluster microservices-cluster --namespace kube-system --name aws-load-balancer-controller --attach-policy-arn <ARN> --approve
```

### Docker Commands
```powershell
# Build image
docker build -t my-service ./my-service

# Tag for ECR
docker tag my-service:latest <ACCOUNT>.dkr.ecr.us-east-1.amazonaws.com/my-service:latest

# Push to ECR
docker push <ACCOUNT>.dkr.ecr.us-east-1.amazonaws.com/my-service:latest

# List local images
docker images

# Check Docker is running
docker info
```

### kubectl Commands
```powershell
# Which cluster am I talking to?
kubectl config current-context

# Switch context
kubectl config use-context docker-desktop
kubectl config use-context arn:aws:eks:...

# List contexts
kubectl config get-contexts

# Get nodes
kubectl get nodes

# Get all pods in a namespace
kubectl get pods -n microservices

# Get pods with more info (IP, node)
kubectl get pods -n microservices -o wide

# Get services
kubectl get svc -n microservices

# Get ingress (ALB URL)
kubectl get ingress -n microservices

# Get everything in a namespace
kubectl get all -n microservices

# Apply a manifest
kubectl apply -f k8s/user-service/deployment.yml

# Describe a pod (shows events, errors)
kubectl describe pod <pod-name> -n microservices

# Stream logs
kubectl logs -f deployment/gateway-service -n microservices

# Get events (sorted, useful for debugging)
kubectl get events -n microservices --sort-by='.lastTimestamp'

# Wait for deployment to be ready
kubectl rollout status deployment/user-service -n microservices

# Wait for pod condition
kubectl wait pod -n kube-system -l app.kubernetes.io/name=aws-load-balancer-controller --for=condition=Ready --timeout=120s

# Force delete a stuck pod
kubectl delete pod <pod-name> -n microservices --force

# Execute command inside a pod
kubectl exec -it <pod-name> -n microservices -- sh
```

### Helm Commands
```powershell
# Add a Helm repo
helm repo add eks https://aws.github.io/eks-charts

# Update repos
helm repo update

# Install a chart
helm install aws-load-balancer-controller eks/aws-load-balancer-controller -n kube-system

# List installed releases
helm list -n kube-system

# Uninstall
helm uninstall aws-load-balancer-controller -n kube-system
```

---

## Key Numbers to Remember

| Item | Value |
|---|---|
| AWS Account ID | 113725941421 |
| Region | us-east-1 |
| EKS Version | 1.32 |
| Cluster Name | microservices-cluster |
| Gateway Port | 8080 |
| User Service Port | 8081 |
| Product Service Port | 8082 |
| Order Service Port | 8083 |
| Node Instance Type | t3.micro (Free Tier) |
| Node Count | 3 |
| Memory per service (request) | 128Mi |
| Memory per service (limit) | 256Mi |

---

## Final Tips for Your Interview

1. **Say "we faced challenges"** — interviewers respect honesty and problem-solving over "it just worked"
2. **Know WHY you made decisions** — "I chose ClusterIP because backends should not be public-facing"
3. **Understand the traffic flow** — be able to trace a request from browser → ALB → gateway → service
4. **Know the difference** between Deployment (what runs), Service (how to reach it), Ingress (how internet reaches it)
5. **IRSA is a must-know** — it comes up in every AWS+K8s interview
6. **Be ready to explain probes** — readiness vs liveness is a very common question
7. **Know kubectl debug commands** — `describe`, `logs`, `get events` show you're operational, not just theoretical

**Good luck!**
