package com.qq2008.game.bird.model.vo;

import com.qq2008.game.bird.model.dbo.*;
import com.qq2008.game.bird.model.dbo.*;

public class RolePackVO extends RolePack {

    // 诱饵配置
    public BaseBait baseBait;
    // 鸟笼配置
    public BaseCage baseCage;
    // 巢穴配置
    public BaseNest baseNest;
    // 道具配置
    public BaseProp baseProp;
    // 陷阱配置
    public BaseTrap baseTrap;
    // 训练场配置
    public BaseTrain baseTrain;

    public BaseBait getBaseBait() {
        return baseBait;
    }

    public void setBaseBait(BaseBait baseBait) {
        this.baseBait = baseBait;
    }

    public BaseCage getBaseCage() {
        return baseCage;
    }

    public void setBaseCage(BaseCage baseCage) {
        this.baseCage = baseCage;
    }

    public BaseNest getBaseNest() {
        return baseNest;
    }

    public void setBaseNest(BaseNest baseNest) {
        this.baseNest = baseNest;
    }

    public BaseProp getBaseProp() {
        return baseProp;
    }

    public void setBaseProp(BaseProp baseProp) {
        this.baseProp = baseProp;
    }

    public BaseTrap getBaseTrap() {
        return baseTrap;
    }

    public void setBaseTrap(BaseTrap baseTrap) {
        this.baseTrap = baseTrap;
    }

    public BaseTrain getBaseTrain() {
        return baseTrain;
    }

    public void setBaseTrain(BaseTrain baseTrain) {
        this.baseTrain = baseTrain;
    }
}
