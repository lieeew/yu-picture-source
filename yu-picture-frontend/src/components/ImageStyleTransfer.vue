<template>
  <a-modal
    class="image-style-transfer"
    v-model:visible="visible"
    title="AI 风格转换"
    :footer="false"
    @cancel="closeModal"
    width="800px"
  >
    <a-row gutter="16">
      <a-col span="12">
        <h4>原始图片</h4>
        <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%; max-height: 300px" />
      </a-col>
      <a-col span="12">
        <h4>转换结果</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          :alt="picture?.name"
          style="max-width: 100%; max-height: 300px"
        />
        <a-empty v-else description="暂无结果" />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px" />
    <a-form layout="vertical" :model="formState">
      <a-form-item label="选择风格" required>
        <a-select
          v-model:value="formState.style"
          placeholder="请选择转换风格"
          style="width: 100%"
          optionLabelProp="label"
        >
          <a-option
            v-for="style in styleList"
            :key="style.value"
            :value="style.label"
            :label="style.label"
          >
            {{ style.label }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <a-flex justify="center" gap="16">
      <a-button
        type="primary"
        :loading="transferLoading"
        :disabled="!formState.style || transferLoading"
        @click="handleTransfer"
      >
        {{ transferLoading ? '转换中...' : '开始转换' }}
      </a-button>
      <a-button
        v-if="taskId && currentTaskStatus === TaskStatusEnum.COMPLETED"
        type="primary"
        :loading="applyLoading"
        @click="handleApply"
      >
        应用修改
      </a-button>
    </a-flex>
    <!-- 任务状态显示 -->
    <a-divider />
    <a-alert
      v-if="taskId && transferLoading"
      :type="getStatusType(currentTaskStatus)"
      :message="getStatusMessage(currentTaskStatus)"
      show-icon
    />
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, onUnmounted } from 'vue'
import {
  listPictureStyles,
  styleTransferPictureByBatch,
  getPictureTask,
  getPictureVoById,
  applyPictureTaskResult,
} from '@/api/pictureController.ts'
import { PICTURE_TASK_STATUS_ENUM } from '@/constants/pictureTask'
import { message } from 'ant-design-vue'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

const resultImageUrl = ref<string>('')

const styleList = ref<{ value: string; label: string }[]>([])

// 风格枚举：label(中文) -> code(英文)
const ArtStyleEnum: Record<string, string> = {
  '水彩画': 'watercolor',
  '油画': 'oil_painting',
  '素描': 'sketch',
  '卡通': 'cartoon',
  '像素画': 'pixel_art',
  '水墨画': 'chinese_ink',
  '浮世绘': 'ukiyo_e',
  '怀旧': 'sepia',
}

// 表单状态
const formState = reactive({
  style: undefined as string | undefined,
})

// 任务状态枚举
const TaskStatusEnum = PICTURE_TASK_STATUS_ENUM

// 当前任务状态
const currentTaskStatus = ref<number>(TaskStatusEnum.PROCESSING)

// 是否正在转换
const transferLoading = ref(false)

// 是否正在应用
const applyLoading = ref(false)

// 轮询定时器
let pollingTimer: ReturnType<typeof setInterval> | null = null

// 轮询开始时间
let pollingStartTime: number = 0

// 最大轮询时间（3分钟）
const MAX_POLLING_DURATION = 3 * 60 * 1000

// 任务 ID
const taskId = ref<number | undefined>(undefined)

// 转换结果图片 ID
const resultPictureId = ref<number | undefined>(undefined)

/**
 * 加载风格列表
 */
const fetchStyleList = async () => {
  try {
    const res = await listPictureStyles()
    if (res.data.code === 0 && res.data.data) {
      // 接口返回中文 label，转换为 { value: code, label: 中文 } 格式
      styleList.value = res.data.data.map((label: string) => ({
        value: ArtStyleEnum[label] || label,
        label: label,
      }))
    }
  } catch (error) {
    console.error('加载风格列表失败', error)
    message.error('加载风格列表失败')
  }
}

/**
 * 开始风格转换
 */
const handleTransfer = async () => {
  if (!props.picture?.id || !formState.style) {
    return
  }

  transferLoading.value = true
  currentTaskStatus.value = TaskStatusEnum.PROCESSING
  resultImageUrl.value = ''

  try {
    const res = await styleTransferPictureByBatch({
      pictureIdList: [props.picture.id],
      style: ArtStyleEnum[formState.style]
    })

    if (res.data.code === 0 && res.data.data && res.data.data.length > 0) {
      taskId.value = res.data.data[0]
      message.success('任务已提交，开始转换...')
      startPolling()
    } else {
      message.error('任务提交失败，' + (res.data.message || '未知错误'))
      transferLoading.value = false
    }
  } catch (error) {
    console.error('任务提交失败', error)
    message.error('任务提交失败，' + (error as Error).message)
    transferLoading.value = false
  }
}

