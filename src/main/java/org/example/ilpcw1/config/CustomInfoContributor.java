package org.example.ilpcw1.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

// NOT NEEDED ANYMORE
// http://localhost:8080/actuator/info
@Component
public class CustomInfoContributor implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("messages", new String[]{
                "status: UP",
                "app: ILP CW1 Application",
                "description: A Spring Boot application for ILP coursework 1"
        });
    }
}
