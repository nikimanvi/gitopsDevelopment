package com.example.app.order;

import com.example.app.models.Order;
import com.example.app.models.User;
import com.example.app.models.Product;
import com.example.app.client.UserServiceClient;
import com.example.app.client.ProductServiceClient;
import com.example.app.client.ServiceUnavailableException;
import com.example.app.client.ServiceCommunicationException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * OrderService - Business logic layer
 * Demonstrates DECOUPLED cross-service communication between User and Product microservices
 * Now uses HTTP-based REST calls instead of direct Java object references
 * 
 * This enables:
 * - True microservice independence (services can run on separate machines)
 * - Loose coupling (services communicate via HTTP/REST)
 * - Resilience patterns (timeouts, fallbacks, circuit breakers)
 * - Service discovery and dynamic configuration
 */
@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private OrderRepository orderRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    public OrderService() {
        this.orderRepository = new OrderRepository();
    }
    
    // Legacy constructor for backward compatibility
    @Deprecated
    public OrderService(Object userService, Object productService) {
        this.orderRepository = new OrderRepository();
        logger.warn("Using deprecated constructor - HTTP clients will be used via autowiring");
    }
    
    /**
     * CREATE: Place a new order
     * Demonstrates DECOUPLED cross-service integration via HTTP REST calls
     * 
     * Process:
     * 1. Call User Service via HTTP to validate user exists
     * 2. Call Product Service via HTTP to validate product exists
     * 3. Call Product Service via HTTP to check available stock
     * 4. Create order in local repository
     * 5. Call Product Service via HTTP to decrease stock
     * 
     * Error Handling:
     * - ServiceUnavailableException: Service is down or unreachable
     * - ServiceCommunicationException: Error communicating with service
     * - IllegalArgumentException: Validation failed
     */
    public Order placeOrder(int userId, int productId, int quantity) {
        logger.info("Processing order - UserId: {}, ProductId: {}, Quantity: {}", userId, productId, quantity);
        
        try {
            // Step 1: Validate user exists via HTTP call to User Service
            logger.debug("Validating user via HTTP call to User Service (Port 8081)");
            User user = userServiceClient.getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
            logger.info("✓ User validation successful: {}", user.getName());
            
            // Step 2: Validate product exists via HTTP call to Product Service
            logger.debug("Validating product via HTTP call to Product Service (Port 8082)");
            Product product = productServiceClient.getProductById(productId);
            if (product == null) {
                throw new IllegalArgumentException("Product not found with ID: " + productId);
            }
            logger.info("✓ Product validation successful: {}", product.getName());
            
            // Step 3: Check available stock via HTTP call to Product Service
            logger.debug("Checking stock via HTTP call to Product Service (Port 8082)");
            int availableStock = productServiceClient.getAvailableStock(productId);
            if (availableStock < quantity) {
                throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + availableStock + ", Requested: " + quantity
                );
            }
            logger.info("✓ Stock validation successful: {} units available", availableStock);
            
            // Step 4: Calculate total price and create order
            double totalPrice = product.getPrice() * quantity;
            Order order = orderRepository.createOrder(userId, productId, quantity, totalPrice);
            logger.info("✓ Order created in local repository: Order ID {}", order.getId());
            
            // Step 5: Decrease product stock via HTTP call to Product Service
            logger.debug("Updating stock via HTTP call to Product Service (Port 8082)");
            boolean stockUpdated = productServiceClient.purchaseProduct(productId, quantity);
            if (!stockUpdated) {
                logger.warn("⚠ Failed to update stock, but order was created. Manual reconciliation may be needed.");
                order.setStatus("PENDING_STOCK_UPDATE");
            } else {
                order.setStatus("CONFIRMED");
                logger.info("✓ Stock updated successfully");
            }
            
            logger.info("✓ Order placement complete: Order ID {}, Status: {}", order.getId(), order.getStatus());
            return order;
            
        } catch (ServiceUnavailableException e) {
            logger.error("✗ Service unavailable while processing order: {}", e.getMessage());
            throw new IllegalArgumentException("A required microservice is currently unavailable. Please try again later.", e);
        } catch (ServiceCommunicationException e) {
            logger.error("✗ Communication error while processing order: {}", e.getMessage());
            throw new IllegalArgumentException("Error communicating with microservice. Please try again.", e);
        }
    }
    
    /**
     * READ: Get order details by ID
     */
    public Order getOrderDetails(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        return order;
    }
    
    /**
     * READ: Get all orders
     */
    public List<Order> listAllOrders() {
        return orderRepository.getAllOrders();
    }
    
    /**
     * READ: Get orders for a specific user
     * Validates user exists via HTTP call to User Service
     */
    public List<Order> getUserOrders(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        
        try {
            // Validate user exists via HTTP call to User Service
            logger.debug("Validating user via HTTP call to User Service (Port 8081)");
            boolean userExists = userServiceClient.userExists(userId);
            if (!userExists) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
            logger.info("✓ User validation successful for userId: {}", userId);
            
        } catch (ServiceUnavailableException e) {
            logger.warn("⚠ User Service unavailable, returning orders without validation: {}", e.getMessage());
            // Continue without validation - eventual consistency
        } catch (ServiceCommunicationException e) {
            logger.warn("⚠ Communication error with User Service, returning orders without validation: {}", e.getMessage());
            // Continue without validation - eventual consistency
        }
        
        return orderRepository.getOrdersByUserId(userId);
    }
    
    /**
     * UPDATE: Update order status
     */
    public Order updateOrderStatus(int orderId, String status) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }
        if (!orderRepository.orderExists(orderId)) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        
        return orderRepository.updateOrderStatus(orderId, status);
    }
    
    /**
     * DELETE: Cancel an order
     * Restores product stock via HTTP call to Product Service
     */
    public boolean cancelOrder(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }
        
        Order order = orderRepository.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        
        // Restore product stock when order is cancelled via HTTP call to Product Service
        if ("PENDING".equals(order.getStatus()) || "CONFIRMED".equals(order.getStatus())) {
            try {
                logger.debug("Restoring stock via HTTP call to Product Service (Port 8082)");
                productServiceClient.purchaseProduct(order.getProductId(), -order.getQuantity());
                logger.info("✓ Stock restored for product: {}", order.getProductId());
            } catch (ServiceUnavailableException e) {
                logger.warn("⚠ Product Service unavailable, order cancelled but stock may not be restored: {}", e.getMessage());
                // Continue - order is cancelled but stock restoration failed
            } catch (ServiceCommunicationException e) {
                logger.warn("⚠ Communication error with Product Service, order cancelled but stock may not be restored: {}", e.getMessage());
                // Continue - order is cancelled but stock restoration failed
            }
        }
        
        return orderRepository.deleteOrder(orderId);
    }
    
    /**
     * Get total number of orders
     */
    public int getOrderCount() {
        return orderRepository.getTotalOrders();
    }
}

