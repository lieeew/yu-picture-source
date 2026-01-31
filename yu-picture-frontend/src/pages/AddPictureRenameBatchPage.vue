<template>
  <div id="addPictureRenameBatchPage">
    <h2 style="margin-bottom: 16px">批量智能命名</h2>

    <!-- 已选择的图片 -->
    <SelectedPicturesCard
      :selectedRowKeys="selectedRowKeys"
      @clear="handleClear"
      @remove="handleRemove"
    />

    <!-- 图片选择表格 -->
    <PictureSelectTable ref="pictureSelectTableRef" v-model:selectedRowKeys="selectedRowKeys" />

    <!-- 操作按钮 -->
    <a-card style="margin-top: 16px">
      <a-space direction="vertical" style="width: 100%">
        <a-button
          type="primary"
          size="large"
          style="width: 100%"
          :disabled="selectedRowKeys.length === 0"
          :loading="submitting"
          @click="handleSubmit"
        >
          执行批量智能命名 (已选 {{ selectedRowKeys.length }} 张)
        </a-button>

        <!-- 任务进度 -->
        <template v-if="taskIds.length > 0">
          <a-divider>任务进度</a-divider>
          <a-progress
            :percent="taskProgress"
            :status="progressStatus"
            :stroke-color="progressColor"
          />
          <div class="task-status">
            <a-space>
              <span>总任务: {{ taskIds.length }}</span>
              <span>已完成: {{ completedCount }}</span>
              <span>失败: {{ failedCount }}</span>
              <span v-if="isPolling">轮询中...</span>
              <span v-if="isTimeout" style="color: #ff4d4f">已超时</span>
            </a-space>
          </div>

          <!-- 任务详情 -->
          <a-collapse v-if="taskDetails.length > 0">
            <a-collapse-panel key="1" header="任务详情">
              <a-table
                :columns="taskColumns"
                :data-source="taskDetails"
                :pagination="false"
                size="small"
                :row-key="(record: API.PictureTask) => record.id"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.dataIndex === 'status'">
                    <a-tag :color="getStatusColor(record.status)">
                      {{ getStatusText(record.status) }}
                    </a-tag>
                  </template>
                  <template v-if="column.dataIndex === 'createTime'">
                    {{ record.createTime ? dayjs(record.createTime).format('HH:mm:ss') : '-' }}
                  </template>
                </template>
              </a-table>
            </a-collapse-panel>
          </a-collapse>
        </template>
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import PictureSelectTable from '@/components/PictureSelectTable.vue'
import SelectedPicturesCard from '@/components/SelectedPicturesCard.vue'
import { renamePictureByAiByBatch, listPictureTasks } from '@/api/pictureController.ts'
import { PICTURE_TASK_STATUS_ENUM, PICTURE_TASK_STATUS_MAP } from '@/constants/pictureTask.ts'
import dayjs from 'dayjs'

// 轮询配置
const POLL_INTERVAL = 2000 // 2秒
const POLL_TIMEOUT = 5 * 60 * 1000 // 5分钟超时

// 图片选择表格 ref
const pictureSelectTableRef = ref<InstanceType<typeof PictureSelectTable> | null>(null)

// 选中的图片 ID 列表
const selectedRowKeys = ref<string[]>([])

// 提交状态
const submitting = ref(false)

// 任务 ID 列表
const taskIds = ref<number[]>([])

// 任务详情
const taskDetails = ref<API.PictureTask[]>([])

// 轮询相关
const isPolling = ref(false)
const isTimeout = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null
let startTime: number = 0

// 任务列表列定义
const taskColumns = [
  { title: '任务ID', dataIndex: 'id', width: 80 },
  { title: '原图片ID', dataIndex: 'originalPictureId', width: 100 },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '错误信息', dataIndex: 'errorMessage', ellipsis: true },
  { title: '创建时间', dataIndex: 'createTime', width: 100 },
]

// 计算属性
const completedCount = computed(() => {
  return taskDetails.value.filter((t) => t.status === PICTURE_TASK_STATUS_ENUM.COMPLETED).length
})

const failedCount = computed(() => {
  return taskDetails.value.filter((t) => t.status === PICTURE_TASK_STATUS_ENUM.FAILED).length
})

