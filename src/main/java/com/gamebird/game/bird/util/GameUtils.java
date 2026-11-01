package com.gamebird.game.bird.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gamebird.common.util.ArrayUtils;
import com.gamebird.common.util.CommonUtils;

import com.gamebird.game.bird.config.GameConfigGateway;
import com.gamebird.game.bird.data.ConstData;
import com.gamebird.game.bird.data.RandomTextData;
import com.gamebird.game.bird.model.dbo.BaseBird;
import com.gamebird.game.bird.model.dbo.BaseField;
import com.gamebird.game.bird.model.dbo.BaseTitle;
import com.gamebird.game.bird.model.dbo.Role;
import com.gamebird.game.bird.model.vo.ChatRoleVO;

/**
 * 一些公共调用的函数
 */
@Component
public class GameUtils {

    /**
     * 配置读取入口。
     *
     * <p>模板通过 {@code @gameUtils.xxx(...)} 调用本类。原先三个依赖配置的方法是static 的，
     * 内部靠 {@code GameConfigManager.getInstance()} 反查容器；现改为实例方法 + 注入，
     * SpEL 对实例方法同样能解析，模板无需改动。
     */
    @Autowired
    private GameConfigGateway configGateway;

    /**
     * 获取一句随机的格言
     * @return 格言文本
     */
    public static String getRandStr() {
        return ArrayUtils.randElement(RandomTextData.getRandTextArray());
    }

    /**
     * 获取时间差
     * @param timestamp 时间戳
     * @return 显示文本
     */
    public static String getTimeDiff(int timestamp) {
        int nowTime = CommonUtils.nowTime();
        return getTimeDiff(timestamp, nowTime);
    }

