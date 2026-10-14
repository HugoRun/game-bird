package com.qq2008.game.bird.util;

/**
 * 小鸟属性（成长 / 克制）的展示格式化工具。
 *
 * <p><b>为什么单独成类</b>：展示规则必须全项目唯一。若在 {@code TrapController}
 * 里就地 {@code String.format}，将来第二个展示点必然出现第二种格式。
 *
 * <p><b>格式约定</b>：固定两位小数 + 百分号，<b>只输出点值，不输出区间</b>。
 * 改造前展示的是 {@code "2.78%(0.01~5.00)"} 与 {@code "196%(1~300)"}，
 * 但这两个区间经核实无法从 {@code base_bird} 的任何字段导出（该表 15 个字段中
 * 没有 min/max 列，{@code s} / {@code r} 是 Integer 代号字段、无百分比语义），
 * 属捏造值；且它与代码在十几行前刚写进库的 {@code grow} / {@code ko}
 * 互相矛盾。现改为展示数据库真实值。
 */
public final class BirdTextUtils {

    private BirdTextUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 成长值 → 展示文本。
     *
     * <p>数据源 {@code StorageBird.grow}，{@code sql/game_bird.sql} 中该列注释为
     * 「成长, 显示时/100」，故此处除以 100。1000 → {@code "10.00%"}。
     *
     * @param grow 存储的成长值；可为 null
     * @return 形如 {@code "10.00%"} 的展示文本
     */
    public static String growText(Number grow) {
        return percent(grow);
    }

    /**
     * 克制值 → 展示文本。
     *
     * <p>数据源 {@code StorageBird.ko}。注意：SQL 中 {@code grow} 的注释写了
     * 「显示时/100」，而 {@code ko} 的注释只有「克制」二字，
     * <b>未定义单位语义</b>。此处按与 grow 一致的百分比语义处理（/100）。
     *
     * <p>★ 该假设需产品确认。若确认 {@code ko} 应为绝对值，只改本方法一处。
     *
     * @param ko 存储的克制值；可为 null
     * @return 形如 {@code "0.00%"} 的展示文本
     */
    public static String koText(Number ko) {
        return percent(ko);
    }

    /**
     * 统一百分比格式化：null 安全、负数归零、两位小数。
     *
     * @param raw 原始存储值；可为 null
     * @return 形如 {@code "10.00%"} 的展示文本
     */
    private static String percent(Number raw) {
        if (raw == null) {
            return "0.00%";
        }
        double value = raw.doubleValue() / 100.0;
        if (value < 0) {
            value = 0;
        }
        return String.format("%.2f%%", value);
    }
}