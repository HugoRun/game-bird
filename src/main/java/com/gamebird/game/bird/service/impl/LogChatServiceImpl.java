package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.LogChatMapper;
import com.gamebird.game.bird.model.dbo.LogChat;
import com.gamebird.game.bird.service.LogChatService;

/**
 * <p>
 * 聊天日志记录表 服务实现类
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@Service
public class LogChatServiceImpl extends ServiceImpl<LogChatMapper, LogChat> implements LogChatService {

}
