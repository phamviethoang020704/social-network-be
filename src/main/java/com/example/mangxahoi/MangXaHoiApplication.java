package com.example.mangxahoi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MangXaHoiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MangXaHoiApplication.class, args);
    }

}
