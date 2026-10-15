package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.gamebird.game.bird.mapper.BoardMapper;
import com.gamebird.game.bird.model.dbo.Board;
import com.gamebird.game.bird.service.BoardService;

/**
 * <p>
 * 公告表 服务实现类
 * </p>
 */
@Service
public class BoardServiceImpl extends ServiceImpl<BoardMapper, Board> implements BoardService {

}
