package com.qq2008.game.bird.model.shop;

import com.qq2008.game.bird.model.dbo.BaseBait;
import com.qq2008.game.bird.model.dbo.BaseCage;
import com.qq2008.game.bird.model.dbo.BaseNest;
import com.qq2008.game.bird.model.dbo.BaseProp;
import com.qq2008.game.bird.model.dbo.BaseTrain;
import com.qq2008.game.bird.model.dbo.BaseTrap;

/**
 * 6 种商店配置项的统一只读投影。
 *
 * <p><b>存在的唯一理由</b>：{@code BaseBait.lu} 是 {@code Short} 而其余 5 个是
 * {@code Integer}（实测），两者平级兄弟、无法协变；而 6 个 dbo 既无公共父类型
 * 也不允许新增接口。因此用 record 做投影，把这个类型差异在边界处吸收成 {@code int}，
 * 6 个 dbo 本身保持零改动。
 *
 * <p><b>红线</b>：本 record <b>绝不能放进 Model 传给模板</b>。SpEL 不认 record 的
 * {@code name()} 访问器（只认 JavaBean 的 {@code getName()}），一旦被模板解析会抛
 * {@code EL1008E}，导致 6 个商店列表页与 {@code fragment/shop.html} 全部 500。
 * 故它只作为购买路径内部的瞬时快照使用。
 *
 * <p><b>关于 isShopping</b>：一并投影，但保持现状语义 —— 原 6 个 buy 方法
 * <b>只校验 feeType，未校验 isShopping</b>（只有列表方法用 {@code is_shopping=1} 过滤）。
 * 本次重构<b>不得新增该校验</b>，否则改变行为。
 *
 * @param kind      道具种类
 * @param id        配置 id
 * @param name      名称
 * @param descText  描述
 * @param feeType   支付类型（{@code Byte}，保持原类型）
 * @param feePrice  单价
 * @param lu        等级门槛，已统一为 {@code int}
 * @param isShopping 是否在售
 * @param birdSet   仅 {@code BaseBait} 非 null，模板 {@code getCatchBirdName} 依赖
 */
public record ShopItemView(
        ShopKind kind,
        Integer id,
        String name,
        String descText,
        Byte feeType,
        Integer feePrice,
        int lu,
        Byte isShopping,
        String birdSet) {

    /**
     * 由 {@link BaseBait} 投影。此处是唯一需要处理 {@code Short} 的地方。
     *
     * @param entity 诱饵配置，可为 null
     * @return 投影结果；入参为 null 时返回 null
     */
    public static ShopItemView of(BaseBait entity) {
        return entity == null ? null : new ShopItemView(
                ShopKind.BAIT, entity.getId(), entity.getName(), entity.getDescText(),
                entity.getFeeType(), entity.getFeePrice(),
                entity.getLu() == null ? 0 : entity.getLu().intValue(),
                entity.getIsShopping(), entity.getBirdSet());
    }

    /**
     * 由 {@link BaseTrap} 投影。
     *
     * @param entity 陷阱配置，可为 null
     * @return 投影结果
     */
    public static ShopItemView of(BaseTrap entity) {
        return entity == null ? null : new ShopItemView(ShopKind.TRAP, entity.getId(), entity.getName(),
                entity.getDescText(), entity.getFeeType(), entity.getFeePrice(),
                entity.getLu() == null ? 0 : entity.getLu(), entity.getIsShopping(), null);
    }

    /**
     * 由 {@link BaseCage} 投影。
     *
     * @param entity 鸟笼配置，可为 null
     * @return 投影结果
     */
    public static ShopItemView of(BaseCage entity) {
        return entity == null ? null : new ShopItemView(ShopKind.CAGE, entity.getId(), entity.getName(),
                entity.getDescText(), entity.getFeeType(), entity.getFeePrice(),
                entity.getLu() == null ? 0 : entity.getLu(), entity.getIsShopping(), null);
    }

    /**
     * 由 {@link BaseNest} 投影。
     *
     * @param entity 巢配置，可为 null
     * @return 投影结果
     */
    public static ShopItemView of(BaseNest entity) {
        return entity == null ? null : new ShopItemView(ShopKind.NEST, entity.getId(), entity.getName(),
                entity.getDescText(), entity.getFeeType(), entity.getFeePrice(),
                entity.getLu() == null ? 0 : entity.getLu(), entity.getIsShopping(), null);
    }

    /**
     * 由 {@link BaseTrain} 投影。
     *
     * @param entity 训练场配置，可为 null
     * @return 投影结果
     */
    public static ShopItemView of(BaseTrain entity) {
        return entity == null ? null : new ShopItemView(ShopKind.TRAIN, entity.getId(), entity.getName(),
                entity.getDescText(), entity.getFeeType(), entity.getFeePrice(),
                entity.getLu() == null ? 0 : entity.getLu(), entity.getIsShopping(), null);
    }

    /**
     * 由 {@link BaseProp} 投影。
     *
     * @param entity 道具配置，可为 null
     * @return 投影结果
     */
    public static ShopItemView of(BaseProp entity) {
        return entity == null ? null : new ShopItemView(ShopKind.PROP, entity.getId(), entity.getName(),
                entity.getDescText(), entity.getFeeType(), entity.getFeePrice(),
                entity.getLu() == null ? 0 : entity.getLu(), entity.getIsShopping(), null);
    }
}