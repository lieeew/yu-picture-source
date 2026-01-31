<template>
  <a-card title="选择图片" style="margin-bottom: 16px">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="handleSearch" style="margin-bottom: 16px">
      <a-form-item label="关键词">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介搜索"
          allow-clear
          style="width: 200px"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>

    <!-- 图片列表 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      :row-selection="rowSelection"
      :loading="loading"
      :row-key="(record: API.Picture) => String(record.id)"
      :row-class-name="getRowClassName"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="60" />
        </template>
        <template v-if="column.dataIndex === 'name'">
          <a-typography-paragraph :ellipsis="{ rows: 2 }" :content="record.name" style="margin-bottom: 0" />
        </template>
        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap :size="[4, 4]">
            <a-tag v-for="tag in getDisplayTags(record)" :key="tag">
              {{ tag }}
            </a-tag>
            <a-tag
              v-if="showMoreButton(record)"
              class="more-tag"
              @click="toggleExpand(record)"
            >
              {{ isExpanded(record) ? '收起' : `+${getAllTags(record.tags).length - 5}个` }}
            </a-tag>
          </a-space>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
      </template>
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { listPictureByPage } from '@/api/pictureController.ts'
import dayjs from 'dayjs'

// Props
interface Props {
  selectedRowKeys?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  selectedRowKeys: () => [],
})

// Emits
const emit = defineEmits<{
  'update:selectedRowKeys': [keys: string[]]
}>()

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 数据列表
const dataList = ref<API.Picture[]>([])
const total = ref(0)
const loading = ref(false)

// 标签展开状态
const expandedRecords = ref<Set<string>>(new Set())

// 获取所有标签
const getAllTags = (tagsJson: string | undefined) => {
  try {
    return JSON.parse(tagsJson || '[]')
  } catch {
    return []
  }
}

// 显示更多按钮的条件
const showMoreButton = (record: API.Picture) => {
  return getAllTags(record.tags).length > 5
}

// 根据展开状态获取显示的标签
const getDisplayTags = (record: API.Picture) => {
  const allTags = getAllTags(record.tags)
  if (expandedRecords.value.has(String(record.id))) {
    return allTags
  }
  return allTags.slice(0, 5)
}

// 检查是否展开
const isExpanded = (record: API.Picture) => {
  return expandedRecords.value.has(String(record.id))
}

// 切换展开状态
const toggleExpand = (record: API.Picture) => {
  const id = String(record.id)
  if (expandedRecords.value.has(id)) {
    expandedRecords.value.delete(id)
  } else {
    expandedRecords.value.add(id)
  }
}

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '图片',
    dataIndex: 'url',
    width: 80,
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: '标签',
    dataIndex: 'tags',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
]

// 分页配置
const pagination = computed(() => ({
  current: searchParams.current,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

// 行选择配置
const rowSelection = computed(() => ({
  type: 'checkbox' as const,
  selectedRowKeys: props.selectedRowKeys,
  preserveSelectedRowKeys: true,
  onChange: (keys: (string | number)[]) => {
    emit('update:selectedRowKeys', keys.map((k) => String(k)))
  },
}))

// 获取行样式类名
const getRowClassName = (record: API.Picture) => {
  return record.id && props.selectedRowKeys.includes(String(record.id)) ? 'selected-row' : ''
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await listPictureByPage({
      ...searchParams,
      nullSpaceId: true,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败', error)
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 表格变化
const handleTableChange = (page: { current?: number; pageSize?: number }) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const handleSearch = () => {
  searchParams.current = 1
  fetchData()
}

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})

// 暴露方法给父组件
defineExpose({
  refresh: fetchData,
})
</script>

<style scoped>
/* 选中行高亮样式 */
:deep(.selected-row) {
  background-color: #bae7ff !important;
}

:deep(.selected-row > td) {
  background-color: #bae7ff !important;
}

:deep(.selected-row:hover > td) {
  background-color: #91d5ff !important;
}

/* 选中行左边框强调 */
:deep(.selected-row > td:first-child) {
  position: relative;
}

:deep(.selected-row > td:first-child::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background-color: #1890ff;
}

/* 更多标签按钮样式 */
.more-tag {
  cursor: pointer;
  background-color: #f5f5f5;
  border-style: dashed;
}

.more-tag:hover {
  color: #1890ff;
  border-color: #1890ff;
}
</style>
