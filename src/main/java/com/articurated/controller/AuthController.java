package com.articurated.controller;

import com.articurated.dto.AuthRequest;
import com.articurated.dto.AuthResponse;
import com.articurated.model.enums.Role;
import com.articurated.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request, Role.CUSTOMER);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request, Role.ADMIN);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/manager")
    public ResponseEntity<AuthResponse> registerManager(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request, Role.MANAGER);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}



