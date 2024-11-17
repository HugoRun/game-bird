package com.qq2008.game.bird.model.vo;

import com.qq2008.game.bird.model.dbo.BaseBait;
import com.qq2008.game.bird.model.dbo.BaseBird;
import com.qq2008.game.bird.model.dbo.BaseTrap;
import com.qq2008.game.bird.model.dbo.RoleTrap;

/***
 * 陷阱VO
 */
public class RoleTrapVO extends RoleTrap {

    // 当前使用陷阱
    private BaseTrap useTrap = null;
    // 当前捕捉小鸟
    private BaseBird catchBird = null;
    // 当前使用的诱饵
    private BaseBait usrBait = null;

    public BaseTrap getUseTrap() {
        return useTrap;
    }

    public void setUseTrap(BaseTrap useTrap) {
        this.useTrap = useTrap;
    }

    public BaseBird getCatchBird() {
        return catchBird;
    }

    public void setCatchBird(BaseBird catchBird) {
        this.catchBird = catchBird;
    }

    public BaseBait getUsrBait() {
        return usrBait;
    }

    public void setUsrBait(BaseBait usrBait) {
        this.usrBait = usrBait;
    }
}
