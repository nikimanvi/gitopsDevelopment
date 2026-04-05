# Jenkins Pipeline Architecture

## 🏗️ Pipeline Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         JENKINS CI/CD PIPELINE                   │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│   GitHub     │
│  Repository  │──── Webhook/Poll SCM ────┐
└──────────────┘                          │
                                          ▼
                               ┌────────────────────┐
                               │  1. CHECKOUT       │
                               │  - Clone repo      │
                               │  - Get commit hash │
                               └──────────┬─────────┘
                                          │
                                          ▼
                               ┌────────────────────┐
                               │  2. BUILD          │
                               │  - Maven compile   │
                               │  - Parallel (-T 4) │
                               └──────────┬─────────┘
                                          │
                                          ▼
                               ┌────────────────────┐
                               │  3. TEST           │
                               │  - JUnit tests     │
                               │  - Generate reports│
                               └──────────┬─────────┘
                                          │
                                          ▼
                               ┌────────────────────┐
                               │  4. CODE COVERAGE  │
                               │  - JaCoCo reports  │
                               │  - HTML output     │
                               └──────────┬─────────┘
                                          │
                                          ▼
                               ┌────────────────────┐
                               │  5. SONARQUBE      │
                               │  - Upload metrics  │
                               │  - Code analysis   │
                               └──────────┬─────────┘
                                          │
                                          ▼
                               ┌────────────────────┐
                               │  6. QUALITY GATE   │
                               │  - Wait for result │
                               │  - Fail if issues  │
                               └──────────┬─────────┘
                                          │
                                          ▼
                               ┌────────────────────┐
                               │  7. PACKAGE        │
                               │  - Maven package   │
                               │  - Create JARs     │
                               └──────────┬─────────┘
                                          │
                                          ▼
        ┌─────────────────────────────────────────────────────┐
        │          8. BUILD DOCKER IMAGES (Parallel)          │
        ├──────────┬──────────┬───────────┬──────────────────┤
        │ Gateway  │   User   │  Product  │     Order        │
        │ Service  │ Service  │  Service  │    Service       │
        └────┬─────┴────┬─────┴─────┬─────┴────┬─────────────┘
             │          │           │          │
             └──────────┴───────────┴──────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │  9. PUSH TO ECR    │ ◄── Only on main/develop
                 │  - Tag images      │
                 │  - Push to registry│
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ 10. DEPLOY TO EKS  │ ◄── Only on main
                 │  - Update configs  │
                 │  - Rolling update  │
                 └──────────┬─────────┘
                            │
                            ▼
                 ┌────────────────────┐
                 │ 11. HEALTH CHECK   │
                 │  - Verify pods     │
                 │  - Check endpoints │
                 └────────────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │   SUCCESS!   │
                    │  Send Email  │
                    └──────────────┘
