package com.gamebird.game.bird.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.gamebird.game.bird.mapper.FriendMapper;
import com.gamebird.game.bird.model.dbo.Friend;
import com.gamebird.game.bird.service.FriendService;

/**
 * <p>
 * 好友表 服务实现类
 * </p>
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

}
