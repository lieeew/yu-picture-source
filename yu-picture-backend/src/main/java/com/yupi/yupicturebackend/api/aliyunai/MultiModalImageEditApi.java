package com.yupi.yupicturebackend.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.exception.UploadFileException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author 李鱼皮
 */
@Slf4j
@Component
public class MultiModalImageEditApi {

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://[\\w\\-.]+(/[^\\s]*)?$");

    private final Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
            .retryIfResult(StringUtils::isBlank)
            .retryIfExceptionOfType(UploadFileException.class)
            .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .build();

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.image.options.model}")
    private String model;


    /**
     * 根据消息和图片生成新图片
     * @param message 用户消息指令
     * @param imageUrl 参考图片地址
     * @return 生成图片的URL
     */
    public String getImageUrl(String message, String imageUrl) {
        preCheck(message, imageUrl);
        MultiModalMessage multiModalMessage = getMessage(message, imageUrl);

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .messages(Collections.singletonList(multiModalMessage))
                .apiKey(apiKey)
                .model(model)
                // 生成一张图片
                .n(1)
                .watermark(false)
                .build();
        try {
            return retryer.call(() -> {
                MultiModalConversation conv = new MultiModalConversation();
                MultiModalConversationResult call = conv.call(param);
                return call.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("image").toString();
            });
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "多模态生成图片失败: " + e.getMessage());
        }
    }

    private static MultiModalMessage getMessage(String message, String imageUrl) {
        return MultiModalMessage.builder()
                .role("user")
                .content(Arrays.asList(
                        Collections.singletonMap("text", message),
                        Collections.singletonMap("image", imageUrl)
                ))
                .build();
    }

    private static void preCheck(String message, String imageUrl) {
        if (StrUtil.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
        }
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片地址不能为空");
        }
        if (!URL_PATTERN.matcher(imageUrl).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片地址格式不正确");
        }
    }
}
