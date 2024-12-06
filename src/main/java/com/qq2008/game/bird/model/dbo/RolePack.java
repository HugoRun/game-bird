package com.qq2008.game.bird.model.dbo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 诱饵包裹数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_pack")
public class RolePack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 背包类型, 1 诱饵/2 陷阱/3 鸟笼/4 巢穴/5 训练场/6 道具
     */
    private Byte typeId;

    /**
     * 配置Id
     */
    private Integer baseId;

    /**
     * 道具数量
     */
    private Integer num;

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

    public Byte getTypeId() {
        return typeId;
    }

    public void setTypeId(Byte typeId) {
        this.typeId = typeId;
    }

    public Integer getBaseId() {
        return baseId;
    }

    public void setBaseId(Integer baseId) {
        this.baseId = baseId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "RolePack{" +
            "id = " + id +
            ", roleId = " + roleId +
            ", typeId = " + typeId +
            ", baseId = " + baseId +
            ", num = " + num +
        "}";
    }
}
