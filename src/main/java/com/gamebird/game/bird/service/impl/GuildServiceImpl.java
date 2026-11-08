package com.gamebird.game.bird.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.gamebird.game.bird.mapper.GuildMapper;
import com.gamebird.game.bird.model.dbo.Guild;
import com.gamebird.game.bird.service.GuildService;

/**
 * <p>
 * 公会表 服务实现类
 * </p>
 */
@Service
public class GuildServiceImpl extends ServiceImpl<GuildMapper, Guild> implements GuildService {

}
