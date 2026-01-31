package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 李鱼皮
 */

@Getter
public enum PictureTaskTypeEnum {

    STYLE_TRANSFER("风格转换", "STYLE_TRANSFER"),
    TAG_RECOGNITION("标签识别", "TAG_RECOGNITION"),
    RENAME("重命名", "RENAME");

    private final String text;

    private final String value;

    private static final Map<String, PictureTaskTypeEnum> VALUE_TO_ENUM = new HashMap<>();

    static {
        for (PictureTaskTypeEnum typeEnum : PictureTaskTypeEnum.values()) {
            VALUE_TO_ENUM.put(typeEnum.value, typeEnum);
        }
    }

    PictureTaskTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static PictureTaskTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return VALUE_TO_ENUM.get(value);
    }
}
