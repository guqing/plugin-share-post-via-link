<script lang="ts" setup>
import { postShareApiClient } from "@/api";
import SharedPostLinkListItem from "@/components/SharedPostLinkListItem.vue";
import {
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import RiShareForwardBoxLine from "~icons/ri/share-forward-box-line";

const page = ref(1);
const size = ref(20);

const { data, isLoading, refetch, isFetching } = useQuery({
  queryKey: ["plugin:share-post-via-link:list", page, size],
  queryFn: async () => {
    const { data } = await postShareApiClient.postShare.listPostShareLink({
      page: page.value,
      size: size.value,
    });
    return data;
  },
  refetchInterval(data) {
    const hasDeletingData = data?.items
      .some((item) => item.metadata.deletionTimestamp);
    return hasDeletingData ? 1000 : false;
  },
});
</script>

<template>
  <VPageHeader title="文章分享链接">
    <template #icon>
      <RiShareForwardBoxLine class="mr-2 self-center" />
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
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
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li
            v-for="postShareLink in data?.items"
            :key="postShareLink.metadata.name"
          >
            <SharedPostLinkListItem :post-share-link="postShareLink" />
          </li>
        </ul>
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
