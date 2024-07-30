<script lang="ts" setup>
import { postShareApiClient } from "@/api";
import {
  PostShareLinkSpecShareTypeEnum,
  type PostShareLink,
} from "@/api/generated";
import { toISOString } from "@/utils/date";
import type { Post } from "@halo-dev/api-client";
import { VAlert, VButton, VModal, VSpace } from "@halo-dev/components";
import { useLocalStorage } from "@vueuse/core";
import { ref } from "vue";
import PostShareLinkViewResultModal from "./PostShareLinkViewResultModal.vue";

interface PostShareLinkFormState {
  expirationAt: string;
  shareType: PostShareLinkSpecShareTypeEnum;
}

const props = withDefaults(
  defineProps<{
    post: Post;
  }>(),
  {},
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const isSubmitting = ref(false);

const postShareLink = ref<PostShareLink>();
const viewResultModalVisible = ref(false);

async function onSubmit(data: PostShareLinkFormState) {
  isSubmitting.value = true;

  try {
    const { data: newPostShareLink } =
      await postShareApiClient.postShare.createPostShareLink({
        postShareLink: {
          kind: "PostShareLink",
          apiVersion: "postshare.guqing.io/v1alpha1",
          metadata: {
            generateName: "shared-post-",
            name: "",
          },
          spec: {
            postName: props.post.metadata.name,
            expirationAt: toISOString(data.expirationAt),
            // TODO: Replace with actual owner
            owner: "admin",
            shareType: data.shareType,
          },
        },
      });

    postShareLink.value = newPostShareLink;
    viewResultModalVisible.value = true;
  } catch (error) {
    console.error(error);
  } finally {
    isSubmitting.value = false;
  }
}

function onViewResultModalClose() {
  viewResultModalVisible.value = false;
  modal.value?.close();
}

const showAlert = useLocalStorage(
  "plugin:share-post-via-link:show-creation-tips",
  true,
);
</script>

<template>
  <VModal
    ref="modal"
    :centered="false"
    :width="600"
    title="创建分享链接"
    mount-to-body
    @close="emit('close')"
  >
    <div v-if="showAlert" class="mb-5">
      <VAlert
        type="info"
        title="提示"
        description="创建的分享链接不限制文章发布状态、可见性，访客只要得到此链接即可访问。"
        @close="showAlert = false"
      />
    </div>
    <FormKit
      id="post-share-creation-form"
      :config="{ validationVisibility: 'submit' }"
      name="post-share-creation-form"
      type="form"
      @submit="onSubmit"
    >
      <FormKit
        :options="[
          {
            label: '已发布的内容',
            value: PostShareLinkSpecShareTypeEnum.Published,
          },
          {
            label: '最新内容',
            value: PostShareLinkSpecShareTypeEnum.Latest,
          },
        ]"
        label="分享类型"
        name="shareType"
        :value="PostShareLinkSpecShareTypeEnum.Published"
        type="radio"
      >
      </FormKit>
      <FormKit
        name="expirationAt"
        label="过期时间"
        max="9999-12-31T23:59"
        min="0000-01-01T00:00"
        type="datetime-local"
        help="不设置代表永久有效，但可以手动删除"
      ></FormKit>
    </FormKit>

    <template #footer>
      <VSpace>
        <!-- @vue-ignore -->
        <VButton
          :loading="isSubmitting"
          type="secondary"
          @click="$formkit.submit('post-share-creation-form')"
        >
          创建
        </VButton>
        <VButton @click="modal?.close()"> 取消 </VButton>
      </VSpace>
    </template>
  </VModal>

  <PostShareLinkViewResultModal
    v-if="postShareLink && viewResultModalVisible"
    :post-share-link="postShareLink"
    @close="onViewResultModalClose"
  />
</template>
