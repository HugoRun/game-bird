package com.gamebird.game.bird.model.dbo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 活动表
 * </p>
 *
 * @author game-bird
 * @since 2026-10-14
 */
@TableName("activity")
@Getter
@Setter
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键Id（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 开始时间戳（秒级）
     */
    private Integer startTime;

    /**
     * 结束时间戳（秒级）
     */
    private Integer endTime;

    /**
     * 状态:0下线/1上线
     */
    private Integer status;
}
