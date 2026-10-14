package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.BaseBirdMapper;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.service.BaseBirdService;

/**
 * <p>
 * 小鸟配置表 服务实现类
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@Service
public class BaseBirdServiceImpl extends ServiceImpl<BaseBirdMapper, BaseBird> implements BaseBirdService {

}
