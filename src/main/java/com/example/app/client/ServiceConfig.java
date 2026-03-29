package com.example.app.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ServiceConfig - Centralized configuration for service URLs
 * Allows dynamic configuration of service endpoints
 */
@Component
public class ServiceConfig {
    
    @Value("${service.user.url:http://localhost:8081}")
    private String userServiceUrl;
    
    @Value("${service.product.url:http://localhost:8082}")
    private String productServiceUrl;
    
    @Value("${service.order.url:http://localhost:8083}")
    private String orderServiceUrl;
    
    @Value("${service.timeout.connect:5000}")
    private int connectTimeout;
    
    @Value("${service.timeout.read:10000}")
    private int readTimeout;
    
    public String getUserServiceUrl() {
        return userServiceUrl;
    }
    
    public String getProductServiceUrl() {
        return productServiceUrl;
    }
    
    public String getOrderServiceUrl() {
        return orderServiceUrl;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setUserServiceUrl(String url) {
        this.userServiceUrl = url;
    }
    
    public void setProductServiceUrl(String url) {
        this.productServiceUrl = url;
    }
    
    public void setOrderServiceUrl(String url) {
        this.orderServiceUrl = url;
    }
}
