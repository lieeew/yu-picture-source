package com.yupi.yupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import com.yupi.yupicturebackend.mapper.PictureTaskMapper;
import com.yupi.yupicturebackend.model.entity.PictureTask;
import com.yupi.yupicturebackend.service.PictureTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 李鱼皮
 */
@Slf4j
@Service
public class PictureTaskServiceImpl extends ServiceImpl<PictureTaskMapper, PictureTask>
        implements PictureTaskService {


    @Override
    public List<PictureTask> listByUserId(Long userId, List<Long> taskIds) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        if (CollUtil.isEmpty(taskIds)) {
            return List.of();
        }
        return this.lambdaQuery()
                .eq(PictureTask::getUserId, userId)
                .in(PictureTask::getId, taskIds)
                .list();
    }

    @Override
    public PictureTask getByIdAndUserId(Long taskId, Long userId) {
        ThrowUtils.throwIf(taskId == null || taskId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
        PictureTask task = this.getById(taskId);
        ThrowUtils.throwIf(task == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        ThrowUtils.throwIf(!task.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权访问该任务");
        return task;
    }
}
