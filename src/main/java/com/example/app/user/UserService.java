package com.example.app.user;

import com.example.app.models.User;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * UserService - Business logic layer
 * Handles CRUD operations and business rules
 */
@Service
public class UserService {
    private UserRepository userRepository;
    
    public UserService() {
        this.userRepository = new UserRepository();
    }
    
    /**
     * CREATE: Register a new user
     */
    public User registerUser(String name, String email, String phone, String address) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        return userRepository.createUser(name, email, phone, address);
    }
    
    /**
     * READ: Get user details by ID
     */
    public User getUserDetails(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return userRepository.getUserById(userId);
    }
    
    /**
     * READ: Get all users
     */
    public List<User> listAllUsers() {
        return userRepository.getAllUsers();
    }
    
    /**
     * UPDATE: Modify user information
     */
    public User updateUserInfo(int userId, String name, String email, String phone, String address) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (!userRepository.userExists(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        return userRepository.updateUser(userId, name, email, phone, address);
    }
    
    /**
     * DELETE: Remove a user
     */
    public boolean removeUser(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return userRepository.deleteUser(userId);
    }
    
    /**
     * Get total number of users
     */
    public int getUserCount() {
        return userRepository.getTotalUsers();
    }
    
    /**
     * Check if user exists
     */
    public boolean isUserExists(int userId) {
        return userRepository.userExists(userId);
    }
}
