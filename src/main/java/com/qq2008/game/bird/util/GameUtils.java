package com.qq2008.game.bird.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qq2008.common.util.CommonUtils;

import com.qq2008.game.bird.data.GameConfigGateway;
import com.qq2008.game.bird.data.RandomTextData;
import com.qq2008.game.bird.model.dbo.BaseBird;
import com.qq2008.game.bird.model.dbo.BaseField;
import com.qq2008.game.bird.model.dbo.BaseTitle;
import com.qq2008.game.bird.model.dbo.Role;
import com.qq2008.game.bird.model.vo.ChatRoleVO;

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
        return switch (star) {
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
     * 获取性称号名称
     * @param titleId 称号Id
     * @return 称号名称
     */
    public String getTitleName(int titleId) {
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
        chatRoleVO.setTitleName("小菜鸟");
        return chatRoleVO;
    }
}
