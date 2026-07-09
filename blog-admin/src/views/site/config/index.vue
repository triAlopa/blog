<template>
  <div class="app-container">
    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 基本信息 Tab -->
        <el-tab-pane name="basic">
          <template #label>
            <el-icon>
              <Setting />
            </el-icon>
            <span class="tab-label">基本信息</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="网站Logo" prop="logo">
                  <upload-image v-model="form.logo" :limit="1" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="网站名称" prop="name">
                  <el-input v-model="form.name" placeholder="请输入网站名称" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="网站介绍" prop="summary">
                  <el-input v-model="form.summary" type="textarea" placeholder="请输入网站介绍" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="备案号" prop="recordNum">
                  <el-input v-model="form.recordNum" placeholder="请输入备案号" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="网站地址" prop="webUrl">
                  <el-input v-model="form.webUrl" placeholder="请输入网站地址" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 作者信息 Tab -->
        <el-tab-pane name="author">
          <template #label>
            <el-icon>
              <User />
            </el-icon>
            <span class="tab-label">作者信息</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="作者头像" prop="authorAvatar">
                  <upload-image v-model="form.authorAvatar" :limit="1" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="作者名称" prop="author">
                  <el-input v-model="form.author" placeholder="请输入作者名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="个性签名" prop="authorInfo">
                  <el-input v-model="form.authorInfo" placeholder="请输入个性签名" />
                </el-form-item>
              </el-col>
            </el-row>
                <el-form-item label="关于我" prop="aboutMe">
                  <div style="border: 1px solid #ccc;">
                      <Toolbar style="border-bottom: 1px solid #ccc;width: 1200px;" :editor="editorRef" :defaultConfig="toolbarConfig" :mode="mode" />
                      <Editor style=" overflow-y: hidden;width: 1200px" v-model="form.aboutMe" :defaultConfig="editorConfig" :mode="mode"
                      @onCreated="handleCreated"/>
                  </div>
                </el-form-item>

          </el-form>
        </el-tab-pane>

        <!-- 社交信息 Tab -->
        <el-tab-pane name="social">
          <template #label>
            <el-icon>
              <Share />
            </el-icon>
            <span class="tab-label">社交信息</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-form-item label="Github地址" prop="github">
              <el-input v-model="form.github" placeholder="请输入Github地址">
                <template #prefix>
                  <el-icon>
                    <ElementPlus />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="Gitee地址" prop="gitee">
              <el-input v-model="form.gitee" placeholder="请输入Gitee地址">
                <template #prefix>
                  <el-icon>
                    <Platform />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="QQ号" prop="qqNumber">
              <el-input v-model="form.qqNumber" placeholder="请输入QQ号">
                <template #prefix>
                  <el-icon>
                    <ChatDotRound />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="QQ群" prop="qqGroup">
              <el-input v-model="form.qqGroup" placeholder="请输入QQ群">
                <template #prefix>
                  <el-icon>
                    <User />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="微信" prop="wechat">
              <el-input v-model="form.wechat" placeholder="请输入微信号">
                <template #prefix>
                  <el-icon>
                    <ChatLineRound />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱地址">
                <template #prefix>
                  <el-icon>
                    <Message />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 收款信息 Tab -->
        <el-tab-pane name="payment">
          <template #label>
            <el-icon>
              <Money />
            </el-icon>
            <span class="tab-label">收款信息</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="支付宝收款码" prop="aliPay">
                  <upload-image v-model="form.aliPay" :limit="1" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="微信收款码" prop="weixinPay">
                  <upload-image v-model="form.weixinPay" :limit="1" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 网站设置 Tab -->
        <el-tab-pane name="settings">
          <template #label>
            <el-icon>
              <Tools />
            </el-icon>
            <span class="tab-label">网站设置</span>
          </template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="游客头像" prop="touristAvatar">
                  <upload-image v-model="form.touristAvatar" :limit="1" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="显示的社交信息" prop="showList">
                  <el-select v-model="showList" multiple placeholder="请选择要显示的社交信息">
                    <el-option label="邮箱" value="email" />
                    <el-option label="QQ" value="qq" />
                    <el-option label="QQ群" value="qqGroup" />
                    <el-option label="Github" value="github" />
                    <el-option label="Gitee" value="gitee" />
                    <el-option label="微信" value="wechat" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="登录方式" prop="loginTypeList">
                  <el-select v-model="loginTypeList" multiple placeholder="请选择登录方式">
                    <el-option v-for="item in loginTypes" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="开启评论">
                  <el-switch v-model="form.openComment" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="开启赞赏">
                  <el-switch v-model="form.openAdmiration" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="开启灯笼">
                  <el-switch v-model="form.openLantern" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
            </el-row>
             <!--<el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="公告" prop="bulletin">
                  <el-input v-model="form.bulletin" type="textarea" :rows="3" placeholder="请输入公告内容" />
                </el-form-item>
              </el-col>
            </el-row>-->
          </el-form>
        </el-tab-pane>

        <!-- 主题配色 Tab -->
        <el-tab-pane name="theme">
          <template #label>
            <el-icon>
              <Brush />
            </el-icon>
            <span class="tab-label">主题配色</span>
          </template>

          <!-- 预设配色方案 -->
          <div class="theme-section">
            <div class="section-header">
              <h3 class="section-title">预设配色方案</h3>
              <el-button type="primary" size="small" @click="addPresetTheme">
                <el-icon><Plus /></el-icon>添加当前配色为预设
              </el-button>
            </div>
            <div class="theme-presets">
              <div
                v-for="(theme, index) in presetThemes"
                :key="theme.name"
                class="theme-card"
                :class="{ active: form.themeName === theme.name }"
                @click="applyPresetTheme(theme)"
              >
                <div class="theme-preview">
                  <div
                    class="preview-primary"
                    :style="{ background: theme.primary }"
                  ></div>
                  <div
                    class="preview-secondary"
                    :style="{ background: theme.secondary }"
                  ></div>
                  <div
                    class="preview-accent"
                    :style="{ background: theme.accent }"
                  ></div>
                </div>
                <div class="theme-card-footer">
                  <span class="theme-name">{{ theme.label }}</span>
                  <el-icon
                    v-if="index >= 12"
                    class="theme-delete"
                    @click.stop="removePresetTheme(index)"
                  >
                    <Delete />
                  </el-icon>
                </div>
              </div>
            </div>
          </div>

          <!-- 自定义颜色 -->
          <div class="theme-section">
            <h3 class="section-title">自定义颜色</h3>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="主色调">
                  <el-color-picker v-model="form.primaryColor" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.primaryColor" class="color-input" placeholder="#6366f1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="次要色">
                  <el-color-picker v-model="form.secondaryColor" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.secondaryColor" class="color-input" placeholder="#8b5cf6" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="强调色">
                  <el-color-picker v-model="form.accentColor" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.accentColor" class="color-input" placeholder="#f59e0b" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="背景色">
                  <el-color-picker v-model="form.bgColor" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.bgColor" class="color-input" placeholder="#ffffff" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="文字色">
                  <el-color-picker v-model="form.textColor" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.textColor" class="color-input" placeholder="#1f2937" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="卡片背景">
                  <el-color-picker v-model="form.cardBgColor" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.cardBgColor" class="color-input" placeholder="#ffffff" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="渐变开始">
                  <el-color-picker v-model="form.gradientStart" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.gradientStart" class="color-input" placeholder="#6366f1" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="渐变结束">
                  <el-color-picker v-model="form.gradientEnd" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.gradientEnd" class="color-input" placeholder="#8b5cf6" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="阴影颜色">
                  <el-color-picker v-model="form.shadowColor" show-alpha @change="updateThemeVars" />
                  <el-input v-model="form.shadowColor" class="color-input" placeholder="rgba(99,102,241,0.1)" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <!-- 实时预览 -->
          <div class="theme-section">
            <h3 class="section-title">实时预览</h3>
            <div class="theme-preview-box" :style="previewStyle">
              <div class="preview-header" :style="{ background: previewGradient }">
                <h2>预览效果</h2>
                <p>这是主题配色的实时预览</p>
              </div>
              <div class="preview-content">
                <el-button type="primary" :style="{ background: form.primaryColor, borderColor: form.primaryColor }">
                  主要按钮
                </el-button>
                <el-button :style="{ color: form.primaryColor, borderColor: form.primaryColor }">
                  次要按钮
                </el-button>
                <div class="preview-card" :style="{ background: form.cardBgColor, boxShadow: `0 4px 12px ${form.shadowColor}` }">
                  <p :style="{ color: form.textColor }">卡片内容预览</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 底部按钮 -->
      <div class="bottom-buttons">
        <el-button icon="Refresh" type="primary" v-permission="['sys:web:update']" @click="submitForm">保存配置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import UploadImage from '@/components/Upload/Image.vue'
