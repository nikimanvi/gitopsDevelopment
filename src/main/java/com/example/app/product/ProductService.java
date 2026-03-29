package com.example.app.product;

import com.example.app.models.Product;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * ProductService - Business logic layer
 * Handles CRUD operations and business rules for products
 */
@Service
public class ProductService {
    private ProductRepository productRepository;
    
    public ProductService() {
        this.productRepository = new ProductRepository();
    }
    
    /**
     * CREATE: Add a new product
     */
    public Product addProduct(String name, String description, double price, int quantity) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        return productRepository.createProduct(name, description, price, quantity);
    }
    
    /**
     * READ: Get product details by ID
     */
    public Product getProductDetails(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        Product product = productRepository.getProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }
        return product;
    }
    
    /**
     * READ: Get all products
     */
    public List<Product> listAllProducts() {
        return productRepository.getAllProducts();
    }
    
    /**
     * UPDATE: Modify product information
     */
    public Product updateProductInfo(int productId, String name, String description, double price, int quantity) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (!productRepository.productExists(productId)) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        return productRepository.updateProduct(productId, name, description, price, quantity);
    }
    
    /**
     * DELETE: Remove a product
     */
    public boolean removeProduct(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        return productRepository.deleteProduct(productId);
    }
    
    /**
     * Get total number of products
     */
    public int getProductCount() {
        return productRepository.getTotalProducts();
    }
    
    /**
     * Check if product exists
     */
    public boolean productExists(int productId) {
        return productRepository.productExists(productId);
    }
    
    /**
     * Get available stock
     */
    public int getAvailableStock(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (!productRepository.productExists(productId)) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }
        return productRepository.getProductStock(productId);
    }
    
    /**
     * Purchase product (decrease stock)
     */
    public boolean purchaseProduct(int productId, int quantity) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!productRepository.productExists(productId)) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }
        
        return productRepository.decreaseStock(productId, quantity);
    }
}
