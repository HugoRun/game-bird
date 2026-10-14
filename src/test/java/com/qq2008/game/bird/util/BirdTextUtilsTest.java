package com.qq2008.game.bird.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link BirdTextUtils} 展示格式单元测试。
 *
 * <p>★ 本测试的作用是「锁死格式」，防止将来有人改回区间拼接或改成整数百分比。
 * 纯重构 RT-6b 的代码改动已由代码走查证明，本测试是唯一的自动化回归防线。
 */
class BirdTextUtilsTest {

    /** 成长值按百分比输出两位小数。 */
    @Test
    void 成长值按百分比输出两位小数() {
        assertEquals("10.00%", BirdTextUtils.growText((short) 1000));   // 捕捉流程实际写入值
        assertEquals("2.50%", BirdTextUtils.growText((short) 250));
        assertEquals("0.00%", BirdTextUtils.growText((short) 0));
    }

    /** 克制值与成长值使用同一展示格式。 */
    @Test
    void 克制值同上() {
        assertEquals("0.00%", BirdTextUtils.koText((short) 0));           // 捕捉流程实际写入值
        assertEquals("10.00%", BirdTextUtils.koText((short) 1000));
    }

    /** null 输入不得抛 NPE。 */
    @Test
    void null安全() {
        assertEquals("0.00%", BirdTextUtils.growText(null));
        assertEquals("0.00%", BirdTextUtils.koText(null));
    }

    /** 负数归零且不显示负号。 */
    @Test
    void 负数归零不显示负号() {
        assertEquals("0.00%", BirdTextUtils.growText(-100));
    }

    /** ★ 捏造值不得复活：这是 RT-6b 的核心验收点。 */
    @Test
    void 输出不含区间与捏造样例() {
        String g = BirdTextUtils.growText((short) 278);
        assertEquals("2.78%", g);
        assertFalse(g.contains("~"), "不得输出区间");
        assertFalse(g.contains("("), "不得输出捏造的括号样例");
    }
}