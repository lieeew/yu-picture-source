<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存至空间：<a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
    </a-typography-paragraph>
    <!-- 选择上传方式 -->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <!-- 图片上传组件 -->
        <PictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL 上传" force-render>
        <!-- URL 图片上传组件 -->
        <UrlPictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
      </a-tab-pane>
    </a-tabs>
    <!-- 图片编辑 -->
    <div v-if="picture" class="edit-bar">
      <a-space size="middle">
        <a-button :icon="h(EditOutlined)" @click="doEditPicture">编辑图片</a-button>
        <a-button type="primary" :icon="h(FullscreenOutlined)" @click="doImagePainting">
          AI 扩图
        </a-button>
        <a-button type="primary" :icon="h(HighlightOutlined)" @click="doStyleTransfer">
          风格转换
        </a-button>
        <a-button type="primary" :icon="h(TagsOutlined)" @click="doGenerateTags" :loading="generateTagsLoading">
          智能标签
        </a-button>
        <a-button type="primary" :icon="h(FileTextOutlined)" @click="doRenameByAi" :loading="renameLoading">
          智能命名
        </a-button>
      </a-space>
      <ImageCropper
        ref="imageCropperRef"
        :imageUrl="picture?.url"
        :picture="picture"
        :spaceId="spaceId"
        :space="space"
        :onSuccess="onCropSuccess"
      />
      <ImageOutPainting
        ref="imageOutPaintingRef"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onImageOutPaintingSuccess"
      />
      <ImageStyleTransfer
        ref="imageStyleTransferRef"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onImageStyleTransferSuccess"
      />
    </div>
    <!-- 图片信息表单 -->
    <a-form
      v-if="picture"
      name="pictureForm"
      layout="vertical"
      :model="pictureForm"
      @finish="handleSubmit"
    >
      <a-form-item name="name" label="名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" allow-clear />
      </a-form-item>
      <a-form-item name="introduction" label="简介">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="category" label="分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item name="tags" label="标签">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, h, onMounted, onUnmounted, reactive, ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import {
  editPicture,
  getPictureVoById,
  listPictureTagCategory,
  generatePictureTags,
  renamePictureByAi,
  getPictureTask,
} from '@/api/pictureController.ts'
import { PICTURE_TASK_STATUS_ENUM } from '@/constants/pictureTask'
import { useRoute, useRouter } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { EditOutlined, FullscreenOutlined, HighlightOutlined, TagsOutlined, FileTextOutlined } from '@ant-design/icons-vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import ImageStyleTransfer from '@/components/ImageStyleTransfer.vue'
import { getSpaceVoById } from '@/api/spaceController.ts'

const router = useRouter()
const route = useRoute()

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const uploadType = ref<'file' | 'url'>('file')
// AI 功能加载状态
const generateTagsLoading = ref(false)
const renameLoading = ref(false)

// 任务状态枚举
const TaskStatusEnum = PICTURE_TASK_STATUS_ENUM

// 轮询定时器
let generateTagsPollingTimer: ReturnType<typeof setInterval> | null = null
let renamePollingTimer: ReturnType<typeof setInterval> | null = null

// 轮询开始时间
let generateTagsPollingStartTime: number = 0
let renamePollingStartTime: number = 0

// 最大轮询时间（3分钟）
const MAX_POLLING_DURATION = 3 * 60 * 1000

// 任务 ID
const generateTagsTaskId = ref<number | undefined>(undefined)
const renameTaskId = ref<number | undefined>(undefined)

// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId
})

