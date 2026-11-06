package com.gamebird.game.bird.model.vo;

/**
 * 包裹使用类操作的结果。
 *
 * <p>{@code text} 是直接展示给玩家的最终文案（成功或失败原因），
 * {@code redirectPath} 非空时表示本操作不渲染消息页而是跳转到指定页面
 * （如改名卡跳改名页、玫瑰跳教堂页）。
 */
public record UseResult(boolean success, String text, String redirectPath) {

    /** 成功且渲染消息页。 */
    public static UseResult ok(String text) {
        return new UseResult(true, text, null);
    }

    /** 成功且跳转。 */
    public static UseResult redirectTo(String path) {
        return new UseResult(true, "", path);
    }

    /** 失败且渲染消息页。 */
    public static UseResult fail(String text) {
        return new UseResult(false, text, null);
    }
}
