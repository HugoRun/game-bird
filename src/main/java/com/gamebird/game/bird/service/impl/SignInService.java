package com.gamebird.game.bird.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamebird.common.util.CommonUtils;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.dbo.SignLog;
import com.gamebird.game.bird.service.RoleService;
import com.gamebird.game.bird.service.SignLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 每日签到。
 *
 * <p>主页「福利:签到」此前是置灰的占位。规则（可玩基线）：
 * 奖励金币 = 100 × 连签天数（封顶 700），连签第 3 天赠 5 钻、第 7 天赠 20 钻；
 * 断签从头计。签到日以东八区 yyyyMMdd 为准，唯一键防重复。
 */
@Service
public class SignInService {

    /** 东八区日期格式（与 DB 唯一键一致的字符串）。 */
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    /** 单日基础金币奖励。 */
    public static final long BASE_COIN = 100;
    /** 连签金币封顶天数。 */
    public static final int COIN_CAP_DAYS = 7;
    /** 第 3 / 7 天的钻石奖励。 */
    public static final int DIAMOND_DAY3 = 5;
    public static final int DIAMOND_DAY7 = 20;

    @Resource
    public SignLogService signLogService;

    @Resource
    public RoleService roleService;

    /** 今天是否已签到。 */
    public boolean isSignedToday(String roleId) {
        return getByDay(roleId, today()) != null;
    }

    /** 取角色最近一次签到（计算连签用）。 */
    public SignLog latest(String roleId) {
        LambdaQueryWrapper<SignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignLog::getRoleId, roleId);
        wrapper.orderByDesc(SignLog::getSignDate);
        wrapper.last("LIMIT 1");
        return signLogService.getOne(wrapper);
    }

    /**
     * 执行签到。
     *
     * @return 结果文案（含奖励明细）；已签到过则返回失败提示
     */
    @Transactional(rollbackFor = Exception.class)
    public String sign(Role role) {
        String today = today();
        if (getByDay(role.getRoleId(), today) != null) {
            return null; // 已签到，由调用方提示
        }
        // 连签判定：昨天签过则 +1，否则重置为 1
        SignLog latest = latest(role.getRoleId());
        String yesterday = LocalDate.now(ZONE).minusDays(1).format(DAY_FORMAT);
        int continuous = (latest != null && yesterday.equals(latest.getSignDate()))
                ? (latest.getContinuous() == null ? 1 : latest.getContinuous() + 1)
                : 1;
        long coin = BASE_COIN * Math.min(continuous, COIN_CAP_DAYS);
        int diamond = continuous == 3 ? DIAMOND_DAY3 : (continuous == 7 ? DIAMOND_DAY7 : 0);
        // 落库
        SignLog log = new SignLog();
        log.setId(0);
        log.setRoleId(role.getRoleId());
        log.setSignDate(today);
        log.setContinuous(continuous);
        log.setRewardCoin(coin);
        log.setRewardDiamond(diamond);
        log.setCreateTime(CommonUtils.nowTime());
        signLogService.save(log);
        // 发奖（改内存后统一落库）
        role.setCoin((role.getCoin() == null ? 0L : role.getCoin()) + coin);
        if (diamond > 0) {
            role.setDiamond((role.getDiamond() == null ? 0 : role.getDiamond()) + diamond);
        }
        roleService.updateById(role);
        StringBuilder text = new StringBuilder();
        text.append("签到成功！连签").append(continuous).append("天，获得金币").append(coin);
        if (diamond > 0) {
            text.append("、钻石").append(diamond);
        }
        text.append("。");
        return text.toString();
    }

    /** 角色今天的签到记录。 */
    public SignLog getByDay(String roleId, String day) {
        LambdaQueryWrapper<SignLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignLog::getRoleId, roleId);
        wrapper.eq(SignLog::getSignDate, day);
        return signLogService.getOne(wrapper);
    }

    private String today() {
        return LocalDate.now(ZONE).format(DAY_FORMAT);
    }
}
