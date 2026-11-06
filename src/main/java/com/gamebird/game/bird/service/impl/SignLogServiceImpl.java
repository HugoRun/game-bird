package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.SignLogMapper;
import com.gamebird.game.bird.model.dbo.SignLog;
import com.gamebird.game.bird.service.SignLogService;

/**
 * <p>
 * 每日签到表 服务实现类
 * </p>
 */
@Service
public class SignLogServiceImpl extends ServiceImpl<SignLogMapper, SignLog> implements SignLogService {

}