import { getWebConfigApi, updateWebConfigApi } from '@/api/site/config'
import { getDictDataByDictTypesApi } from '@/api/system/dict'
import { uploadApi } from '@/api/file'

import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
const editorRef = shallowRef()
const mode = 'default'
const toolbarConfig = {}
const editorConfig = {
  placeholder: "请输入内容...",
  MENU_CONF: {
    // 配置上传图片
    uploadImage: {
      customUpload: contentUpload,
    },

    codeSelectLang: {
      // 代码语言
      codeLangs: [
        { text: "CSS", value: "css" },
        { text: "HTML", value: "html" },
        { text: "XML", value: "xml" },
        { text: "Java", value: "java" },
        // 其他
      ],
    },
  },
}


const activeTab = ref('basic')
const formRef = ref<FormInstance>()
const form = ref({
  logo: '',
  name: '',
  summary: '',
  recordNum: '',
  webUrl: '',
  author: '',
  authorInfo: '',
  authorAvatar: '',
  github: '',
  gitee: '',
  qqNumber: '',
  qqGroup: '',
  wechat: '',
  email: '',
  aliPay: '',
  weixinPay: '',
  showList: '',
  loginTypeList: '',
  openComment: 1,
  openAdmiration: 1,
  touristAvatar: '',
  bulletin: '',
  aboutMe: '',
  openLantern: 0,
  // 主题配色字段
  themeName: 'default',
  primaryColor: '#6366f1',
  secondaryColor: '#8b5cf6',
  accentColor: '#f59e0b',
  bgColor: '#ffffff',
  textColor: '#1f2937',
  cardBgColor: '#ffffff',
  gradientStart: '#6366f1',
  gradientEnd: '#8b5cf6',
  shadowColor: 'rgba(99,102,241,0.1)',
  themePresets: ''
})

