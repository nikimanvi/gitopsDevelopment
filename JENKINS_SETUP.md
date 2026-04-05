# Jenkins CI/CD Setup Guide

Complete guide to set up Jenkins CI/CD pipeline for this microservices project.

## 📋 Prerequisites

- Jenkins 2.400+ installed
- Docker installed on Jenkins agent
- AWS CLI configured (for ECR/EKS deployment)
- kubectl installed (for Kubernetes deployment)

## 🚀 Jenkins Installation (Windows)

### Option 1: Docker (Recommended for Testing)
```powershell
docker run -d -p 8080:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  --name jenkins `
  jenkins/jenkins:lts
```

### Option 2: Windows Installer
1. Download from: https://www.jenkins.io/download/
2. Run installer
3. Access: http://localhost:8080
4. Get initial password: `C:\Program Files\Jenkins\secrets\initialAdminPassword`

---

## 🔧 Step 1: Install Required Plugins

Navigate to **Manage Jenkins** → **Plugin Manager** → **Available Plugins**

### Essential Plugins:
- ✅ **Pipeline** (Jenkins Pipeline)
- ✅ **Git** (Git integration)
- ✅ **Docker Pipeline** (Docker build/push)
- ✅ **SonarQube Scanner** (Code quality)
- ✅ **Amazon ECR** (AWS container registry)
- ✅ **Kubernetes** (K8s deployment)
- ✅ **Email Extension** (Notifications)
- ✅ **Blue Ocean** (Better UI - optional)
- ✅ **Pipeline AWS Steps** (AWS integration)
- ✅ **JUnit** (Test reporting)
- ✅ **HTML Publisher** (Test reports)

Install and restart Jenkins.

---

## 🛠️ Step 2: Global Tool Configuration

**Manage Jenkins** → **Global Tool Configuration**

### Maven Configuration
1. Click **Add Maven**
2. Name: `Maven-3.9`
3. ☑️ Install automatically
4. Version: `3.9.6` (or latest)

### JDK Configuration
1. Click **Add JDK**
2. Name: `JDK-21`
3. ☑️ Install automatically
4. Version: `java.net` → `jdk-21`
   - OR manually point to existing: `C:\Program Files\Java\jdk-21`

### Docker Configuration
1. Click **Add Docker**
2. Name: `Docker`
3. ☑️ Install automatically
4. OR use system Docker if already installed

### SonarQube Scanner
1. Click **Add SonarQube Scanner**
2. Name: `SonarQube Scanner`
3. ☑️ Install automatically
4. Version: Latest

---

## 🔐 Step 3: Configure Credentials

**Manage Jenkins** → **Credentials** → **System** → **Global credentials**

### 1. SonarQube Token
- **Kind**: Secret text
- **ID**: `sonarqube-token`
- **Secret**: [Your SonarQube/SonarCloud token]
- **Description**: SonarQube Authentication Token

**To get token:**
- SonarCloud: https://sonarcloud.io/account/security
- Local SonarQube: User → My Account → Security → Generate Token

### 2. AWS Credentials (for ECR/EKS)
- **Kind**: AWS Credentials
- **ID**: `aws-credentials-id`
- **Access Key ID**: [Your AWS Access Key]
- **Secret Access Key**: [Your AWS Secret Key]
- **Description**: AWS ECR & EKS Access

### 3. GitHub/Git Credentials (if private repo)
- **Kind**: Username with password
- **ID**: `github-credentials`
- **Username**: [Your GitHub username]
- **Password**: [Personal Access Token]

### 4. DockerHub Credentials (Alternative to ECR)
- **Kind**: Username with password
- **ID**: `dockerhub-credentials`
- **Username**: [DockerHub username]
- **Password**: [DockerHub password/token]

---

## ⚙️ Step 4: Configure SonarQube Server

**Manage Jenkins** → **Configure System** → **SonarQube Servers**

1. Click **Add SonarQube**
2. **Name**: `SonarQube`
3. **Server URL**: 
   - SonarCloud: `https://sonarcloud.io`
   - Local: `http://localhost:9000`
4. **Server authentication token**: Select `sonarqube-token`
5. **Save**

---

## 📝 Step 5: Create Jenkins Pipeline Job

### Method 1: Pipeline from SCM (Recommended)

1. **New Item** → Enter name: `Microservices-Pipeline`
2. Choose **Pipeline**
3. **Pipeline** section:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/yourusername/microservices.git`
   - Credentials: Select if private repo
   - Branch: `*/main` or `*/develop`
   - Script Path: `Jenkinsfile`
4. **Save**

### Method 2: Multibranch Pipeline (Advanced)

1. **New Item** → `Microservices-Multibranch`
2. Choose **Multibranch Pipeline**
3. **Branch Sources** → Add Git
4. Configure repository URL
5. **Save**

---

## 🎯 Step 6: Configure Jenkinsfile

Update the `Jenkinsfile` environment variables:

```groovy
environment {
    // Update these values
    DOCKER_REGISTRY = 'your-account-id.dkr.ecr.us-east-1.amazonaws.com'
    AWS_REGION = 'us-east-1'
    AWS_CREDENTIALS = 'aws-credentials-id'
    
    SONAR_HOST_URL = 'https://sonarcloud.io' // or your SonarQube URL
    SONAR_PROJECT_KEY = 'your-sonarcloud-project-key'
    
    EKS_CLUSTER_NAME = 'microservices-cluster'
}
```

---

## 📊 Step 7: Update POM Files for SonarQube

Add to root `pom.xml` and each service's `pom.xml`:

```xml
<properties>
    <sonar.organization>your-org</sonar.organization>
    <sonar.projectKey>microservices-project</sonar.projectKey>
    <sonar.host.url>https://sonarcloud.io</sonar.host.url>
    <sonar.coverage.jacoco.xmlReportPaths>target/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