const taskProgress = computed(() => {
  if (taskIds.value.length === 0) return 0
  const finished = completedCount.value + failedCount.value
  return Math.round((finished / taskIds.value.length) * 100)
})

const progressStatus = computed(() => {
  if (isTimeout.value) return 'exception'
  if (failedCount.value > 0 && taskProgress.value === 100) return 'exception'
  if (taskProgress.value === 100) return 'success'
  return 'active'
})

const progressColor = computed(() => {
  if (isTimeout.value || failedCount.value > 0) return '#ff4d4f'
  if (taskProgress.value === 100) return '#52c41a'
  return '#1890ff'
})

// 获取状态颜色
const getStatusColor = (status: number | undefined) => {
  switch (status) {
    case PICTURE_TASK_STATUS_ENUM.PROCESSING:
      return 'processing'
    case PICTURE_TASK_STATUS_ENUM.COMPLETED:
      return 'success'
    case PICTURE_TASK_STATUS_ENUM.FAILED:
      return 'error'
    default:
      return 'default'
  }
}

// 获取状态文本
const getStatusText = (status: number | undefined) => {
  if (status !== undefined && status in PICTURE_TASK_STATUS_MAP) {
    return PICTURE_TASK_STATUS_MAP[status as keyof typeof PICTURE_TASK_STATUS_MAP]
  }
  return '未知'
}

// 清空选择
const handleClear = () => {
  selectedRowKeys.value = []
}

// 移除单个选择
const handleRemove = (id: string) => {
  selectedRowKeys.value = selectedRowKeys.value.filter((key) => key !== id)
}

// 提交任务
const handleSubmit = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择图片')
    return
  }

  submitting.value = true
  taskIds.value = []
  taskDetails.value = []
  isTimeout.value = false

  try {
    const res = await renamePictureByAiByBatch({
      ids: selectedRowKeys.value as unknown as number[],
    })

    if (res.data.code === 0 && res.data.data) {
      taskIds.value = res.data.data
      message.success(`已提交 ${taskIds.value.length} 个任务`)
      // 开始轮询
      startPolling()
    } else {
      message.error('提交失败：' + res.data.message)
    }
  } catch (error) {
    console.error('提交失败', error)
    message.error('提交失败：' + (error instanceof Error ? error.message : String(error)))
  } finally {
    submitting.value = false
  }
}

// 开始轮询
const startPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }

  isPolling.value = true
  startTime = Date.now()

  // 立即执行一次
  fetchTaskStatus()

  // 定时轮询
  pollTimer = setInterval(() => {
    // 超时检测
    if (Date.now() - startTime > POLL_TIMEOUT) {
      isTimeout.value = true
      stopPolling()
      message.error('任务轮询超时，请稍后刷新页面查看结果')
      return
    }

    fetchTaskStatus()
  }, POLL_INTERVAL)
}

// 停止轮询
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  isPolling.value = false
}

// 获取任务状态
const fetchTaskStatus = async () => {
  if (taskIds.value.length === 0) {
    stopPolling()
    return
  }

  try {
    const res = await listPictureTasks(
      { taskId: taskIds.value },
      { paramsSerializer: (params: { taskId: number[] }) => params.taskId.map((id) => `taskId=${id}`).join('&') }
    )

    if (res.data.code === 0 && res.data.data) {
      taskDetails.value = res.data.data

      // 检查是否所有任务都完成
      const allFinished = taskDetails.value.every(
        (t) => t.status === PICTURE_TASK_STATUS_ENUM.COMPLETED || t.status === PICTURE_TASK_STATUS_ENUM.FAILED
      )

      if (allFinished) {
        stopPolling()
        if (failedCount.value === 0) {
          message.success('所有任务执行完成！')
        } else {
          message.warning(`任务完成，${failedCount.value} 个任务失败`)
        }
        // 刷新表格数据并清空选择
        selectedRowKeys.value = []
        pictureSelectTableRef.value?.refresh()
      }
    }
  } catch (error) {
    console.error('获取任务状态失败', error)
  }
}

// 组件销毁时停止轮询
onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
#addPictureRenameBatchPage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.task-status {
  text-align: center;
  color: #666;
}
</style>
