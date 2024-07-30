<script lang="ts" setup>
import type { ListedPost } from "@halo-dev/api-client";
import { VDropdownItem } from "@halo-dev/components";
import { ref } from "vue";
import PostShareLinkCreationModal from "./PostShareLinkCreationModal.vue";

withDefaults(
  defineProps<{
    post: ListedPost;
  }>(),
  {},
);

const visible = ref(false);
</script>

<template>
  <div>
    <VDropdownItem @click="visible = true"> 创建分享链接 </VDropdownItem>
    <PostShareLinkCreationModal
      v-if="visible"
      :post="post.post"
      @close="visible = false"
    />
  </div>
</template>
