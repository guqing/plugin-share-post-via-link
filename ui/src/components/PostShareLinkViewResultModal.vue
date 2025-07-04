<script lang="ts" setup>
import { postShareApiClient } from '@/api'
import type { PostShareLink } from '@/api/generated'
import { Toast, VButton, VLoading, VModal, VSpace } from '@halo-dev/components'
import { useQuery } from '@tanstack/vue-query'
import { useClipboard } from '@vueuse/core'
import { ref, toRefs } from 'vue'

const props = withDefaults(
  defineProps<{
    postShareLink: PostShareLink
  }>(),
  {}
)

const { postShareLink } = toRefs(props)

const emit = defineEmits<{
  (event: 'close'): void
}>()

const modal = ref<InstanceType<typeof VModal> | null>(null)

const { copy } = useClipboard({ legacy: true })

const { data, isLoading } = useQuery({
  queryKey: ['plugin:share-post-via-link:get-by-name', postShareLink],
  queryFn: async () => {
    const { data } = await postShareApiClient.postShare.getPostShareLink({
      name: postShareLink.value.metadata.name
    })

    if (!data.status?.permalink) {
      throw new Error('Failed to get permalink')
    }

    return data
  },
  retry: 5
})

function handleCopyLink() {
  copy(data.value?.status?.permalink || '')
  Toast.success('复制成功')
  modal.value?.close()
}
</script>

<template>
  <VModal
    ref="modal"
    :centered="false"
    mount-to-body
    :width="500"
    title="创建分享链接成功"
    @close="emit('close')"
  >
    <div>
      <VLoading v-if="isLoading" />
      <div v-else class=":uno: flex flex-col space-y-2">
        <span class=":uno: text-sm text-gray-900"> 分享链接为： </span>
        <a
          class=":uno: text-sm text-indigo-500 hover:text-indigo-600"
          :href="data?.status?.permalink"
          target="_blanks"
        >
          {{ data?.status?.permalink }}
        </a>
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton :disabled="!data?.status?.permalink" type="secondary" @click="handleCopyLink">
          复制
        </VButton>
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
