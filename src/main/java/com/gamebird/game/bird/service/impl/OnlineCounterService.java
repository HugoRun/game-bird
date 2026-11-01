package com.gamebird.game.bird.service.impl;

import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.model.dbo.Role;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在线玩家统计。
 *
 * <p>原先 {@code OnlineCounterListener} 只在 session 创建/销毁时打日志，
 * 「谁在玩」导航项因没有数据源而一直置灰。本类维护活跃 session 的注册表，
 * 提供「在线角色列表」；session 销毁时自动注销。
 */
@Service
public class OnlineCounterService {

    /** 活跃 session 注册表（sessionId → session）。 */
    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    /** session 创建时登记。 */
    public void register(HttpSession session) {
        sessions.put(session.getId(), session);
    }

    /** session 销毁时注销。 */
    public void unregister(HttpSession session) {
        sessions.remove(session.getId());
    }

    /** 当前在线 session 数。 */
    public int count() {
        return sessions.size();
    }

    /**
     * 当前在线且已选择角色的列表（用于「谁在玩」页）。
     * 遍历过程中注销的失效 session 会被顺手剔除。
     */
    public List<Role> onlineRoles() {
        List<Role> roles = new ArrayList<>();
        sessions.values().removeIf(session -> {
            try {
                return session.getAttribute(ConstData.SESSION_ROLE_KEY) == null;
            } catch (IllegalStateException e) {
                // session 已失效
                return true;
            }
        });
        for (HttpSession session : sessions.values()) {
            try {
                Object value = session.getAttribute(ConstData.SESSION_ROLE_KEY);
                if (value instanceof Role role) {
                    roles.add(role);
                }
            } catch (IllegalStateException ignored) {
                // session 已失效
            }
        }
        return roles;
    }
}
