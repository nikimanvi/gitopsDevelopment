# API Gateway Service

This is the API Gateway for the microservices architecture. It routes all external traffic to the appropriate backend services.

## Features

- **Routing**: Routes requests to user, product, and order services
- **Circuit Breaker**: Implements resilience patterns using Resilience4j
- **CORS**: Configured for cross-origin requests
- **Health Checks**: Actuator endpoints for monitoring
- **Metrics**: Prometheus-compatible metrics

## Port

- Gateway runs on port **8080**

## Routes

- `/api/users/**` → User Service (port 8081)
- `/api/products/**` → Product Service (port 8082)
- `/api/orders/**` → Order Service (port 8083)

## Running the Gateway

```bash
cd gateway-service
mvn clean install
mvn spring-boot:run
```

## Health Check

```
http://localhost:8080/actuator/health
```

## Metrics

```
http://localhost:8080/actuator/prometheus
```

## Gateway Routes Info

```
http://localhost:8080/actuator/gateway/routes
```
