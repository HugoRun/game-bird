package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 训练场数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_train")
@Getter
@Setter
public class RoleTrain implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 训练场唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 排序Id
     */
    private Integer idx;

    /**
     * 训练小鸟Id
     */
    private Integer birdId;

    /**
     * 训练小鸟配置Id
     */
    private Integer birdBaseId;

    /**
     * 开始训练时间戳
     */
    private Integer trainTime;

    /**
     * 训练结束时间戳
     */
    private Integer trainEndTime;

    /**
     * 使用训练场Id
     */
    private Integer useTrainId;

    /**
     * 使用训练场过期时间戳
     */
    private Integer useTrainExpireTime;
}
