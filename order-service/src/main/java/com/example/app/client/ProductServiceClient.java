package com.example.app.client;

import com.example.app.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * ProductServiceClient - HTTP Client for communicating with Product Microservice
 * 
 * Provides methods to call Product Service REST endpoints
 * Handles network errors and service unavailability gracefully
 */
@Component
public class ProductServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ServiceConfig serviceConfig;
    
    /**
     * Get product by ID from Product Service via HTTP
     * GET /api/products/{id}
     */
    public Product getProductById(int productId) {
        try {
            String url = serviceConfig.getProductServiceUrl() + "/api/products/" + productId;
            logger.info("Calling Product Service: GET {}", url);
            
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
            
            if (response != null && "success".equals(response.getStatus())) {
                // Convert LinkedHashMap to Product object
                if (response.getData() instanceof java.util.LinkedHashMap) {
                    return mapToProduct((java.util.LinkedHashMap) response.getData());
                }
                return (Product) response.getData();
            }
            
            logger.warn("Product Service returned error: {}", response != null ? response.getMessage() : "null");
            return null;
            
        } catch (RestClientException e) {
            logger.error("Failed to call Product Service - getProductById({}): {}", productId, e.getMessage());
            throw new ServiceUnavailableException("Product Service is unavailable", e);
        } catch (Exception e) {
            logger.error("Error calling Product Service - getProductById({}): {}", productId, e.getMessage());
            throw new ServiceCommunicationException("Error communicating with Product Service", e);
        }
    }
    
    /**
     * Get available stock for a product from Product Service via HTTP
     * GET /api/products/{id}/stock
     */
    public int getAvailableStock(int productId) {
        try {
            String url = serviceConfig.getProductServiceUrl() + "/api/products/" + productId + "/stock";
            logger.info("Calling Product Service: GET {}", url);
            
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
            
            if (response != null && "success".equals(response.getStatus())) {
                if (response.getData() instanceof Number) {
                    return ((Number) response.getData()).intValue();
                }
            }
            
            logger.warn("Failed to get stock for product {}", productId);
            return 0;
            
        } catch (RestClientException e) {
            logger.error("Failed to call Product Service - getAvailableStock({}): {}", productId, e.getMessage());
            throw new ServiceUnavailableException("Product Service is unavailable", e);
        } catch (Exception e) {
            logger.error("Error calling Product Service - getAvailableStock({}): {}", productId, e.getMessage());
            throw new ServiceCommunicationException("Error communicating with Product Service", e);
        }
    }
    
    /**
     * Purchase product (decrease stock) via Product Service
     * POST /api/products/{id}/purchase
     */
    public boolean purchaseProduct(int productId, int quantity) {
        try {
            String url = serviceConfig.getProductServiceUrl() + "/api/products/" + productId + "/purchase";
            logger.info("Calling Product Service: POST {} (quantity: {})", url, quantity);
            
            Map<String, Object> request = new HashMap<>();
            request.put("quantity", quantity);
            
            ApiResponse response = restTemplate.postForObject(url, request, ApiResponse.class);
            
            if (response != null && "success".equals(response.getStatus())) {
                logger.info("Product purchase successful for product {}", productId);
                return true;
            }
            
            logger.warn("Product Service purchase failed: {}", response != null ? response.getMessage() : "null");
            return false;
            
        } catch (RestClientException e) {
            logger.error("Failed to call Product Service - purchaseProduct({}, {}): {}", productId, quantity, e.getMessage());
            throw new ServiceUnavailableException("Product Service is unavailable", e);
        } catch (Exception e) {
            logger.error("Error calling Product Service - purchaseProduct({}, {}): {}", productId, quantity, e.getMessage());
            throw new ServiceCommunicationException("Error communicating with Product Service", e);
        }
    }
    
    /**
     * Helper to convert LinkedHashMap to Product
     */
    private Product mapToProduct(java.util.LinkedHashMap map) {
        Product product = new Product();
        if (map.containsKey("id")) {
            product.setId(((Number) map.get("id")).intValue());
        }
        if (map.containsKey("name")) {
            product.setName((String) map.get("name"));
        }
        if (map.containsKey("description")) {
            product.setDescription((String) map.get("description"));
        }
        if (map.containsKey("price")) {
            product.setPrice(((Number) map.get("price")).doubleValue());
        }
        if (map.containsKey("quantity")) {
            product.setQuantity(((Number) map.get("quantity")).intValue());
        }
        return product;
    }
}
