package com.yupi.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 根据图片标签搜索相似图片请求
 */
@Data
public class SearchPictureRequest implements Serializable {

    private Long pictureId;

    private Integer topK;

    private static final long serialVersionUID = 1L;
}
