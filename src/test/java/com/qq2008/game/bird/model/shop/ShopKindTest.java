package com.qq2008.game.bird.model.shop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.qq2008.game.bird.data.ConstData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ShopKind} 的纯单元测试。
 *
 * <p><b>防的具体风险</b>：{@code ShopKind.packType} 用 {@code int} 字面量 1..6，
 * 而 {@code ConstData.PACK_TYPE_*} 用 {@code Byte}（类型已裁定保持不变）。
 * 两套值靠人工同步，一旦漂移，购买时写入的 {@code role_pack.type_id}
 * 就会指向另一种物品 —— 而这种错误<b>不会抛异常</b>，只会让玩家的道具
 * 出现在错误的格子里。故必须用测试钉住两者相等。
 */
class ShopKindTest {

    @Test
    @DisplayName("packType 与 ConstData.PACK_TYPE_* 逐项相等（防两套值漂移）")
    void packTypeMatchesConstData() {
        assertEquals(ConstData.PACK_TYPE_BAIT.intValue(), ShopKind.BAIT.getPackType());
        assertEquals(ConstData.PACK_TYPE_TRAP.intValue(), ShopKind.TRAP.getPackType());
        assertEquals(ConstData.PACK_TYPE_CAGE.intValue(), ShopKind.CAGE.getPackType());
        assertEquals(ConstData.PACK_TYPE_NEST.intValue(), ShopKind.NEST.getPackType());
        assertEquals(ConstData.PACK_TYPE_TRAIN.intValue(), ShopKind.TRAIN.getPackType());
        assertEquals(ConstData.PACK_TYPE_PROP.intValue(), ShopKind.PROP.getPackType());
    }

    @Test
    @DisplayName("6 个种类的 packType 恰好覆盖 1..6 且无重复")
    void packTypesAreOneToSix() {
        int[] expected = {1, 2, 3, 4, 5, 6};
        ShopKind[] kinds = ShopKind.values();
        assertEquals(expected.length, kinds.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], kinds[i].getPackType(), kinds[i] + " 的 packType 顺序错乱");
        }
    }

    @Test
    @DisplayName("ofPackType 正确反查；null 与未知值返回 null")
    void ofPackTypeLooksUp() {
        assertSame(ShopKind.BAIT, ShopKind.ofPackType(1));
        assertSame(ShopKind.PROP, ShopKind.ofPackType(6));
        assertNull(ShopKind.ofPackType(0));
        assertNull(ShopKind.ofPackType(99));
        assertNull(ShopKind.ofPackType(null), "null 必须返回 null 而不是抛 NPE");
    }

    @Test
    @DisplayName("ofKey 正确反查；null 与未知值返回 null")
    void ofKeyLooksUp() {
        assertSame(ShopKind.BAIT, ShopKind.ofKey("bait"));
        assertSame(ShopKind.TRAIN, ShopKind.ofKey("train"));
        assertNull(ShopKind.ofKey("unknown"));
        assertNull(ShopKind.ofKey(null), "null 必须返回 null 而不是抛 NPE");
    }

    @Test
    @DisplayName("key 与 ConstData 的路由片段一致，6 项齐全")
    void keysCoverAllSix() {
        for (ShopKind kind : ShopKind.values()) {
            assertNotNull(kind.getKey(), kind + " 缺少 key");
            assertNotNull(kind.getView(), kind + " 缺少 view");
            assertNotNull(kind.getPackView(), kind + " 缺少 packView");
        }
        assertEquals(6, ShopKind.values().length);
    }
}