/**
 * 图片任务类型枚举
 */
export const PICTURE_TASK_TYPE_ENUM = {
  STYLE_TRANSFER: 'STYLE_TRANSFER',
  TAG_RECOGNITION: 'TAG_RECOGNITION',
  RENAME: 'RENAME',
  IMAGE_MATCH: 'IMAGE_MATCH',
}

/**
 * 图片任务类型映射
 */
export const PICTURE_TASK_TYPE_MAP = {
  STYLE_TRANSFER: '风格转换',
  TAG_RECOGNITION: '标签识别',
  RENAME: '重命名',
  IMAGE_MATCH: '以图搜图',
}

/**
 * 图片任务类型下拉表单选项
 */
export const PICTURE_TASK_TYPE_OPTIONS = Object.entries(PICTURE_TASK_TYPE_MAP).map(([key, label]) => ({
  label,
  value: key,
}))

/**
 * 图片任务状态枚举
 */
export const PICTURE_TASK_STATUS_ENUM = {
  PROCESSING: 0,
  COMPLETED: 1,
  FAILED: 2,
}

/**
 * 图片任务状态映射
 */
export const PICTURE_TASK_STATUS_MAP = {
  0: '处理中',
  1: '完成',
  2: '失败',
}

/**
 * 图片任务状态下拉表单选项
 */
export const PICTURE_TASK_STATUS_OPTIONS = Object.entries(PICTURE_TASK_STATUS_MAP).map(([value, label]) => ({
  label,
  value: Number(value),
}))
