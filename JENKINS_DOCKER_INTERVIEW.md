# Jenkins & Docker Interview Questions — 6 Years Experience

---

## Table of Contents
1. [Jenkins – Core Concepts](#jenkins--core-concepts)
2. [Jenkins – Pipeline (Declarative & Scripted)](#jenkins--pipeline)
3. [Jenkins – Plugins & Integrations](#jenkins--plugins--integrations)
4. [Jenkins – CI/CD Best Practices](#jenkins--cicd-best-practices)
5. [Jenkins – Security & Credentials](#jenkins--security--credentials)
6. [Jenkins – Distributed Builds & Agents](#jenkins--distributed-builds--agents)
7. [Jenkins – Troubleshooting](#jenkins--troubleshooting)
8. [Docker – Core Concepts](#docker--core-concepts)
9. [Docker – Images & Dockerfile](#docker--images--dockerfile)
10. [Docker – Containers & Networking](#docker--containers--networking)
11. [Docker – Volumes & Storage](#docker--volumes--storage)
12. [Docker – Docker Compose](#docker--docker-compose)
13. [Docker – Security](#docker--security)
14. [Docker – Performance & Optimization](#docker--performance--optimization)
15. [Docker – Troubleshooting](#docker--troubleshooting)
16. [Jenkins + Docker Integration](#jenkins--docker-integration)
17. [Scenario-Based Questions](#scenario-based-questions)

---

## Jenkins – Core Concepts

**Q1. What is Jenkins and how does it support CI/CD?**
> Jenkins is an open-source automation server that automates building, testing, and deploying software. It supports CI by triggering builds on code commits and CD by automating deployment pipelines. It uses a master-agent architecture and has 1800+ plugins for integrations.

**Q2. What is the difference between Freestyle and Pipeline jobs?**
> Freestyle jobs are GUI-configured, single-stage jobs — good for simple tasks. Pipeline jobs use a `Jenkinsfile` (Groovy DSL), are version-controlled, support parallel stages, conditionals, loops, shared libraries, and are preferred for production CI/CD.

**Q3. What are the types of Jenkins pipelines?**
> - **Declarative Pipeline** – structured, opinionated syntax with `pipeline {}` block. Easier to read, built-in error handling.
> - **Scripted Pipeline** – uses `node {}` block, full Groovy flexibility, more control but harder to maintain.
> - Most teams prefer Declarative for maintainability.

**Q4. What is a Multibranch Pipeline?**
> Automatically creates a pipeline job for each branch in the repository. It detects branches, PRs, and tags, runs the `Jenkinsfile` found in each branch. Ideal for feature-branch workflows and GitFlow.

**Q5. What is Blue Ocean in Jenkins?**
> Blue Ocean is a modern UI for Jenkins that provides a visual, intuitive pipeline editor and visualization. It simplifies pipeline creation with a drag-and-drop interface and shows real-time stage logs.

**Q6. What is the Jenkins build lifecycle?**
> Trigger → SCM Checkout → Build → Test → Analyze → Package → Deploy → Notify → Post-Actions

**Q7. How does Jenkins handle concurrent builds?**
> By default, builds queue up. You can enable concurrent builds using `options { disableConcurrentBuilds() }` to prevent it, or set executor counts per agent to allow parallel execution.

**Q8. What is the difference between `post { always {} }` and `post { cleanup {} }`?**
> `always` runs regardless of build result. `cleanup` runs last, after all other post conditions, useful for workspace cleanup. Both run even on failure.

---

## Jenkins – Pipeline

**Q9. Explain Declarative vs Scripted Pipeline syntax differences.**
```groovy
// Declarative
pipeline {
    agent any
    stages {
        stage('Build') {
            steps { sh 'mvn clean install' }
        }
    }
}

// Scripted
node {
    stage('Build') {
        sh 'mvn clean install'
    }
}
```
> Declarative enforces structure; Scripted allows arbitrary Groovy code anywhere.

**Q10. What are `agent`, `stages`, `steps`, and `post` in a Declarative pipeline?**
> - `agent` – where to run (any, none, label, docker, kubernetes)
> - `stages` – container for all stage blocks
> - `steps` – actual commands/actions to run
> - `post` – actions after pipeline finishes (always, success, failure, unstable, changed, cleanup)

**Q11. How do you run stages in parallel?**
```groovy
stage('Parallel Build') {
    parallel {
        stage('Service A') { steps { sh 'mvn -f service-a/pom.xml package' } }
        stage('Service B') { steps { sh 'mvn -f service-b/pom.xml package' } }
    }
}
```

**Q12. How do you pass parameters to a Jenkins pipeline?**
```groovy
parameters {
    string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to build')
    booleanParam(name: 'SKIP_TESTS', defaultValue: false)
    choice(name: 'ENV', choices: ['dev','staging','prod'])
}
// Access via: params.BRANCH, params.SKIP_TESTS
```

**Q13. What is `stash` and `unstash`?**
> `stash` saves files from the current workspace to be reused in another stage or agent. `unstash` retrieves them. Useful when stages run on different agents.
```groovy
stash name: 'built-jars', includes: '**/target/*.jar'
unstash 'built-jars'
```

**Q14. How do you use `when` conditions in a pipeline?**
```groovy
stage('Deploy to Prod') {
    when {
        branch 'main'
        environment name: 'DEPLOY_TO_PROD', value: 'true'
    }
    steps { sh './deploy.sh prod' }
}
```

**Q15. What is a Shared Library in Jenkins?**
> A Shared Library is a reusable Groovy code repository referenced by multiple pipelines. Avoids code duplication. Stored in a separate Git repo, configured in Jenkins Global Settings.
```groovy
@Library('my-shared-lib') _
import com.myorg.DeployHelper
```

**Q16. How do you handle retries and timeouts in a pipeline?**
```groovy
stage('Deploy') {
    options {
        retry(3)
        timeout(time: 10, unit: 'MINUTES')
    }
    steps { sh './deploy.sh' }
}
```

**Q17. What is `catchError` and when do you use it?**
> Catches failures in a step without failing the whole pipeline. Used when you want the build to continue after a non-critical failure (e.g., optional static analysis).
```groovy
catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
    sh 'optional-check.sh'
}
```

**Q18. How do you trigger a downstream job from a pipeline?**
```groovy
build job: 'downstream-pipeline',
      parameters: [string(name: 'VERSION', value: env.BUILD_NUMBER)],
      wait: true
```

---

## Jenkins – Plugins & Integrations

**Q19. What are the most important Jenkins plugins you've used?**
> - **Pipeline** – core pipeline support
> - **Git** – SCM integration
> - **Maven Integration** – Maven build support
> - **Docker Pipeline** – Docker operations in pipeline
> - **Credentials Binding** – secure credential injection
> - **Blue Ocean** – visualized pipelines
> - **JUnit** – test result publishing
> - **HTML Publisher** – coverage and report publishing
> - **Email Extension** – build notifications
> - **Kubernetes** – dynamic agent provisioning
> - **OWASP Dependency-Check** – security scanning
> - **SonarQube Scanner** – code quality

**Q20. How does the SonarQube plugin integrate with Jenkins?**
> Configure SonarQube server in Jenkins → Manage Jenkins → Configure System. Use `withSonarQubeEnv('server-name')` wrapper in pipeline. Quality Gate results fetched via `waitForQualityGate()`.

**Q21. How do you integrate Maven with Jenkins?**
> Install Maven under Manage Jenkins → Global Tool Config, give it a name. Reference in pipeline:
```groovy
tools { maven 'Maven' }
steps { bat 'mvn clean install' }
```

**Q22. How do you send email notifications from Jenkins?**
```groovy
post {
    failure {
        mail to: 'team@company.com',
             subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
             body: "Check: ${env.BUILD_URL}"
    }
}
```

---

## Jenkins – CI/CD Best Practices

**Q23. What is the difference between CI, CD (Delivery), and CD (Deployment)?**
> - **CI** – Continuous Integration: merge + build + test on every push
> - **Continuous Delivery** – code is always in a deployable state; deployment is manual
> - **Continuous Deployment** – every passing build is automatically deployed to production

**Q24. How do you implement GitOps with Jenkins?**
> Store all infrastructure and deployment config in Git. Jenkins watches the repo, any change to config triggers the pipeline which applies changes to the environment (K8s, Helm charts, etc.). The Git repo is the single source of truth.

**Q25. What is a blue-green deployment and how does Jenkins support it?**
> Two identical environments (blue=live, green=new). Deploy to green, run tests, then switch traffic. Jenkins orchestrates this via deployment scripts or Kubernetes ingress updates, rolls back by routing back to blue.

**Q26. How do you implement canary releases with Jenkins?**
> Deploy new version to a small subset of users/pods first. Monitor metrics. Gradually increase traffic. Jenkins uses Helm or Kubernetes rollout commands with percentage-based traffic splitting (Istio/NGINX).

**Q27. What is the `Jenkinsfile` best practice for large teams?**
> - Store `Jenkinsfile` in the root of each repository (pipeline-as-code)
> - Use Shared Libraries for common logic
> - Pin tool/image versions explicitly
> - Use `lock()` for critical resources
> - Always clean workspace in `post { cleanup {} }`
> - Use `credentialsId` — never hardcode secrets

---

## Jenkins – Security & Credentials

**Q28. How do you manage secrets in Jenkins securely?**
> Use Jenkins Credential Manager (Manage Jenkins → Credentials). Never echo or print credentials. Use `withCredentials` block:
```groovy
withCredentials([usernamePassword(
    credentialsId: 'docker-hub-credentials',
    usernameVariable: 'USER',
    passwordVariable: 'PASS'
)]) {
    sh 'echo $PASS | docker login -u $USER --password-stdin'
}
```

**Q29. What are the types of credentials in Jenkins?**
> - Username with password
> - Secret text (tokens, API keys)
> - SSH username with private key
> - Certificate (PKCS#12)
> - Secret file

**Q30. What is the Jenkins security realm and authorization strategy?**
> - **Security Realm** – controls authentication (Jenkins DB, LDAP, GitHub OAuth, SAML)
> - **Authorization Strategy** – controls what users can do (Matrix-based, Role-based via Role Strategy plugin, Project-based)

**Q31. How do you prevent secret leakage in Jenkins logs?**
> Credentials bound via `withCredentials` are automatically masked in logs. Avoid `echo $SECRET`. Use `set +x` in shell scripts. The Mask Passwords plugin adds extra protection.

**Q32. What is CSRF protection in Jenkins?**
> Cross-Site Request Forgery protection. Jenkins generates a crumb token for each session. API calls must include the crumb. Enable under Manage Jenkins → Configure Global Security.

---

## Jenkins – Distributed Builds & Agents

**Q33. What is a Jenkins agent (slave)?**
> An agent is a machine that runs build jobs delegated by the controller (master). Agents can be permanent (static) or ephemeral (cloud/Kubernetes). They connect via JNLP, SSH, or Docker.

**Q34. What is the difference between `agent any`, `agent none`, and `agent { label 'linux' }`?**
> - `agent any` – run on any available agent
> - `agent none` – no global agent; each stage must define its own
> - `agent { label 'linux' }` – run only on agents tagged with 'linux'

**Q35. How do you use Docker as a Jenkins agent?**
```groovy
agent {
    docker {
        image 'maven:3.9-eclipse-temurin-21'
        args '-v /root/.m2:/root/.m2'
    }
}
```
> Each build runs in a fresh Docker container. Clean, isolated, reproducible builds.

**Q36. How do you use Kubernetes as a Jenkins agent?**
> Install Kubernetes plugin. Configure cloud in Jenkins. Define pod templates in pipeline:
```groovy
agent {
    kubernetes {
        yaml '''
        spec:
          containers:
          - name: maven
            image: maven:3.9
            command: [sleep, infinity]
        '''
    }
}
```
> K8s spins up a pod per build, tears it down after — infinite scalability.

**Q37. What is the executor count in Jenkins?**
> The number of concurrent builds an agent can run. Set per agent/node. Controller should have 0 executors (don't run builds on master/controller in production).

---

## Jenkins – Troubleshooting

**Q38. A Jenkins build is stuck in queue. How do you debug it?**
> Check: no available executors, label mismatch between job and agents, offline agents, resource locks held, build throttling plugin, or disk space full on agent.

**Q39. How do you debug a failing pipeline stage?**
> - Add `echo` statements to print variables
> - Use `sh 'env'` to dump environment
> - Check Blue Ocean stage logs
> - Use `try/catch` to capture errors
> - Re-run with `Replay` to edit pipeline on the fly without committing

**Q40. What causes "workspace is missing" error?**
> Build ran on an agent where the workspace was cleaned or the agent was replaced. Solutions: use `checkout scm` at the start of every pipeline, avoid assuming workspace persists across builds.

**Q41. What is the "No such DSL method" error?**
> A plugin is missing that provides that DSL step. Install the required plugin (e.g., publishHTML requires HTML Publisher plugin).

---

## Docker – Core Concepts

**Q42. What is Docker and how does it differ from a VM?**
> Docker uses OS-level virtualization (containers share the host kernel). VMs include a full OS per instance. Containers start in milliseconds, use less memory, are more portable. VMs provide stronger isolation.

**Q43. What are the core components of Docker architecture?**
> - **Docker Daemon** (`dockerd`) – background service managing containers
> - **Docker Client** (`docker`) – CLI sending commands to daemon
> - **Docker Registry** – image storage (Docker Hub, ECR, GCR)
> - **Images** – read-only templates
> - **Containers** – running instances of images
> - **Volumes** – persistent storage
> - **Networks** – container communication

**Q44. What is the difference between an image and a container?**
> An image is a static, immutable blueprint (like a class). A container is a live, running instance of an image (like an object). Multiple containers can run from the same image.

**Q45. What is a Docker registry vs a repository?**
> - **Registry** – the server hosting repositories (e.g., hub.docker.com, ECR)
> - **Repository** – a collection of related images with the same name but different tags (e.g., `nginx:latest`, `nginx:1.25`)

**Q46. What is the Docker build context?**
> The set of files sent to the Docker daemon when running `docker build`. It's the directory specified (`.` for current). Keep it small — use `.dockerignore` to exclude files not needed in the image.

---

## Docker – Images & Dockerfile

**Q47. What is a multi-stage build and why is it important?**
```dockerfile
# Stage 1 - Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 - Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```
> Final image only contains the JRE + JAR, not the Maven/JDK build tools. Significantly reduces image size.

**Q48. What is the difference between `CMD` and `ENTRYPOINT`?**
> - `CMD` – default command, overridden by arguments passed to `docker run`
> - `ENTRYPOINT` – fixed entry point, `docker run` arguments are appended
> - Combined: `ENTRYPOINT` sets the executable, `CMD` provides default args
```dockerfile
ENTRYPOINT ["java", "-jar"]
CMD ["app.jar"]   # docker run myimage other.jar → overrides CMD only
```

**Q49. What is the difference between `COPY` and `ADD`?**
> `COPY` – simple file/directory copy from build context. Preferred for clarity.
> `ADD` – has extra features: auto-extracts tar archives, supports URLs. Use `COPY` unless you specifically need `ADD`'s features.

**Q50. What is `EXPOSE` and does it actually publish ports?**
> `EXPOSE` is documentation — it tells Docker the container listens on that port. It does NOT open the port on the host. You need `-p 8080:8080` in `docker run` or `ports:` in docker-compose to actually publish.

**Q51. How do you reduce Docker image size?**
> - Use multi-stage builds
> - Use `alpine` or `slim` base images
> - Combine `RUN` commands with `&&` to reduce layers
> - Use `.dockerignore` to exclude build artifacts
> - Run `apt-get clean && rm -rf /var/lib/apt/lists/*` after package installs
> - Don't install dev dependencies in production image

**Q52. What is a Docker layer and how does layer caching work?**
> Each instruction in a Dockerfile creates a layer. Layers are cached. If a layer and all layers below it are unchanged, Docker uses the cache. Place frequently changing instructions (like `COPY src`) after stable ones (like `RUN mvn dependency:go-offline`) to maximize cache hits.

**Q53. What is the difference between `docker build` and `docker buildx`?**
> `docker build` uses the legacy builder. `docker buildx` uses BuildKit — supports multi-platform builds (AMD64, ARM64), better caching, secrets mounting, and is significantly faster.

**Q54. How do you pass build arguments to a Dockerfile?**
```dockerfile
ARG APP_VERSION=1.0
RUN echo "Building version $APP_VERSION"
```
```bash
docker build --build-arg APP_VERSION=2.5 .
```
> `ARG` is build-time only (not available at runtime). Use `ENV` if you need it at runtime.

---

## Docker – Containers & Networking

**Q55. What is the difference between `docker stop` and `docker kill`?**
> `docker stop` sends SIGTERM, waits 10s for graceful shutdown, then sends SIGKILL.
> `docker kill` sends SIGKILL immediately — hard stop, no cleanup.

**Q56. What are Docker network types?**
> - **bridge** – default; containers on same bridge can communicate by container name
> - **host** – container shares host network stack (no port mapping needed)
> - **none** – no network
> - **overlay** – multi-host networking (Docker Swarm/Kubernetes)
> - **macvlan** – assigns MAC address to container, appears as physical device

**Q57. How do containers communicate on the same Docker network?**
> Containers on a user-defined bridge network can reach each other using their **container name or service name** as hostname (Docker's internal DNS). Default bridge network does NOT support name resolution.

**Q58. What is `--network host` and when would you use it?**
> Container uses host's network directly — same IP, no NAT. Used for performance-sensitive applications or when port mapping overhead matters. Not available on Docker Desktop for Mac/Windows (only Linux).

**Q59. How do you inspect a container's network?**
```bash
docker inspect <container_id> --format '{{json .NetworkSettings}}'
docker network inspect bridge
```

---

## Docker – Volumes & Storage

**Q60. What are the types of Docker data storage?**
> - **Volumes** – managed by Docker, stored in `/var/lib/docker/volumes/`. Best for persistence.
> - **Bind mounts** – maps host directory to container path. Good for dev (live code changes).
> - **tmpfs mounts** – in-memory only, lost on container stop. Good for sensitive temp data.

**Q61. What is the difference between a named volume and an anonymous volume?**
> Named: `docker run -v mydata:/app/data` — survives container deletion, reusable.
> Anonymous: `docker run -v /app/data` — Docker assigns a random name, harder to manage.

**Q62. How do you back up a Docker volume?**
```bash
docker run --rm -v myvolume:/data -v $(pwd):/backup alpine \
  tar czf /backup/backup.tar.gz -C /data .
```

**Q63. What happens to volume data when a container is removed?**
> Named volumes persist after `docker rm`. Use `docker rm -v` or `docker volume rm` to delete them. Anonymous volumes are typically removed with the container.

---

## Docker – Docker Compose

**Q64. What is Docker Compose and what problem does it solve?**
> Docker Compose is a tool for defining and running multi-container applications using a YAML file (`docker-compose.yml`). Solves the problem of manually running and linking multiple `docker run` commands.

**Q65. What is the difference between `docker-compose up` and `docker-compose up --build`?**
> `up` starts containers, reuses existing images if present.
> `up --build` forces rebuild of images before starting. Use when Dockerfile or source code changed.

**Q66. What does `depends_on` do in Docker Compose?**
> Controls startup order — a service waits for its dependency to start. It does NOT wait for the dependency to be healthy/ready. Use healthchecks + `condition: service_healthy` for true readiness:
```yaml
depends_on:
  db:
    condition: service_healthy
```

**Q67. What is a Docker Compose healthcheck?**
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```
> Docker periodically runs the test command. Container is marked healthy/unhealthy accordingly.

**Q68. How do you scale a service in Docker Compose?**
```bash
docker-compose up --scale product-service=3
```
> Spins up 3 instances of product-service. Port mappings must use dynamic ports (omit host port) to avoid conflicts.

**Q69. What is the difference between `docker-compose.yml` version 2 and version 3?**
> Version 2 supports more volume/network options, suitable for single-host.
> Version 3 is designed for Docker Swarm deployment with `deploy` key support. Modern Compose (v2 CLI) has merged both — the `version` field is now optional/deprecated.

**Q70. How do you use environment variables in Docker Compose?**
```yaml
environment:
  - DB_HOST=${DB_HOST:-localhost}   # with default
  - DB_PASS=${DB_PASSWORD}          # from shell or .env file
env_file:
  - .env
```

---

## Docker – Security

**Q71. How do you run a container as a non-root user?**
```dockerfile
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser
```
> Root in container = root on host if container escapes. Always use non-root for production.

**Q72. What is Docker Content Trust (DCT)?**
> Ensures image integrity and publisher authenticity using digital signatures. Enable with `DOCKER_CONTENT_TRUST=1`. Only signed images can be pulled/run.

**Q73. How do you scan Docker images for vulnerabilities?**
> - `docker scout cves <image>` (Docker official)
> - `trivy image <image>` (Aqua Security, widely used)
> - `snyk container test <image>`
> - AWS ECR has built-in scan on push

**Q74. What are Docker security best practices?**
> - Run as non-root user
> - Use read-only filesystem: `docker run --read-only`
> - Drop unnecessary Linux capabilities: `--cap-drop ALL --cap-add NET_BIND_SERVICE`
> - Use official/verified base images
> - Scan images in CI pipeline
> - Don't store secrets in images or env vars — use secrets management (Vault, AWS Secrets Manager)
> - Enable Docker Content Trust
> - Use AppArmor/Seccomp profiles

**Q75. What is the risk of mounting the Docker socket (`/var/run/docker.sock`) into a container?**
> Full Docker daemon access — the container can create privileged containers, escape isolation, access host filesystem. Effectively root on the host. Only do this for trusted tools (Jenkins agent, monitoring). Use Docker-in-Docker (DinD) or BuildKit instead for CI.

---

## Docker – Performance & Optimization

**Q76. How do you optimize Docker build times in CI?**
> - Use layer caching (order Dockerfile instructions from least to most changing)
> - Use BuildKit (`DOCKER_BUILDKIT=1`)
> - Cache dependency layers separately (copy `pom.xml` → `mvn dependency:go-offline` → copy source)
> - Use registry-side caching (`--cache-from`)
> - Mount Maven/NPM cache as volume in CI

**Q77. How do you limit container resources?**
```bash
docker run --memory="512m" --cpus="1.5" myimage
```
```yaml
# docker-compose
deploy:
  resources:
    limits:
      memory: 512M
      cpus: '1.5'
```

**Q78. What is the difference between `docker stats` and `docker top`?**
> `docker stats` – live resource usage (CPU, memory, network, block I/O) for running containers.
> `docker top <container>` – running processes inside a container (like `ps`).

**Q79. How do you tune JVM settings for Spring Boot in Docker?**
```dockerfile
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```
> `-XX:+UseContainerSupport` makes JVM respect container memory limits (default in Java 10+).

---

## Docker – Troubleshooting

**Q80. How do you debug a container that keeps crashing (CrashLoopBackOff)?**
```bash
docker logs <container_id>              # check logs
docker logs --tail 50 <container_id>
docker run -it --entrypoint /bin/sh <image>   # override entrypoint to debug
docker inspect <container_id>           # check exit code, env, mounts
```

**Q81. How do you exec into a running container?**
```bash
docker exec -it <container_id> /bin/bash
docker exec -it <container_id> /bin/sh   # for alpine images
```

**Q82. How do you copy files between a container and host?**
```bash
docker cp <container_id>:/app/logs/app.log ./local-app.log
docker cp ./config.yml <container_id>:/app/config.yml
```

**Q83. Container starts but application is not accessible. How do you troubleshoot?**
> 1. Check port mapping: `docker ps` → confirm ports column shows `0.0.0.0:8080->8080/tcp`
> 2. Check app actually started in logs: `docker logs <id>`
> 3. Exec into container and curl localhost: `docker exec -it <id> curl http://localhost:8080`
> 4. Check firewall rules on host
> 5. Verify app binds to `0.0.0.0` not `127.0.0.1` inside container

**Q84. How do you clean up unused Docker resources?**
```bash
docker system prune          # removes stopped containers, unused networks, dangling images
docker system prune -a       # also removes unused images
docker volume prune          # removes unused volumes
docker image prune -a        # removes all unused images
```

---

## Jenkins + Docker Integration

**Q85. How do you build and push Docker images in a Jenkins pipeline?**
```groovy
stage('Build & Push') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: 'docker-hub-credentials',
            usernameVariable: 'DH_USER',
            passwordVariable: 'DH_PASS'
        )]) {
            bat 'echo %DH_PASS% | docker login -u %DH_USER% --password-stdin'
            bat 'docker build -t %DH_USER%/myapp:%BUILD_NUMBER% .'
            bat 'docker push %DH_USER%/myapp:%BUILD_NUMBER%'
            bat 'docker logout'
        }
    }
}
```

**Q86. How do you use the Docker Pipeline plugin?**
```groovy
script {
    def image = docker.build("myapp:${env.BUILD_NUMBER}")
    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
        image.push()
        image.push('latest')
    }
}
```

**Q87. How do you run tests inside a Docker container in Jenkins?**
```groovy
agent {
    docker {
        image 'maven:3.9-eclipse-temurin-21-alpine'
        args '-v /root/.m2:/root/.m2 --network host'
    }
}
steps {
    sh 'mvn test'
}
```

**Q88. How do you handle Docker-in-Docker (DinD) in Jenkins?**
> Mount Docker socket: `-v /var/run/docker.sock:/var/run/docker.sock` on the Jenkins agent container. Security risk — only for controlled environments. Alternative: use Buildah or Kaniko for rootless image builds in CI.

**Q89. How do you tag Docker images with meaningful versions in Jenkins?**
```groovy
environment {
    VERSION = "${env.BUILD_NUMBER}-${env.GIT_COMMIT[0..6]}"
    // e.g., 42-a3f9b21
}
```
> Best practice: `<build-number>-<git-short-sha>` for traceability.

---

## Scenario-Based Questions

**Q90. Your Jenkins pipeline is passing locally but failing in CI. What do you check?**
> - Environment differences: Java version, Maven version, tool paths
> - Missing environment variables or credentials
> - Network access (firewall rules in CI environment)
> - Workspace contamination from previous builds — add `cleanWs()` in post
> - Different OS (local=Windows, CI=Linux) — path separators, line endings

**Q91. Docker image works locally but fails in production (Kubernetes). What do you investigate?**
> - Image tag mismatch (verify exact same tag/digest)
> - Resource limits too low in K8s (OOMKilled)
> - Missing ConfigMaps or Secrets
> - Health/readiness probe misconfigured
> - Network policies blocking traffic
> - Container runs as root but K8s has restrictive PodSecurityPolicy

**Q92. Jenkins disk space is filling up. How do you handle it?**
> - Set `buildDiscarder(logRotator(numToKeepStr: '10'))` in pipeline options
> - Use `cleanWs()` post-build
> - Schedule `docker system prune` on agents
> - Move artifacts to S3/Nexus instead of archiving in Jenkins
> - Set up workspace cleanup plugin

**Q93. How would you migrate from Jenkins to GitHub Actions?**
> - Map `Jenkinsfile` stages to GitHub Actions `jobs`
> - Replace Jenkins credentials with GitHub Secrets
> - Replace Jenkins agents with `runs-on` runners
> - Replace shared libraries with reusable workflows
> - Replace build triggers with GitHub event triggers (`on: push, pull_request`)

**Q94. How do you implement zero-downtime deployment with Docker and Jenkins?**
> 1. Build and push new image in pipeline
> 2. Deploy new containers (blue-green or rolling): `docker service update --image newimage:v2 myservice`
> 3. Health check new containers
> 4. Route traffic to new containers (update load balancer/ingress)
> 5. Remove old containers
> All orchestrated via Jenkins pipeline with rollback stage on failure.

**Q95. How do you rollback a failed Docker deployment from Jenkins?**
```groovy
stage('Deploy') {
    steps {
        script {
            try {
                sh "docker service update --image myapp:${env.VERSION} myservice"
                sh "docker service inspect myservice --pretty"
            } catch (err) {
                sh "docker service rollback myservice"
                error "Deployment failed — rolled back to previous version"
            }
        }
    }
}
```

---

## Quick Reference Cheat Sheet

| Command | Purpose |
|---|---|
| `docker build -t name:tag .` | Build image |
| `docker run -d -p 8080:8080 name` | Run container detached |
| `docker exec -it <id> /bin/sh` | Shell into container |
| `docker logs -f <id>` | Follow container logs |
| `docker ps -a` | All containers including stopped |
| `docker images` | List local images |
| `docker system prune -a` | Clean all unused resources |
| `docker-compose up -d` | Start all services |
| `docker-compose down -v` | Stop and remove volumes |
| `docker inspect <id>` | Full container metadata |
| `docker stats` | Live resource usage |
| `jenkins.restart()` | Restart Jenkins (from script console) |
| `Replay` button | Re-run pipeline with inline edits |