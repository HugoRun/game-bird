package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.FightLogMapper;
import com.gamebird.game.bird.model.dbo.FightLog;
import com.gamebird.game.bird.service.FightLogService;

/**
 * <p>
 * 战斗记录表 服务实现类
 * </p>
 *
 * @author game-bird
 * @since 2026-10-14
 */
@Service
public class FightLogServiceImpl extends ServiceImpl<FightLogMapper, FightLog> implements FightLogService {

}
