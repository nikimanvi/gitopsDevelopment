package com.example.app.core;

/**
 * Base interface for all microservices
 * This defines the contract that every microservice must follow
 */
public interface MicroService {
    
    /**
     * Service initialization
     */
    void start();
    
    /**
     * Service shutdown
     */
    void stop();
    
    /**
     * Get service name/identifier
     */
    String getServiceName();
    
    /**
     * Get service port
     */
    int getPort();
    
    /**
     * Check if service is running
     */
    boolean isRunning();
}
