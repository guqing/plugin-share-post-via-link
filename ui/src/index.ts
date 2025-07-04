import type { ListedPost } from '@halo-dev/api-client'
import { VDropdownItem, VLoading } from '@halo-dev/components'
import { definePlugin } from '@halo-dev/console-shared'
import 'uno.css'
import { defineAsyncComponent, h, markRaw, type Ref } from 'vue'
import RiShareForwardBoxLine from '~icons/ri/share-forward-box-line'

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: 'PostsRoot',
      route: {
        path: 'shared-links',
        name: 'SharedPostLinks',
        component: defineAsyncComponent({
          loader: () => import('./views/SharedPostLinkList.vue'),
          loadingComponent: VLoading
        }),
        meta: {
          title: '文章分享链接',
          permissions: ['plugin:share-post-via-link:manage'],
          menu: {
            name: '分享链接',
            icon: markRaw(RiShareForwardBoxLine)
          }
        }
      }
    }
  ],
  extensionPoints: {
    'post:list-item:operation:create': (post: Ref<ListedPost>) => {
      return [
        {
          priority: 20,
          component: defineAsyncComponent({
            loader: () => import('./components/PostShareLinkCreationDropdownItem.vue'),
            loadingComponent: h(VDropdownItem, undefined, '加载中')
          }),
          props: {
            post
          }
        }
      ]
    }
  }
})