```

---

## 📊 Pipeline Stages Breakdown

### Stage 1: Checkout (10-15s)
**Purpose**: Get the latest code from repository

**Actions**:
- Clone repository
- Checkout specific branch
- Get Git commit hash and branch name
- Set environment variables

**Key Metrics**:
- Commit SHA (for traceability)
- Branch name

### Stage 2: Build All Services (1-2 min)
**Purpose**: Compile all microservices

**Actions**:
```bash
mvn clean compile -T 4 -DskipTests
```
- `-T 4`: Use 4 threads for parallel compilation
- `-DskipTests`: Skip test execution (run separately)

**Outputs**:
- Compiled `.class` files in `target/classes/`
- Build logs

### Stage 3: Run Tests (1-2 min)
**Purpose**: Execute all unit tests

**Actions**:
```bash
mvn test -T 4
```
- Run JUnit tests
- Generate Surefire reports
- Parallel execution

**Outputs**:
- `target/surefire-reports/*.xml`
- Test success/failure count
- Jenkins test report

### Stage 4: Code Coverage (30s)
**Purpose**: Measure test coverage

**Actions**:
```bash
mvn jacoco:report
```
- Analyze test execution
- Generate coverage metrics
- Create HTML reports

**Outputs**:
- Coverage percentage
- Line coverage report
- Branch coverage report
- `target/site/jacoco/index.html`

**Thresholds**:
- Minimum: 50% (configured in pom.xml)
- Target: 70-80%

### Stage 5: SonarQube Analysis (1-2 min)
**Purpose**: Static code analysis

**Actions**:
```bash
mvn sonar:sonar \
  -Dsonar.projectKey=xxx \
  -Dsonar.host.url=xxx \
  -Dsonar.login=xxx
```

**Metrics Analyzed**:
- Code smells
- Bugs
- Vulnerabilities
- Technical debt
- Duplications
- Complexity
- Coverage

### Stage 6: Quality Gate (10-60s)
**Purpose**: Enforce quality standards

**Checks**:
- Coverage > threshold
- No blocker/critical issues
- Duplications < 3%
- Maintainability rating A/B
- Security rating A

**Result**:
- ✅ PASS → Continue pipeline
- ❌ FAIL → Abort pipeline

### Stage 7: Package Services (1 min)
**Purpose**: Create deployable artifacts

**Actions**:
```bash
mvn package -DskipTests -T 4
```

**Outputs**:
- JAR files for each service
- Archived in Jenkins
- `**/target/*.jar`

### Stage 8: Build Docker Images (2-4 min)
**Purpose**: Create containerized applications

**Parallel Execution**:
```groovy
parallel {
    stage('Gateway') { ... }
    stage('User')    { ... }
    stage('Product') { ... }
    stage('Order')   { ... }
}
```

**Per Service**:
```bash
cd service-name
docker build -t service-name:${VERSION} .
docker tag service-name:${VERSION} service-name:latest
```

**Optimization**:
- Multi-stage builds
- Layer caching
- Small base images (Alpine)

### Stage 9: Push to Registry (1-2 min)
**Condition**: Only on `main`, `develop`, or `release/*` branches

**Actions**:
```bash
# Login to ECR
aws ecr get-login-password | docker login ...

# Tag for registry
docker tag service:v1 registry/service:v1
docker tag service:v1 registry/service:latest

# Push
docker push registry/service:v1
docker push registry/service:latest
```

### Stage 10: Deploy to EKS (1-3 min)
**Condition**: Only on `main` branch

**Actions**:
```bash
# Update kubeconfig
aws eks update-kubeconfig --name cluster

# Apply configurations
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/*/

# Update images (rolling update)
kubectl set image deployment/service service=image:tag

