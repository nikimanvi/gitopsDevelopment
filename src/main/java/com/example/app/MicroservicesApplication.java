package com.example.app;

import com.example.app.user.UserMicroService;
import com.example.app.product.ProductMicroService;
import com.example.app.order.OrderMicroService;
import com.example.app.models.User;
import com.example.app.models.Product;
import com.example.app.models.Order;
import java.util.Scanner;
import java.util.List;

/**
 * MicroservicesApplication - Main entry point to start microservices
 * This launcher starts three integrated microservices: User, Product, and Order
 * Demonstrates cross-service communication and integration
 */
public class MicroservicesApplication {
    
    private static UserMicroService userService;
    private static ProductMicroService productService;
    private static OrderMicroService orderService;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║   MULTI-MICROSERVICES APPLICATION LAUNCHER            ║");
        System.out.println("║   User Service | Product Service | Order Service       ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        // Initialize scanner for user input
        scanner = new Scanner(System.in);
        
        // Create and start User Microservice on port 8081
        userService = new UserMicroService(8081);
        userService.start();
        System.out.println("\n✓ " + userService.getServiceName() + " started on PORT: " + userService.getPort());
        
        // Create and start Product Microservice on port 8082
        productService = new ProductMicroService(8082);
        productService.start();
        System.out.println("✓ " + productService.getServiceName() + " started on PORT: " + productService.getPort());
        
        // Create and start Order Microservice (orchestrator) on port 8083
        orderService = new OrderMicroService(8083, userService, productService);
        orderService.start();
        System.out.println("✓ " + orderService.getServiceName() + " started on PORT: " + orderService.getPort());
        
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("All microservices running. Services are ready for API calls.");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        System.out.println("Available Endpoints:");
        System.out.println("  User Service:    http://localhost:8081/api/users");
        System.out.println("  Product Service: http://localhost:8082/api/products");
        System.out.println("  Order Service:   http://localhost:8083/api/orders\n");
        
        // Keep the application running
        System.out.println("Application is running. Press Ctrl+C to stop.");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Application interrupted");
        }
        
