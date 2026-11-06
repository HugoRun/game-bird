package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.LogTreeMapper;
import com.gamebird.game.bird.model.dbo.LogTree;
import com.gamebird.game.bird.service.LogTreeService;

/**
 * <p>
 * 树林日志记录表 服务实现类
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@Service
public class LogTreeServiceImpl extends ServiceImpl<LogTreeMapper, LogTree> implements LogTreeService {

}
