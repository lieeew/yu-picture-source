package com.yupi.yupicturebackend.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用的 ID 请求类
 */
@Data
public class IdRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * ids
     */
    private List<Long> ids;

    private static final long serialVersionUID = 1L;
}
