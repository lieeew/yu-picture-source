package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author 李鱼皮
 */

@Getter
public enum PictureTaskStatusEnum {

    PROCESSING("处理中", 0),
    COMPLETED("完成", 1),
    FAILED("失败", 2);

    private final String text;

    private final int value;

    PictureTaskStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PictureTaskStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureTaskStatusEnum statusEnum : PictureTaskStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
}
