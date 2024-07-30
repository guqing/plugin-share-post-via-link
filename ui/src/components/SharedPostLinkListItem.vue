<script lang="ts" setup>
import { postShareApiClient } from "@/api";
import type { PostShareLink } from "@/api/generated";
import { formatDatetime } from "@/utils/date";
import {
  Dialog,
  IconExternalLinkLine,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
  VStatusDot,
} from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { useClipboard } from "@vueuse/core";

const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    postShareLink: PostShareLink;
  }>(),
  {},
);

const { copy } = useClipboard({ legacy: true });

function handleCopy() {
  copy(props.postShareLink.status?.permalink || "");
  Toast.success("复制成功");
}

function handleDelete() {
  Dialog.warning({
    title: "删除分享链接",
    description: "确定要删除该分享链接吗？",
    confirmType: "danger",
    async onConfirm() {
      await postShareApiClient.postShare.deletePostShareLink({
        name: props.postShareLink.metadata.name,
      });

      Toast.success("删除成功");

      queryClient.invalidateQueries({
        queryKey: ["plugin:share-post-via-link:list"],
      });
    },
  });
}
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField
        :title="postShareLink.status?.title"
        :description="postShareLink.status?.permalink"
      >
        <template #extra>
          <a
            target="_blank"
            :href="postShareLink.status?.permalink"
            class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
          >
            <IconExternalLinkLine class="size-3.5" />
          </a>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="postShareLink.metadata.deletionTimestamp">
        <template #description>
          <VStatusDot v-tooltip="`删除中`" state="warning" animate />
        </template>
      </VEntityField>
      <VEntityField
        :description="formatDatetime(postShareLink.metadata.creationTimestamp)"
      ></VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem @click="handleCopy">复制</VDropdownItem>
      <VDropdownDivider />
      <VDropdownItem type="danger" @click="handleDelete">删除</VDropdownItem>
    </template>
  </VEntity>
</template>
