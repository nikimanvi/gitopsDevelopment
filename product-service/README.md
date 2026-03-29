# Product Service

Product microservice for managing product catalog.

## Port
- **8082**

## Endpoints

### Base URL: `http://localhost:8082/products`

- `GET /products` - Get all products
- `GET /products/{id}` - Get product by ID
- `POST /products` - Create new product
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product

## Running the Service

```powershell
cd product-service
mvn clean install
mvn spring-boot:run
```

## Health Check

```
http://localhost:8082/actuator/health
```

## Access via API Gateway

```
http://localhost:8080/api/products
```
