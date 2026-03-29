package com.example.app.user;

import com.example.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * UserController - REST API endpoints for User microservice
 * Handles HTTP requests for user CRUD operations
 * 
 * API Base Path: http://localhost:8081/api/users
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * GET /api/users - Get all users
     * @return List of all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.listAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "Users retrieved successfully",
                users,
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
     * GET /api/users/{id} - Get user by ID
     * @param id User ID
     * @return User object if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        try {
            User user = userService.getUserDetails(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "User not found with ID: " + id,
                        null,
                        null
                    ));
            }
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "User retrieved successfully",
                user,
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
     * POST /api/users - Create a new user
     * @param request CreateUserRequest containing user details
     * @return Created user object
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Validate input
            if (request.getName() == null || request.getName().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(
                        "error",
                        "Name is required",
                        null,
                        null
                    ));
            }
            
            User user = userService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    "success",
                    "User created successfully",
                    user,
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
     * PUT /api/users/{id} - Update user information
     * @param id User ID
     * @param request UpdateUserRequest containing updated user details
     * @return Updated user object
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable int id,
            @RequestBody UpdateUserRequest request) {
        try {
            User user = userService.updateUserInfo(
                id,
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress()
            );
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "User not found with ID: " + id,
                        null,
                        null
                    ));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "User updated successfully",
                user,
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
     * DELETE /api/users/{id} - Delete a user
     * @param id User ID
     * @return Success/failure message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        try {
            boolean deleted = userService.removeUser(id);
            
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(
                        "error",
                        "User not found with ID: " + id,
                        null,
                        null
                    ));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "User deleted successfully",
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
     * GET /api/users/count - Get total user count
     * @return Total number of users
     */
    @GetMapping("/count")
    public ResponseEntity<?> getUserCount() {
        try {
            List<User> users = userService.listAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(
                "success",
                "User count retrieved",
                users.size(),
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
    
    // Request DTOs
    
    /**
     * Request DTO for creating a user
     */
    public static class CreateUserRequest {
        private String name;
        private String email;
        private String phone;
        private String address;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
    
    /**
     * Request DTO for updating a user
     */
    public static class UpdateUserRequest {
        private String name;
        private String email;
        private String phone;
        private String address;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
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
