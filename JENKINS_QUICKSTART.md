# 🚀 Quick Start Guide - Jenkins CI/CD

Get your Jenkins pipeline running in under 30 minutes!

## 📦 What You'll Get

✅ Automated build and test
✅ Code quality checks with SonarQube
✅ Docker image building
✅ Test coverage reports
✅ Quality gate enforcement

---

## 🏃 Quick Setup (3 Steps)

### Step 1: Install Jenkins (5 minutes)

**Using Docker (Easiest):**
```powershell
# Start Jenkins
docker run -d -p 8080:8080 -p 50000:50000 `
  -v jenkins_home:/var/jenkins_home `
  -v /var/run/docker.sock:/var/run/docker.sock `
  --name jenkins `
  jenkins/jenkins:lts

# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Open in browser
start http://localhost:8080
```

**Or Download Installer:**
- Windows: https://www.jenkins.io/download/
- Run installer → Access at http://localhost:8080

### Step 2: Install Plugins (5 minutes)

After Jenkins starts:
1. Choose **"Install suggested plugins"**
2. Create admin user
3. Go to **Manage Jenkins** → **Plugins** → **Available**
4. Install these additional plugins:
   - ✅ Pipeline
   - ✅ Git
   - ✅ Docker Pipeline
   - ✅ SonarQube Scanner
   - ✅ HTML Publisher
5. Restart Jenkins

### Step 3: Configure Tools (5 minutes)

**Manage Jenkins** → **Global Tool Configuration**

**Add Maven:**
- Name: `Maven-3.9`
- ✅ Install automatically
- Version: 3.9.6

**Add JDK:**
- Name: `JDK-21`
- ✅ Install automatically
- OR point to: `C:\Program Files\Java\jdk-21`

**Add SonarQube Scanner:**
- Name: `SonarQube Scanner`
- ✅ Install automatically

**Save**

---

## 🔐 Add Credentials (5 minutes)

### SonarCloud Setup
1. Go to https://sonarcloud.io
2. Sign in with GitHub
3. Click **+** → **Analyze new project**
4. Select your repository
5. Copy the **project key**
6. Go to **My Account** → **Security** → **Generate Token**
7. Copy the token

### Add to Jenkins
**Manage Jenkins** → **Credentials** → **Global** → **Add Credentials**

- Kind: **Secret text**
- ID: `sonarqube-token`
- Secret: [Paste your token]
- Description: `SonarQube Token`
- **Create**

---

## ⚙️ Configure SonarQube Server (2 minutes)

**Manage Jenkins** → **Configure System** → **SonarQube servers**

1. ✅ Enable injection of SonarQube server configuration
2. Click **Add SonarQube**
3. Name: `SonarQube`
4. Server URL: `https://sonarcloud.io`
5. Server authentication token: Select `sonarqube-token`
6. **Save**

---

## 📝 Update Your Configuration (3 minutes)

### Update pom.xml
Already done! The root `pom.xml` has been updated with:
- JaCoCo for code coverage
- SonarQube configuration

**Change these values:**
```xml
<properties>
    <sonar.organization>your-sonarcloud-org</sonar.organization>
    <sonar.projectKey>your-project-key</sonar.projectKey>
</properties>
```

### Choose Your Jenkinsfile

**For Local Testing (No AWS):**
```powershell
# Rename for use
mv Jenkinsfile Jenkinsfile.full
mv Jenkinsfile.local Jenkinsfile
```

**For Full AWS/EKS Deployment:**
Use the existing `Jenkinsfile` (update environment variables first)

---

## 🎯 Create Jenkins Job (3 minutes)

1. **New Item** → Name: `Microservices-Pipeline`
2. Choose: **Pipeline**
3. **Pipeline** section:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: Your Git repo URL
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
4. **Save**

---

## 🚀 Run Your First Build!

1. Click **Build Now**
2. Watch the pipeline in **Blue Ocean** (better UI)
3. Check console output for progress

**Expected Result:**
```
✅ Checkout         - 10s
✅ Build            - 1-2 min
✅ Unit Tests       - 1-2 min
✅ Code Coverage    - 30s
✅ SonarQube        - 1-2 min
✅ Quality Gate     - 30s
✅ Package          - 1 min
✅ Docker Build     - 2-3 min
```

**Total: ~8-10 minutes**

---

## 📊 View Reports

After build completes:

**Test Reports:**
- Build page → **Test Result**

**Code Coverage:**
- Build page → **JaCoCo Coverage Report**

**SonarQube Analysis:**
- Go to https://sonarcloud.io
- View your project dashboard

