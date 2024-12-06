package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 陷阱数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_trap")
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

    public Integer getBaitId() {
        return baitId;
    }

    public void setBaitId(Integer baitId) {
        this.baitId = baitId;
    }

    public Integer getCatchBirdId() {
        return catchBirdId;
    }

    public void setCatchBirdId(Integer catchBirdId) {
        this.catchBirdId = catchBirdId;
    }

    public Byte getCatchBirdSex() {
        return catchBirdSex;
    }

    public void setCatchBirdSex(Byte catchBirdSex) {
        this.catchBirdSex = catchBirdSex;
    }

    public Long getCatchBirdWeight() {
        return catchBirdWeight;
    }

    public void setCatchBirdWeight(Long catchBirdWeight) {
        this.catchBirdWeight = catchBirdWeight;
    }

    public Byte getCatchBirdStar() {
        return catchBirdStar;
    }

    public void setCatchBirdStar(Byte catchBirdStar) {
        this.catchBirdStar = catchBirdStar;
    }

    public Integer getCatchTime() {
        return catchTime;
    }

    public void setCatchTime(Integer catchTime) {
        this.catchTime = catchTime;
    }

    public Integer getCatchEndTime() {
        return catchEndTime;
    }

    public void setCatchEndTime(Integer catchEndTime) {
        this.catchEndTime = catchEndTime;
    }

    public Integer getUseTrapId() {
        return useTrapId;
    }

    public void setUseTrapId(Integer useTrapId) {
        this.useTrapId = useTrapId;
    }

    public Integer getUseTrapNum() {
        return useTrapNum;
    }

    public void setUseTrapNum(Integer useTrapNum) {
        this.useTrapNum = useTrapNum;
    }

    @Override
    public String toString() {
        return "RoleTrap{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", idx = " + idx +
            ", baitId = " + baitId +
            ", catchBirdId = " + catchBirdId +
            ", catchBirdSex = " + catchBirdSex +
            ", catchBirdWeight = " + catchBirdWeight +
            ", catchBirdStar = " + catchBirdStar +
            ", catchTime = " + catchTime +
            ", catchEndTime = " + catchEndTime +
            ", useTrapId = " + useTrapId +
            ", useTrapNum = " + useTrapNum +
        "}";
    }
}
