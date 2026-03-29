package com.example.app;

import com.example.app.user.UserMicroService;
import com.example.app.product.ProductMicroService;
import com.example.app.order.OrderService;
import com.example.app.models.User;
import com.example.app.models.Product;
import com.example.app.models.Order;
import java.util.Scanner;
import java.util.List;

/**
 * MicroservicesIntegrationDemo - Demonstrates how microservices work together
 * Shows:
 * 1. Independent service startup on different ports
 * 2. Each service managing its own data
 * 3. Cross-service communication (OrderService using both UserService and ProductService)
 */
public class MicroservicesIntegrationDemo {
    
    private static UserMicroService userService;
    private static ProductMicroService productService;
    private static OrderService orderService;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   MICROSERVICES INTEGRATION DEMONSTRATION                 ║");
        System.out.println("║   This shows how multiple independent services work       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
        
        scanner = new Scanner(System.in);
        
        // Step 1: Start independent services on different ports
        System.out.println("┌─ STEP 1: STARTING INDEPENDENT MICROSERVICES ─┐");
        startIndependentServices();
        
        // Step 2: Demonstrate service independence
        System.out.println("\n┌─ STEP 2: DEMONSTRATING SERVICE INDEPENDENCE ─┐");
        demonstrateServiceIndependence();
        
        // Step 3: Demonstrate cross-service communication
        System.out.println("\n┌─ STEP 3: CROSS-SERVICE COMMUNICATION ─┐");
        demonstrateCrossServiceCommunication();
        
        // Cleanup
        shutdownServices();
        scanner.close();
    }
    
    /**
     * STEP 1: Start independent services
     * Shows that each service:
     * - Runs on its own port
     * - Can be started independently
     * - Has its own lifecycle
     */
    private static void startIndependentServices() {
        // Initialize UserMicroService on port 8081
        System.out.println("\n🚀 Starting UserMicroService on port 8081...");
        userService = new UserMicroService(8081);
        userService.start();
        System.out.println("   ✓ Service Name: " + userService.getServiceName());
        System.out.println("   ✓ Running: " + userService.isRunning());
        System.out.println("   ✓ Has own repository for USER data");
        
        // Initialize ProductMicroService on port 8082
        System.out.println("\n🚀 Starting ProductMicroService on port 8082...");
        productService = new ProductMicroService(8082);
        productService.start();
        System.out.println("   ✓ Service Name: " + productService.getServiceName());
        System.out.println("   ✓ Running: " + productService.isRunning());
        System.out.println("   ✓ Has own repository for PRODUCT data");
        
        // Initialize OrderService (orchestrates both services)
        System.out.println("\n🚀 Initializing OrderService (combines both services)...");
        orderService = new OrderService(userService, productService);
        System.out.println("   ✓ OrderService can access both UserService and ProductService");
    }
    
    /**
     * STEP 2: Demonstrate service independence
     * Shows that:
     * - Services don't share databases
     * - Services can operate independently
     * - Each service manages its own data
     */
    private static void demonstrateServiceIndependence() {
        System.out.println("\n📋 Creating Users via UserMicroService (Port 8081)");
        System.out.println("─────────────────────────────────────────────────");
        
        // Create users using UserMicroService
        User user1 = userService.createUser("John Doe", "john@example.com", "+1234567890", "123 Main St");
        System.out.println("✓ Created User: " + user1.getName() + " (ID: " + user1.getId() + ")");
        
        User user2 = userService.createUser("Jane Smith", "jane@example.com", "+0987654321", "456 Oak Ave");
        System.out.println("✓ Created User: " + user2.getName() + " (ID: " + user2.getId() + ")");
        
        System.out.println("\n📦 Creating Products via ProductMicroService (Port 8082)");
        System.out.println("─────────────────────────────────────────────────────────");
        
        // Create products using ProductMicroService
        Product product1 = productService.addProduct("Laptop", "High-performance laptop", 999.99, 10);
        System.out.println("✓ Created Product: " + product1.getName() + " (ID: " + product1.getId() + ")");
        System.out.println("  Price: $" + product1.getPrice() + ", Stock: " + product1.getQuantity());
        
        Product product2 = productService.addProduct("Mouse", "Wireless mouse", 29.99, 50);
        System.out.println("✓ Created Product: " + product2.getName() + " (ID: " + product2.getId() + ")");
        System.out.println("  Price: $" + product2.getPrice() + ", Stock: " + product2.getQuantity());
        
        System.out.println("\n📊 Data Isolation - Each Service Manages Its Own Data");
        System.out.println("───────────────────────────────────────────────────");
        System.out.println("UserMicroService Data Store:");
        System.out.println("  - Contains: User objects");
        System.out.println("  - Count: " + userService.getAllUsers().size() + " users");
        System.out.println("  - UserRepository handles all user operations");
        
        System.out.println("\nProductMicroService Data Store:");
        System.out.println("  - Contains: Product objects");
        System.out.println("  - Count: " + productService.getAllProducts().size() + " products");
        System.out.println("  - ProductRepository handles all product operations");
        
        System.out.println("\n💡 KEY POINT: Services don't share databases!");
        System.out.println("   UserMicroService doesn't know about ProductRepository");
        System.out.println("   ProductMicroService doesn't know about UserRepository");
        System.out.println("   This is LOOSE COUPLING between services!");
    }
    
