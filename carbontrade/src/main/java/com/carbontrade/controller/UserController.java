package com.carbontrade.controller;

import com.carbontrade.model.User;
import com.carbontrade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");

        User user;
        String message;

        // Try to login if exists
        User existingUser = userService.loginUser(email, password);
        if (existingUser != null) {
            user = existingUser;
            message = "User already exists. Logged in instead.";
        } else {
            // Try to register
            try {
                user = userService.registerUser(name, email, password);
                message = "User registered successfully";
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "User already exists or invalid data"));
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userService.loginUser(email, password);

        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("success", true);
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("cerBalance", user.getCerBalance());
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<User> getProfile(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getAllUsers() {
        // --- FIX: Connected to real database service ---
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
