# Order Service

Order microservice for managing orders and orchestrating user/product services.

## Port
- **8083**

## Endpoints

### Base URL: `http://localhost:8083/orders`

- `GET /orders` - Get all orders
- `GET /orders/{id}` - Get order by ID
- `POST /orders` - Create new order (calls User & Product services)
- `PUT /orders/{id}` - Update order
- `DELETE /orders/{id}` - Delete order

## Running the Service

```powershell
cd order-service
mvn clean install
mvn spring-boot:run
```

## Dependencies

This service communicates with:
- **User Service** (port 8081)
- **Product Service** (port 8082)

Make sure these services are running before starting Order Service.

## Health Check

```
http://localhost:8083/actuator/health
```

## Access via API Gateway

```
http://localhost:8080/api/orders
```