// 默认预设配色方案（内置 12 套）
const defaultPresetThemes = [
  {
    name: 'default',
    label: '靛蓝紫 (默认)',
    primaryColor: '#6366f1',
    secondaryColor: '#8b5cf6',
    accentColor: '#f59e0b',
    bgColor: '#ffffff',
    textColor: '#1f2937',
    cardBgColor: '#ffffff',
    gradientStart: '#6366f1',
    gradientEnd: '#8b5cf6',
    shadowColor: 'rgba(99,102,241,0.1)',
    primary: '#6366f1',
    secondary: '#8b5cf6',
    accent: '#f59e0b'
  },
  {
    name: 'ocean',
    label: '海洋蓝',
    primaryColor: '#0ea5e9',
    secondaryColor: '#06b6d4',
    accentColor: '#f97316',
    bgColor: '#f0f9ff',
    textColor: '#0c4a6e',
    cardBgColor: '#ffffff',
    gradientStart: '#0ea5e9',
    gradientEnd: '#06b6d4',
    shadowColor: 'rgba(14,165,233,0.1)',
    primary: '#0ea5e9',
    secondary: '#06b6d4',
    accent: '#f97316'
  },
  {
    name: 'forest',
    label: '森林绿',
    primaryColor: '#10b981',
    secondaryColor: '#059669',
    accentColor: '#eab308',
    bgColor: '#f0fdf4',
    textColor: '#14532d',
    cardBgColor: '#ffffff',
    gradientStart: '#10b981',
    gradientEnd: '#059669',
    shadowColor: 'rgba(16,185,129,0.1)',
    primary: '#10b981',
    secondary: '#059669',
    accent: '#eab308'
  },
  {
    name: 'sunset',
    label: '日落橙',
    primaryColor: '#f97316',
    secondaryColor: '#ef4444',
    accentColor: '#8b5cf6',
    bgColor: '#fff7ed',
    textColor: '#7c2d12',
    cardBgColor: '#ffffff',
    gradientStart: '#f97316',
    gradientEnd: '#ef4444',
    shadowColor: 'rgba(249,115,22,0.1)',
    primary: '#f97316',
    secondary: '#ef4444',
    accent: '#8b5cf6'
  },
  {
    name: 'rose',
    label: '玫瑰红',
    primaryColor: '#e11d48',
    secondaryColor: '#be123c',
    accentColor: '#06b6d4',
    bgColor: '#fff1f2',
    textColor: '#881337',
    cardBgColor: '#ffffff',
    gradientStart: '#e11d48',
    gradientEnd: '#be123c',
    shadowColor: 'rgba(225,29,72,0.1)',
    primary: '#e11d48',
    secondary: '#be123c',
    accent: '#06b6d4'
  },
  {
    name: 'mocha',
    label: '摩卡棕',
    primaryColor: '#92400e',
    secondaryColor: '#78350f',
    accentColor: '#d97706',
    bgColor: '#fef3c7',
    textColor: '#451a03',
    cardBgColor: '#ffffff',
    gradientStart: '#92400e',
    gradientEnd: '#78350f',
    shadowColor: 'rgba(146,64,14,0.1)',
    primary: '#92400e',
    secondary: '#78350f',
    accent: '#d97706'
  },
  {
    name: 'aurora',
    label: '极光紫',
    primaryColor: '#7c3aed',
    secondaryColor: '#a855f7',
    accentColor: '#14b8a6',
    bgColor: '#f5f3ff',
    textColor: '#2e1065',
    cardBgColor: '#ffffff',
    gradientStart: '#7c3aed',
    gradientEnd: '#a855f7',
    shadowColor: 'rgba(124,58,237,0.1)',
    primary: '#7c3aed',
    secondary: '#a855f7',
    accent: '#14b8a6'
  },
  {
    name: 'dark',
    label: '暗夜黑',
    primaryColor: '#6366f1',
    secondaryColor: '#8b5cf6',
    accentColor: '#22d3ee',
    bgColor: '#0f172a',
    textColor: '#e2e8f0',
    cardBgColor: '#1e293b',
    gradientStart: '#6366f1',
    gradientEnd: '#8b5cf6',
    shadowColor: 'rgba(0,0,0,0.3)',
    primary: '#6366f1',
    secondary: '#8b5cf6',
    accent: '#22d3ee'
  },
  {
    name: 'teal-elegance',
    label: '蓝绿雅韵',
    primaryColor: '#3A506B',
    secondaryColor: '#5BC0BE',
    accentColor: '#C9A68B',
    bgColor: '#E6E9ED',
    textColor: '#33475E',
    cardBgColor: '#ffffff',
    gradientStart: '#3A506B',
    gradientEnd: '#5BC0BE',
    shadowColor: 'rgba(58,80,107,0.1)',
    primary: '#3A506B',
    secondary: '#5BC0BE',
    accent: '#C9A68B'
  },
  {
    name: 'rose-brown',
    label: '粉棕柔情',
    primaryColor: '#A77C74',
    secondaryColor: '#CFA6A0',
    accentColor: '#E8D5B7',
    bgColor: '#E6E9ED',
    textColor: '#624B3C',
    cardBgColor: '#ffffff',
    gradientStart: '#A77C74',
    gradientEnd: '#CFA6A0',
    shadowColor: 'rgba(167,124,116,0.1)',
    primary: '#A77C74',
    secondary: '#CFA6A0',
    accent: '#E8D5B7'
  },
  {
    name: 'sage-nature',
    label: '绿灰自然',
    primaryColor: '#5F6B4F',
    secondaryColor: '#A8B59F',
    accentColor: '#D9C9A3',
    bgColor: '#E6E9ED',
    textColor: '#5F6B4F',
    cardBgColor: '#ffffff',
    gradientStart: '#5F6B4F',
    gradientEnd: '#A8B59F',
    shadowColor: 'rgba(95,107,79,0.1)',
    primary: '#5F6B4F',
    secondary: '#A8B59F',
    accent: '#D9C9A3'
  },
  {
    name: 'mauve-grace',
    label: '紫灰雅致',
    primaryColor: '#5D546A',
    secondaryColor: '#B8A9C9',
    accentColor: '#A9B8C8',
    bgColor: '#E6E9ED',
    textColor: '#5D546A',
    cardBgColor: '#ffffff',
    gradientStart: '#5D546A',
    gradientEnd: '#B8A9C9',
    shadowColor: 'rgba(93,84,106,0.1)',
    primary: '#5D546A',
    secondary: '#B8A9C9',
    accent: '#A9B8C8'
  }
]

