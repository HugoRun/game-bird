package com.gamebird.game.bird.service;

import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.vo.TrapResultVO;

/**
 * 捕捉循环核心业务（下饵 / 收网）。
 *
 * <p>失败统一抛 {@code BizException}（文案即玩家提示），成功返回结果对象。
 */
public interface TrapService {

    /**
     * 下饵：抽鸟、掷逃脱骰、写入捕捉状态并扣减诱饵。
     *
     * @param role       当前角色
     * @param trapId     陷阱槽位 id（role_trap.id）
     * @param baitPackId 诱饵包裹记录 id（role_pack.id）
     * @param useSticky  是否勾选消耗一张粘性道具削减逃脱率
     */
    void useBait(Role role, int trapId, int baitPackId, boolean useSticky);

    /**
     * 收网：结算捕获（或逃脱）、产出小鸟、加经验、扣陷阱耐久并清空槽位。
     *
     * @param role   当前角色
     * @param trapId 陷阱槽位 id
     * @return 结算结果（含逃脱标记）
     */
    TrapResultVO over(Role role, int trapId);
}
