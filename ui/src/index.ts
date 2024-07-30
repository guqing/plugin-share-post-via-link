import type { ListedPost } from "@halo-dev/api-client";
import { definePlugin } from "@halo-dev/console-shared";
import { markRaw, type Ref } from "vue";
import RiShareForwardBoxLine from "~icons/ri/share-forward-box-line";
import PostShareLinkCreationDropdownItem from "./components/PostShareLinkCreationDropdownItem.vue";
import SharedPostLinkList from "./views/SharedPostLinkList.vue";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "PostsRoot",
      route: {
        path: "shared-links",
        name: "SharedPostLinks",
        component: SharedPostLinkList,
        meta: {
          title: "文章分享链接",
          permissions: ["plugin:share-post-via-link:manage"],
          menu: {
            name: "分享链接",
            icon: markRaw(RiShareForwardBoxLine),
          },
        },
      },
    },
  ],
  extensionPoints: {
    "post:list-item:operation:create": (post: Ref<ListedPost>) => {
      return [
        {
          priority: 20,
          component: markRaw(PostShareLinkCreationDropdownItem),
          props: {
            post,
          },
        },
      ];
    },
  },
});