// 预设配色方案（从配置加载，fallback 到默认值）
const presetThemes = ref([])

// 加载预设主题
const loadPresetThemes = () => {
  if (form.value.themePresets) {
    try {
      const parsed = JSON.parse(form.value.themePresets)
      if (Array.isArray(parsed) && parsed.length > 0) {
        presetThemes.value = parsed
        return
      }
    } catch (e) {
      console.warn('解析预设配色方案失败，使用默认值', e)
    }
  }
  presetThemes.value = [...defaultPresetThemes]
}

// 添加当前配色为预设
const addPresetTheme = () => {
  ElMessageBox.prompt('请输入配色方案名称', '添加预设', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.{1,20}$/,
    inputErrorMessage: '名称长度为 1-20 个字符'
  }).then(({ value }) => {
    const newPreset = {
      name: 'custom-' + Date.now(),
      label: value,
      primaryColor: form.value.primaryColor,
      secondaryColor: form.value.secondaryColor,
      accentColor: form.value.accentColor,
      bgColor: form.value.bgColor,
      textColor: form.value.textColor,
      cardBgColor: form.value.cardBgColor,
      gradientStart: form.value.gradientStart,
      gradientEnd: form.value.gradientEnd,
      shadowColor: form.value.shadowColor,
      primary: form.value.primaryColor,
      secondary: form.value.secondaryColor,
      accent: form.value.accentColor
    }
    presetThemes.value.push(newPreset)
    form.value.themePresets = JSON.stringify(presetThemes.value)
    ElMessage.success('预设添加成功')
  }).catch(() => {})
}

