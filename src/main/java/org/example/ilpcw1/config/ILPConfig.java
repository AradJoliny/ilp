package org.example.ilpcw1.config;

import org.springframework.context.annotation.Bean;

public class ILPConfig {

    @Bean
    public String ilpEndpoint() {
        String env = System.getenv("ILP_ENDPOINT");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net";
    }
}
