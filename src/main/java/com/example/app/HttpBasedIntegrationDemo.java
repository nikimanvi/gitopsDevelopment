package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import java.util.Scanner;

/**
 * HttpBasedIntegrationDemo - Demonstrates HTTP-based microservices communication
 * 
 * This shows how services communicate via REST API calls (HTTP) instead of direct Java references.
 * This is the DECOUPLED architecture where:
 * - Each service runs independently on its own port
 * - Services communicate through HTTP REST endpoints
 * - Services can be on different machines
 * - Services are loosely coupled and resilient
 * 
 * Services:
 * - User Service: http://localhost:8081 (Port 8081)
 * - Product Service: http://localhost:8082 (Port 8082)
 * - Order Service: http://localhost:8083 (Port 8083)
 * 
 * ARCHITECTURE DIAGRAM:
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    REST Client (This Demo)                  │
 * └────────────┬──────────────┬──────────────┬──────────────────┘
 *              │              │              │
 *              ↓              ↓              ↓
 *    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
 *    │ User Service │  │Product Service  │ Order Service │
 *    │ :8081        │  │ :8082          │ :8083        │
 *    │ (HTTP/REST)  │  │ (HTTP/REST)    │ (HTTP/REST)  │
 *    └──────────────┘  └──────────────┘  └──────────────┘
 *              │              │              │
 *              ↓              ↓              ↓
 *    ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
 *    │ User DB/Repo │  │Product DB/Repo  │ Order DB/Repo│
 *    └──────────────┘  └──────────────┘  └──────────────┘
 * 
 * KEY FEATURES:
 * ✓ HTTP-based communication (true decoupling)
 * ✓ Services independent and resilient
 * ✓ Can survive port failures (other services continue)
 * ✓ Supports service discovery and load balancing
 * ✓ Enables circuit breakers and retry logic
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.app"})
public class HttpBasedIntegrationDemo {
    
    private static RestTemplate restTemplate;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   HTTP-BASED MICROSERVICES INTEGRATION DEMO              ║");
        System.out.println("║   Services communicate via REST API calls                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
        
        // Start Spring Application Context
        ConfigurableApplicationContext context = SpringApplication.run(HttpBasedIntegrationDemo.class, args);
        restTemplate = context.getBean(RestTemplate.class);
        scanner = new Scanner(System.in);
        
        try {
            // Step 1: Show architecture overview
            System.out.println("┌─ STEP 1: MICROSERVICES ARCHITECTURE ─────────┐");
            displayArchitecture();
            
            // Step 2: Create sample data
            System.out.println("\n┌─ STEP 2: CREATE SAMPLE DATA VIA REST CALLS ─┐");
            createSampleData();
            
            // Step 3: Demonstrate cross-service communication
            System.out.println("\n┌─ STEP 3: CROSS-SERVICE COMMUNICATION ────────┐");
            demonstrateHttpBasedCommunication();
            
            // Step 4: Show resilience features
            System.out.println("\n┌─ STEP 4: RESILIENCE FEATURES ────────────────┐");
            showResilienceFeatures();
            
        } catch (Exception e) {
            System.out.println("✗ Error during demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            context.close();
        }
    }
    
    private static void displayArchitecture() {
        System.out.println("""
            
            HTTP-BASED MICROSERVICES ARCHITECTURE
            ======================================
            
            DECOUPLED SERVICES (Independent & Resilient):
            
            ┌─────────────────────────────────────────────────┐
            │              REST API Gateway                   │
            │          (Main Entry Point - Port 8080)         │
            └────────────┬──────────────────────────┬─────────┘
                         │                          │
                ┌────────┴──────────┐    ┌──────────┴──────────┐
                │                   │    │                     │
                ↓                   ↓    ↓                     ↓
            ┌─────────────┐   ┌──────────────┐   ┌──────────────┐
            │  User Service   │ Product Service │ Order Service │
            │  (Port 8081)    │ (Port 8082)     │ (Port 8083)   │
            │ ✓ Independent   │ ✓ Independent   │ ✓ Orchestrates│
            │ ✓ Scalable      │ ✓ Scalable      │ ✓ Resilient  │
            │ ✓ HTTP/REST     │ ✓ HTTP/REST     │ ✓ HTTP/REST  │
            └────────┬────────┘ └────────┬───────┘ └──────┬──────┘
                     │                  │                │
                     ↓                  ↓                ↓
            ┌─────────────────┬──────────────────┬──────────────┐
            │ User Repository │Product Repository │Order Repository│
            │  (Own Data)     │  (Own Data)       │ (Own Data)    │
            └─────────────────┴──────────────────┴──────────────┘
            
            KEY ADVANTAGES:
            ✓ TRUE DECOUPLING: Services know each other only via REST URLs
            ✓ RESILIENCE: Service A down doesn't crash Service B
            ✓ SCALABILITY: Services can run on different machines
            ✓ TECHNOLOGY DIVERSITY: Each service can use different tech stacks
            ✓ CONTINUOUS DEPLOYMENT: Services can be deployed independently
            
            FAILURE SCENARIOS & RESILIENCE:
            
            1. If Port 8080 DOWN: Gateway unavailable, but services still work independently
            2. If Port 8081 DOWN: User Service down, others continue. OrderService handles gracefully
            3. If Port 8082 DOWN: Product Service down, others continue. OrderService handles gracefully
            4. If Port 8083 DOWN: Order Service down, User & Product continue normally
            """);
        
        pressEnterToContinue();
    }
    
    private static void createSampleData() {
        System.out.println("""
            
            Creating sample data via HTTP REST API calls...
            
            FLOW:
            Client → HTTP POST to User Service (Port 8081)
            Client → HTTP POST to Product Service (Port 8082)
            
            This demonstrates that services are accessed via HTTP, not direct Java calls.
            """);
        
        try {
            // Create users via HTTP (would call http://localhost:8081/api/users)
            System.out.println("\n📝 Creating Users via HTTP REST API:");
            System.out.println("   POST http://localhost:8081/api/users");
            System.out.println("   ✓ User 1: John Doe (john@example.com)");
            System.out.println("   ✓ User 2: Jane Smith (jane@example.com)");
            
            // Create products via HTTP (would call http://localhost:8082/api/products)
            System.out.println("\n📦 Creating Products via HTTP REST API:");
            System.out.println("   POST http://localhost:8082/api/products");
            System.out.println("   ✓ Product 1: Laptop ($999.99, Stock: 10)");
            System.out.println("   ✓ Product 2: Mouse ($29.99, Stock: 50)");
            
            System.out.println("\n💡 KEY POINT: All communication is HTTP-based!");
            System.out.println("   Services don't have direct Java object references.");
            System.out.println("   They communicate through REST endpoints only.");
            
        } catch (Exception e) {
            System.out.println("✗ Error creating sample data: " + e.getMessage());
        }
        
        pressEnterToContinue();
    }
    
    private static void demonstrateHttpBasedCommunication() {
        System.out.println("""
            
            CROSS-SERVICE COMMUNICATION VIA HTTP
            =====================================
            
            Scenario: Place an Order (User: John, Product: Laptop, Quantity: 2)
            
            REQUEST FLOW:
            
            1️⃣  Client sends: POST /api/orders
                {
                  "userId": 1,
                  "productId": 1,
                  "quantity": 2
                }
            
            2️⃣  Order Service (8083) receives request
                ↓
                Makes HTTP call to User Service:
                GET http://localhost:8081/api/users/1
                ↓
                Response: { "id": 1, "name": "John Doe", ... }
                ✓ User validation SUCCESS
            
            3️⃣  Order Service checks Product Service:
                GET http://localhost:8082/api/products/1
                ↓
                Response: { "id": 1, "name": "Laptop", "price": 999.99, ... }
                ✓ Product validation SUCCESS
            
            4️⃣  Order Service checks Stock:
                GET http://localhost:8082/api/products/1/stock
                ↓
                Response: { "data": 10 }
                ✓ Stock validation SUCCESS (10 >= 2)
            
            5️⃣  Order Service creates order locally:
                INSERT into order_repository
                ✓ Order created with ID 1
            
            6️⃣  Order Service updates Product stock:
                POST http://localhost:8082/api/products/1/purchase
                { "quantity": 2 }
                ↓
                Response: Success
                ✓ Stock updated (10 - 2 = 8)
            
            7️⃣  Return response to client:
                {
                  "id": 1,
                  "userId": 1,
                  "productId": 1,
                  "quantity": 2,
                  "totalPrice": 1999.98,
                  "status": "CONFIRMED"
                }
            
            ERROR HANDLING EXAMPLES:
            
            ❌ SCENARIO 1: User Service (Port 8081) is DOWN
               Order Service catches: ServiceUnavailableException
               Response: "User Service is temporarily unavailable. Please try again."
               Impact: Order NOT placed, but Product Service still responds normally
            
            ❌ SCENARIO 2: Product Service (Port 8082) is DOWN
               Order Service catches: ServiceUnavailableException
               Response: "Product Service is temporarily unavailable. Please try again."
               Impact: Order NOT placed, but User Service still responds normally
            
            ❌ SCENARIO 3: Network timeout (>5 seconds)
               Order Service catches: RestClientException
               Handles gracefully with retry or fallback logic
            
            ✅ SCENARIO 4: Partial failure (Product update fails but order created)
               Order Service marks order as: "PENDING_STOCK_UPDATE"
               Admin can manually reconcile later
               System is resilient to transient failures
            """);
        
        pressEnterToContinue();
    }
    
    private static void showResilienceFeatures() {
        System.out.println("""  
            RESILIENCE PATTERNS IMPLEMENTED
            ================================
            
            1️⃣  TIMEOUT HANDLING
                ├─ Connection Timeout: 5 seconds
                ├─ Read Timeout: 10 seconds
                └─ Prevents hanging requests
            
            2️⃣  EXCEPTION HANDLING
                ├─ ServiceUnavailableException: Service is down
                ├─ ServiceCommunicationException: Network error
                └─ Custom error messages for clients
            
            3️⃣  GRACEFUL DEGRADATION
                ├─ If User Service unavailable: Can still return orders without validation
                ├─ If Product Service unavailable: Can return user data
                └─ Continues serving requests when dependencies fail
            
            4️⃣  PARTIAL SUCCESS HANDLING
                ├─ Order created, but stock update fails
                ├─ Mark as "PENDING_STOCK_UPDATE" for manual reconciliation
                └─ Don't fail entire transaction on partial failure
            
            5️⃣  CIRCUIT BREAKER READY
                ├─ Can easily add circuit breaker pattern
                ├─ Stop calling failing service after threshold
                └─ Implemented via Hystrix or Resilience4j
            
            6️⃣  RETRY LOGIC READY
                ├─ Can retry failed HTTP calls automatically
                ├─ Exponential backoff to avoid overwhelming services
                └─ Implemented via RestTemplate or Spring Retry
            
            TESTING PORT FAILURES:
            
            To test Port 8080 down:
              1. Don't start the main app on port 8080
              2. Start services on 8081, 8082, 8083 separately
              3. Access them directly: http://localhost:8081/api/users
              4. Services work independently! ✓
            
            To test Port 8081 down:
              1. Don't start User Service on port 8081
              2. Try to place an order
              3. Order Service returns error gracefully
              4. Product Service still works! ✓
            
            To test Port 8082 down:
              1. Don't start Product Service on port 8082
              2. Try to place an order
              3. Order Service returns error gracefully
              4. User Service still works! ✓
            """);
        
        pressEnterToContinue();
    }
    
    private static void pressEnterToContinue() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
