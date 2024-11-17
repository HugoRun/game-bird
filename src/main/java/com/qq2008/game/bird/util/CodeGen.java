package com.qq2008.game.bird.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;

/***
 * 数据库代码生成器
 */
public class CodeGen {

    public static void main(String[] args) {
        // DB配置
        String JDBC_URL = "jdbc:mysql://localhost:3306/game_bird?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false";
        String DB_USER = "root";
        String DB_PASSWORD = "";
        // 生成路径配置
        String CODE_PATH_PROJECT = Paths.get(System.getProperty("user.dir")) + "/game-bird/src/main/java";
        String CODE_PATH_TEST = Paths.get(System.getProperty("user.dir")) + "/game-bird/code-gen";

        //
        FastAutoGenerator.create(JDBC_URL, DB_USER, DB_PASSWORD)
                .globalConfig(builder -> builder
                        // 作者
                        .author("HugoRun")
                        // 生成目录
                        .outputDir(CODE_PATH_TEST)
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        // 父包路径
                        .parent("com.qq2008.game.bird")
                        // 实体包
                        .entity("model.dbo")
                        // mapper包
                        .mapper("mapper")
                        .xml("mapper.xml")
                        // service包
                        .service("service")
                        .serviceImpl("service.impl")
                )
                .strategyConfig(builder -> builder
                        // 过滤表前缀
                        // .addTablePrefix("game_bird_")
                        // 过滤要生成的表名
                        // .addInclude("game_bird_shop")
                        //
                        .entityBuilder()
                        // 允许覆盖已经生成的文件
                        .enableFileOverride()
                        // 禁止生成controller
                        .controllerBuilder().disable()
                        // 禁止生成Service
                        // .serviceBuilder().disable()
                        // 允许使用LomBok插件
                        // .enableLombok()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

    }
}
