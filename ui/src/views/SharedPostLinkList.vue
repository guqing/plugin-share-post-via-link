<script lang="ts" setup>
import { postShareApiClient } from '@/api'
import type { PostShareLink } from '@/api/generated'
import SharedPostLinkListItem from '@/components/SharedPostLinkListItem.vue'
import {
  Dialog,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VEntityContainer,
  VLoading,
  VPageHeader,
  VPagination
} from '@halo-dev/components'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { chunk } from 'es-toolkit'
import { computed, ref, watch } from 'vue'
import RiShareForwardBoxLine from '~icons/ri/share-forward-box-line'

const page = ref(1)
const size = ref(20)
const isCleaning = ref(false)
const queryClient = useQueryClient()

const { data, isLoading, refetch, isFetching } = useQuery({
  queryKey: ['plugin:share-post-via-link:list', page, size],
  queryFn: async () => {
    const { data } = await postShareApiClient.postShare.listPostShareLink({
      page: page.value,
      size: size.value
    })
    return data
  },
  refetchInterval(data) {
    const hasDeletingData = data?.items.some((item) => item.metadata.deletionTimestamp)
    return hasDeletingData ? 1000 : false
  }
})

const currentPageLinks = computed(() =>
  (data.value?.items || []).filter((item) => !item.metadata.deletionTimestamp)
)

watch(data, (result) => {
  if (result && result.page === page.value) {
    page.value = Math.min(page.value, Math.max(1, result.totalPages))
  }
})

async function fetchAllLinks() {
  const links: PostShareLink[] = []
  for (let page = 1; ; page++) {
    const { data } = await postShareApiClient.postShare.listPostShareLink({
      page,
      size: 1000,
      sort: ['metadata.name,asc']
    })
    links.push(...data.items)
    if (!data.hasNext) {
      return links.filter((item) => !item.metadata.deletionTimestamp)
    }
  }
}

function handleClean(scope: 'all' | 'page') {
  const pageLinks = [...currentPageLinks.value]
  Dialog.warning({
    title: scope === 'all' ? '清理所有链接' : '清理当前页',
    description:
      scope === 'all'
        ? '确定要清理所有文章分享链接吗？清理后，原有分享链接将无法访问。'
        : `确定要清理当前页的 ${pageLinks.length} 条分享链接吗？清理后，这些分享链接将无法访问。`,
    confirmType: 'danger',
    async onConfirm() {
      if (isCleaning.value) return
      isCleaning.value = true
      try {
        const links = scope === 'all' ? await fetchAllLinks() : pageLinks
        if (!links.length) {
          Toast.info('没有需要清理的分享链接')
          return
        }

        let failed = 0
        for (const batch of chunk(links, 5)) {
          const results = await Promise.allSettled(
            batch.map((link) =>
              postShareApiClient.postShare.deletePostShareLink({ name: link.metadata.name })
            )
          )
          failed += results.filter((result) => result.status === 'rejected').length
        }

        if (failed) {
          Toast.warning(
            `已提交 ${links.length - failed} 条链接的清理请求，${failed} 条失败，请重试`
          )
        } else {
          Toast.success(`已提交 ${links.length} 条链接的清理请求`)
        }
      } catch (error) {
        console.error('Failed to clean share links', error)
        Toast.error('清理失败，请重试')
      } finally {
        isCleaning.value = false
        await queryClient.invalidateQueries({
          queryKey: ['plugin:share-post-via-link:list']
        })
      }
    }
  })
}
</script>

<template>
  <VPageHeader title="文章分享链接">
    <template #icon>
      <RiShareForwardBoxLine class=":uno: mr-2 self-center" />
    </template>
    <template #actions>
      <div class=":uno: flex flex-wrap gap-2">
        <VButton
          type="danger"
          ghost
          :loading="isCleaning"
          :disabled="isCleaning || isFetching || !currentPageLinks.length"
          @click="handleClean('page')"
        >
          清理当前页
        </VButton>
        <VButton
          type="danger"
          ghost
          :loading="isCleaning"
          :disabled="isCleaning || isFetching || !data?.total"
          @click="handleClean('all')"
        >
          清理所有链接
        </VButton>
      </div>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!data?.items?.length" appear name="fade">
        <VEmpty
          message="当前没有已创建的分享链接，你可以在文章列表的更多按钮中创建"
          title="无分享链接"
        >
          <template #actions>
            <VButton :loading="isFetching" @click="refetch()">刷新</VButton>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <VEntityContainer>
          <SharedPostLinkListItem
            v-for="postShareLink in data?.items"
            :key="postShareLink.metadata.name"
            :post-share-link="postShareLink"
          />
        </VEntityContainer>
      </Transition>
      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :total="data?.total"
          :size-options="[20, 50, 100]"
        ></VPagination>
      </template>
    </VCard>
  </div>
</template>

<style scoped></style>