---

## 🐛 Common Issues & Fixes

### ❌ Maven not found
**Fix:** 
- Ensure Maven configured in Global Tool Configuration
- Restart Jenkins

### ❌ Docker not found
**Fix (Windows):**
```powershell
# Ensure Docker Desktop is running
docker --version

# Check Docker plugin is installed
```

### ❌ SonarQube Quality Gate fails
**Fix:**
- Go to SonarCloud → Your project
- Check what failed (coverage, duplications, bugs)
- Fix or adjust Quality Gate settings

### ❌ Permission denied on Windows
**Fix:**
```powershell
# Run PowerShell as Administrator
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

---

## 📈 See Your Pipeline in Action

**Blue Ocean (Better Visualization):**
```
http://localhost:8080/blue/organizations/jenkins/Microservices-Pipeline/
```

---

## 🎓 Interview Demo Checklist

Before your interview, ensure:

- [ ] Pipeline runs successfully
- [ ] All tests pass
- [ ] Quality Gate is green
- [ ] SonarCloud dashboard looks good
- [ ] Docker images build successfully
- [ ] You can explain each stage
- [ ] Coverage report shows >70% (aim for this)

---

## 🎯 Pipeline Stages Explained (For Interview)

### 1. **Checkout**
*"We clone the repository and get the current commit hash for tracking"*

### 2. **Build**
*"We compile all microservices in parallel using Maven with 4 threads for speed"*

### 3. **Unit Tests**
*"We run JUnit tests and generate reports using Surefire plugin"*

### 4. **Code Coverage**
*"JaCoCo measures code coverage and generates HTML reports"*

### 5. **SonarQube Analysis**
*"We scan for code smells, bugs, vulnerabilities, and technical debt"*

### 6. **Quality Gate**
*"We enforce quality standards - build fails if code doesn't meet thresholds"*

### 7. **Package**
*"We create JAR files for each microservice"*

### 8. **Docker Build**
*"We build optimized Docker images using multi-stage Dockerfiles"*

---

## 🚀 Next Steps

### For AWS/EKS Deployment:
1. Create AWS account
2. Set up ECR repositories
3. Create EKS cluster
4. Switch to full `Jenkinsfile`
5. Add AWS credentials

See **JENKINS_SETUP.md** for full details.

### Add More Features:
- **Slack notifications**: Install Slack plugin
- **Email alerts**: Configure SMTP
- **Security scanning**: Add OWASP Dependency Check
- **Integration tests**: Add new stage
- **Performance tests**: Add JMeter stage

---

## 📚 Useful Commands

```powershell
# Check Jenkins logs
docker logs jenkins

# Restart Jenkins
docker restart jenkins

# Stop Jenkins
docker stop jenkins

# Start Jenkins
docker start jenkins

# Rebuild without cache
docker-compose build --no-cache

# View running containers
docker ps

# Check Maven version in Jenkins
mvn --version
```

---

## 💡 Pro Tips

1. **Use Blue Ocean** - Much better UI than classic Jenkins
2. **Enable Console Timestamps** - Helps with debugging
3. **Keep builds fast** - Use parallel execution
4. **Cache Maven dependencies** - Use Docker volumes
5. **Version everything** - Build number in Docker tags
6. **Monitor resource usage** - Jenkins can be memory-hungry

---

## 🎤 Interview Talking Points

**"Why Jenkins?"**
*"Jenkins is industry-standard, highly extensible with 1500+ plugins, and has strong community support. It gives us full control over our CI/CD pipeline."*

**"Why declarative pipeline?"**
*"Declarative syntax is more maintainable, easier to read, and follows best practices. It provides better error handling and visualization."*

**"How do you optimize build time?"**
*"Parallel execution with Maven's -T flag, caching dependencies, multi-stage Docker builds, and running stages in parallel where possible."*

**"How do you ensure code quality?"**
*"We use SonarQube for static analysis, JaCoCo for coverage, enforce quality gates that fail builds if standards aren't met, and have mandatory code reviews."*

---

## ✅ Success Indicators

Your pipeline is ready for interviews when:
- ✅ Builds complete in under 10 minutes
- ✅ All tests are passing
- ✅ Code coverage > 70%
- ✅ SonarQube Quality Gate: PASSED
- ✅ No critical/blocker issues
- ✅ Docker images build successfully
- ✅ You can explain every stage confidently

---

**Need Help?** Check JENKINS_SETUP.md for detailed configuration.

**Ready for Production?** Switch to the full Jenkinsfile with AWS/EKS deployment.

Good luck with your interview! 🚀