    /**
     * STEP 3: Demonstrate cross-service communication
     * Shows that:
     * - Services can communicate with each other
     * - OrderService coordinates between User and Product services
     * - Business logic can span multiple services
     */
    private static void demonstrateCrossServiceCommunication() {
        System.out.println("\n🔗 Cross-Service Communication - OrderService");
        System.out.println("──────────────────────────────────────────────");
        System.out.println("OrderService needs to:");
        System.out.println("  1. Validate user exists (calls UserMicroService)");
        System.out.println("  2. Validate product exists (calls ProductMicroService)");
        System.out.println("  3. Check inventory (calls ProductMicroService)");
        System.out.println("  4. Create order (combines data from both services)");
        
        System.out.println("\n🛒 Creating Orders");
        System.out.println("─────────────────");
        
        // Create order - OrderService will coordinate with both services
        try {
            Order order1 = orderService.placeOrder(1, 1, 2);
            System.out.println("✓ Order Created Successfully!");
            System.out.println("  Order ID: " + order1.getId());
            System.out.println("  User ID: " + order1.getUserId() + " (verified by UserMicroService)");
            System.out.println("  Product ID: " + order1.getProductId() + " (verified by ProductMicroService)");
            System.out.println("  Quantity: " + order1.getQuantity());
            System.out.println("  Total Price: $" + order1.getTotalPrice());
            System.out.println("  Status: " + order1.getStatus());
        } catch (Exception e) {
            System.out.println("✗ Order creation failed: " + e.getMessage());
        }
        
        try {
            Order order2 = orderService.placeOrder(2, 2, 5);
            System.out.println("\n✓ Another Order Created Successfully!");
            System.out.println("  Order ID: " + order2.getId());
            System.out.println("  User ID: " + order2.getUserId());
            System.out.println("  Product ID: " + order2.getProductId());
            System.out.println("  Quantity: " + order2.getQuantity());
            System.out.println("  Total Price: $" + order2.getTotalPrice());
        } catch (Exception e) {
            System.out.println("✗ Order creation failed: " + e.getMessage());
        }
        
        System.out.println("\n📊 Service Architecture Summary");
        System.out.println("────────────────────────────────");
        System.out.println("┌──────────────────────────────────────────┐");
        System.out.println("│        MicroservicesApplication          │");
        System.out.println("│         (Service Orchestrator)           │");
        System.out.println("└──────────────────────────────────────────┘");
        System.out.println("        ↓              ↓              ↓");
        System.out.println("   ┌────────────┐  ┌──────────────┐  ┌────────────┐");
        System.out.println("   │User Service│  │Product Service │ │OrderService│");
        System.out.println("   │ (Port 8081)│  │ (Port 8082)    │ │(Orchestrates)");
        System.out.println("   └────────────┘  └──────────────┘  └────────────┘");
        System.out.println("        ↓              ↓                    ↓");
        System.out.println("   ┌────────────┐  ┌──────────────┐  ┌────────────┐");
        System.out.println("   │User Repo   │  │Product Repo  │  │Order Repo  │");
        System.out.println("   │(Own Data)  │  │(Own Data)    │  │(Own Data)  │");
        System.out.println("   └────────────┘  └──────────────┘  └────────────┘");
        
        System.out.println("\n✅ MICROSERVICES BENEFITS DEMONSTRATED:");
        System.out.println("  1. ✓ Independence: Services run on different ports");
        System.out.println("  2. ✓ Data Isolation: Each service has its own repository");
        System.out.println("  3. ✓ Loose Coupling: Services don't share databases");
        System.out.println("  4. ✓ Integration: OrderService coordinates between services");
        System.out.println("  5. ✓ Scalability: Services can scale independently");
        System.out.println("  6. ✓ Fault Isolation: One service crash won't affect others");
    }
    
    /**
     * Shutdown all services gracefully
     */
    private static void shutdownServices() {
        System.out.println("\n\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   SHUTTING DOWN MICROSERVICES                             ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        
        userService.stop();
        System.out.println("✓ UserMicroService stopped");
        
        productService.stop();
        System.out.println("✓ ProductMicroService stopped");
        
        System.out.println("\n✓ All microservices shut down gracefully");
    }
}
