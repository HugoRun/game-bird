package com.qq2008.game.bird.controller.game;

import com.qq2008.game.bird.controller.common.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/***
 * 排行榜相关路由
 */
@Controller
@RequestMapping("/rank")
public class RankController extends BaseController {
    // 等级排行榜 rankLevel
    // 鲜花排行榜 rankFlower
    // 重量排行榜 rankWeight
    // 亲密排行榜 rankIntimacy
    // 金币排行榜 rankCoin
    // 战斗排行榜 rankWin
}
