package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
// role 是 MySQL 保留字。MP 生成 SQL 时会自动为表名加反引号，
// 因此这里写 "role" 即可，不要自行加反引号（会破坏 MP 的表名解析）。
// 该映射已实测：roleService.getById(...) 可正常查出角色。
@TableName("role")
@Getter
@Setter
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色Id
     */
    @TableId("role_id")
    private String roleId;

    /**
     * 所属用户Id
     */
    private String userId;

    /**
     * 用户名
     */
    private String roleName;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 当前经验值
     */
    private Long exp;

    /**
     * 总经验值
     */
    private Long totalExp;

    /**
     * VIP等级
     */
    private Short vip;

    /**
     * VIP经验
     */
    private Integer vipExp;

    /**
     * 总VIP经验值
     */
    private Integer totalVipExp;

    /**
     * 性别,0保密/1男/2女
     */
    private Byte sex;

    /**
     * 当前所在场景
     */
    private Short fieldId;

    /**
     * 小鸟数量
     */
    private Short birdNum;

    /**
     * 金钱数量
     */
    private Long coin;

    /**
     * 银行储存金钱数量
     */
    private Long bankCoin;

    /**
     * 钻石数量
     */
    private Integer diamond;

    /**
     * 爱心值数量
     */
    private Long lovePoint;

    /**
     * 当前装备称号Id
     */
    private Integer titleId;

    /**
     * 头像地址
     */
    private String avatarUrl;

    /**
     * 婚姻类型,0无/1喜鹊之恋/2鸳鸯之恋/3比翼鸟之恋/4天使之恋
     */
    private Byte marriageType;

    /**
     * 个性签名
     */
    private String dynamic;

    /**
     * 注册时间戳
     */
    private Integer regTime;

    /**
     * 最后一次登录时间戳
     */
    private Integer lastLoginTime;
}
