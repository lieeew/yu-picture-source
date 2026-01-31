package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum PictureStyleEnum {

    WATERCOLOR("水彩画", "watercolor", "prompt/prompt_watercolor.txt"),
    OIL_PAINTING("油画", "oil_painting", "prompt/prompt_oil_painting.txt"),
    SKETCH("素描", "sketch", "prompt/prompt_sketch.txt"),
    CARTOON("卡通", "cartoon", "prompt/prompt_cartoon.txt"),
    PIXEL_ART("像素画", "pixel_art", "prompt/prompt_pixel_art.txt"),
    CHINESE_INK("水墨画", "chinese_ink", "prompt/prompt_chinese_ink.txt"),
    UKIYO_E("浮世绘", "ukiyo_e", "prompt/prompt_ukiyo_e.txt"),
    SEPIA("怀旧", "sepia", "prompt/prompt_sepia.txt");

    private final String text;

    private final String value;

    private final String promptFile;

    private static final Map<String, PictureStyleEnum> VALUE_TO_ENUM = new HashMap<>();
    private static final List<String> STYLE_LIST = Arrays.stream(values())
            .map(PictureStyleEnum::getText)
            .toList();

    static {
        for (PictureStyleEnum styleEnum : PictureStyleEnum.values()) {
            VALUE_TO_ENUM.put(styleEnum.value, styleEnum);
        }
    }

    public static List<String> getStyleList() {
        return STYLE_LIST;
    }

    PictureStyleEnum(String text, String value, String promptFile) {
        this.text = text;
        this.value = value;
        this.promptFile = promptFile;
    }

    public static PictureStyleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        return VALUE_TO_ENUM.get(value);
    }
}
