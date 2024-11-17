package com.qq2008.game.bird.model.vo;

import com.qq2008.game.bird.model.dbo.BaseNest;
import com.qq2008.game.bird.model.dbo.BaseTrap;
import com.qq2008.game.bird.model.dbo.RoleBirth;

/***
 * 巢穴VO
 */
public class RoleBirthVO extends RoleBirth {

    // 当前使用巢穴
    private BaseNest useNest = null;
    // 当前孵化的小鸟配置
    private BaseTrap baseBird = null;

    public BaseNest getUseNest() {
        return useNest;
    }

    public void setUseNest(BaseNest useNest) {
        this.useNest = useNest;
    }

    public BaseTrap getBaseBird() {
        return baseBird;
    }

    public void setBaseBird(BaseTrap baseBird) {
        this.baseBird = baseBird;
    }
}
