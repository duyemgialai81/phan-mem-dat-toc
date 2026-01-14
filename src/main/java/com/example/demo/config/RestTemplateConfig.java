package com.example.demo.config;

import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;


@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Sử dụng ClientHttpRequestFactorySettings để tránh lỗi Deprecated
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(60000)) // Đợi kết nối 60s
                .withReadTimeout(Duration.ofSeconds(60000));    // Đợi AI xử lý 60s

        return builder
                .requestFactorySettings(settings)
                .build();
    }
}
