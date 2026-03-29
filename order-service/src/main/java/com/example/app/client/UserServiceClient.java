package com.example.app.client;

import com.example.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserServiceClient - HTTP Client for communicating with User Microservice
 * 
 * Provides methods to call User Service REST endpoints
 * Handles network errors and service unavailability gracefully
 */
@Component
public class UserServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ServiceConfig serviceConfig;
    
    /**
     * Get user by ID from User Service via HTTP
     * GET /api/users/{id}
     */
    public User getUserById(int userId) {
        try {
            String url = serviceConfig.getUserServiceUrl() + "/api/users/" + userId;
            logger.info("Calling User Service: GET {}", url);
            
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
            
            if (response != null && "success".equals(response.getStatus())) {
                // Convert LinkedHashMap to User object
                if (response.getData() instanceof java.util.LinkedHashMap) {
                    return mapToUser((java.util.LinkedHashMap) response.getData());
                }
                return (User) response.getData();
            }
            
            logger.warn("User Service returned error: {}", response != null ? response.getMessage() : "null");
            return null;
            
        } catch (RestClientException e) {
            logger.error("Failed to call User Service - getUserById({}): {}", userId, e.getMessage());
            throw new ServiceUnavailableException("User Service is unavailable", e);
        } catch (Exception e) {
            logger.error("Error calling User Service - getUserById({}): {}", userId, e.getMessage());
            throw new ServiceCommunicationException("Error communicating with User Service", e);
        }
    }
    
    /**
     * Validate if user exists
     */
    public boolean userExists(int userId) {
        try {
            User user = getUserById(userId);
            return user != null;
        } catch (Exception e) {
            logger.warn("Unable to validate user existence: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper to convert LinkedHashMap to User
     */
    private User mapToUser(java.util.LinkedHashMap map) {
        User user = new User();
        if (map.containsKey("id")) {
            user.setId(((Number) map.get("id")).intValue());
        }
        if (map.containsKey("name")) {
            user.setName((String) map.get("name"));
        }
        if (map.containsKey("email")) {
            user.setEmail((String) map.get("email"));
        }
        if (map.containsKey("phone")) {
            user.setPhone((String) map.get("phone"));
        }
        if (map.containsKey("address")) {
            user.setAddress((String) map.get("address"));
        }
        return user;
    }
}
