package dev.serguncheouss.authservice.controller;

import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dev.serguncheouss.authservice.dto.JwtResponse;
import dev.serguncheouss.authservice.dto.RefreshJwtRequest;
import dev.serguncheouss.authservice.service.CustomersService;

@RestController
@RequestMapping("api/clients/v1/")
public class CustomerController {
    @Autowired
    CustomersService customersService;

    @PostMapping("token")
    public ResponseEntity<?> getNewAccessToken(@RequestBody RefreshJwtRequest request) {
        try {
            final String[] tokens = customersService.getAccessToken(request.getRefreshToken());

            return ResponseEntity.ok(new JwtResponse(tokens[0], tokens[1]));
        } catch (AuthException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("refresh")
    public ResponseEntity<?> getNewRefreshToken(@RequestBody RefreshJwtRequest request) {
        try {
            final String[] tokens = customersService.getNewRefreshToken(request.getRefreshToken());

            return ResponseEntity.ok(new JwtResponse(tokens[0], tokens[1]));
        } catch (AuthException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
