package com.qq2008.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 鸟笼数据表
 * </p>
 *
 * @author HugoRun
 * @since 2024-11-17
 */
@TableName("role_cage")
@Getter
@Setter
public class RoleCage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 鸟笼唯一Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属角色Id
     */
    private String roleId;

    /**
     * 小鸟唯一Id
     */
    private Integer idx;

    /**
     * 排序Id
     */
    private Integer birdId;

    /**
     * 小鸟配置Id
     *
     * ⚠️ <b>勿改拼写！</b>此字段名对应 SQL 列 {@code base_brid_id}（注意是 brid，不是 bird）。
     * 字段名与列名同为「错误拼写」因而巧合正确。若有人顺手修正为 {@code baseBirdId}，
     * MyBatis-Plus 会推导出 {@code base_bird_id}，而实际列名是 {@code base_brid_id}，
     * 结果是<b>静默查不到数据且不抛任何异常</b>。
     * 同理下方 {@code useCageNum0} 对应列 {@code use_cage_num0}（尾部 0）。
     *
     * <p>本轮刻意<b>不改名、不加 {@code @TableField}</b>：改名会变更列名（违反数据库红线），
     * 加注解会把可疑拼写固化成显式契约，把「明显的笔误」变成「看不出所以然的约定」。
     */
    private Integer baseBridId;

    /**
     * 使用鸟笼Id
     */
    private Integer useCageId;

    /**
     * 使用鸟笼剩余次数
     *
     * ⚠️ <b>勿改拼写！</b>对应 SQL 列 {@code use_cage_num0}（尾部是一个 0，疑似笔误）。
     * 与上方 {@code baseBridId} 同理：拼写与列名同为错误，因而巧合正确。
     */
    private Integer useCageNum0;
}
