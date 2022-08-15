package com.pcy.pmp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class PointMgtPracticeApplication {
    public static void main(String[] args) {
        log.info("application arguments : " + String.join(",", args));
        SpringApplication.run(PointMgtPracticeApplication.class, args);
    }
}
