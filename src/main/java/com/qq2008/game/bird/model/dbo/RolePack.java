package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 诱饵包裹数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_pack")
@Getter
@Setter
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
}
