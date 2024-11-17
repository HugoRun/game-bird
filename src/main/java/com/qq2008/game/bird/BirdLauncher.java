package com.qq2008.game.bird;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.qq2008.game.bird"})
@MapperScan("com.qq2008.game.bird.mapper")
public class BirdLauncher {
    public static void main(String[] args) {
        SpringApplication.run(BirdLauncher.class, args);
    }
}
