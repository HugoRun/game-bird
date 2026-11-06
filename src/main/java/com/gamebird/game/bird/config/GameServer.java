package com.gamebird.game.bird.config;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.gamebird.game.bird.model.vo.GameStatusVO;

@Component
public class GameServer {

    // 程序状态（多线程读写：拦截器读、启动监听器写）
    protected static volatile GameStatusVO status = GameStatusVO.NULL;
    // 程序上下文
    protected static volatile ApplicationContext applicationContext;

    /**
     * 获取GameServer单例对象
     * @return GameServer
     * @deprecated 依赖静态 ApplicationContext，无法脱离 Spring 容器使用。
     *             请直接注入 {@code GameServer} bean。
     */
    @Deprecated
    public static GameServer getInstance() {
        return GameServer.getApplicationContext().getBean(GameServer.class);
    }

    /**
     * 获取Springboot上下文
     * @return ApplicationContext
     * @deprecated 同 {@link #getInstance()}，请改为注入 bean。
     */
    @Deprecated
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 设置Springboot上下文
     * @param applicationContext Springboot上下文
     * @deprecated 仅保留给启动流程使用，业务代码不应读取静态上下文。
     */
    @Deprecated
    public static void setApplicationContext(ApplicationContext applicationContext) {
        GameServer.applicationContext = applicationContext;
    }

    /**
     * 设置游戏状态
     * @param status 游戏状态
     */
    public static void setStatus(GameStatusVO status) {
        GameServer.status = status;
    }

    /**
     * 获取游戏状态
     * @return 游戏状态
     */
    public static GameStatusVO getStatus() {
        return status;
    }

    /**
     * 初始化游戏
     */
    public static void init() {
        // 初始化游戏活动
        getInstance().initActivity();
        // 初始化排行榜 todo
        getInstance().initRank();
    }

    /**
     * 初始化活动 todo
     */
    public void initActivity(){

    }

    /**
     * 初始化排行榜 todo
     */
    public void initRank(){
        // 加载等级排行榜
        // 加载财富榜
    }
}
