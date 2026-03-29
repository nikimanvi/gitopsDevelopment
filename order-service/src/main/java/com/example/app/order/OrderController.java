package com.example.app.order;

import com.example.app.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * OrderController - REST API endpoints for Order microservice
 * Handles HTTP requests for order operations and cross-service communication
 * 
 * API Base Path: http://localhost:8083/api/orders
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * GET /api/orders - Get all orders
     * @return List of all orders
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderService.listAllOrders();
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Orders retrieved successfully",
                orders,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        }
    }
    
    /**
     * GET /api/orders/{id} - Get order by ID
     * @param id Order ID
     * @return Order object if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable int id) {
        try {
            Order order = orderService.getOrderDetails(id);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "Order not found with ID: " + id,
                        null,
                        null
                    ));
            }
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Order retrieved successfully",
                order,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        }
    }
    
    /**
     * POST /api/orders - Place a new order
     * This demonstrates cross-service communication:
     * - Validates user (calls UserService)
     * - Validates product (calls ProductService)
     * - Checks inventory (calls ProductService)
     * - Updates stock (calls ProductService)
     * 
     * @param request PlaceOrderRequest containing user ID, product ID, and quantity
     * @return Created order object
     */
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest request) {
        try {
            // Validate input
            if (request.getUserId() <= 0 || request.getProductId() <= 0 || request.getQuantity() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                        "error",
                        "Invalid request: userId, productId, and quantity must be positive",
                        null,
                        null
                    ));
            }
            
            Order order = orderService.placeOrder(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    "success",
                    "Order placed successfully",
                    order,
                    null
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        }
    }
    
    /**
     * GET /api/orders/user/{userId} - Get all orders for a specific user
     * @param userId User ID
     * @return List of orders for the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable int userId) {
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "User orders retrieved successfully",
                orders,
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        }
    }
    
    /**
     * PUT /api/orders/{id}/status - Update order status
     * @param id Order ID
     * @param request UpdateOrderStatusRequest containing new status
     * @return Updated order object
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int id,
            @RequestBody UpdateOrderStatusRequest request) {
        try {
            Order order = orderService.updateOrderStatus(id, request.getStatus());
            
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "Order not found with ID: " + id,
                        null,
                        null
                    ));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Order status updated successfully",
                order,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        }
    }
    
    /**
     * DELETE /api/orders/{id} - Cancel an order
     * @param id Order ID
     * @return Success/failure message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable int id) {
        try {
            boolean cancelled = orderService.cancelOrder(id);
            
            if (!cancelled) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "Order not found with ID: " + id,
                        null,
                        null
                    ));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Order cancelled successfully",
                null,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        }
    }
    
    /**
     * GET /api/orders/stats - Get order statistics
     * @return Order statistics
     */
    @GetMapping("/stats/summary")
    public ResponseEntity<?> getOrderStats() {
        try {
            List<Order> allOrders = orderService.listAllOrders();
            
            OrderStats stats = new OrderStats();
            stats.setTotalOrders(allOrders.size());
            stats.setTotalRevenue(allOrders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum());
            stats.setConfirmedOrders(
                (int) allOrders.stream()
                    .filter(o -> "CONFIRMED".equals(o.getStatus()))
                    .count()
            );
            stats.setCancelledOrders(
                (int) allOrders.stream()
                    .filter(o -> "CANCELLED".equals(o.getStatus()))
                    .count()
            );
            
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Order statistics retrieved successfully",
                stats,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    null
                ));
        }
    }
    
    // Request DTOs
    
    /**
     * Request DTO for placing an order
     */
    public static class PlaceOrderRequest {
        private int userId;
        private int productId;
        private int quantity;
        
        // Getters and Setters
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    /**
     * Request DTO for updating order status
     */
    public static class UpdateOrderStatusRequest {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * Response DTO for order statistics
     */
    public static class OrderStats {
        private int totalOrders;
        private double totalRevenue;
        private int confirmedOrders;
        private int cancelledOrders;
        
        // Getters and Setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public int getConfirmedOrders() { return confirmedOrders; }
        public void setConfirmedOrders(int confirmedOrders) { this.confirmedOrders = confirmedOrders; }
        
        public int getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    }
    
    /**
     * Generic API Response wrapper
     */
    public static class ApiResponse<T> {
        private String status;
        private String message;
        private T data;
        private String error;
        
        public ApiResponse(String status, String message, T data, String error) {
            this.status = status;
            this.message = message;
            this.data = data;
            this.error = error;
        }
        
        // Getters
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public T getData() { return data; }
        public String getError() { return error; }
    }
}
