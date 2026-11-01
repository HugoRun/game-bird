package com.gamebird.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 口令哈希工具：BCrypt 加盐哈希 + 存量明文的兼容校验与透明升级。
 *
 * <p>历史包袱：account.password 长期为明文存储（README 已知问题 #1）。
 * 直接批量迁移需要停机改库，因此采用「兼容校验 + 透明升级」策略：
 * <ul>
 *   <li>校验时若存量口令是 {@code $2...} BCrypt 格式则走 {@link BCryptPasswordEncoder#matches}；</li>
 *   <li>否则做常量时间明文比较（原逻辑），<b>验证通过后由调用方立即改写为哈希</b>，
 *       让存量账号在各自下一次登录时无感完成迁移；</li>
 *   <li>新注册账号直接存哈希。</li>
 * </ul>
 */
public final class PasswordHasher {

    /** BCrypt 强度 10（默认），单次校验约几十毫秒，可接受。 */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordHasher() {
    }

    /** 判断存量口令是否已是 BCrypt 哈希。 */
    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith("$2");
    }

    /** 哈希一个明文口令。 */
    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验口令：兼容 BCrypt 哈希与存量明文。
     *
     * @param input  用户提交的明文口令
     * @param stored 数据库存储（哈希或明文）
     * @return 是否一致
     */
    public static boolean matches(String input, String stored) {
        if (input == null || input.isEmpty() || stored == null || stored.isEmpty()) {
            return false;
        }
        if (isHashed(stored)) {
            try {
                return ENCODER.matches(input, stored);
            } catch (IllegalArgumentException e) {
                // 存量串形似哈希但非法，按不匹配处理
                return false;
            }
        }
        // 存量明文：常量时间比较，避免时序侧信道
        return MessageDigest.isEqual(
                input.getBytes(StandardCharsets.UTF_8),
                stored.getBytes(StandardCharsets.UTF_8));
    }
}
