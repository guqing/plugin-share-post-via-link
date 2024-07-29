<script lang="ts" setup>
import {IconBookRead, VCard, VPageHeader,} from "@halo-dev/components";
</script>

<template>
  <VPageHeader title="文章分享管理">
    <template #icon>
      <IconBookRead class="mr-2 self-center"/>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
    </VCard>
  </div>
</template>

<style scoped>

</style>
