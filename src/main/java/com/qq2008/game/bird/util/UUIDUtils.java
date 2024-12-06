package com.qq2008.game.bird.util;

import java.util.UUID;

/***
 * UUID工具集
 */
public class UUIDUtils {

    /***
     * 生成用户Id
     * @param platform 平台key
     * @return userId
     */
    public static String genUserId(String platform) {
        return platform + "-" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    /***
     * 生成角色Id
     * @return roleId
     */
    public static String genRoleId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
