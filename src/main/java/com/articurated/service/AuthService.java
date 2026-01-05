package com.articurated.service;

import com.articurated.dto.AuthRequest;
import com.articurated.dto.AuthResponse;
import com.articurated.exception.ResourceNotFoundException;
import com.articurated.model.Customer;
import com.articurated.model.enums.Role;
import com.articurated.repository.CustomerRepository;
import com.articurated.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(AuthRequest request, Role role) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setFirstName(""); // Can be extended
        customer.setLastName(""); // Can be extended
        customer.setRole(role);

        customerRepository.save(customer);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = tokenProvider.generateToken(authentication);

        return new AuthResponse(token, "Bearer", customer.getEmail(), customer.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return new AuthResponse(token, "Bearer", customer.getEmail(), customer.getRole().name());
    }
}



