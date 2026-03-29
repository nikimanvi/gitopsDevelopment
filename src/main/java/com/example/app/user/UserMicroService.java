package com.example.app.user;

import com.example.app.core.BaseService;
import com.example.app.models.User;
import java.util.List;

/**
 * UserMicroService - The actual microservice
 * Extends BaseService and uses UserService for business logic
 */
public class UserMicroService extends BaseService {
    private UserService userService;
    
    public UserMicroService(int port) {
        super("USER_SERVICE", port);
        this.userService = new UserService();
    }
    
    @Override
    protected void onStart() {
        System.out.println("USER_SERVICE initialized and ready to handle requests");
    }
    
    @Override
    protected void onStop() {
        System.out.println("USER_SERVICE shutting down gracefully");
    }
    
    // CRUD Operations exposed by the microservice
    
    /**
     * CREATE: Register a new user
     */
    public User createUser(String name, String email, String phone, String address) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return userService.registerUser(name, email, phone, address);
    }
    
    /**
     * READ: Get user by ID
     */
    public User getUser(int userId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return userService.getUserDetails(userId);
    }
    
    /**
     * READ: Get all users
     */
    public List<User> getAllUsers() {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return userService.listAllUsers();
    }
    
    /**
     * UPDATE: Update user information
     */
    public User updateUser(int userId, String name, String email, String phone, String address) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return userService.updateUserInfo(userId, name, email, phone, address);
    }
    
    /**
     * DELETE: Remove a user
     */
    public boolean deleteUser(int userId) {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return userService.removeUser(userId);
    }
    
    /**
     * Get user count
     */
    public int getUserCount() {
        if (!isRunning()) {
            throw new RuntimeException("Service is not running");
        }
        return userService.getUserCount();
    }
}
