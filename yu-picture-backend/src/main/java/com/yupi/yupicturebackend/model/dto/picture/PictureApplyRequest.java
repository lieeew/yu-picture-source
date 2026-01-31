package com.yupi.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片应用任务结果请求
 */
@Data
public class PictureApplyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务 id
     */
    private Long taskId;

    /**
     * 图片 id
     */
    private Long pictureId;
}
