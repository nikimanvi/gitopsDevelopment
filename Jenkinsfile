pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9' // Configure in Jenkins Global Tool Configuration
        jdk 'JDK-21'      // Must match Java 21 for this project
    }
    
    environment {
        // Docker & AWS Configuration
        DOCKER_REGISTRY = 'your-account-id.dkr.ecr.us-east-1.amazonaws.com'
        AWS_REGION = 'us-east-1'
        AWS_CREDENTIALS = 'aws-credentials-id' // Jenkins credential ID
        
        // SonarQube Configuration
        SONAR_TOKEN = credentials('sonarqube-token')
        SONAR_HOST_URL = 'https://sonarcloud.io' // or http://localhost:9000
        SONAR_PROJECT_KEY = 'microservices-project'
        
        // Services to build
        SERVICES = 'gateway-service,user-service,product-service,order-service'
        VERSION = "${env.BUILD_NUMBER}"
        
        // Kubernetes Configuration
        EKS_CLUSTER_NAME = 'microservices-cluster'
        KUBECTL_VERSION = '1.28.0'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "🔄 Checking out code from repository..."
                    checkout scm
                    
                    // Get Git commit info
                    env.GIT_COMMIT_SHORT = bat(
                        script: '@git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH = bat(
                        script: '@git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()
                    
                    echo "Branch: ${env.GIT_BRANCH}, Commit: ${env.GIT_COMMIT_SHORT}"
                }
            }
        }
        
        stage('Build All Services') {
            steps {
                script {
                    echo "🔨 Building all microservices..."
                    bat '''
                        echo Building all services...
                        mvn clean compile -T 4 -DskipTests
                    '''
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    echo "🧪 Running unit tests..."
                    bat 'mvn test -T 4'
                }
            }
            post {
                always {
                    // Publish test results
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    
                    // Archive test reports
                    publishHTML([
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'index.html',
                        reportName: 'Test Report'
                    ])
                }
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                script {
                    echo "📊 Running SonarQube analysis..."
                    withSonarQubeEnv('SonarQube') {
                        bat """
                            mvn sonar:sonar ^
                            -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                            -Dsonar.host.url=%SONAR_HOST_URL% ^
                            -Dsonar.login=%SONAR_TOKEN% ^
                            -Dsonar.branch.name=%GIT_BRANCH%
                        """
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    echo "⏳ Waiting for SonarQube Quality Gate..."
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                    echo "✅ Quality Gate passed!"
                }
            }
        }
        
        stage('Package Services') {
            steps {
                script {
                    echo "📦 Packaging microservices..."
                    bat 'mvn package -DskipTests -T 4'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Gateway Service') {
                    steps {
                        script {
                            buildDockerImage('gateway-service')
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        script {
                            buildDockerImage('user-service')
                        }
                    }
                }
                stage('Product Service') {
                    steps {
                        script {
                            buildDockerImage('product-service')
                        }
                    }
                }
                stage('Order Service') {
                    steps {
                        script {
                            buildDockerImage('order-service')
                        }
                    }
                }
            }
        }
        
        stage('Push to Registry') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch pattern: 'release/.*', comparator: 'REGEXP'
                }
            }
            steps {
                script {
                    echo "🚀 Pushing Docker images to registry..."
                    withAWS(credentials: "${AWS_CREDENTIALS}", region: "${AWS_REGION}") {
                        // Login to ECR
                        bat """
                            aws ecr get-login-password --region %AWS_REGION% | docker login --username AWS --password-stdin %DOCKER_REGISTRY%
                        """
                        
                        // Push all service images
                        env.SERVICES.split(',').each { service ->
                            bat """
                                docker tag ${service}:${VERSION} %DOCKER_REGISTRY%/${service}:${VERSION}
                                docker tag ${service}:${VERSION} %DOCKER_REGISTRY%/${service}:latest
                                docker push %DOCKER_REGISTRY%/${service}:${VERSION}
                                docker push %DOCKER_REGISTRY%/${service}:latest
                            """
                        }
                    }
                }
            }
        }
        
        stage('Deploy to EKS') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "☸️ Deploying to EKS cluster..."
                    withAWS(credentials: "${AWS_CREDENTIALS}", region: "${AWS_REGION}") {
                        bat """
                            aws eks update-kubeconfig --name %EKS_CLUSTER_NAME% --region %AWS_REGION%
                            
                            REM Apply Kubernetes configurations
                            kubectl apply -f k8s/namespace.yml
                            kubectl apply -f k8s/gateway-service/
                            kubectl apply -f k8s/user-service/
                            kubectl apply -f k8s/product-service/
                            kubectl apply -f k8s/order-service/
                            kubectl apply -f k8s/ingress.yml
                            
                            REM Update image tags
                            kubectl set image deployment/gateway-service gateway-service=%DOCKER_REGISTRY%/gateway-service:%VERSION% -n microservices
                            kubectl set image deployment/user-service user-service=%DOCKER_REGISTRY%/user-service:%VERSION% -n microservices
                            kubectl set image deployment/product-service product-service=%DOCKER_REGISTRY%/product-service:%VERSION% -n microservices
                            kubectl set image deployment/order-service order-service=%DOCKER_REGISTRY%/order-service:%VERSION% -n microservices
                            
                            REM Wait for rollout
                            kubectl rollout status deployment/gateway-service -n microservices
                            kubectl rollout status deployment/user-service -n microservices
                            kubectl rollout status deployment/product-service -n microservices
                            kubectl rollout status deployment/order-service -n microservices
                        """
                    }
                }
            }
        }
        
        stage('Health Check') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "🏥 Running health checks..."
                    sleep(time: 30, unit: 'SECONDS') // Wait for services to be ready
                    
                    // Check service health
                    bat """
                        kubectl get pods -n microservices
                        kubectl get services -n microservices
                    """
                }
            }
        }
    }
    
    post {
        always {
            echo "📧 Sending notifications..."
            cleanWs()
        }
        success {
            script {
                echo "✅ Pipeline completed successfully!"
                // Add email/Slack notification here
                emailext(
                    subject: "✅ Jenkins Build Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Build Successful</h2>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>Branch:</strong> ${env.GIT_BRANCH}</p>
                        <p><strong>Commit:</strong> ${env.GIT_COMMIT_SHORT}</p>
                        <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                        <p><a href="${env.BUILD_URL}">View Build</a></p>
                    """,
                    recipientProviders: [developers(), requestor()],
                    mimeType: 'text/html'
                )
            }
        }
        failure {
            script {
                echo "❌ Pipeline failed!"
                emailext(
                    subject: "❌ Jenkins Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>Build Failed</h2>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                        <p><strong>Branch:</strong> ${env.GIT_BRANCH}</p>
                        <p><strong>Commit:</strong> ${env.GIT_COMMIT_SHORT}</p>
                        <p><a href="${env.BUILD_URL}console">View Console Output</a></p>
                    """,
                    recipientProviders: [developers(), requestor()],
                    mimeType: 'text/html'
                )
            }
        }
        unstable {
            echo "⚠️ Pipeline unstable!"
        }
    }
}

// Helper function to build Docker images
def buildDockerImage(serviceName) {
    echo "🐳 Building Docker image for ${serviceName}..."
    dir(serviceName) {
        bat """
            docker build -t ${serviceName}:${env.VERSION} .
            docker tag ${serviceName}:${env.VERSION} ${serviceName}:latest
        """
    }
}
