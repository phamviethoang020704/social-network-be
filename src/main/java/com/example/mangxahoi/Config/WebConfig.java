package com.example.mangxahoi.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Map URL /uploads/** đến thư mục thật
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///F:/webgoat/images-mangxahoi/");
    }
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
