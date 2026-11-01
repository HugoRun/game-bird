package com.gamebird.game.bird.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.model.dbo.LogTree;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 游戏事件日志（log_tree）。
 *
 * <p>把「收网捕获 / 孵化诞生 / 战斗胜负」等关键事件写入 log_tree，作为主页两个
 * 面板与动态页的统一数据源：按 role_id 过滤即「树林记录」（个人动态），
 * 不过滤即「世界动态」（全服事件流）。此前该表无任何写入方，两个面板也一直 hidden。
 *
 * <p>事件写入失败不抛出——日志属于旁路，不能让一次成功的收网因为日志写失败
 * 而给玩家报错。
 */
@Service
public class GameEventService {

    /** 动态页每页条数。 */
    public static final int FEED_PAGE_SIZE = 10;

    @Resource
    public LogTreeService logTreeService;

    /**
     * 记录一条角色事件。
     *
     * @param roleId  角色 id
     * @param message 事件文案（已是最终展示文本，直接入库）
     */
    public void record(String roleId, String message) {
        try {
            LogTree log = new LogTree();
            log.setId(0L);
            log.setRoleId(roleId);
            log.setMessage(message);
            log.setTime(CommonUtils.nowTime());
            logTreeService.save(log);
        } catch (Exception ignored) {
            // 日志旁路失败不影响主流程
        }
    }

    /** 个人的树林记录（按时间倒序分页）。 */
    public Page<LogTree> pageByRole(String roleId, int pageNo) {
        LambdaQueryWrapper<LogTree> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LogTree::getRoleId, roleId);
        queryWrapper.orderByDesc(LogTree::getId);
        Page<LogTree> page = new Page<>();
        page.setCurrent(Math.max(pageNo, 1));
        page.setSize(FEED_PAGE_SIZE);
        return logTreeService.page(page, queryWrapper);
    }

    /** 全服世界动态（不过滤角色，按时间倒序取前几条）。 */
    public List<LogTree> latestGlobal(int limit) {
        LambdaQueryWrapper<LogTree> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(LogTree::getId);
        queryWrapper.last("LIMIT " + Math.max(1, limit));
        return logTreeService.list(queryWrapper);
    }
}
