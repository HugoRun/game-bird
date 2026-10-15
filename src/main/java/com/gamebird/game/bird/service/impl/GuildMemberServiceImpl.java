package com.gamebird.game.bird.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.gamebird.game.bird.mapper.GuildMemberMapper;
import com.gamebird.game.bird.model.dbo.GuildMember;
import com.gamebird.game.bird.service.GuildMemberService;

/**
 * <p>
 * 公会成员表 服务实现类
 * </p>
 */
@Service
public class GuildMemberServiceImpl extends ServiceImpl<GuildMemberMapper, GuildMember> implements GuildMemberService {

}
