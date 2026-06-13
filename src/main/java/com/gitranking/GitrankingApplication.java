package com.gitranking;

import com.gitranking.service.ScoringProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(ScoringProperties.class)
public class GitrankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitrankingApplication.class, args);
    }

}
