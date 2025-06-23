package com.example.docmgmt.controller;

import com.example.docmgmt.model.JwtRequest;
import com.example.docmgmt.model.User;
import com.example.docmgmt.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        authService.register(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(summary = "Login and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody JwtRequest authenticationRequest) {
        String token = authService.login(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Logout (client-side token invalidation)")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Client should discard the token
        return ResponseEntity.ok("Logged out successfully");
    }
}