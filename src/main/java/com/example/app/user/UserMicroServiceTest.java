package com.example.app.user;

import com.example.app.models.User;
import java.util.List;

/**
 * UserMicroServiceTest - Demonstrates CRUD operations
 */
public class UserMicroServiceTest {
    
    public static void main(String[] args) {
        System.out.println("=== User Microservice CRUD Demo ===\n");
        
        // Create and start the service
        UserMicroService userService = new UserMicroService(8081);
        userService.start();
        
        try {
            // ========== CREATE ==========
            System.out.println("\n--- CREATE Operations ---");
            User user1 = userService.createUser("John Doe", "john@example.com", "9876543210", "123 Main St");
            User user2 = userService.createUser("Jane Smith", "jane@example.com", "9876543211", "456 Oak Ave");
            User user3 = userService.createUser("Bob Johnson", "bob@example.com", "9876543212", "789 Pine Rd");
            
            // ========== READ (Single User) ==========
            System.out.println("\n--- READ Operations (Single User) ---");
            User retrievedUser = userService.getUser(1);
            System.out.println("Retrieved User: " + retrievedUser);
            
            // ========== READ (All Users) ==========
            System.out.println("\n--- READ Operations (All Users) ---");
            List<User> allUsers = userService.getAllUsers();
            System.out.println("Total Users: " + userService.getUserCount());
            allUsers.forEach(user -> System.out.println("  - " + user));
            
            // ========== UPDATE ==========
            System.out.println("\n--- UPDATE Operations ---");
            User updatedUser = userService.updateUser(1, "John Updated", "john.new@example.com", "9111111111", "999 New St");
            System.out.println("Updated User: " + updatedUser);
            
            // Verify update
            System.out.println("Verification after update:");
            System.out.println(userService.getUser(1));
            
            // ========== DELETE ==========
            System.out.println("\n--- DELETE Operations ---");
            boolean deleted = userService.deleteUser(2);
            System.out.println("User with ID 2 deleted: " + deleted);
            
            // Verify deletion
            System.out.println("User count after deletion: " + userService.getUserCount());
            System.out.println("Remaining users:");
            userService.getAllUsers().forEach(user -> System.out.println("  - " + user));
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            userService.stop();
        }
        
        System.out.println("\n=== Demo Complete ===");
    }
}
