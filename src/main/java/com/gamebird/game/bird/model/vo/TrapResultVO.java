package com.gamebird.game.bird.model.vo;

public class TrapResultVO {

    // 小鸟唯一Id
    int birdId = 0;
    // 小鸟配置Id
    int birdBaseId;
    // 小鸟名称描述，4.56斤的雄松鸡
    String nameText;
    // 星级描述，☆☆☆☆☆
    String starText;
    // 成长描述, 数据源 storage_bird.grow / 100, 形如 10.00%
    String growText;
    // 克制描述, 数据源 storage_bird.ko / 100, 形如 0.00%
    String koText;
    // 最小重量
    int minWeight;
    // 最大重量
    int maxWeight;
    // 增加经验值
    long addExp;
    // 升级所需经验值
    long needExp;
    // 好友祝福次数
    int wishNum;
    // 旧的角色等级
    int oldLevel;
    int newLevel;

    public int getBirdId() {
        return birdId;
    }

    public void setBirdId(int birdId) {
        this.birdId = birdId;
    }

    public int getBirdBaseId() {
        return birdBaseId;
    }

    public void setBirdBaseId(int birdBaseId) {
        this.birdBaseId = birdBaseId;
    }

    public String getNameText() {
        return nameText;
    }

    public void setNameText(String nameText) {
        this.nameText = nameText;
    }

    public String getStarText() {
        return starText;
    }

    public void setStarText(String starText) {
        this.starText = starText;
    }

    public String getGrowText() {
        return growText;
    }

    public void setGrowText(String growText) {
        this.growText = growText;
    }

    public String getKoText() {
        return koText;
    }

    public void setKoText(String koText) {
        this.koText = koText;
    }

    public int getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(int minWeight) {
        this.minWeight = minWeight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    public long getAddExp() {
        return addExp;
    }

    public void setAddExp(long addExp) {
        this.addExp = addExp;
    }

    public long getNeedExp() {
        return needExp;
    }

    public void setNeedExp(long needExp) {
        this.needExp = needExp;
    }

    public int getWishNum() {
        return wishNum;
    }

    public void setWishNum(int wishNum) {
        this.wishNum = wishNum;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public void setOldLevel(int oldLevel) {
        this.oldLevel = oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public void setNewLevel(int newLevel) {
        this.newLevel = newLevel;
    }
}
