package com.example.app.product;

import com.example.app.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ProductController - REST API endpoints for Product microservice
 * Handles HTTP requests for product CRUD operations
 * 
 * API Base Path: http://localhost:8082/api/products
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * GET /api/products - Get all products
     * @return List of all products
     */
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productService.listAllProducts();
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Products retrieved successfully",
                products,
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
     * GET /api/products/{id} - Get product by ID
     * @param id Product ID
     * @return Product object if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        try {
            Product product = productService.getProductDetails(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "Product not found with ID: " + id,
                        null,
                        null
                    ));
            }
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Product retrieved successfully",
                product,
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
     * POST /api/products - Create a new product
     * @param request CreateProductRequest containing product details
     * @return Created product object
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody CreateProductRequest request) {
        try {
            // Validate input
            if (request.getName() == null || request.getName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                        "error",
                        "Product name is required",
                        null,
                        null
                    ));
            }
            
            Product product = productService.addProduct(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getQuantity()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    "success",
                    "Product created successfully",
                    product,
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
     * PUT /api/products/{id} - Update product information
     * @param id Product ID
     * @param request UpdateProductRequest containing updated product details
     * @return Updated product object
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @RequestBody UpdateProductRequest request) {
        try {
            Product product = productService.updateProductInfo(
                id,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getQuantity()
            );
            
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "Product not found with ID: " + id,
                        null,
                        null
                    ));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Product updated successfully",
                product,
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
     * DELETE /api/products/{id} - Delete a product
     * @param id Product ID
     * @return Success/failure message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        try {
            boolean deleted = productService.removeProduct(id);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "Product not found with ID: " + id,
                        null,
                        null
                    ));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Product deleted successfully",
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
     * GET /api/products/{id}/stock - Get available stock for a product
     * @param id Product ID
     * @return Available stock quantity
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getAvailableStock(@PathVariable int id) {
        try {
            int stock = productService.getAvailableStock(id);
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Stock retrieved successfully",
                stock,
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
     * POST /api/products/{id}/purchase - Purchase (reduce stock) a product
     * @param id Product ID
     * @param request PurchaseRequest containing quantity
     * @return Updated product object
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchaseProduct(
            @PathVariable int id,
            @RequestBody PurchaseRequest request) {
        try {
            boolean success = productService.purchaseProduct(id, request.getQuantity());
            if (!success) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                        "error",
                        "Purchase failed - insufficient stock",
                        null,
                        null
                    ));
            }
            Product product = productService.getProductDetails(id);
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Purchase successful",
                product,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
     * Request DTO for creating a product
     */
    public static class CreateProductRequest {
        private String name;
        private String description;
        private double price;
        private int quantity;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    /**
     * Request DTO for updating a product
     */
    public static class UpdateProductRequest {
        private String name;
        private String description;
        private double price;
        private int quantity;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    /**
     * Request DTO for purchasing a product
     */
    public static class PurchaseRequest {
        private int quantity;
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
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
