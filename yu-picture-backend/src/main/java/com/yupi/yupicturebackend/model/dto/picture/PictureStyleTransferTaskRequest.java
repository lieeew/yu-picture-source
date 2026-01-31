package com.yupi.yupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureStyleTransferTaskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Long> pictureIdList;

    private String style;

}
