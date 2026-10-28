package br.com.ecomovement.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InicioResource {
    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of(
                "api", "EcoMovement API - Missões",
                "status", "online",
                "swagger", "/swagger-ui.html"
        );
    }
}
