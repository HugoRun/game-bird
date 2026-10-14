package com.qq2008.game.bird.util;

import java.nio.file.Paths;
import java.util.Collections;

import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;

/**
 * 数据库代码生成器
 */
public class CodeGen {

    public static void main(String[] args) {
        // DB配置。【RT-6 安全项】user/password 原为明文硬编码 root/root 并进 git 仓库。
        // 改为读环境变量，变量名与 application.properties 保持一致（DB_USERNAME / DB_PASSWORD），
        // 便于统一在 docker/.env 里配置。
        // 【注意】这是 main 方法，不读 application.properties（那需要 Spring 上下文），
        // 只能用 System.getenv。默认值仅保证本地开发免配置可用，
        // 共享 / CI 环境必须显式提供环境变量。
        String JDBC_URL = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/game_bird?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false");
        String DB_USER = System.getenv().getOrDefault("DB_USERNAME", "root");
        String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "root");
        // 生成路径配置
        String CODE_PATH_PROJECT = Paths.get(System.getProperty("user.dir")) + "/src/main/java";
        String CODE_PATH_TEST = Paths.get(System.getProperty("user.dir")) + "/code-gen";
        String MAPPER_XML_PATH =  Paths.get(System.getProperty("user.dir")) + "/src/main/resources/mapper";

        //
        FastAutoGenerator
                // 设置db链接配置
                .create(JDBC_URL, DB_USER, DB_PASSWORD)
                // 全局配置
                .globalConfig(builder -> builder
                        // 作者
                        .author("HugoRun")
                        // 生成目录
                        .outputDir(CODE_PATH_PROJECT).commentDate("yyyy-MM-dd")).packageConfig(builder -> builder
                        // 父包路径
                        .parent("com.qq2008.game.bird")
                        // 实体包
                        .entity("model.dbo")
                        // mapper包
                        .mapper("mapper")
                        // .xml("mapper.xml")
                        // service包
                        .service("service")
                        // service实现包
                        .serviceImpl("service.impl")
                        // 指定xml文件生成未知
                        .pathInfo(Collections.singletonMap(OutputFile.xml, MAPPER_XML_PATH)))
                // 策略配置
                .strategyConfig(builder -> builder
                                // 过滤表前缀
                                // .addTablePrefix("game_bird_")
                                // 过滤要生成的表名
                                // .addInclude("game_bird_shop")
                                // 实体类生成器
                                .entityBuilder()
                                //
                                .enableChainModel()
                                // mapper生成器
                                .mapperBuilder()
                                // 开启mapper注解
                                // .enableMapperAnnotation()
                                // 启用xml文件中的BaseResultMap 生成
                                // .enableBaseResultMap()
                                // 启用xml文件中的BaseResultMap 生成
                                // .enableBaseColumnList()
                                // 允许覆盖已经生成的文件
                                .enableFileOverride()
                                // controller生成器
                                .controllerBuilder()
                                // 禁止生成
                                .disable()
                                // Service生成器
                                .serviceBuilder()
                                // 禁止生成
                                .disable()
                        // 允许使用LomBok插件
                        // .enableLombok()
                )
                // 使用模板引擎
                .templateEngine(new FreemarkerTemplateEngine())
                // 执行生成
                .execute();
    }
}
