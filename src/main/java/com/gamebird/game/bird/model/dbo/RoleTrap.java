package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 陷阱数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_trap")
@Getter
@Setter
public class RoleTrap implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 陷阱唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 顺序Id
     */
    private Integer idx;

    /**
     * 使用诱饵Id
     */
    private Integer baitId;

    /**
     * 捕捉小鸟配置Id
     */
    private Integer catchBirdId;

    /**
     * 捕捉小鸟性别, 0中性/1雄性/2雌性
     */
    private Byte catchBirdSex;

    /**
     * 捕捉小鸟重量
     */
    private Long catchBirdWeight;

    /**
     * 捕捉小鸟星级
     */
    private Byte catchBirdStar;

    /**
     * 开始捕捉时间戳
     */
    private Integer catchTime;

    /**
     * 捕捉结束时间戳
     */
    private Integer catchEndTime;

    /**
     * 使用陷阱Id
     */
    private Integer useTrapId;

    /**
     * 使用陷阱剩余次数
     */
    private Integer useTrapNum;
}
