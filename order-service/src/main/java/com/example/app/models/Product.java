package com.example.app.models;

/**
 * Product model/entity for the Product Microservice
 */
public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private long createdAt;
    
    public Product() { }

    // Constructor
    public Product(int id, String name, String description, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getPrice() {
        return price;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Product{id=%d, name='%s', description='%s', price=%.2f, quantity=%d, createdAt=%d}",
            id, name, description, price, quantity, createdAt
        );
    }
}
