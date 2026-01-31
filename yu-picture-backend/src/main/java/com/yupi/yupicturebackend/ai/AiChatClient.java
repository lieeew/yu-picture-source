package com.yupi.yupicturebackend.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yupi.yupicturebackend.ai.tools.UpdateTools;
import com.yupi.yupicturebackend.constant.PictureConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.ThinkingTagCleaner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.dashscope.spec.DashScopeModel.ChatModel.QWEN_VL_MAX;

/**
 * @author 李鱼皮
 * @date 2026/1/24
 * @description
 */
@Slf4j
@Component
public class AiChatClient {

    private final ChatClient chatClient;

    private final ChatModel chatModel;

    private final ThinkingTagCleaner thinkingTagCleaner = new ThinkingTagCleaner();

    public AiChatClient(UpdateTools updateTools, ChatModel chatModel) {
        this.chatModel = chatModel;
        this.chatClient = ChatClient
                .builder(chatModel)
                .defaultTools(updateTools)
                .build();
    }

    public Boolean getPictureName(String pictureId, String pictureDescription) {
        ChatClient.CallResponseSpec response = chatClient.prompt()
                .user(pictureDescription)
                .system(new ClassPathResource("prompt/prompt_picture_name.txt"), StandardCharsets.UTF_8)
                .toolContext(Map.of(PictureConstant.TOOL_PICTURE_KEY, pictureId))
                .call();
        return checkResult(response);
    }

    public Boolean getPictureTags(String pictureId, String pictureDescription) {
        ChatClient.CallResponseSpec response = chatClient.prompt()
                .user(pictureDescription)
                .system(new ClassPathResource("prompt/prompt_picture_tags.txt"), StandardCharsets.UTF_8)
                .toolContext(Map.of(PictureConstant.TOOL_PICTURE_KEY, pictureId))
                .call();
        return checkResult(response);
    }

    private Boolean checkResult(ChatClient.CallResponseSpec response) {
        try {
            String content = thinkingTagCleaner.clean(response.content());
            return StringUtils.isNotBlank(content) && "true".equals(content);
        } catch (Exception e) {
            log.error("AI 生成出现报错 {}", e);
            return false;
        }
    }

    public String getImageContentDescription(String url) {
        UserMessage userMessage = UserMessage.builder()
                .text(new ClassPathResource("prompt/prompt_image_description.txt"))
                .media(List.of(Media.builder()
                        .mimeType(MediaType.IMAGE_PNG)
                        .data(url)
                        .build()))
                .build();
        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions.builder().multiModel(true)
                .model(QWEN_VL_MAX.value).build();
        Prompt prompt = Prompt.builder().messages(List.of(userMessage))
                .chatOptions(dashScopeChatOptions).build();
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
