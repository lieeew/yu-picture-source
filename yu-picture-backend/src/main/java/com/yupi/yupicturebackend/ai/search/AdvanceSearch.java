package com.yupi.yupicturebackend.ai.search;

import cn.hutool.core.util.StrUtil;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.service.PictureService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author 李鱼皮
 */
@Component
public class AdvanceSearch {

    @Resource
    private VectorStore vectorStore;

    @Resource
    @Lazy
    private PictureService pictureService;

    /**
     * 将图片描述写入向量库
     * @param pictureId 图片 ID
     */
    public void writePictureDescriptionToVector(Long pictureId) {
        Picture picture = pictureService.getById(pictureId);
        if (picture == null || StrUtil.isEmpty(picture.getContentDescription())) {
            return;
        }
        Document doc = Document.builder()
                .text(picture.getContentDescription())
                .metadata(Map.of("pictureId", pictureId))
                .build();
        vectorStore.add(List.of(doc));
    }

    /**
     * 统一搜索方法
     * @param searchText 搜索文本
     * @param pictureIds 待搜索的图片ID范围
     * @param topK 返回结果数量
     * @return 图片列表
     */
    private List<Picture> search(String searchText, List<Long> pictureIds, int topK) {
        if (pictureIds == null || pictureIds.isEmpty()) {
            return List.of();
        }
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        Filter.Expression filter = filterExpressionBuilder.in("pictureId", pictureIds.toArray()).build();
        SearchRequest request = SearchRequest.builder()
                .query(searchText)
                .filterExpression(filter)
                .topK(topK)
                .build();
        List<Document> documents = vectorStore.similaritySearch(request);
        return documents.stream()
                .limit(topK)
                .map(doc -> {
                    Object id = doc.getMetadata().get("pictureId");
                    Long pictureId = id instanceof Long ? (Long) id : ((Integer) id).longValue();
                    return pictureService.getById(pictureId);
                })
                .toList();
    }

    /**
     * 根据图片搜索相似图片，使用该图片的 contentDescription 作为搜索文本
     * @param pictureId 源图片ID
     * @param pictureIds 待搜索的图片ID范围
     * @param topK 返回结果数量
     * @return 图片列表
     */
    public List<Picture> searchByPicture(Long pictureId, List<Long> pictureIds, int topK) {
        Picture picture = pictureService.getById(pictureId);
        if (picture == null || StrUtil.isEmpty(picture.getContentDescription())) {
            return List.of();
        }
        return search(picture.getContentDescription(), pictureIds, topK);
    }

    /**
     * 根据文本搜索图片，按 pictureId 去重后返回结果
     * @param searchText 搜索文本
     * @param pictureIds 待搜索的图片ID范围
     * @param topK 返回结果数量
     * @return 图片列表
     */
    public List<Picture> searchByText(String searchText, List<Long> pictureIds, int topK) {
        return search(searchText, pictureIds, topK);
    }
}
