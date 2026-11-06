package com.gamebird.game.bird.model.vo;

import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.BaseNest;
import com.gamebird.game.bird.model.dbo.BaseTrap;
import com.gamebird.game.bird.model.dbo.RoleBirth;

/**
 * 巢穴VO
 */
public class RoleBirthVO extends RoleBirth {

    // 当前使用巢穴
    private BaseNest useNest = null;
    // 当前孵化的小鸟配置
    private BaseBird baseBird = null;

    public BaseNest getUseNest() {
        return useNest;
    }

    public void setUseNest(BaseNest useNest) {
        this.useNest = useNest;
    }

    public BaseBird getBaseBird() {
        return baseBird;
    }

    public void setBaseBird(BaseBird baseBird) {
        this.baseBird = baseBird;
    }
}
