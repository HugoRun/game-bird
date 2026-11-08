package com.gamebird.game.bird.controller.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.controller.common.BaseController;
import com.gamebird.game.bird.model.dbo.Board;
import com.gamebird.game.bird.service.BoardService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 公告相关路由。
 *
 * <p>原项目空壳控制器，路由 404。公告数据来自新建的 {@code board} 表，
 * 列出全部公告（按 id 倒序）。
 */
@Controller
@RequestMapping("/board")
public class BoardController extends BaseController {

    @Resource
    public BoardService boardService;

    /** 公告列表：列出全部公告（按 id 倒序）。 */
    @GetMapping(value = {"", "/list"})
    public String list(HttpSession session, Model model) {
        LambdaQueryWrapper<Board> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Board::getId);
        List<Board> boards = boardService.list(queryWrapper);
        model.addAttribute("boards", boards);
        model.addAttribute("now", CommonUtils.nowTime());
        return "board/list";
    }
}
