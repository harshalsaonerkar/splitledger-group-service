package com.splitledger.group.client;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private final WebClient.Builder webClientBuilder;

    public UserInfo getUserByEmail(String email, String bearerToken) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(authServiceUrl + "/api/users/by-email?email=" + email)
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .bodyToMono(UserInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch user from Auth Service: {}", e.getMessage());
            throw new RuntimeException("User not found with email: " + email);
        }
    }

    @Data
    public static class UserInfo {
        private UUID id;
        private String email;
        private String name;
    }
}
