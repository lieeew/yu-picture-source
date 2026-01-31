<template>
  <div id="searchPictureByTagsPage">
    <h2 style="margin-bottom: 16px">AI 标签搜索</h2>
    <h3 style="margin-bottom: 16px">原图</h3>
    <a-card hoverable style="width: 240px">
      <template #cover>
        <img
          :alt="picture.name"
          :src="picture.thumbnailUrl ?? picture.url"
          style="height: 180px; object-fit: cover"
        />
      </template>
    </a-card>
    <h3 style="margin: 16px 0">搜索结果</h3>
    <a-descriptions :column="1" bordered style="margin-bottom: 16px">
      <a-descriptions-item label="搜索标签">
        <a-space wrap>
          <a-tag v-for="tag in searchTags" :key="tag" color="blue">
            {{ tag }}
          </a-tag>
        </a-space>
      </a-descriptions-item>
    </a-descriptions>
    <!-- 图片结果列表 -->
    <PictureList :data-list="dataList" :loading="loading" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getPictureVoById, searchPictureByAi } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PictureList from '@/components/PictureList.vue'

const route = useRoute()

const pictureId = computed(() => {
  return route.query?.pictureId
})
const picture = ref<API.PictureVO>({})
const searchTags = ref<string[]>([])

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoById({
      id: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
      searchTags.value = res.data.data.tags ?? []
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  }
}

// 以标签搜图结果
const dataList = ref<API.PictureVO[]>([])
const loading = ref<boolean>(true)

// 获取搜图结果
const fetchResultData = async () => {
  loading.value = true
  try {
    const res = await searchPictureByAi({
      pictureId: pictureId.value,
      topK: 10,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取数据失败：' + e.message)
  }
  loading.value = false
}

// 页面加载时请求一次
onMounted(() => {
  fetchPictureDetail()
  fetchResultData()
})
</script>

<style scoped>
#searchPictureByTagsPage {
  margin-bottom: 16px;
}
</style>
