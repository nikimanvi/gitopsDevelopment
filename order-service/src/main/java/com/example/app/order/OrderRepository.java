package com.example.app.order;

import com.example.app.models.Order;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * OrderRepository - Data access layer for orders
 * Simulates a database with in-memory storage
 */
public class OrderRepository {
    private Map<Integer, Order> orders;
    private int nextId;
    
    public OrderRepository() {
        this.orders = new HashMap<>();
        this.nextId = 1;
    }
    
    /**
     * CREATE: Add a new order
     */
    public Order createOrder(int userId, int productId, int quantity, double totalPrice) {
        Order order = new Order(nextId++, userId, productId, quantity, totalPrice);
        orders.put(order.getId(), order);
        return order;
    }
    
    /**
     * READ: Get order by ID
     */
    public Order getOrderById(int orderId) {
        return orders.get(orderId);
    }
    
    /**
     * READ: Get all orders
     */
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
    
    /**
     * READ: Get orders for a specific user
     */
    public List<Order> getOrdersByUserId(int userId) {
        List<Order> userOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getUserId() == userId) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }
    
    /**
     * UPDATE: Update order status
     */
    public Order updateOrderStatus(int orderId, String status) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus(status);
        }
        return order;
    }
    
    /**
     * DELETE: Remove an order
     */
    public boolean deleteOrder(int orderId) {
        return orders.remove(orderId) != null;
    }
    
    /**
     * Check if order exists
     */
    public boolean orderExists(int orderId) {
        return orders.containsKey(orderId);
    }
    
    /**
     * Get total number of orders
     */
    public int getTotalOrders() {
        return orders.size();
    }
}
