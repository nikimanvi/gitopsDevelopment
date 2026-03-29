package com.example.app.product;

import com.example.app.models.Product;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * ProductRepository - Data access layer for products
 * Simulates a database with in-memory storage
 */
public class ProductRepository {
    private Map<Integer, Product> products;
    private int nextId;
    
    public ProductRepository() {
        this.products = new HashMap<>();
        this.nextId = 1;
        initializeSampleData();
    }
    
    /**
     * Initialize with sample product data
     */
    private void initializeSampleData() {
        createProduct("Laptop", "High-performance laptop with 16GB RAM", 999.99, 10);
        createProduct("Mouse", "Wireless optical mouse", 29.99, 50);
        createProduct("Keyboard", "Mechanical keyboard with RGB lighting", 79.99, 25);
        createProduct("Monitor", "27-inch 4K display", 349.99, 15);
    }
    
    /**
     * CREATE: Add a new product
     */
    public Product createProduct(String name, String description, double price, int quantity) {
        Product product = new Product(nextId++, name, description, price, quantity);
        products.put(product.getId(), product);
        return product;
    }
    
    /**
     * READ: Get product by ID
     */
    public Product getProductById(int productId) {
        return products.get(productId);
    }
    
    /**
     * READ: Get all products
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }
    
    /**
     * UPDATE: Update product information
     */
    public Product updateProduct(int productId, String name, String description, double price, int quantity) {
        Product product = products.get(productId);
        if (product != null) {
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setQuantity(quantity);
        }
        return product;
    }
    
    /**
     * DELETE: Remove a product
     */
    public boolean deleteProduct(int productId) {
        return products.remove(productId) != null;
    }
    
    /**
     * Check if product exists
     */
    public boolean productExists(int productId) {
        return products.containsKey(productId);
    }
    
    /**
     * Get total number of products
     */
    public int getTotalProducts() {
        return products.size();
    }
    
    /**
     * Get available stock for a product
     */
    public int getProductStock(int productId) {
        Product product = products.get(productId);
        return product != null ? product.getQuantity() : 0;
    }
    
    /**
     * Decrease stock when product is purchased
     */
    public boolean decreaseStock(int productId, int quantity) {
        Product product = products.get(productId);
        if (product != null && product.getQuantity() >= quantity) {
            product.setQuantity(product.getQuantity() - quantity);
            return true;
        }
        return false;
    }
}
