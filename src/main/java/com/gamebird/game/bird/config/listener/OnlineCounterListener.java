package com.gamebird.game.bird.config.listener;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Session管理监听器
 * 当session被创建或者销毁时做一些处理
 */
@Component
public class OnlineCounterListener implements HttpSessionListener {
    // 日志句柄
    private static final Logger logger = LoggerFactory.getLogger(OnlineCounterListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        logger.info("Session被创建: " + session.getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        logger.info("Session被销毁: " + session.getId());
    }
}
