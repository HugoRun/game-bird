package com.gamebird.game.bird;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication(scanBasePackages = {"com.gamebird.game.bird", "com.gamebird.common"})
@MapperScan("com.gamebird.game.bird.mapper")
public class BirdLauncher {
    public static void main(String[] args) {
        SpringApplication.run(BirdLauncher.class, args);
    }
}
