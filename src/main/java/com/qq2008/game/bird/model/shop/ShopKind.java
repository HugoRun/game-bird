package com.qq2008.game.bird.model.shop;

/**
 * 6 种可购买道具的元数据。
 *
 * <p><b>为什么是枚举而不是接口</b>：6 个 {@code Base*} 实体无公共父类型，
 * 而 {@code BaseBait.lu} 是 {@code Short}、其余 5 个是 {@code Integer}，
 * 二者平级兄弟、无法协变实现同一个 {@code Integer getLu()}，
 * 且已裁定不为 6 个实体新建公共父类或接口（PRD N14）。
 * 因此改用「枚举持有元数据 + 访问器以方法引用注入」：
 * 见 {@link com.qq2008.game.bird.data.ShopItemAccessors}。
 *
 * <p><b>与 {@code ConstData} 的关系</b>：{@code packType} 的值 1..6 与
 * {@code ConstData.PACK_TYPE_*} 相同但类型不同（本枚举用 {@code int} 字面量，
 * 而 {@code ConstData} 的 {@code Byte} 类型已裁定保持不变）。
 * 为防两套值漂移，{@code ShopKindTest} 断言两者相等。
 */
public enum ShopKind {

    /** 诱饵。 */
    BAIT(1, "bait", "诱饵", "shop/bait", "pack/bait"),

    /** 陷阱。 */
    TRAP(2, "trap", "陷阱", "shop/trap", "pack/trap"),

    /** 鸟笼。 */
    CAGE(3, "cage", "鸟笼", "shop/cage", "pack/cage"),

    /** 巢穴。 */
    NEST(4, "nest", "巢穴", "shop/nest", "pack/nest"),

    /** 训练场。 */
    TRAIN(5, "train", "训练场", "shop/train", "pack/train"),

    /** 道具。 */
    PROP(6, "prop", "道具", "shop/prop", "pack/prop");

    /** role_pack.type_id，与 ConstData.PACK_TYPE_* 值相同（1..6）。 */
    private final int packType;

    /** 路由片段，如 {@code bait}，用于拼接 {@code /pack/bait/items}。 */
    private final String key;

    /** 中文名，用于提示语。 */
    private final String label;

    /** 商店列表视图名。 */
    private final String view;

    /** 包裹列表视图名。 */
    private final String packView;

    ShopKind(int packType, String key, String label, String view, String packView) {
        this.packType = packType;
        this.key = key;
        this.label = label;
        this.view = view;
        this.packView = packView;
    }

    public int getPackType() {
        return packType;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getView() {
        return view;
    }

    public String getPackView() {
        return packView;
    }

    /**
     * 按 role_pack.type_id 反查。
     *
     * @param packType 类型 id，可为 null
     * @return 对应的枚举；未匹配时返回 {@code null}（调用方必须处理）
     */
    public static ShopKind ofPackType(Integer packType) {
        if (packType == null) {
            return null;
        }
        for (ShopKind kind : values()) {
            if (kind.packType == packType) {
                return kind;
            }
        }
        return null;
    }

    /**
     * 按路由片段反查。
     *
     * @param key 路由片段，可为 null
     * @return 对应的枚举；未匹配时返回 {@code null}
     */
    public static ShopKind ofKey(String key) {
        if (key == null) {
            return null;
        }
        for (ShopKind kind : values()) {
            if (kind.key.equals(key)) {
                return kind;
            }
        }
        return null;
    }
}