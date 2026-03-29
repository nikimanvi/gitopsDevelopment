# User Service

User microservice for managing user operations.

## Port
- **8081**

## Endpoints

### Base URL: `http://localhost:8081/users`

- `GET /users` - Get all users
- `GET /users/{id}` - Get user by ID
- `POST /users` - Create new user
- `PUT /users/{id}` - Update user
- `DELETE /users/{id}` - Delete user

## Running the Service

```powershell
cd user-service
mvn clean install
mvn spring-boot:run
```

## Health Check

```
http://localhost:8081/actuator/health
```

## Access via API Gateway

```
http://localhost:8080/api/users
```
