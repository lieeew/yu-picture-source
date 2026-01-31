package com.yupi.yupicturebackend.utils;

import cn.hutool.core.util.StrUtil;
import com.yupi.yupicturebackend.model.dto.picture.PictureQueryRequest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 搜索工具类
 * @author 李鱼皮
 */
public class SearchUtils {

    /**
     * 构建 advance search 搜索文本
     * @param request 查询请求
     * @return 拼装的搜索文本
     */
    public static String buildAdvanceSearchText(PictureQueryRequest request) {
        return Stream.of(
                request.getName(),
                request.getIntroduction(),
                request.getSearchText()
        )
        .filter(StrUtil::isNotBlank)
        .collect(Collectors.joining(" "));
    }
}
