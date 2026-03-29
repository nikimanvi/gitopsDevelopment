package com.example.app.product;

import com.example.app.core.BaseService;
import com.example.app.models.Product;
import java.util.List;

/**
 * ProductMicroService - The actual microservice
 * Extends BaseService and uses ProductService for business logic
 */
public class ProductMicroService extends BaseService {
    private ProductService productService;
    
    public ProductMicroService(int port) {
        super("PRODUCT_SERVICE", port);
        this.productService = new ProductService();
    }
    
    @Override
    protected void onStart() {
        System.out.println("PRODUCT_SERVICE initialized and ready to handle requests");
    }
    
    @Override
    protected void onStop() {
        System.out.println("PRODUCT_SERVICE shutting down gracefully");
    }
    
    // CRUD Operations exposed by the microservice
    
    /**
     * CREATE: Add a new product
     */
    public Product addProduct(String name, String description, double price, int quantity) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.addProduct(name, description, price, quantity);
    }
    
    /**
     * READ: Get product by ID
     */
    public Product getProduct(int productId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.getProductDetails(productId);
    }
    
    /**
     * READ: Get all products
     */
    public List<Product> getAllProducts() {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.listAllProducts();
    }
    
    /**
     * UPDATE: Update product information
     */
    public Product updateProduct(int productId, String name, String description, double price, int quantity) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.updateProductInfo(productId, name, description, price, quantity);
    }
    
    /**
     * DELETE: Remove a product
     */
    public boolean deleteProduct(int productId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.removeProduct(productId);
    }
    
    /**
     * Get product count
     */
    public int getProductCount() {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.getProductCount();
    }
    
    /**
     * Get available stock for a product
     */
    public int getAvailableStock(int productId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.getAvailableStock(productId);
    }
    
    /**
     * Purchase a product (decrease stock)
     */
    public boolean purchaseProduct(int productId, int quantity) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return productService.purchaseProduct(productId, quantity);
    }
}
