package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 训练场数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_train")
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public Integer getBirdId() {
        return birdId;
    }

    public void setBirdId(Integer birdId) {
        this.birdId = birdId;
    }

    public Integer getBirdBaseId() {
        return birdBaseId;
    }

    public void setBirdBaseId(Integer birdBaseId) {
        this.birdBaseId = birdBaseId;
    }

    public Integer getTrainTime() {
        return trainTime;
    }

    public void setTrainTime(Integer trainTime) {
        this.trainTime = trainTime;
    }

    public Integer getTrainEndTime() {
        return trainEndTime;
    }

    public void setTrainEndTime(Integer trainEndTime) {
        this.trainEndTime = trainEndTime;
    }

    public Integer getUseTrainId() {
        return useTrainId;
    }

    public void setUseTrainId(Integer useTrainId) {
        this.useTrainId = useTrainId;
    }

    public Integer getUseTrainExpireTime() {
        return useTrainExpireTime;
    }

    public void setUseTrainExpireTime(Integer useTrainExpireTime) {
        this.useTrainExpireTime = useTrainExpireTime;
    }

    @Override
    public String toString() {
        return "RoleTrain{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", idx = " + idx +
            ", birdId = " + birdId +
            ", birdBaseId = " + birdBaseId +
            ", trainTime = " + trainTime +
            ", trainEndTime = " + trainEndTime +
            ", useTrainId = " + useTrainId +
            ", useTrainExpireTime = " + useTrainExpireTime +
        "}";
    }
}
