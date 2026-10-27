import {viteConfig} from "@halo-dev/ui-plugin-bundler-kit/vite"
import path from "node:path"
import UnoCSS from "unocss/vite"
import Icons from "unplugin-icons/vite"

export default viteConfig({
  vite: {
    resolve: {
      alias: {
        "@": path.resolve(import.meta.dirname, "src")
      }
    },
    plugins: [
      UnoCSS({
        configFile: "./uno.config.ts",
        mode: "vue-scoped"
      }),
      Icons({
        compiler: "vue3"
      })
    ]
  }
})
