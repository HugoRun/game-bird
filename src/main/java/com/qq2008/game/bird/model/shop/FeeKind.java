package com.qq2008.game.bird.model.shop;

import com.qq2008.common.entity.BizException;
import com.qq2008.game.bird.data.ConstData;
import com.qq2008.game.bird.model.dbo.Role;

/**
 * 三种购买货币。
 *
 * <p><b>为什么必须用枚举取代 3 个独立 if</b>（实测 {@code ShopController} 原文）：
 * 现状是 {@code if (COIN) {...} if (DIAMOND) {...} if (LOVE_POINT) {...}} 三段
 * <b>彼此独立、不是 else if</b>，且每段扣费校验通过后都<b>不 return</b>，直接往下走发货逻辑。
 * 现状之所以没出事，纯粹是因为前置的 {@code isFeeTypeError()} 保证 feeType 必属三者之一。
 * 一旦 {@code base_*} 表新增第 4 种 feeType、或 {@code isFeeTypeError()} 被误改，
 * 三个分支都不进 → <b>道具白送且不扣任何货币</b>。这是典型的静默漏扣费隐患。
 *
 * <p>本枚举用穷举分派取代 3 个 if：匹配不到即抛
 * {@link BizException#itemNotOnSale()}，其文案正是现状的「没有该商品哦。」，
 * 保证「非法 feeType」的用户可见行为与重构前完全一致；
 * 而新增币种时若忘记加 case，会立即暴露而不是悄悄白送。
 *
 * <p><b>文案红线</b>：3 条不足文案逐字保留 ——
 * 「您的金钱不足哦(x/y)。」「您的钻石不足(x/y)。」「您的爱心值不足(x/y)。」
 * 它们直接展示给玩家，且已被购买流程测试逐字断言，<b>一字不许改</b>。
 *
 * <p><b>类型红线</b>：{@code feeType} 保持 {@code Byte}，已裁定不改 {@code ConstData}
 * 为 {@code int}（改 {@code int} 会让既有 7 处 {@code Objects.equals} 静默失效）。
 * 故下方用 {@code FeeKind.COIN.feeType.equals(feeType)} 做值比较 ——
 * {@code Byte.equals(Byte)} 是值比较，且常量在前不会 NPE。
 */
public enum FeeKind {

    /** 金币。 */
    COIN(ConstData.FEE_TYPE_COIN, "金币") {
        @Override
        public long balance(Role role) {
            return role.getCoin();
        }

        @Override
        public void deduct(Role role, long fee) {
            role.setCoin(role.getCoin() - fee);
        }

        @Override
        public String notEnoughText(Role role, long need) {
            return "您的金钱不足哦(" + role.getCoin() + "/" + need + ")。";
        }
    },

    /** 钻石。 */
    DIAMOND(ConstData.FEE_TYPE_DIAMOND, "钻石") {
        @Override
        public long balance(Role role) {
            return role.getDiamond();
        }

        @Override
        public void deduct(Role role, long fee) {
            role.setDiamond(role.getDiamond() - (int) fee);
        }

        @Override
        public String notEnoughText(Role role, long need) {
            return "您的钻石不足(" + role.getDiamond() + "/" + need + ")。";
        }
    },

    /** 爱心值。 */
    LOVE_POINT(ConstData.FEE_TYPE_LOVE_POINT, "爱心值") {
        @Override
        public long balance(Role role) {
            return role.getLovePoint();
        }

        @Override
        public void deduct(Role role, long fee) {
            role.setLovePoint(role.getLovePoint() - fee);
        }

        @Override
        public String notEnoughText(Role role, long need) {
            return "您的爱心值不足(" + role.getLovePoint() + "/" + need + ")。";
        }
    };

    /** 支付类型原始值，保持 {@code Byte}。 */
    private final Byte feeType;

    /** 中文名，用于提示语。 */
    private final String label;

    FeeKind(Byte feeType, String label) {
        this.feeType = feeType;
        this.label = label;
    }

    public Byte getFeeType() {
        return feeType;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 取角色在该货币下的余额。
     *
     * @param role 角色
     * @return 余额（统一为 {@code long}，避免钻石的 int 与金币的 long 分身）
     */
    public abstract long balance(Role role);

    /**
     * 从角色身上扣除指定金额。<b>调用方必须先做余额校验。</b>
     *
     * @param role 角色
     * @param fee  金额
     */
    public abstract void deduct(Role role, long fee);

    /**
     * 拼装余额不足的提示文案（逐字红线）。
     *
     * @param role 角色
     * @param need 需要金额
     * @return 提示文案
     */
    public abstract String notEnoughText(Role role, long need);

    /**
     * 按 feeType 分派。
     *
     * <p><b>为什么用 if 链而不用 switch 表达式</b>：本参数是 {@code Byte}（包装类型），
     * 无法像枚举那样被 switch <b>穷尽</b>（编译器会要求 default 分支，且漏加 case 时
     * 静默落到 default）。这里的 if 链是逐值显式比较，加新币种时忘记补分支同样会走到末尾
     * 统一抛异常 —— 行为可控且意图明确。
     *
     * <p><b>注意与 {@code ShopItemAccessors#load} 的枚举分派规则相反</b>：那边对
     * {@link ShopKind}（枚举）分派，Java 编译器能在开发者漏加 case 时直接编译报错，
     * 因此那里刻意<b>不写</b> default（写了反而会掩盖未来的编译期保护）。
     * 两处规则不同是有意为之。
     *
     * @param feeType 支付类型
     * @return 对应的枚举
     * @throws BizException feeType 为 null 或不属于三者之一，文案为「没有该商品哦。」
     */
    public static FeeKind of(Byte feeType) {
        if (FeeKind.COIN.feeType.equals(feeType)) {
            return COIN;
        }
        if (FeeKind.DIAMOND.feeType.equals(feeType)) {
            return DIAMOND;
        }
        if (FeeKind.LOVE_POINT.feeType.equals(feeType)) {
            return LOVE_POINT;
        }
        throw BizException.itemNotOnSale();
    }

    /**
     * 按 feeType 分派，匹配不到时返回 {@code null} 而非抛异常。
     *
     * <p><b>供购买流程使用</b>：调用方需要把「非法 feeType」渲染成
     * {@code common/message} 视图里的红字提示（原实现的行为），
     * 而不是让异常冒泡到 {@code GlobalExceptionHandler} 渲染 {@code error.html}
     * —— 后者对玩家而言是一次可见的行为变更。用返回值而非异常，
     * 也让「非法 feeType」这条分支与「商品不存在」在代码上显式同构。
     *
     * @param feeType 支付类型，可为 null
     * @return 对应的枚举；{@code feeType} 为 null 或不属于三者之一时返回 {@code null}
     */
    public static FeeKind ofOrNull(Byte feeType) {
        if (FeeKind.COIN.feeType.equals(feeType)) {
            return COIN;
        }
        if (FeeKind.DIAMOND.feeType.equals(feeType)) {
            return DIAMOND;
        }
        if (FeeKind.LOVE_POINT.feeType.equals(feeType)) {
            return LOVE_POINT;
        }
        return null;
    }
}