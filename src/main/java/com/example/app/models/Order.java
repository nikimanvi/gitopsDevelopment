package com.example.app.models;

/**
 * Order model for demonstrating cross-service integration
 * Links users (from UserService) with products (from ProductService)
 */
public class Order {
    private int id;
    private int userId;
    private int productId;
    private int quantity;
    private double totalPrice;
    private String status;
    private long createdAt;
    
    public Order(int id, int userId, int productId, int quantity, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = "PENDING";
        this.createdAt = System.currentTimeMillis();
    }
    
    public int getId() {
        return id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Order{id=%d, userId=%d, productId=%d, quantity=%d, totalPrice=%.2f, status='%s', createdAt=%d}",
            id, userId, productId, quantity, totalPrice, status, createdAt
        );
    }
}
