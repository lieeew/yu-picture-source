package com.yupi.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 李鱼皮
 */
@Data
public class PictureStyleTransferRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long pictureId;

    private String style;

}
