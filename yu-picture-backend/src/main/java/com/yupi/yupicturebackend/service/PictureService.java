package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yupi.yupicturebackend.model.dto.picture.*;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.PictureVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author 李鱼皮
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2024-12-11 20:45:51
 */
public interface PictureService extends IService<Picture> {

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     *
     * @param inputSource          文件输入源
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取图片包装类（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);

    /**
     * 清理图片文件
     *
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 校验空间图片的权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     *
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

    /**
     * 风格转换图片
     *
     * @param pictureStyleTransferRequest
     * @param loginUser
     * @return
     */
    Long styleTransferPicture(PictureStyleTransferRequest pictureStyleTransferRequest, User loginUser);

    /**
     * 批量风格转换图片
     *
     * @param request
     * @param loginUser
     * @return 任务ID列表
     */
    List<Long> styleTransferPictureByBatch(PictureStyleTransferTaskRequest request, User loginUser);

    /**
     * 应用任务结果到图片
     *
     * @param taskId
     * @param pictureId
     * @param loginUser
     */
    void applyPictureTaskResult(Long taskId, Long pictureId, User loginUser);

    /**
     * 生成图片 JSON 描述
     *
     * @param pictureId
     * @param loginUser
     * @return
     */
    String genPictureDescription(Long pictureId, User loginUser);

    /**
     * AI 编辑图片（仅更新指定字段）
     *
     * @param picture
     */
    void editPictureByAi(Picture picture);

    /**
     * AI 自动生成图片标签
     *
     * @param pictureId
     * @param loginUser
     * @return 任务 ID
     */
    Long generatePictureTags(Long pictureId, User loginUser);

    /**
     * AI 重命名图片
     *
     * @param pictureId
     * @param loginUser
     * @return 任务 ID
     */
    Long renamePictureByAI(Long pictureId, User loginUser);

    /**
     * AI 批量生成图片标签
     *
     * @param pictureIdList
     * @param loginUser
     * @return 任务 ID 列表
     */
    List<Long> generatePictureTagsByBatch(List<Long> pictureIdList, User loginUser);

    /**
     * AI 批量重命名图片
     *
     * @param pictureIdList
     * @param loginUser
     * @return 任务 ID 列表
     */
    List<Long> renamePictureByAIByBatch(List<Long> pictureIdList, User loginUser);

    /**
     * rag 搜索
     *
     * @param searchText
     * @param pages
     * @return
     */
    void searchPictureByText(String searchText, Page<Picture> pages);

    /**
     * 获取公共空间且审核通过的图片ID列表
     * @return 图片ID列表
     */
    List<Long> getPublicApprovedPictureIds();


    /**
     * 根据图片标签搜索相似图片
     * @param searchPictureByTagsRequest
     * @param httpRequest
     * @return 相似图片列表
     */
    List<PictureVO> searchPicture(SearchPictureRequest searchPictureByTagsRequest, HttpServletRequest httpRequest);
}