# Wait for rollout
kubectl rollout status deployment/service
```

**Deployment Strategy**: Rolling update (zero downtime)

### Stage 11: Health Check (30s)
**Purpose**: Verify deployment success

**Checks**:
```bash
kubectl get pods -n microservices
kubectl get services -n microservices
curl http://service/actuator/health
```

---

## 🔄 Branch Strategy

| Branch | Stages Run | Deploy Target |
|--------|-----------|---------------|
| `feature/*` | Build → Test → Quality | None |
| `develop` | Build → Test → Quality → Docker → Push | Dev Registry |
| `main` | All stages | Production EKS |
| `release/*` | Build → Test → Quality → Docker → Push | Staging |

---

## 🎯 Parallel Execution Strategy

### Why Parallel?
- **4x faster compilation** with Maven `-T 4`
- **Simultaneous Docker builds** for 4 services
- **Reduced total pipeline time** from ~20min to ~10min

### Where We Parallelize:

1. **Maven Compilation** (`-T 4`)
   - Uses 4 threads
   - Compiles multiple modules simultaneously

2. **Maven Tests** (`-T 4`)
   - Run independent test classes in parallel

3. **Docker Image Builds**
   - All 4 services build simultaneously
   - No dependencies between them

### Where We Don't:
- SonarQube analysis (sequential by nature)
- Deployment (order matters)
- Quality gate (must complete before proceeding)

---

## 🔐 Security & Credentials

### Stored Credentials:
1. **SonarQube Token** (`sonarqube-token`)
   - Type: Secret text
   - Used in: Code analysis stage
   - Scope: Global

2. **AWS Credentials** (`aws-credentials-id`)
   - Type: AWS Credentials
   - Used in: ECR push, EKS deploy
   - Permissions needed:
     - ECR: `GetAuthorizationToken`, `PutImage`
     - EKS: `DescribeCluster`, `UpdateClusterConfig`

3. **Git Credentials** (optional)
   - Type: Username/Password
   - Used in: Checkout stage
   - Only for private repos

### Best Practices:
- ✅ Never hardcode credentials
- ✅ Use Jenkins credential manager
- ✅ Rotate tokens regularly
- ✅ Principle of least privilege
- ✅ Audit credential usage

---

## 📈 Key Metrics & SLAs

### Build Times:
- **Target**: < 10 minutes
- **Current**: 8-12 minutes
- **Bottlenecks**: 
  - Maven tests (2-3 min)
  - Docker builds (2-4 min)
  - SonarQube analysis (1-2 min)

### Success Rate:
- **Target**: > 95%
- **Common Failures**:
  - Test failures (60%)
  - Quality gate (25%)
  - Infrastructure (15%)

### Coverage Goals:
- **Minimum**: 50%
- **Target**: 70%
- **Excellent**: 80%+

---

## 🚨 Failure Handling

### Stage Failures:

**Test Failure**:
```
Action: Pipeline STOPS
Notification: Email to dev team
Recovery: Fix tests → Re-run
```

**Quality Gate Failure**:
```
Action: Pipeline STOPS
Notification: Email with SonarQube link
Recovery: Fix issues → Re-run
```

**Docker Build Failure**:
```
Action: Pipeline STOPS
Notification: Check Dockerfile
Recovery: Fix syntax → Re-run
```

**Deploy Failure**:
```
Action: Previous version still running
Notification: Critical alert
Recovery: Fix config → Re-deploy
```

### Rollback Strategy:
```bash
# K8s automatic rollback on failed deployment
kubectl rollout undo deployment/service-name

# Or rollback to specific version
kubectl rollout undo deployment/service-name --to-revision=2
```

---

## 🔧 Optimization Tips

### Speed Improvements:
1. **Use Maven Daemon** → 20% faster builds
2. **Cache Docker layers** → 50% faster builds
3. **Incremental compilation** → 30% faster
4. **Parallel test execution** → 40% faster

### Resource Optimization:
```groovy
options {
    buildDiscarder(logRotator(numToKeepStr: '10'))  // Keep only 10 builds
    timeout(time: 30, unit: 'MINUTES')              // Hard timeout
    disableConcurrentBuilds()                       // One at a time
}
```

### Cost Optimization:
- Run expensive stages only on main branch
- Skip Docker build on feature branches
- Use spot instances for Jenkins agents
- Auto-scale Jenkins agents

---

## 📊 Monitoring & Reporting

### Available Reports:

1. **Test Reports**
   - Location: Build page → Test Result
   - Shows: Pass/fail, trends, flaky tests

2. **Coverage Reports**
   - Location: Build page → JaCoCo Coverage
   - Shows: Line, branch, method coverage

3. **SonarQube Dashboard**
   - Location: https://sonarcloud.io
   - Shows: Quality metrics, trends, hotspots

4. **Build Trends**
   - Location: Job page → Build History
   - Shows: Success rate, duration trends

### Notifications:
- ✅ Email on failure
- ✅ Email on success (configurable)
- ⚙️ Slack (optional)
- ⚙️ MS Teams (optional)

---

## 🎓 Interview Questions & Answers

**Q: Why use declarative instead of scripted pipeline?**
*A: Declarative is more maintainable, has better error handling, provides automatic stage visualization in Blue Ocean, and follows Jenkins best practices with a simpler syntax.*

**Q: How do you handle secrets in Jenkins?**
*A: Use Jenkins Credentials Manager, never hardcode in Jenkinsfile, use the `credentials()` helper to inject at runtime, and rotate regularly.*

**Q: How do you optimize build time?**
*A: Parallel execution with Maven -T flag, parallel Docker builds, caching dependencies, multi-stage Docker builds, and running only necessary stages per branch.*

**Q: What happens if Quality Gate fails?**
*A: Pipeline stops immediately with `abortPipeline: true`, sends notification with SonarQube link, prevents bad code from reaching production.*

**Q: How do you ensure zero-downtime deployments?**
*A: Use Kubernetes rolling updates with readiness probes, health checks, proper resource requests/limits, and PodDisruptionBudgets.*

**Q: How is this different from GitHub Actions?**
*A: Jenkins offers more control and plugins (1500+), better for complex workflows, can run on-premises, though GitHub Actions is simpler for basic CI/CD and better integrated with GitHub.*

---

## 📚 Additional Resources

- **Jenkinsfile**: Main pipeline definition
- **Jenkinsfile.local**: Local testing version (no AWS)
- **JENKINS_SETUP.md**: Detailed setup instructions
- **JENKINS_QUICKSTART.md**: 30-minute quick start guide
- **Docker Compose**: Local container orchestration
- **K8s manifests**: `/k8s/*` directory

---

Built with ❤️ for interview success! 🚀