        // Cleanup
        userService.stop();
        productService.stop();
        orderService.stop();
        scanner.close();
        System.out.println("\n✓ Application shutdown complete");
    }
    
    /**
     * Interactive command menu for testing all microservices
     */
    private static void runCommandMenu() {
        boolean running = true;
        
        while (running) {
            System.out.println("\n╔══════════════════════════════════════════════════════╗");
            System.out.println("║           MICROSERVICES MANAGEMENT MENU             ║");
            System.out.println("╠══════════════════════════════════════════════════════╣");
            System.out.println("║ USER SERVICE:                                        ║");
            System.out.println("║  1. Create User  2. View User  3. List Users        ║");
            System.out.println("║  4. Update User  5. Delete User  6. User Count      ║");
            System.out.println("║ PRODUCT SERVICE:                                     ║");
            System.out.println("║  7. Add Product  8. View Product  9. List Products  ║");
            System.out.println("║  10. Update Product  11. Delete Product             ║");
            System.out.println("║ ORDER SERVICE (Cross-Service Demo):                 ║");
            System.out.println("║  12. Place Order  13. View Order  14. My Orders     ║");
            System.out.println("║  15. Update Order Status  16. Cancel Order          ║");
            System.out.println("║ SYSTEM:                                              ║");
            System.out.println("║  17. Service Status  18. Help  19. Exit             ║");
            System.out.println("╚══════════════════════════════════════════════════════╝");
            System.out.print("Enter your choice (1-19): ");
            
            String choice = scanner.nextLine().trim();
            
            try {
                switch (choice) {
                    // User Service
                    case "1":
                        createUserMenu();
                        break;
                    case "2":
                        viewUserMenu();
                        break;
                    case "3":
                        listAllUsersMenu();
                        break;
                    case "4":
                        updateUserMenu();
                        break;
                    case "5":
                        deleteUserMenu();
                        break;
                    case "6":
                        getUserCountMenu();
                        break;
                    // Product Service
                    case "7":
                        addProductMenu();
                        break;
                    case "8":
                        viewProductMenu();
                        break;
                    case "9":
                        listAllProductsMenu();
                        break;
                    case "10":
                        updateProductMenu();
                        break;
                    case "11":
                        deleteProductMenu();
                        break;
                    // Order Service
                    case "12":
                        placeOrderMenu();
                        break;
                    case "13":
                        viewOrderMenu();
                        break;
                    case "14":
                        listUserOrdersMenu();
                        break;
                    case "15":
                        updateOrderStatusMenu();
                        break;
                    case "16":
                        cancelOrderMenu();
                        break;
                    // System
                    case "17":
                        serviceStatusMenu();
                        break;
                    case "18":
                        showHelp();
                        break;
                    case "19":
                        System.out.println("\n✓ Shutting down...");
                        running = false;
                        break;
                    default:
                        System.out.println("✗ Invalid choice. Please try again. Type '18' for help.");
                }
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * CREATE: Add a new user
     */
    private static void createUserMenu() {
        System.out.println("\n--- CREATE NEW USER ---");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();
        
        try {
            User newUser = userService.createUser(name, email, phone, address);
            System.out.println("\n✓ User created successfully!");
            System.out.println("  " + newUser);
        } catch (Exception e) {
            System.out.println("\n✗ Error creating user: " + e.getMessage());
        }
    }
    
    /**
     * READ: Get a specific user by ID
     */
    private static void viewUserMenu() {
        System.out.println("\n--- VIEW USER BY ID ---");
        System.out.print("Enter User ID: ");
        
        try {
            int userId = Integer.parseInt(scanner.nextLine());
            User user = userService.getUser(userId);
            
            if (user != null) {
                System.out.println("\n✓ User found:");
                System.out.println("  " + user);
            } else {
                System.out.println("\n✗ User not found with ID: " + userId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * READ: Get all users
     */
    private static void listAllUsersMenu() {
        System.out.println("\n--- LIST ALL USERS ---");
        try {
            List<User> users = userService.getAllUsers();
            
            if (users.isEmpty()) {
                System.out.println("✗ No users found");
                return;
            }
            
            System.out.println("✓ Total Users: " + users.size());
            System.out.println("─".repeat(80));
            for (User user : users) {
                System.out.println("  " + user);
            }
            System.out.println("─".repeat(80));
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * UPDATE: Modify user information
     */
    private static void updateUserMenu() {
        System.out.println("\n--- UPDATE USER ---");
        System.out.print("Enter User ID to update: ");
        
        try {
            int userId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter new Name (or press Enter to skip): ");
            String name = scanner.nextLine();
            if (name.isEmpty()) name = null;
            
            System.out.print("Enter new Email (or press Enter to skip): ");
            String email = scanner.nextLine();
            if (email.isEmpty()) email = null;
            
            System.out.print("Enter new Phone (or press Enter to skip): ");
            String phone = scanner.nextLine();
            if (phone.isEmpty()) phone = null;
            
            System.out.print("Enter new Address (or press Enter to skip): ");
            String address = scanner.nextLine();
            if (address.isEmpty()) address = null;
            
            User updatedUser = userService.updateUser(userId, name, email, phone, address);
            
            if (updatedUser != null) {
                System.out.println("\n✓ User updated successfully!");
                System.out.println("  " + updatedUser);
            } else {
                System.out.println("\n✗ User not found with ID: " + userId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * DELETE: Remove a user
     */
    private static void deleteUserMenu() {
        System.out.println("\n--- DELETE USER ---");
        System.out.print("Enter User ID to delete: ");
        
        try {
            int userId = Integer.parseInt(scanner.nextLine());
            
            boolean deleted = userService.deleteUser(userId);
            
            if (deleted) {
                System.out.println("\n✓ User deleted successfully!");
            } else {
                System.out.println("\n✗ User not found with ID: " + userId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * Get total user count
     */
    private static void getUserCountMenu() {
        System.out.println("\n--- USER COUNT ---");
        try {
            int count = userService.getUserCount();
            System.out.println("✓ Total Users in System: " + count);
        } catch (Exception e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * Show service status for all services
     */
    private static void serviceStatusMenu() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("           MICROSERVICES STATUS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("\n[USER SERVICE]");
        System.out.println("  Port: " + userService.getPort());
        System.out.println("  Status: " + (userService.isRunning() ? "✓ RUNNING" : "✗ STOPPED"));
        System.out.println("  Total Users: " + userService.getUserCount());
        
        System.out.println("\n[PRODUCT SERVICE]");
        System.out.println("  Port: " + productService.getPort());
        System.out.println("  Status: " + (productService.isRunning() ? "✓ RUNNING" : "✗ STOPPED"));
        System.out.println("  Total Products: " + productService.getProductCount());
        
        System.out.println("\n[ORDER SERVICE]");
        System.out.println("  Port: " + orderService.getPort());
        System.out.println("  Status: " + (orderService.isRunning() ? "✓ RUNNING" : "✗ STOPPED"));
        System.out.println("  Total Orders: " + orderService.getOrderCount());
        System.out.println("  Connected to: USER_SERVICE, PRODUCT_SERVICE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
    
    // ======================== PRODUCT SERVICE METHODS ========================
    
    /**
     * CREATE: Add a new product
     */
    private static void addProductMenu() {
        System.out.println("\n--- ADD NEW PRODUCT ---");
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter Description: ");
        String description = scanner.nextLine();
        
        System.out.print("Enter Price: ");
        double price = Double.parseDouble(scanner.nextLine());
        
        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        
        try {
            Product newProduct = productService.addProduct(name, description, price, quantity);
            System.out.println("\n✓ Product created successfully!");
            System.out.println("  " + newProduct);
        } catch (Exception e) {
            System.out.println("\n✗ Error creating product: " + e.getMessage());
        }
    }
    
    /**
     * READ: Get a specific product by ID
     */
    private static void viewProductMenu() {
        System.out.println("\n--- VIEW PRODUCT BY ID ---");
        System.out.print("Enter Product ID: ");
        
        try {
            int productId = Integer.parseInt(scanner.nextLine());
            Product product = productService.getProduct(productId);
            
            if (product != null) {
                System.out.println("\n✓ Product found:");
                System.out.println("  " + product);
                System.out.println("  Available Stock: " + productService.getAvailableStock(productId));
            } else {
                System.out.println("\n✗ Product not found with ID: " + productId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * READ: Get all products
     */
    private static void listAllProductsMenu() {
        System.out.println("\n--- LIST ALL PRODUCTS ---");
        try {
            List<Product> products = productService.getAllProducts();
            
            if (products.isEmpty()) {
                System.out.println("✗ No products found");
                return;
            }
            
            System.out.println("✓ Total Products: " + products.size());
            System.out.println("─".repeat(100));
            for (Product product : products) {
                System.out.println("  " + product);
            }
            System.out.println("─".repeat(100));
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * UPDATE: Modify product information
     */
    private static void updateProductMenu() {
        System.out.println("\n--- UPDATE PRODUCT ---");
        System.out.print("Enter Product ID to update: ");
        
        try {
            int productId = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Enter new Name (or press Enter to skip): ");
            String name = scanner.nextLine();
            if (name.isEmpty()) name = null;
            
            System.out.print("Enter new Description (or press Enter to skip): ");
            String description = scanner.nextLine();
            if (description.isEmpty()) description = null;
            
            System.out.print("Enter new Price (or -1 to skip): ");
            String priceStr = scanner.nextLine();
            double price = priceStr.isEmpty() ? -1 : Double.parseDouble(priceStr);
            
            System.out.print("Enter new Quantity (or -1 to skip): ");
            String qtyStr = scanner.nextLine();
            int quantity = qtyStr.isEmpty() ? -1 : Integer.parseInt(qtyStr);
            
            Product product = productService.getProduct(productId);
            if (product != null) {
                Product updated = productService.updateProduct(productId,
                    name != null ? name : product.getName(),
                    description != null ? description : product.getDescription(),
                    price >= 0 ? price : product.getPrice(),
                    quantity >= 0 ? quantity : product.getQuantity()
                );
                System.out.println("\n✓ Product updated successfully!");
                System.out.println("  " + updated);
            } else {
                System.out.println("\n✗ Product not found with ID: " + productId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * DELETE: Remove a product
     */
    private static void deleteProductMenu() {
        System.out.println("\n--- DELETE PRODUCT ---");
        System.out.print("Enter Product ID to delete: ");
        
        try {
            int productId = Integer.parseInt(scanner.nextLine());
            boolean deleted = productService.deleteProduct(productId);
            
            if (deleted) {
                System.out.println("\n✓ Product deleted successfully!");
            } else {
                System.out.println("\n✗ Product not found with ID: " + productId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    // ======================== ORDER SERVICE METHODS ========================
    
    /**
     * CREATE: Place a new order (Cross-service demonstration)
     */
    private static void placeOrderMenu() {
        System.out.println("\n--- PLACE NEW ORDER ---");
        System.out.println("(This demonstrates cross-service communication)");
        
        System.out.print("Enter User ID: ");
        int userId = Integer.parseInt(scanner.nextLine());
        
        System.out.print("Enter Product ID: ");
        int productId = Integer.parseInt(scanner.nextLine());
        
        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());
        
        try {
            Order newOrder = orderService.placeOrder(userId, productId, quantity);
            System.out.println("\n✓ Order placed successfully!");
            System.out.println("  " + newOrder);
            System.out.println("\n  → User Service validates user existence");
            System.out.println("  → Product Service checks stock availability");
            System.out.println("  → Order Service orchestrates the transaction");
        } catch (Exception e) {
            System.out.println("\n✗ Error placing order: " + e.getMessage());
        }
    }
    
    /**
     * READ: Get a specific order by ID
     */
    private static void viewOrderMenu() {
        System.out.println("\n--- VIEW ORDER BY ID ---");
        System.out.print("Enter Order ID: ");
        
        try {
            int orderId = Integer.parseInt(scanner.nextLine());
            Order order = orderService.getOrder(orderId);
            
            if (order != null) {
                System.out.println("\n✓ Order found:");
                System.out.println("  " + order);
            } else {
                System.out.println("\n✗ Order not found with ID: " + orderId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * READ: Get all orders for a user
     */
    private static void listUserOrdersMenu() {
        System.out.println("\n--- MY ORDERS ---");
        System.out.print("Enter User ID: ");
        
        try {
            int userId = Integer.parseInt(scanner.nextLine());
            List<Order> orders = orderService.getUserOrders(userId);
            
            if (orders.isEmpty()) {
                System.out.println("\n✗ No orders found for user " + userId);
                return;
            }
            
            System.out.println("\n✓ User Orders (Total: " + orders.size() + ")");
            System.out.println("─".repeat(120));
            for (Order order : orders) {
                System.out.println("  " + order);
            }
            System.out.println("─".repeat(120));
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * UPDATE: Change order status
     */
    private static void updateOrderStatusMenu() {
        System.out.println("\n--- UPDATE ORDER STATUS ---");
        System.out.print("Enter Order ID: ");
        int orderId = Integer.parseInt(scanner.nextLine());
        
        System.out.println("Valid statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED");
        System.out.print("Enter new Status: ");
        String status = scanner.nextLine().toUpperCase();
        
        try {
            Order updated = orderService.updateOrderStatus(orderId, status);
            System.out.println("\n✓ Order status updated successfully!");
            System.out.println("  " + updated);
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * DELETE: Cancel an order
     */
    private static void cancelOrderMenu() {
        System.out.println("\n--- CANCEL ORDER ---");
        System.out.print("Enter Order ID to cancel: ");
        
        try {
            int orderId = Integer.parseInt(scanner.nextLine());
            boolean cancelled = orderService.cancelOrder(orderId);
            
            if (cancelled) {
                System.out.println("\n✓ Order cancelled successfully!");
                System.out.println("  → Stock has been restored to Product Service");
            } else {
                System.out.println("\n✗ Order not found with ID: " + orderId);
            }
        } catch (NumberFormatException e) {
            System.out.println("\n✗ Invalid ID format");
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
        }
    }
    
    /**
     * Show help information
     */
    private static void showHelp() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║                  HELP & DOCUMENTATION                 ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║ MICROSERVICES ARCHITECTURE DEMONSTRATION               ║");
        System.out.println("║                                                        ║");
        System.out.println("║ This application demonstrates three integrated         ║");
        System.out.println("║ microservices that work together:                      ║");
        System.out.println("║                                                        ║");
        System.out.println("║ 1. USER SERVICE (Port 8081)                            ║");
        System.out.println("║    - Manages user registration and profiles           ║");
        System.out.println("║    - CRUD operations on user data                     ║");
        System.out.println("║                                                        ║");
        System.out.println("║ 2. PRODUCT SERVICE (Port 8082)                         ║");
        System.out.println("║    - Manages product catalog and inventory            ║");
        System.out.println("║    - Tracks stock levels                              ║");
        System.out.println("║    - Handles product CRUD operations                  ║");
        System.out.println("║                                                        ║");
        System.out.println("║ 3. ORDER SERVICE (Port 8083)                           ║");
        System.out.println("║    - Orchestrates orders across services              ║");
        System.out.println("║    - Validates users (calls USER SERVICE)            ║");
        System.out.println("║    - Checks stock (calls PRODUCT SERVICE)            ║");
        System.out.println("║    - Manages order lifecycle                          ║");
        System.out.println("║                                                        ║");
        System.out.println("║ KEY CONCEPT: Cross-Service Communication              ║");
        System.out.println("║ When you place an order, the ORDER SERVICE validates  ║");
        System.out.println("║ both user existence and product stock by calling      ║");
        System.out.println("║ the other services. This is a real microservice       ║");
        System.out.println("║ integration pattern!                                  ║");
        System.out.println("║                                                        ║");
        System.out.println("║ TRY THIS SCENARIO:                                     ║");
        System.out.println("║ 1. Create a user (option 1)                           ║");
        System.out.println("║ 2. View available products (option 9)                 ║");
        System.out.println("║ 3. Place an order (option 12)                         ║");
        System.out.println("║ 4. Check user orders (option 14)                      ║");
        System.out.println("║ 5. Check product stock decreased (option 9)           ║");
        System.out.println("║                                                        ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
    }
}
