<template>
  <a-card v-if="selectedRowKeys.length > 0" style="margin-bottom: 16px">
    <template #title>
      <a-space>
        <span>已选择 <a-badge :count="selectedRowKeys.length" /></span>
        <a-button type="link" @click="handleClear">清空选择</a-button>
      </a-space>
    </template>
    <a-space wrap :size="[8, 8]">
      <a-tag v-for="id in selectedRowKeys" :key="id" closable @close="handleRemove(id)">
        {{ id }}
      </a-tag>
    </a-space>
  </a-card>
</template>

<script setup lang="ts">
interface Props {
  selectedRowKeys: string[]
}

defineProps<Props>()

const emit = defineEmits<{
  clear: []
  remove: [id: string]
}>()

const handleClear = () => {
  emit('clear')
}

const handleRemove = (id: string) => {
  emit('remove', id)
}
</script>
