package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.ActivityMapper;
import com.gamebird.game.bird.model.dbo.Activity;
import com.gamebird.game.bird.service.ActivityService;

/**
 * <p>
 * 活动表 服务实现类
 * </p>
 *
 * @author game-bird
 * @since 2026-10-14
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

}