    /**
     * 获取时间差
     * @param timestamp 时间戳
     * @param nowTime 戳时间戳
     * @return 显示文本
     */
    public static String getTimeDiff(int timestamp, int nowTime) {
        if (timestamp == nowTime) {
            return "0秒";
        }
        long seconds = timestamp - nowTime > 0 ? timestamp - nowTime : nowTime - timestamp;
        if (seconds < 60) {
            return seconds + "秒";
        }
        if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        }
        if (seconds < 86400) {
            return (seconds / 3600) + "小时";
        }
        return (seconds / 86400) + "天";
    }

    /**
     * 获取花费单位
     * @param feeType 价格类型, 1金钱/2钻石/3爱心值
     * @return 单位
     */
    public static String getFeeTypeName(Byte feeType) {
        if (feeType == 1) {
            return "金币";
        }
        if (feeType == 2) {
            return "钻石";
        }
        if (feeType == 3) {
            return "爱心值";
        }
        return "";
    }

    /**
     * 根据区域Id获取区域名称
     * @param fileId 区域Id
     * @return 区域名称
     */
    public String getFieldName(int fileId) {
        BaseField baseField = configGateway.getBaseField(fileId);
        return baseField == null ? "" : baseField.getName();
    }

    /**
     * 获取可捕捉的小鸟名称集合
     * @param birdIdSet 小鸟Id, ...
     * @return 名称集合
     */
    public String getCatchBirdName(String birdIdSet) {
        List<String> nameList = new ArrayList<>();
        String[] birdIdList = birdIdSet.split(",");
        for (String birdId : birdIdList) {
            BaseBird baseBird = configGateway.getBaseBird(Integer.valueOf(birdId));
            if (baseBird != null) {
                nameList.add(baseBird.getName());
            }
        }
        return StringUtils.join(nameList, ",");
    }

    /**
     * 获取性别名称
     * @param sex 性别Id
     * @return 性别名称
     */
    public static String getSexName(Byte sex) {
        return switch (sex) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "保密";
        };
    }

    /**
     * 获取性别名称
     * @param sex 性别Id
     * @return 性别名称
     */
    public static String getBirdSexName(Byte sex) {
        return switch (sex) {
            case 1 -> "雄性";
            case 2 -> "雌性";
            default -> "中性";
        };
    }

    /**
     * 获取星级文本
     * @param star 星级
     * @return 星级文本
     */
    public static String getStarName(Byte star) {
        if (star == null) {
            return "";
        }
        // 负值是「将逃脱」标记（下饵时掷骰，见 TrapServiceImpl），展示时取绝对值
        byte value = star < 0 ? (byte) -star : star;
        return switch (value) {
            case 0 -> "☆☆☆☆☆";
            case 1 -> "★☆☆☆☆";
            case 2 -> "★★☆☆☆";
            case 3 -> "★★★☆☆";
            case 4 -> "★★★★☆";
            case 5 -> "★★★★★";
            default -> "";
        };
    }

    /**
     * 任务/成就的达成条件说明（任务中心展示，让玩家知道奖励从哪来）。
     *
     * @param taskType 任务类型
     * @return 说明文案
     */
    public static String getTaskTypeDesc(Integer taskType) {
        if (taskType == null) {
            return "";
        }
        return switch (taskType) {
            case 1 -> "每天首次进入游戏自动完成";
            case 2 -> "成功收网一只小鸟后完成";
            case 3 -> "在商店购买任意道具后完成";
            case 4 -> "图鉴每收集满 10/30/60/全部物种各可领一档";
            case 5 -> "首次孵出 5 星小鸟";
            case 6 -> "累计战斗胜利 10 场";
            case 7 -> "角色等级达到 10 级";
            case 8 -> "持有金币满 10 万";
            default -> "";
        };
    }

    /**
     * 任务/成就类型名称（task.task_type：1-3 日常，4-8 成就）。
     *
     * @param taskType 任务类型
     * @return 展示名
     */
    public static String getTaskTypeName(Integer taskType) {
        if (taskType == null) {
            return "未知任务";
        }
        return switch (taskType) {
            case 1 -> "每日登录";
            case 2 -> "捕捉小鸟";
            case 3 -> "购买道具";
            case 4 -> "图鉴收集成就";
            case 5 -> "五星孵蛋成就";
            case 6 -> "战斗达人成就";
            case 7 -> "成长升级成就";
            case 8 -> "富甲一方成就";
            default -> "神秘任务";
        };
    }

    /**
     * 小鸟类型相克倍率（base_bird.type：1敏捷/2力量/3魔法/4幻想）。
     *
     * <p>相克三角：敏捷克魔法、魔法克力量、力量克敏捷（克者 +20%，被克 -15%）；
     * 幻想系与三系互克（双方各 +10%），同类型不克。
     *
     * @param attackerType 进攻方类型
     * @param defenderType 防守方类型
     * @return 战力倍率
     */
    public static double typeCounterMultiplier(Byte attackerType, Byte defenderType) {
        if (attackerType == null || defenderType == null) {
            return 1.0;
        }
        int a = attackerType;
        int d = defenderType;
        if (a == 4 && d != 4) {
            return 1.1;
        }
        if (d == 4 && a != 4) {
            return 1.1;
        }
        if (a == d) {
            return 1.0;
        }
        boolean counter = (a == 1 && d == 3) || (a == 3 && d == 2) || (a == 2 && d == 1);
        return counter ? 1.2 : 0.85;
    }

    /**
     * 获取性称号名称
     * @param titleId 称号Id
     * @return 称号名称
     */
    public String getTitleName(Integer titleId) {
        if (titleId == null || titleId <= 0) {
            // 未设置称号（建角后 titleId 为 null）返回空，而不是抛 500
            return "无";
        }
        BaseTitle title = configGateway.getBaseTitle(titleId);
        if (title == null)
            return "";
        return title.getName();
    }

    /**
     * 构造聊天玩家消息
     * @param role 角色信息
     * @return 聊天玩家消息
     */
    public static ChatRoleVO makeChatRole(Role role) {
        ChatRoleVO chatRoleVO = new ChatRoleVO();
        chatRoleVO.setRoleId(role.getRoleId());
        chatRoleVO.setRoleName(role.getRoleName());
        chatRoleVO.setLevel(role.getLevel());
        chatRoleVO.setVip(0);
        chatRoleVO.setTitleName(ConstData.DEFAULT_NICK_NAME);
        return chatRoleVO;
    }
}
