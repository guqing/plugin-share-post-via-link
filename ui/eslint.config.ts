import { globalIgnores } from 'eslint/config'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import pluginOxlint from 'eslint-plugin-oxlint'
import unocss from '@unocss/eslint-config/flat'
import skipFormatting from 'eslint-config-prettier/flat'

export default defineConfigWithVueTs(
  { files: ['**/*.{ts,mts,tsx,vue}'] },
  globalIgnores(['build/**', 'src/api/generated/**']),
  ...pluginVue.configs['flat/recommended'],
  vueTsConfigs.recommended,
  ...pluginOxlint.buildFromOxlintConfigFile('.oxlintrc.json'),
  unocss,
  { rules: { 'unocss/enforce-class-compile': 'warn' } },
  skipFormatting
)
