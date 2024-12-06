package com.qq2008.game.bird.model.vo;

import com.qq2008.game.bird.model.dbo.BaseBird;
import com.qq2008.game.bird.model.dbo.BaseTrain;
import com.qq2008.game.bird.model.dbo.RoleTrain;

/***
 * 训练场VO
 */
public class RoleTrainVO extends RoleTrain {

    // 当前使用训练场
    private BaseTrain useTrain = null;
    // 训练中的小鸟
    private BaseBird baseBird = null;

    public BaseTrain getUseTrain() {
        return useTrain;
    }

    public void setUseTrain(BaseTrain useTrain) {
        this.useTrain = useTrain;
    }

    public BaseBird getBaseBird() {
        return baseBird;
    }

    public void setBaseBird(BaseBird baseBird) {
        this.baseBird = baseBird;
    }
}