// 删除预设主题
const removePresetTheme = (index) => {
  ElMessageBox.confirm('确定删除该预设配色方案？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    presetThemes.value.splice(index, 1)
    form.value.themePresets = JSON.stringify(presetThemes.value)
    ElMessage.success('预设已删除')
  }).catch(() => {})
}

// 预览样式
const previewStyle = computed(() => ({
  background: form.value.bgColor,
  color: form.value.textColor
}))

const previewGradient = computed(() =>
  `linear-gradient(135deg, ${form.value.gradientStart}, ${form.value.gradientEnd})`
)

// 应用预设主题
const applyPresetTheme = (theme: any) => {
  form.value.themeName = theme.name
  form.value.primaryColor = theme.primaryColor
  form.value.secondaryColor = theme.secondaryColor
  form.value.accentColor = theme.accentColor
  form.value.bgColor = theme.bgColor
  form.value.textColor = theme.textColor
  form.value.cardBgColor = theme.cardBgColor
  form.value.gradientStart = theme.gradientStart
  form.value.gradientEnd = theme.gradientEnd
  form.value.shadowColor = theme.shadowColor
  updateThemeVars()
  ElMessage.success(`已应用「${theme.label}」配色方案，请点击保存按钮持久化配置`)
}

// 更新 CSS 变量（实时预览）
const updateThemeVars = () => {
  const root = document.documentElement
  root.style.setProperty('--primary-color', form.value.primaryColor)
  root.style.setProperty('--secondary-color', form.value.secondaryColor)
  root.style.setProperty('--accent-color', form.value.accentColor)
  root.style.setProperty('--bg-color', form.value.bgColor)
  root.style.setProperty('--text-color', form.value.textColor)
  root.style.setProperty('--card-bg-color', form.value.cardBgColor)
  root.style.setProperty('--gradient-start', form.value.gradientStart)
  root.style.setProperty('--gradient-end', form.value.gradientEnd)
  root.style.setProperty('--shadow-color', form.value.shadowColor)
}
const showList = ref([])
const loginTypeList = ref([])
const loginTypes = ref<any>([])

