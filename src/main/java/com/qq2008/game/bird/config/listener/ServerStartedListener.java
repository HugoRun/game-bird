package com.qq2008.game.bird.config.listener;

import com.qq2008.game.bird.data.GameConfigManager;
import com.qq2008.game.bird.data.GameServer;
import com.qq2008.game.bird.model.vo.GameStatusVO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

/***
 * Servlet上下文监听器
 * 当服务器初始化完成后或者销毁时做一些处理
 */
@Component
public class ServerStartedListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ServerStartedListener.class);

    /** 配置管理器，直接注入而非经静态方法反查容器。 */
    @Autowired
    private GameConfigManager configManager;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        logger.info("————————————————> 正在启动游戏 <————————————————");
        try {
            GameServer.setStatus(GameStatusVO.LOADING);
            GameServer.setApplicationContext(WebApplicationContextUtils.getWebApplicationContext(event.getServletContext()));
            logger.info("————————————————> 加载游戏配置... <————————————————");
            configManager.init();
            logger.info("————————————————> 游戏配置加载完成!!! <————————————————");
            logger.info("————————————————> 初始化游戏功能... <————————————————");
            GameServer.init();
            logger.info("————————————————> 游戏功能初始化完成!!! <————————————————");
            logger.info("————————————————> 更新游戏状态... <————————————————");
            GameServer.setStatus(GameStatusVO.RUNNING);
            logger.info("————————————————> 游戏启动成功!!! <————————————————");
        } catch (Exception e) {
            GameServer.setStatus(GameStatusVO.NULL);
            logger.info("————————————————> 游戏启动失败!!! <————————————————");
            // 必须打印完整堆栈：原实现写的是 e.getStackTrace()，那只是把堆栈数组取出来丢弃，
            // 什么都不会输出，启动失败时日志里只剩一句「失败原因」。
            logger.error("游戏启动失败", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.info("————————————————> 关闭游戏 <————————————————");
        // T掉所有在线玩家
        // 关闭所有服务
        GameServer.setStatus(GameStatusVO.NULL);
        // 容器关闭时时执行的代码
    }
}