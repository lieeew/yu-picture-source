package com.yupi.yupicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片任务
 * @TableName picture_task
 */
@TableName(value ="picture_task")
@Data
public class PictureTask implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务类型：STYLE_TRANSFER/TAG_RECOGNITION/RENAME/IMAGE_MATCH
     */
    @TableField(value = "taskType")
    private String taskType;

    /**
     * 原始图片ID
     */
    @TableField(value = "originalPictureId")
    private Long originalPictureId;

    /**
     * 结果图片ID
     */
    @TableField(value = "resultPictureId")
    private Long resultPictureId;

    /**
     * 状态：0-处理中 1-完成 2-失败
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建用户 id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 错误信息
     */
    @TableField(value = "errorMessage")
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}