/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  console.log(values)
  const pictureId = picture.value.id
  if (!pictureId) {
    return
  }
  const res = await editPicture({
    id: pictureId,
    spaceId: spaceId.value,
    ...values,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategory()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

// 获取老数据
const getOldPicture = async () => {
  // 获取到 id
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoById({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      updatePictureData(res.data.data)
    }
  }
}


// 更新图片数据
const updatePictureData = (data: API.PictureVO) => {
  console.log('[AI] 更新图片数据, name:', data.name, ', tags:', data.tags)
  picture.value = data
  pictureForm.name = data.name
  pictureForm.introduction = data.introduction
  pictureForm.category = data.category
  pictureForm.tags = data.tags
}

onMounted(() => {
  getOldPicture()
})

// ----- 图片编辑器引用 ------
const imageCropperRef = ref()

// 编辑图片
const doEditPicture = async () => {
  imageCropperRef.value?.openModal()
}

// 编辑成功事件
const onCropSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

// ----- AI 扩图引用 -----
const imageOutPaintingRef = ref()

// 打开 AI 扩图弹窗
const doImagePainting = async () => {
  imageOutPaintingRef.value?.openModal()
}

// AI 扩图保存事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

// ----- AI 风格转换引用 -----
const imageStyleTransferRef = ref()

// 打开 AI 风格转换弹窗
const doStyleTransfer = async () => {
  imageStyleTransferRef.value?.openModal()
}

// AI 风格转换保存事件
const onImageStyleTransferSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

// ----- AI 生成图片标签 -----
// 开始生成标签轮询
const startGenerateTagsPolling = (taskId: number) => {
  generateTagsTaskId.value = taskId
  generateTagsPollingStartTime = Date.now()
  console.log('[AI标签] 开始轮询任务, taskId:', taskId)
  message.loading('AI 生成标签中...', { key: 'generateTags' })
  
  generateTagsPollingTimer = setInterval(async () => {
    // 检查是否超过最大轮询时间
    if (Date.now() - generateTagsPollingStartTime > MAX_POLLING_DURATION) {
      message.warning('标签生成超时，请稍后重试', { key: 'generateTags' })
      generateTagsLoading.value = false
      stopGenerateTagsPolling()
      return
    }

    try {
      const res = await getPictureTask({ taskId })
      console.log('[AI标签] 轮询结果:', res.data)
      if (res.data.code === 0 && res.data.data) {
        const task = res.data.data
        console.log('[AI标签] 任务状态:', task.status)
        
        switch (task.status) {
          case TaskStatusEnum.PROCESSING:
            // 继续等待
            break
          case TaskStatusEnum.COMPLETED:
            console.log('[AI标签] 任务完成, result:', task.result)
            message.success('标签生成成功', { key: 'generateTags' })
            generateTagsLoading.value = false
            stopGenerateTagsPolling()
            // 刷新页面
            window.location.reload()
            break
          case TaskStatusEnum.FAILED:
            console.error('[AI标签] 任务失败:', task.errorMessage)
            message.error('标签生成失败：' + (task.errorMessage || '未知错误'), { key: 'generateTags' })
            generateTagsLoading.value = false
            stopGenerateTagsPolling()
            break
        }
      }
    } catch (error) {
      console.error('[AI标签] 轮询失败', error)
    }
  }, 3000)
}

// 停止生成标签轮询
const stopGenerateTagsPolling = () => {
  if (generateTagsPollingTimer) {
    clearInterval(generateTagsPollingTimer)
    generateTagsPollingTimer = null
  }
}

// 清理生成标签轮询
const clearGenerateTagsPolling = () => {
  stopGenerateTagsPolling()
  generateTagsTaskId.value = undefined
}

// AI 生成图片标签
const doGenerateTags = async () => {
  if (!picture.value?.id) {
    return
  }
  generateTagsLoading.value = true
  
  try {
    const res = await generatePictureTags({ id: picture.value.id })
    console.log('[AI标签] API响应:', res.data)
    if (res.data.code === 0 && res.data.data) {
      const taskId = res.data.data
      console.log('[AI标签] 任务ID:', taskId)
      startGenerateTagsPolling(taskId)
    } else {
      message.error('标签生成失败，' + res.data.message)
      generateTagsLoading.value = false
    }
  } catch (error) {
    console.error('[AI标签] API调用失败', error)
    message.error('标签生成失败，' + (error as Error).message)
    generateTagsLoading.value = false
  }
}

// ----- AI 重命名图片 -----
// 开始重命名轮询
const startRenamePolling = (taskId: number) => {
  renameTaskId.value = taskId
  renamePollingStartTime = Date.now()
  console.log('[AI重命名] 开始轮询任务, taskId:', taskId)
  message.loading('AI 重命名中...', { key: 'rename' })
  
  renamePollingTimer = setInterval(async () => {
    // 检查是否超过最大轮询时间
    if (Date.now() - renamePollingStartTime > MAX_POLLING_DURATION) {
      message.warning('重命名超时，请稍后重试', { key: 'rename' })
      renameLoading.value = false
      stopRenamePolling()
      return
    }

    try {
      const res = await getPictureTask({ taskId })
      console.log('[AI重命名] 轮询结果:', res.data)
      if (res.data.code === 0 && res.data.data) {
        const task = res.data.data
        console.log('[AI重命名] 任务状态:', task.status)
        
        switch (task.status) {
          case TaskStatusEnum.PROCESSING:
            // 继续等待
            break
          case TaskStatusEnum.COMPLETED:
            console.log('[AI重命名] 任务完成, result:', task.result)
            message.success('重命名成功', { key: 'rename' })
            renameLoading.value = false
            stopRenamePolling()
            // 刷新页面
            window.location.reload()
            break
          case TaskStatusEnum.FAILED:
            console.error('[AI重命名] 任务失败:', task.errorMessage)
            message.error('重命名失败：' + (task.errorMessage || '未知错误'), { key: 'rename' })
            renameLoading.value = false
            stopRenamePolling()
            break
        }
      }
    } catch (error) {
      console.error('[AI重命名] 轮询失败', error)
    }
  }, 3000)
}

// 停止重命名轮询
const stopRenamePolling = () => {
  if (renamePollingTimer) {
    clearInterval(renamePollingTimer)
    renamePollingTimer = null
  }
}

// 清理重命名轮询
const clearRenamePolling = () => {
  stopRenamePolling()
  renameTaskId.value = undefined
}

// AI 重命名图片
const doRenameByAi = async () => {
  if (!picture.value?.id) {
    return
  }
  renameLoading.value = true
  
  try {
    const res = await renamePictureByAi({ id: picture.value.id })
    console.log('[AI重命名] API响应:', res.data)
    if (res.data.code === 0 && res.data.data) {
      const taskId = res.data.data
      console.log('[AI重命名] 任务ID:', taskId)
      startRenamePolling(taskId)
    } else {
      message.error('重命名失败，' + res.data.message)
      renameLoading.value = false
    }
  } catch (error) {
    console.error('[AI重命名] API调用失败', error)
    message.error('重命名失败，' + (error as Error).message)
    renameLoading.value = false
  }
}

// 获取空间信息
const space = ref<API.SpaceVO>()

// 获取空间信息
const fetchSpace = async () => {
  // 获取数据
  if (spaceId.value) {
    const res = await getSpaceVoById({
      id: spaceId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  }
}

watchEffect(() => {
  fetchSpace()
})

// 组件卸载时清理轮询
onUnmounted(() => {
  clearGenerateTagsPolling()
  clearRenamePolling()
})
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}

#addPicturePage .edit-bar {
  text-align: center;
  margin: 16px 0;
}
</style>
