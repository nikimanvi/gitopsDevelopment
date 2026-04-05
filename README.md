# Microservices Project

A Maven-based Java project for building microservices applications.

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/app/
│   │   └── resources/
│   └── test/
│       ├── java/
│       │   └── com/example/app/
│       └── resources/
├── pom.xml
└── README.md
```

## Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher

## Building the Project

```bash
# Clean and install dependencies
mvn clean install

# Compile the project
mvn compile

# Run tests
mvn test
```

## Development

- Add Java source files to `src/main/java/com/example/app/`
- Add test files to `src/test/java/com/example/app/`
- Update dependencies in [pom.xml](pom.xml) as needed

## 🐳 Docker Deployment

### Build Docker Images
```powershell
# Build all services
.\build-all.ps1

# Or use Docker Compose
docker-compose build

# Start all services
docker-compose up -d
```

See [Docker documentation](docker-compose.yml) for configuration details.

---

## 🚀 Jenkins CI/CD Pipeline

### Quick Decision Guide

**For Enterprise Production:** Use [Jenkinsfile.enterprise](Jenkinsfile.enterprise)  
**For Development/Testing:** Use [Jenkinsfile.local](Jenkinsfile.local)  
**Current Production:** [Jenkinsfile](Jenkinsfile) (already good!)

### Key Features
- ✅ Automated build and testing
- ✅ Docker image creation and registry push
- ✅ Security scanning (SonarQube, Trivy, OWASP)
- ✅ Kubernetes deployment to EKS
- ✅ Multi-environment support (dev/staging/production)
- ✅ Automatic rollback on failure
- ✅ Comprehensive notifications

### Documentation
- 📘 **[Which Pipeline for Enterprise?](JENKINS_COMPARISON.md)** - Feature comparison and recommendations
- 📗 **[Enterprise Guide](JENKINS_ENTERPRISE_GUIDE.md)** - Best practices and advanced features
- 📕 **[Setup Guide](JENKINS_SETUP.md)** - Installation and configuration
- 📙 **[Quick Start](JENKINS_QUICKSTART.md)** - Get running in 30 minutes
- 📓 **[Pipeline Architecture](PIPELINE_ARCHITECTURE.md)** - Flow diagrams and design

### Quick Start
```powershell
# 1. Install Jenkins (using Docker)
docker run -d -p 8080:8080 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts

# 2. Create pipeline job pointing to Jenkinsfile.local (for testing)

# 3. Build! Images will be created and tagged automatically
```

For detailed setup, see [JENKINS_COMPARISON.md](JENKINS_COMPARISON.md)

---

## Project Information

- **GroupId**: com.example
- **ArtifactId**: microservices
- **Version**: 1.0-SNAPSHOT
- **Java Version**: 11
