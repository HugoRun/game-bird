package com.gamebird.game.bird.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.gamebird.game.bird.data.ConstData;

/**
 * 游戏开关类配置项（对应 application.properties 的 {@code game.*} 前缀）。
 *
 * <p><b>为什么把开关移出常量类</b>：注册开关原先是
 * {@code ConstData.IS_ALLOW_REGISTER} 这个 public static 字段，任何代码都能随时改它，
 * 且改动在多实例部署下不可靠。移到 Spring 的配置绑定后，开关有唯一来源
 * （配置文件或环境变量），且能在启动时注入、便于测试覆写。
 */
@Component
@ConfigurationProperties(prefix = "game")
public class GameProperties {

    /** 是否允许注册新账号。 */
    private boolean allowRegister = true;

    /** 包裹总容量（U12 新增：原先是 6 个模板里各写死一个 3000）。 */
    private int packTotalCapacity = 3000;

    /** 加速单价：每缩短 1 小时等待消耗的钻石数（不足 1 小时按 1 小时计）。 */
    private int hastenDiamondPerHour = 10;

    /** 训练场自装入起的有效天数，到期后需重新装载（use_train_expire_time）。 */
    private int trainExpireDays = 7;

    /** 战斗冷却秒数，冷却期内不能再次发起挑战。 */
    private int fightCooldownSeconds = 300;

    public boolean isAllowRegister() {
        return allowRegister;
    }

    public void setAllowRegister(boolean allowRegister) {
        this.allowRegister = allowRegister;
    }

    public int getPackTotalCapacity() {
        return packTotalCapacity;
    }

    public void setPackTotalCapacity(int packTotalCapacity) {
        this.packTotalCapacity = packTotalCapacity;
    }

    public int getHastenDiamondPerHour() {
        return hastenDiamondPerHour;
    }

    public void setHastenDiamondPerHour(int hastenDiamondPerHour) {
        this.hastenDiamondPerHour = hastenDiamondPerHour;
    }

    public int getTrainExpireDays() {
        return trainExpireDays;
    }

    public void setTrainExpireDays(int trainExpireDays) {
        this.trainExpireDays = trainExpireDays;
    }

    public int getFightCooldownSeconds() {
        return fightCooldownSeconds;
    }

    public void setFightCooldownSeconds(int fightCooldownSeconds) {
        this.fightCooldownSeconds = fightCooldownSeconds;
    }
}