const files = ref();

const rules = {
  name: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  logo: [{ required: true, message: '请上传网站Logo', trigger: 'change' }],
  summary: [{ required: true, message: '请输入网站介绍', trigger: 'blur' }],
  recordNum: [{ required: true, message: '请输入备案号', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者名称', trigger: 'blur' }]
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (valid) {
      form.value.showList = JSON.stringify(showList.value)
      form.value.loginTypeList = JSON.stringify(loginTypeList.value)
      form.value.themePresets = JSON.stringify(presetThemes.value)
      updateWebConfigApi(form.value).then(() => {
        ElMessage.success('保存成功')
      })
    }
  })
}
// 获取字典数据
const getDictDataByDictTypes = async () => {
  const res = await getDictDataByDictTypesApi(['login_type'])
  loginTypes.value = res.data.login_type.list
}

const handleCreated = (editor:any) => {
  editorRef.value = editor // 记录 editor 实例，重要！
}

//编辑器上传图片
function contentUpload(file: any, insertFn: any) {
  files.value = file;
  // FormData 对象
  var formData = new FormData();
  // 文件对象
  formData.append("file", files.value);
  uploadApi(formData).then((res: any) => {
    insertFn(res.data, "", res.data);
  });
}

onMounted(() => {
  getWebConfigApi().then((res) => {
    form.value = res.data
    if (form.value.showList) {
      showList.value = JSON.parse(form.value.showList)
    }
    if (form.value.loginTypeList) {
      loginTypeList.value = JSON.parse(form.value.loginTypeList)
    }
    loadPresetThemes()
  })

  getDictDataByDictTypes();
})
</script>

<style scoped>
.app-container {
  padding: 10px;
}

.bottom-buttons {
  margin-top: 20px;
  text-align: center;
}

.tab-label {
  margin-left: 4px;
  vertical-align: middle;
}

:deep(.el-tabs__item) {
  display: flex !important;
  align-items: center;
  justify-content: center;
}

:deep(.el-input-group__prepend) {
  padding: 0 10px;
}

.el-form-item {
  max-width: 600px;
}

/* 主题配色样式 */
.theme-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e5e7eb;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header .section-title {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.theme-presets {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.theme-card {
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.theme-card:hover {
  border-color: #6366f1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

.theme-card.active {
  border-color: #6366f1;
  background: #f5f3ff;
}

.theme-preview {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
}

.preview-primary,
.preview-secondary,
.preview-accent {
  flex: 1;
}

.theme-name {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.theme-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.theme-delete {
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
}

.theme-delete:hover {
  color: #f56c6c;
}

.color-input {
  width: 120px;
  margin-left: 8px;
}

:deep(.el-color-picker) {
  vertical-align: middle;
}

.theme-preview-box {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.preview-header {
  padding: 24px;
  color: white;
  text-align: center;
}

.preview-header h2 {
  margin: 0 0 8px;
  font-size: 20px;
}

.preview-header p {
  margin: 0;
  opacity: 0.9;
}

.preview-content {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
}

.preview-card {
  padding: 16px;
  border-radius: 8px;
  width: 100%;
}

.preview-card p {
  margin: 0;
}
</style>
