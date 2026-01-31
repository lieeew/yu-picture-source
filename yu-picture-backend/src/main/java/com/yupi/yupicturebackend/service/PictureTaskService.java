package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.entity.PictureTask;

import java.util.List;

/**
 * 图片任务服务接口
 *
 * @author 李鱼皮
 */
public interface PictureTaskService extends IService<PictureTask> {

    /**
     * 根据用户 ID 和任务 ID 列表查询任务列表
     *
     * @param userId   用户 ID
     * @param taskIds  任务 ID 列表
     * @return 符合条件的图片任务列表
     */
    List<PictureTask> listByUserId(Long userId, List<Long> taskIds);

    /**
     * 根据任务 ID 和用户 ID 查询单个任务
     *
     * @param taskId  任务 ID
     * @param userId  用户 ID
     * @return 符合条件的图片任务，如果不存在则返回null
     */
    PictureTask getByIdAndUserId(Long taskId, Long userId);
}
