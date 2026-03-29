package com.example.app.user;

import com.example.app.models.User;
import java.util.*;

/**
 * UserRepository handles data storage and retrieval for users
 * Uses in-memory storage for simplicity (can be replaced with database)
 */
public class UserRepository {
    private Map<Integer, User> userStore = new HashMap<>();
    private int nextUserId = 1;
    
    /**
     * CREATE: Add a new user
     */
    public User createUser(String name, String email, String phone, String address) {
        User newUser = new User(nextUserId++, name, email, phone, address);
        userStore.put(newUser.getId(), newUser);
        System.out.println("User created: " + newUser);
        return newUser;
    }
    
    /**
     * READ: Get user by ID
     */
    public User getUserById(int id) {
        User user = userStore.get(id);
        if (user == null) {
            System.out.println("User not found with ID: " + id);
        }
        return user;
    }
    
    /**
     * READ: Get all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(userStore.values());
    }
    
    /**
     * UPDATE: Update an existing user
     */
    public User updateUser(int id, String name, String email, String phone, String address) {
        User user = userStore.get(id);
        if (user == null) {
            System.out.println("User not found with ID: " + id);
            return null;
        }
        
        if (name != null) user.setName(name);
        if (email != null) user.setEmail(email);
        if (phone != null) user.setPhone(phone);
        if (address != null) user.setAddress(address);
        
        System.out.println("User updated: " + user);
        return user;
    }
    
    /**
     * DELETE: Remove a user
     */
    public boolean deleteUser(int id) {
        if (userStore.containsKey(id)) {
            User deletedUser = userStore.remove(id);
            System.out.println("User deleted: " + deletedUser);
            return true;
        }
        System.out.println("User not found with ID: " + id);
        return false;
    }
    
    /**
     * Check if user exists
     */
    public boolean userExists(int id) {
        return userStore.containsKey(id);
    }
    
    /**
     * Get total user count
     */
    public int getTotalUsers() {
        return userStore.size();
    }
}
