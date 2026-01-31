package com.yupi.yupicturebackend.ai.tools;

import cn.hutool.json.JSONUtil;
import com.yupi.yupicturebackend.constant.PictureConstant;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.service.PictureService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author 李鱼皮
 * @date 2026/1/24
 * @description
 */
@Slf4j
@Component
public class UpdateTools {

    @Resource
    @Lazy
    private PictureService pictureService;

    @Tool(description = "update picture info")
    String updatePictureInfo(
            @ToolParam(description = "update picture columns, only support: name, tags")
            String pictureColumn,
            @ToolParam(description = "update info")
            String updateInfo,
            ToolContext toolContext) {
        Long pictureId = Long.parseLong(toolContext.getContext().get(PictureConstant.TOOL_PICTURE_KEY).toString());
        log.info("UpdateTools.updatePictureInfo: pictureId={}, column={}, updateInfo={}", pictureId, pictureColumn, updateInfo);

        Picture picture = new Picture();
        picture.setId(pictureId);
        switch (pictureColumn) {
            case "name":
                if (!isAllChinese(updateInfo)) {
                    return "重新生成，请生成中文名称";
                }
                picture.setName(updateInfo);
                break;
            case "tags":
                try {
                    JSONUtil.toList(updateInfo, String.class);
                } catch (Exception e) {
                    return "重新生成，描述: 生成 JSON String 出现错误，需要生成类似这种*字符串* [\"动漫少女\",\"校服女生\"]";
                }
                if (!isAllChinese(updateInfo)) {
                    return "重新生成，请生成中文标签，例如：[\"动漫少女\",\"校服女生\"]";
                }
                picture.setTags(updateInfo);
                break;
            default:
                log.warn("UpdateTools.updatePictureInfo: invalid column={}", pictureColumn);
                return "pictureColumn 不存在必须是 name 或者 tags";
        }
        try {
            pictureService.editPictureByAi(picture);
            log.info("UpdateTools.updatePictureInfo success: pictureId={}", pictureId);
            return "更新成功";
        } catch (Exception e) {
            log.error("UpdateTools.updatePictureInfo error: pictureId={}, error={}", pictureId, e.getMessage(), e);
            return "更新失败";
        }
    }

    private boolean isAllChinese(String str) {
        return str.matches(".*[\\u4e00-\\u9fa5].*");
    }
}
