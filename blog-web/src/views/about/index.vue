<template>
  <div class="about-page">
    <!-- 标签页切换 -->
    <div class="tab-container">
      <div
        :class="['tab-item', { active: activeTab === 'about' }]"
        @click="activeTab = 'about'"
      >
        <span class="tab-icon">👤</span>
        <span class="tab-text">关于我</span>
      </div>
      <div
        :class="['tab-item', { active: activeTab === 'bangumi' }]"
        @click="activeTab = 'bangumi'"
      >
        <span class="tab-icon">📺</span>
        <span class="tab-text">我的追番</span>
      </div>
      <div
        :class="['tab-item', { active: activeTab === 'manga' }]"
        @click="activeTab = 'manga'"
      >
        <span class="tab-icon">📖</span>
        <span class="tab-text">我的漫画</span>
      </div>
    </div>

    <!-- 关于我 -->
    <el-card v-show="activeTab === 'about'" class="about-card">
      <div class="about-content" v-html="$store.state.webSiteInfo.aboutMe" ref="content">
      </div>
      <mj-image-preview ref="imagePreview" />
    </el-card>

    <!-- 我的追番 -->
    <el-card v-show="activeTab === 'bangumi'" class="bangumi-card">
      <Bangumi />
    </el-card>

    <!-- 我的漫画 -->
    <el-card v-show="activeTab === 'manga'" class="bangumi-card">
      <Manga />
    </el-card>
  </div>
</template>

<script>
import Bangumi from '@/components/Bangumi/index.vue'
import Manga from '@/components/Manga/index.vue'

export default {
  name: 'About',
  components: {
    Bangumi,
    Manga
  },
  data() {
    return {
      activeTab: 'about'
    }
  },
  mounted() {
    this.initImagePreview();
    // 检查 URL 参数，支持直接跳转
    if (this.$route.query.tab === 'bangumi') {
      this.activeTab = 'bangumi'
    }
    if (this.$route.query.tab === 'manga') {
      this.activeTab = 'manga'
    }
  },
  methods: {
    initImagePreview() {
      this.$nextTick(() => {
        setTimeout(() => {
          const content = this.$refs.content;
          if (content) {
            const images = content.getElementsByTagName('img');
            Array.from(images).forEach(img => {
              img.style.cursor = 'zoom-in';
              img.addEventListener('click', () => {
                this.$refs.imagePreview.show(img.src);
              });
            });
          }
        }, 500);
      });
    }
  }
}
</script>

<style lang="scss" scoped>

.about-page {
  max-width: 1200px;
  margin: 0 auto;
  margin-top: $spacing-lg;
  margin-bottom: $spacing-md;
  @include responsive(lg) {
    padding: $spacing-sm;
  }

  .tab-container {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    padding: 0 4px;
  }

  .tab-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    background: var(--card-bg, #fff);
    border: 1px solid var(--border-color, #e5e7eb);
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 14px;
    color: var(--text-primary, #1a1a1a);

    &:hover {
      border-color: var(--primary-color, #6366f1);
      color: var(--primary-color, #6366f1);
    }

    &.active {
      background: var(--primary-color, #6366f1);
      border-color: var(--primary-color, #6366f1);
      color: #fff;
    }

    .tab-icon {
      font-size: 16px;
    }

    .tab-text {
      font-weight: 500;
    }
  }

  .about-card {
    .about-content {
      line-height: 1.8;
      color: var(--text-primary);
      padding: $spacing-lg;
    }
  }

  .bangumi-card {
    :deep(.el-card__body) {
      padding: 0;
    }
  }
}

@include responsive(sm) {
  :deep(img) {
    width: 100% !important;
  }

  .tab-container {
    flex-direction: column;
    gap: 8px !important;
  }

  .tab-item {
    justify-content: center;
  }
}
</style>