// 开始轮询
const startPolling = () => {
  if (!taskId.value) {
    return
  }

  pollingStartTime = Date.now()
  pollingTimer = setInterval(async () => {
    // 检查是否超过最大轮询时间
    if (Date.now() - pollingStartTime > MAX_POLLING_DURATION) {
      message.warning('转换任务超时，请稍后重试', { key: 'transfer' })
      clearPolling()
      transferLoading.value = false
      return
    }

    try {
      const res = await getPictureTask({ taskId: taskId.value })

      if (res.data.code === 0 && res.data.data) {
        const task = res.data.data
        currentTaskStatus.value = task.status ?? TaskStatusEnum.PROCESSING

        switch (task.status) {
          case TaskStatusEnum.PROCESSING:
            if (task.errorMessage) {
              message.error('转换失败：' + task.errorMessage, { key: 'transfer' })
              clearPolling()
              transferLoading.value = false
            } else {
              message.loading('转换处理中...', { key: 'transfer' })
            }
            break

          case TaskStatusEnum.COMPLETED:
            message.success('风格转换成功', { key: 'transfer' })
            transferLoading.value = false
            // 停止轮询定时器，但保留 taskId 和状态用于显示按钮
            stopPollingTimer()
            // 获取结果图片 ID，然后查询图片详情
            if (task.resultPictureId) {
              resultPictureId.value = task.resultPictureId
              fetchResultImage(task.resultPictureId)
            }
            break

          case TaskStatusEnum.FAILED:
            message.error('转换失败：' + (task.errorMessage || '未知错误'), { key: 'transfer' })
            clearPolling()
            transferLoading.value = false
            break

          default:
            message.warning('未知任务状态：' + task.status, { key: 'transfer' })
        }
      } else {
        message.error('查询任务状态失败：' + (res.data.message || '未知错误'), { key: 'transfer' })
      }
    } catch (error) {
      console.error('轮询失败', error)
    }
  }, 6000)
}

// 停止轮询定时器
const stopPollingTimer = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 清理轮询（完全重置）
const clearPolling = () => {
  stopPollingTimer()
  taskId.value = undefined
  resultPictureId.value = undefined
  currentTaskStatus.value = TaskStatusEnum.PROCESSING
}

// 获取状态对应的提示类型
const getStatusType = (status: number): 'info' | 'warning' | 'success' | 'error' => {
  switch (status) {
    case TaskStatusEnum.PROCESSING:
      return 'warning'
    case TaskStatusEnum.COMPLETED:
      return 'success'
    case TaskStatusEnum.FAILED:
      return 'error'
    default:
      return 'info'
  }
}

// 获取状态对应的提示信息
const getStatusMessage = (status: number): string => {
  switch (status) {
    case TaskStatusEnum.PROCESSING:
      return '正在处理图片，请稍候...'
    case TaskStatusEnum.COMPLETED:
      return '转换成功！'
    case TaskStatusEnum.FAILED:
      return '转换失败'
    default:
      return '未知状态'
  }
}

/**
 * 获取转换结果图片详情
 */
const fetchResultImage = async (pictureId: number) => {
  try {
    const res = await getPictureVoById({ id: pictureId })
    if (res.data.code === 0 && res.data.data) {
      resultImageUrl.value = res.data.data.url || ''
    } else {
      message.error('获取结果图片失败')
    }
  } catch (error) {
    console.error('获取结果图片失败', error)
    message.error('获取结果图片失败')
  }
}

/**
 * 应用转换结果
 */
const handleApply = async () => {
  if (!taskId.value || !resultPictureId.value) {
    return
  }

  applyLoading.value = true
  try {
    const res = await applyPictureTaskResult({
      taskId: taskId.value,
      pictureId: resultPictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('应用成功')
      // 通过 getPictureVoById 获取应用后的图片详情并返回
      const pictureRes = await getPictureVoById({ id: resultPictureId.value })
      if (pictureRes.data.code === 0 && pictureRes.data.data) {
        props.onSuccess?.(pictureRes.data.data)
      }
      closeModal()
    } else {
      message.error('应用失败，' + res.data.message)
    }
  } catch (error) {
    console.error('应用失败', error)
    message.error('应用失败，' + (error as Error).message)
  } finally {
    applyLoading.value = false
  }
}

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
  fetchStyleList()
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
  resultImageUrl.value = ''
  resultPictureId.value = undefined
  formState.style = undefined
  clearPolling()
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

// 组件卸载时清理
onUnmounted(() => {
  clearPolling()
})
</script>

<style>
.image-style-transfer {
  text-align: center;
}
</style>
