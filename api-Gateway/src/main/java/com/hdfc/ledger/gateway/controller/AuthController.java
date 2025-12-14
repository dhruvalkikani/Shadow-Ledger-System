package com.hdfc.ledger.gateway.controller;

import com.hdfc.ledger.gateway.dto.AuthRequest;
import com.hdfc.ledger.gateway.dto.AuthResponse;
import com.hdfc.ledger.gateway.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/token")
    public ResponseEntity<AuthResponse> generateToken(@RequestBody AuthRequest request) {
        // Simple token generation for testing
        // In production, validate credentials against database
        String token = jwtUtil.generateToken(request.getUsername(), request.getRoles());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
