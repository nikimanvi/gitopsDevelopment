package com.example.app.order;

import com.example.app.core.BaseService;
import com.example.app.models.Order;
import com.example.app.user.UserMicroService;
import com.example.app.product.ProductMicroService;
import java.util.List;

/**
 * OrderMicroService - Service that orchestrates across multiple services
 * Demonstrates inter-service communication and integration patterns
 */
public class OrderMicroService extends BaseService {
    private OrderService orderService;
    
    public OrderMicroService(int port, UserMicroService userService, ProductMicroService productService) {
        super("ORDER_SERVICE", port);
        this.orderService = new OrderService(userService, productService);
    }
    
    @Override
    protected void onStart() {
        System.out.println("ORDER_SERVICE initialized and ready to handle requests");
        System.out.println("ORDER_SERVICE connected to USER_SERVICE and PRODUCT_SERVICE");
    }
    
    @Override
    protected void onStop() {
        System.out.println("ORDER_SERVICE shutting down gracefully");
    }
    
    /**
     * CREATE: Place a new order
     * This method demonstrates cross-service communication
     */
    public Order placeOrder(int userId, int productId, int quantity) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return orderService.placeOrder(userId, productId, quantity);
    }
    
    /**
     * READ: Get order by ID
     */
    public Order getOrder(int orderId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return orderService.getOrderDetails(orderId);
    }
    
    /**
     * READ: Get all orders
     */
    public List<Order> getAllOrders() {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return orderService.listAllOrders();
    }
    
    /**
     * READ: Get orders for a specific user
     */
    public List<Order> getUserOrders(int userId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return orderService.getUserOrders(userId);
    }
    
    /**
     * UPDATE: Update order status
     */
    public Order updateOrderStatus(int orderId, String status) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return orderService.updateOrderStatus(orderId, status);
    }
    
    /**
     * DELETE: Cancel an order
     */
    public boolean cancelOrder(int orderId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return orderService.cancelOrder(orderId);
    }
    
    /**
     * Get order count
     */
    public int getOrderCount() {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return orderService.getOrderCount();
    }
}
