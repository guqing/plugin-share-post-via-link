<script lang="ts" setup>
import {VButton, VModal, VSpace,} from "@halo-dev/components";
import {ref} from "vue";
import type {Post} from "@halo-dev/api-client";
import {PostShareLink, PostShareLinkSpecShareTypeEnum} from "@/api/generated";

const props = withDefaults(
  defineProps<{
    post?: Post;
  }>(),
  {
    post: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved", param: PostShareLink): void;
}>();

const modal = ref<InstanceType<typeof VModal>>();

const isSubmitting = ref(false);
const expirationAt = ref<string | undefined>(undefined);

const formState = ref<PostShareLink>({
  metadata: {
    generateName: 'shared-post-'
  },
  spec: {
    postName: props.post.metadata.name,
    expirationAt: undefined,
    owner: '',
    shareType: 'PUBLISHED'
  },
});

const handleSubmit = () => {

}
</script>

<template>
  <VModal
    ref="modal"
    :centered="false"
    :width="700"
    title="创建分享"
    @close="emit('close')"
  >
    <FormKit
      id="post-share-setting-form"
      :config="{ validationVisibility: 'submit' }"
      name="post-share-setting-form"
      type="form"
      @submit="handleSubmit"
    >
      <div>
        <div class="md:grid md:grid-cols-4 md:gap-6">
          <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
            <FormKit
              v-model="expirationAt"
              label="过期时间"
              max="9999-12-31T23:59"
              min="0000-01-01T00:00"
              type="datetime-local"
            ></FormKit>
            <FormKit
              v-model="formState.spec.shareType"
              :options="[
                { label: '发布后可见', value: PostShareLinkSpecShareTypeEnum.Published},
                { label: '保存后可见', value: PostShareLinkSpecShareTypeEnum.Latest },
              ]"
              label="分享类型"
              name="shareType"
              type="radio"
            >
            </FormKit>
          </div>
        </div>

        <div class="py-5">
          <div class="border-t border-gray-200"></div>
        </div>
      </div>
    </FormKit>

    <template #footer>
      <div class="flex items-center justify-between">
        <VSpace>
          <VButton
            :loading="isSubmitting"
            type="secondary"
            @click="handleSubmit()"
          >
            创建
          </VButton>
          <VButton type="default" @click="modal?.close()">
            取消
          </VButton>
        </VSpace>
      </div>
    </template>
  </VModal>
</template>
