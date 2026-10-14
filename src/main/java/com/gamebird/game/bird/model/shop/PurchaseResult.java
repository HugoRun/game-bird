package com.gamebird.game.bird.model.shop;

import com.gamebird.game.bird.model.message.MessageLink;
import com.gamebird.game.bird.model.message.MessageVO;

/**
 * 购买结果。<b>只做搬运，不做文案改写</b>。
 *
 * <p>失败路径的文案由 {@link ShopPurchaseService} 用逐字保留的原文构造，
 * 本 record 不追加、不润色、不替换 —— 文案是对玩家可见的业务契约。
 *
 * <p><b>红线</b>：与 {@link ShopItemView} 同理，本 record <b>不得放进 Model</b>。
 * 它只在购买路径内部流转，Controller 取��� {@link #message()} 后塞进 Model。
 *
 * @param ok      是否购买成功
 * @param message 展示给玩家的消息
 */
public record PurchaseResult(boolean ok, MessageVO message) {

    /**
     * 构造失败结果。
     *
     * @param exactText 逐字保留的错误文案
     * @return 失败结果
     */
    public static PurchaseResult fail(String exactText) {
        return new PurchaseResult(false, MessageVO.error(exactText));
    }

    /**
     * 构造成功结果并附加「去包裹看看」链接。
     *
     * @param exactText 成功文案
     * @param link      后续链接
     * @return 成功结果
     */
    public static PurchaseResult success(String exactText, MessageLink link) {
        MessageVO messageVO = MessageVO.success(exactText);
        messageVO.addLink(link);
        return new PurchaseResult(true, messageVO);
    }
}