package dev.serguncheouss.authservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dev.serguncheouss.authservice.dto.Health;

@Slf4j
@RestController
@RequestMapping("api/health/v1/")
public class HealthController {
    @GetMapping("status")
    public ResponseEntity<Health> getHealth() {
        log.debug("REST request to get the Health Status");
        final var health = new Health();
        health.setStatus(Health.Status.UP);
        return ResponseEntity.ok(health);
    }
}