</properties>

<build>
    <plugins>
        <!-- JaCoCo for Code Coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 🐳 Step 8: ECR Repository Setup (AWS)

Create ECR repositories for each service:

```powershell
aws ecr create-repository --repository-name gateway-service --region us-east-1
aws ecr create-repository --repository-name user-service --region us-east-1
aws ecr create-repository --repository-name product-service --region us-east-1
aws ecr create-repository --repository-name order-service --region us-east-1
```

---

## ☸️ Step 9: EKS Cluster Setup (Optional)

If deploying to EKS, ensure cluster exists:

```powershell
# Create EKS cluster (if not exists)
aws eks create-cluster --name microservices-cluster --region us-east-1 --kubernetes-version 1.28

# Configure kubectl
aws eks update-kubeconfig --name microservices-cluster --region us-east-1

# Verify connection
kubectl get nodes
```

---

## 🧪 Step 10: Test the Pipeline

### Local Test (Before Jenkins)
```powershell
# Test build
mvn clean package

# Test Docker build
cd gateway-service
docker build -t gateway-service:test .
cd ..

# Run SonarQube locally
mvn sonar:sonar `
  -Dsonar.projectKey=microservices-project `
  -Dsonar.host.url=https://sonarcloud.io `
  -Dsonar.login=your-token
```

### Run Jenkins Pipeline

1. Open Jenkins job
2. Click **Build Now**
3. View **Blue Ocean** for better visualization
4. Monitor console output

---

## 📈 Pipeline Stages Overview

| Stage | Duration | Description |
|-------|----------|-------------|
| Checkout | ~10s | Clone repository |
| Build All Services | 1-2min | Maven compile |
| Run Tests | 1-2min | Execute unit tests |
| Code Quality Analysis | 1-2min | SonarQube scan |
| Quality Gate | 10-60s | Wait for SonarQube result |
| Package Services | 1-2min | Create JAR files |
| Build Docker Images | 2-4min | Build all service images (parallel) |
| Push to Registry | 1-2min | Push to ECR |
| Deploy to EKS | 1-3min | Kubernetes deployment |
| Health Check | 30s | Verify deployment |

**Total Duration**: ~10-15 minutes

---

## 🔔 Step 11: Configure Notifications

### Email Notifications

**Manage Jenkins** → **Configure System** → **Extended E-mail Notification**

1. SMTP server: `smtp.gmail.com`
2. SMTP Port: `465`
3. Use SSL: ✅
4. Credentials: Add Gmail app password
5. Default recipients: `your-email@example.com`

### Slack Notifications (Optional)

1. Install **Slack Notification** plugin
2. Add Slack token in credentials
3. Update Jenkinsfile post section

---

## 🎨 Optional: Blue Ocean Setup

For better visualization:

```powershell
# Install Blue Ocean plugin
# Access at: http://localhost:8080/blue
```

---

## 🔍 Troubleshooting

### Issue: Maven version mismatch
**Solution**: Ensure Maven 3.9+ is configured in Global Tool Configuration

### Issue: Docker not found
**Solution**: 
```powershell
# Add Jenkins user to docker group (Linux)
sudo usermod -aG docker jenkins

# Windows: Ensure Docker Desktop is running
```

### Issue: Quality Gate fails
**Solution**: Check SonarQube dashboard for issues, adjust thresholds if needed

### Issue: AWS credentials error
**Solution**: Verify IAM permissions for ECR push/pull and EKS access

### Issue: kubectl not found
**Solution**: Install kubectl and add to PATH:
```powershell
choco install kubernetes-cli
```

---

## 🚀 Quick Start Commands

```powershell
# 1. Start Jenkins
docker start jenkins

# 2. Access Jenkins
start http://localhost:8080

# 3. Trigger build
# Via UI: Click "Build Now"
# Or via curl:
curl -X POST http://localhost:8080/job/Microservices-Pipeline/build `
  --user admin:your-token
```

---

## 📚 Additional Resources

- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)
- [SonarQube Integration](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-jenkins/)
- [AWS ECR Documentation](https://docs.aws.amazon.com/ecr/)
- [Jenkins Blue Ocean](https://www.jenkins.io/doc/book/blueocean/)

---

## ✅ Checklist

- [ ] Jenkins installed and accessible
- [ ] All required plugins installed
- [ ] Maven and JDK configured
- [ ] SonarQube server configured
- [ ] AWS credentials added
- [ ] ECR repositories created
- [ ] Jenkinsfile committed to repository
- [ ] Pipeline job created in Jenkins
- [ ] First build successful
- [ ] Email notifications working
- [ ] Quality gate passing

---

## 🎯 Interview Tips

**Be ready to discuss:**
- Why you chose declarative vs scripted pipeline
- Parallel execution strategy for Docker builds
- Quality gate configuration and thresholds
- Blue-green vs rolling deployment strategies
- Pipeline optimization techniques
- Secret management in CI/CD
- Branch-based deployment strategy
