# Microservices Architecture - Complete Setup

A complete microservices-based application with API Gateway, User Service, Product Service, and Order Service.

## Architecture Overview

```
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   Port: 8080    │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
    ┌────▼────┐        ┌────▼────┐        ┌────▼────┐
    │  User   │        │Product  │        │  Order  │
    │ Service │        │ Service │        │ Service │
    │  :8081  │        │  :8082  │        │  :8083  │
    └─────────┘        └─────────┘        └─────────┘
```

## Services

| Service | Port | Description | Direct URL | Via Gateway |
|---------|------|-------------|------------|-------------|
| **API Gateway** | 8080 | Routes all external traffic | - | - |
| **User Service** | 8081 | Manages users | http://localhost:8081/users | http://localhost:8080/api/users |
| **Product Service** | 8082 | Manages products | http://localhost:8082/products | http://localhost:8080/api/products |
| **Order Service** | 8083 | Manages orders | http://localhost:8083/orders | http://localhost:8080/api/orders |

## Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PowerShell (Windows)

### Option 1: Build and Start All Services (Automated)

```powershell
# Build all services
.\build-all.ps1

# Start all services in separate terminals
.\start-all-services.ps1
```

### Option 2: Manual Start (Individual Services)

```powershell
# Terminal 1 - User Service
cd user-service
mvn spring-boot:run

# Terminal 2 - Product Service
cd product-service
mvn spring-boot:run

# Terminal 3 - Order Service
cd order-service
mvn spring-boot:run

# Terminal 4 - API Gateway
cd gateway-service
mvn spring-boot:run
```

## API Endpoints

### User Service (via Gateway)

```bash
# Get all users
curl http://localhost:8080/api/users

# Get user by ID
curl http://localhost:8080/api/users/1

# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","phone":"1234567890","address":"123 Main St"}'

# Update user
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com"}'

# Delete user
curl -X DELETE http://localhost:8080/api/users/1
```

### Product Service (via Gateway)

```bash
# Get all products
curl http://localhost:8080/api/products

# Get product by ID
curl http://localhost:8080/api/products/1

# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"High-end laptop","price":1200.00,"stock":50}'
```

### Order Service (via Gateway)

```bash
# Get all orders
curl http://localhost:8080/api/orders

# Create order (requires user and product services running)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"productId":1,"quantity":2}'
```

## Health Checks

```bash
# Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# Product Service
curl http://localhost:8082/actuator/health

# Order Service
curl http://localhost:8083/actuator/health
```

## Gateway Features

- **Routing**: Routes `/api/users`, `/api/products`, `/api/orders` to respective services
- **Circuit Breaker**: Implements fallback mechanism for service failures
- **CORS**: Configured for cross-origin requests
- **Monitoring**: Actuator endpoints for health and metrics
- **Load Balancing**: Ready for multiple instances (future enhancement)

## Project Structure

```
Microservices/
├── gateway-service/          # API Gateway (Port 8080)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/app/gateway/
│   │       │   ├── GatewayApplication.java
│   │       │   └── FallbackController.java
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
├── user-service/             # User Service (Port 8081)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/app/user/
│   │       │   ├── UserServiceApplication.java
│   │       │   ├── UserController.java
│   │       │   ├── UserService.java
│   │       │   └── UserRepository.java
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
├── product-service/          # Product Service (Port 8082)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/app/product/
│   │       │   ├── ProductServiceApplication.java
│   │       │   ├── ProductController.java
│   │       │   ├── ProductService.java
│   │       │   └── ProductRepository.java
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
├── order-service/            # Order Service (Port 8083)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/app/order/
│   │       │   ├── OrderServiceApplication.java
│   │       │   ├── OrderController.java
│   │       │   ├── OrderService.java
│   │       │   └── OrderRepository.java
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
├── build-all.ps1             # Build all services
├── start-all-services.ps1    # Start all services
└── README.md
```

## Technology Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 3.3.4
- **Spring Cloud Gateway**: 2023.0.4
- **Build Tool**: Maven
- **Patterns**: Microservices, API Gateway, Circuit Breaker

## Next Steps

### 1. Add Authentication & Authorization
- Implement JWT-based authentication at the gateway
- Use OAuth2/OIDC (Keycloak, Auth0, Azure AD)

### 2. Add Service Discovery
- Implement Eureka or Consul for dynamic service registration
- Remove hardcoded service URLs

### 3. Add Database Persistence
- Replace in-memory repositories with PostgreSQL/MySQL
- Add Spring Data JPA

### 4. Add Message Queue
- Implement RabbitMQ/Kafka for async communication
- Event-driven architecture for order processing

### 5. Containerization
- Create Dockerfiles for each service
- Docker Compose for local orchestration
- Kubernetes manifests for production

### 6. Observability
- Centralized logging (ELK Stack)
- Distributed tracing (Zipkin/Jaeger)
- Metrics (Prometheus/Grafana)

### 7. CI/CD Pipeline
- GitHub Actions or Azure DevOps
- Automated testing and deployment

## Troubleshooting

### Port Already in Use
```powershell
# Find process using port
netstat -ano | findstr :8080

# Kill process (use PID from above)
taskkill /PID <PID> /F
```

### Service Not Starting
1. Check if correct Java version is installed: `java -version`
2. Verify all dependencies are downloaded: `mvn clean install`
3. Check logs in the terminal for errors
4. Ensure no port conflicts

### Gateway Cannot Reach Services
1. Verify all backend services are running
2. Check service URLs in [gateway/application.yml](gateway-service/src/main/resources/application.yml)
3. Test direct service URLs before testing through gateway

## License

This is a demonstration project for learning microservices architecture.
