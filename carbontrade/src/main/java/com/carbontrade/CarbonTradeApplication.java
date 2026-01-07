package com.carbontrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
@org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = "com.carbontrade.model")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.carbontrade.repository")
public class CarbonTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarbonTradeApplication.class, args);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("✓ CarbonTrade API is running!");
        System.out.println("✓ Access at: http://localhost:8081");
        System.out.println("✓ Test with: curl http://localhost:8081/api/market/price");
        System.out.println("=".repeat(50) + "\n");
    }
}