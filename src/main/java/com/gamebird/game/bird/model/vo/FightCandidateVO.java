package com.gamebird.game.bird.model.vo;

/**
 * 竞技场候选对手视图对象：由 {@code /fight/arena} 随机生成并暂存在
 * session 中，挑战时按序号取用，防止玩家凭参数指定任意对手。
 */
public class FightCandidateVO {

    /** 对手小鸟的 storage_bird id。 */
    private int birdId;
    /** 对手小鸟名（配置名）。 */
    private String birdName;
    /** 对手主人角色名。 */
    private String ownerName;
    /** 对手小鸟等级。 */
    private int level;
    /** 星级文本。 */
    private String starText;
    /** 对手战力估值。 */
    private int power;
    /** 预估我方胜率（15~85）。 */
    private int winRate;
    /** 类型名（敏捷/力量/魔法/幻想）。 */
    private String typeName;

    public int getBirdId() {
        return birdId;
    }

    public void setBirdId(int birdId) {
        this.birdId = birdId;
    }

    public String getBirdName() {
        return birdName;
    }

    public void setBirdName(String birdName) {
        this.birdName = birdName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getStarText() {
        return starText;
    }

    public void setStarText(String starText) {
        this.starText = starText;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getWinRate() {
        return winRate;
    }

    public void setWinRate(int winRate) {
        this.winRate = winRate;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
