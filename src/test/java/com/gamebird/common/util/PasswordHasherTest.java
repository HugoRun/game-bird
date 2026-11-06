package com.gamebird.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PasswordHasher 的行为门禁：兼容校验、透明升级与哈希不可逆。
 */
class PasswordHasherTest {

    @Test
    @DisplayName("明文口令可被校验通过（存量兼容路径）")
    void matchesPlaintext() {
        assertTrue(PasswordHasher.matches("test123", "test123"));
        assertFalse(PasswordHasher.matches("test123", "test124"));
    }

    @Test
    @DisplayName("BCrypt 哈希可被校验通过（新账号路径）")
    void matchesHash() {
        String hash = PasswordHasher.hash("test123");
        assertTrue(PasswordHasher.isHashed(hash));
        assertTrue(PasswordHasher.matches("test123", hash));
        assertFalse(PasswordHasher.matches("wrong", hash));
    }

    @Test
    @DisplayName("同一明文两次哈希结果不同（BCrypt 随机盐）")
    void hashIsSalted() {
        assertNotEquals(PasswordHasher.hash("same"), PasswordHasher.hash("same"));
    }

    @Test
    @DisplayName("空值与非法哈希一律不通过，不抛异常")
    void nullAndIllegalInput() {
        assertFalse(PasswordHasher.matches(null, "x"));
        assertFalse(PasswordHasher.matches("", "x"));
        assertFalse(PasswordHasher.matches("x", null));
        assertFalse(PasswordHasher.matches("x", ""));
        assertFalse(PasswordHasher.matches("x", "$2a$10$not-a-valid-hash"));
    }

    @Test
    @DisplayName("透明升级闭环：明文验证通过 → 升级为哈希 → 仍验证通过")
    void transparentUpgradeLoop() {
        String legacy = "old-pass";
        assertTrue(PasswordHasher.matches("old-pass", legacy));
        String upgraded = PasswordHasher.hash("old-pass");
        assertTrue(PasswordHasher.isHashed(upgraded));
        assertTrue(PasswordHasher.matches("old-pass", upgraded));
        assertFalse(PasswordHasher.isHashed(legacy));
    }
}
