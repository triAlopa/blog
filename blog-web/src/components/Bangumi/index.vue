<template>
  <div class="bangumi-container">
    <!-- 页面标题 -->
    <div class="bangumi-header">
      <h2 class="bangumi-title">
        <span class="title-bar"></span>
        <span class="title-text">我的追番</span>
      </h2>
      <p class="bangumi-subtitle">记录我的二次元之旅</p>
    </div>

    <!-- 过滤按钮 -->
    <div class="filter-container">
      <button
        v-for="filter in filters"
        :key="filter.value"
        :class="['filter-tag', { active: currentFilter === filter.value }]"
        @click="handleFilter(filter.value)"
      >
        {{ filter.label }}
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">加载中...</p>
    </div>

    <!-- 错误提示 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">😢</div>
      <h3 class="error-title">{{ error }}</h3>
      <p class="error-desc">请检查 API 配置是否正确</p>
      <button class="retry-btn" @click="fetchData">重试</button>
    </div>

    <!-- 空状态 -->
    <div v-else-if="animeList.length === 0" class="empty-container">
      <div class="empty-icon">😢</div>
      <h3 class="empty-title">暂无追番数据</h3>
      <p class="empty-desc">请在后台配置 Bangumi 用户ID</p>
    </div>

    <!-- 追番列表 -->
    <div v-else class="anime-grid" ref="animeGrid">
      <transition-group name="anime-card" tag="div" class="anime-grid-inner">
        <div
          v-for="anime in filteredAnimeList"
          :key="anime.id"
          :class="['anime-card', { 'anime-hidden': !isVisible(anime) }]"
          :data-anime-status="anime.status"
        >
          <!-- 封面区域 - 竖屏比例 2:3 -->
          <div class="anime-cover-container">
            <a :href="anime.link" target="_blank" rel="noopener noreferrer" class="anime-cover-link">
              <img
                :src="anime.cover"
                :alt="anime.title"
                class="anime-cover"
                @error="handleImageError"
                loading="lazy"
              />
              <!-- 悬停遮罩 -->
              <div class="anime-cover-overlay">
                <div class="play-button">
                  <svg class="play-icon" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z"/>
                  </svg>
                </div>
              </div>
            </a>

            <!-- 状态标签 -->
            <div :class="['anime-status-tag', getStatusClass(anime.status)]">
              <span class="status-icon">{{ getStatusIcon(anime.status) }}</span>
              <span class="status-text">{{ getStatusText(anime.status) }}</span>
            </div>

            <!-- 评分标签 -->
            <div v-if="anime.rating > 0" class="anime-rating-tag">
              <svg class="star-icon" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
              </svg>
              <span>{{ anime.rating }}</span>
            </div>

            <!-- 进度条 - 仅在看状态显示 -->
            <div v-if="anime.status === 'watching'" class="anime-progress-bar">
              <div class="progress-track">
                <div
                  class="progress-fill"
                  :style="{ width: getProgressPercent(anime) + '%' }"
                ></div>
              </div>
              <div class="progress-text">
                {{ anime.progress }}/{{ anime.totalEpisodes }} ({{ Math.round(getProgressPercent(anime)) }}%)
              </div>
            </div>
          </div>

          <!-- 内容区域 -->
          <div class="anime-content">
            <h3 class="anime-title" :title="anime.title">{{ anime.title }}</h3>
            <p class="anime-description" :title="anime.description">
              {{ anime.description || '暂无简介' }}
            </p>

            <!-- 详细信息 -->
            <div class="anime-meta">
              <div class="meta-row">
                <span class="meta-label">年份</span>
                <span class="meta-value">{{ anime.year || '未知' }}</span>
              </div>
              <div class="meta-row">
                <span class="meta-label">制作</span>
                <span class="meta-value">{{ anime.studio || '未知' }}</span>
              </div>
              <div class="meta-tags">
                <span
                  v-for="tag in anime.genre"
                  :key="tag"
                  class="genre-tag"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<script>
import { getAllCollectionsApi } from '@/api/bangumi'

export default {
  name: 'Bangumi',
  data() {
    return {
      loading: false,
      error: null,
      animeList: [],
      currentFilter: 'all',
      filters: [
        { label: '全部', value: 'all' },
        { label: '在看', value: 'watching' },
        { label: '想看', value: 'planned' },
        { label: '看过', value: 'completed' },
        { label: '搁置', value: 'onhold' },
        { label: '抛弃', value: 'dropped' }
      ]
    }
  },
  computed: {
    filteredAnimeList() {
      if (this.currentFilter === 'all') {
        return this.animeList
      }
      return this.animeList.filter(a => a.status === this.currentFilter)
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    async fetchData() {
      this.loading = true
      this.error = null

      try {
        const res = await getAllCollectionsApi()
        if (res.code === 200) {
          this.processData(res.data)
        } else {
          this.error = res.message || '获取数据失败'
        }
      } catch (err) {
        console.error('获取追番数据失败:', err)
        this.error = '获取数据失败，请检查 API 配置'
      } finally {
        this.loading = false
      }
    },

    processData(data) {
      const list = []
      const statusMap = {
        watching: 3,
        completed: 2,
        planned: 1,
        onhold: 4,
        dropped: 5
      }

      Object.entries(statusMap).forEach(([status, type]) => {
        const key = status
        if (data[key] && data[key].data) {
          data[key].data.forEach(item => {
            list.push(this.transformItem(item, status))
          })
        }
      })

      this.animeList = list
    },

    transformItem(item, status) {
      const subject = item.subject || {}
      const progress = item.ep_status || 0
      const totalEpisodes = subject.eps || progress

      // 提取制作公司
      let studio = '未知'
      if (subject.tags) {
        const studioTag = subject.tags.find(t =>
          t.name.includes('动画制作') || t.name.includes('製作') || t.name.includes('制作')
        )
        if (studioTag) studio = studioTag.name
      }

      return {
        id: item.subject_id || subject.id,
        title: subject.name_cn || subject.name || '未知标题',
        status: status,
        rating: item.rate ? parseFloat(item.rate.toFixed(1)) : 0,
        cover: subject.images?.medium || subject.images?.large || '/default-cover.jpg',
        description: (subject.short_summary || '').trim(),
        year: subject.date ? subject.date.split('-')[0] : '未知',
        genre: subject.tags ? subject.tags.slice(0, 3).map(t => t.name) : [],
        studio: studio,
        link: subject.id ? `https://bgm.tv/subject/${subject.id}` : '#',
        progress: progress,
        totalEpisodes: totalEpisodes
      }
    },

    handleFilter(value) {
      this.currentFilter = value
    },

    isVisible(anime) {
      return this.currentFilter === 'all' || anime.status === this.currentFilter
    },

    getProgressPercent(anime) {
      if (!anime.totalEpisodes || anime.totalEpisodes === 0) return 0
      return (anime.progress / anime.totalEpisodes) * 100
    },

    getStatusText(status) {
      const map = {
        watching: '在看',
        completed: '看过',
        planned: '想看',
        onhold: '搁置',
        dropped: '抛弃'
      }
      return map[status] || status
    },

    getStatusClass(status) {
      const map = {
        watching: 'status-watching',
        completed: 'status-completed',
        planned: 'status-planned',
        onhold: 'status-onhold',
        dropped: 'status-dropped'
      }
      return map[status] || ''
    },

    getStatusIcon(status) {
      const map = {
        watching: '▶',
        completed: '✓',
        planned: '⏰',
        onhold: '⏸',
        dropped: '✗'
      }
      return map[status] || '?'
    },

    handleImageError(e) {
      e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2YzZjRmNiIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIGZpbGw9IiM5Y2EzYWYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7mmoLml6Dlm77niYc8L3RleHQ+PC9zdmc+'
    }
  }
}
</script>

<style scoped>
/* ========================================
   Apple iOS 26 Liquid Glass 设计风格
   ======================================== */

.bangumi-container {
  padding: 0;
}

/* 页面标题 */
.bangumi-header {
  margin-top: 0.5rem;
  margin-bottom: 1.5rem;
  padding: 0 1.5rem;
}

.bangumi-title {
  font-size: 1.375rem;
  font-weight: 700;
  color: var(--text-primary, #1d1d1f);
  margin-bottom: 0.25rem;
  display: flex;
  align-items: center;
  gap: 0.375rem;
  letter-spacing: -0.01em;
}

.title-bar {
  width: 0.2rem;
  height: 1.125rem;
  border-radius: 0.375rem;
  background: linear-gradient(180deg, #007aff, #5856d6);
  flex-shrink: 0;
}

.title-text {
  line-height: 1.4;
}

.bangumi-subtitle {
  color: var(--text-secondary, #86868b);
  font-size: 0.75rem;
  padding-left: 0.575rem;
  font-weight: 400;
  letter-spacing: 0.01em;
}

/* ========================================
   筛选按钮 - Apple Liquid Glass 风格
   ======================================== */
.filter-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.625rem;
  margin-bottom: 2rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 1.25rem;
  border: 0.5px solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 0 0 0.5px rgba(0, 0, 0, 0.02),
    0 2px 8px rgba(0, 0, 0, 0.04),
    inset 0 0.5px 0 rgba(255, 255, 255, 0.5);
}

.filter-tag {
  padding: 0.5rem 1.125rem;
  border: none;
  border-radius: 2rem;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: #1d1d1f;
  font-size: 0.8125rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  letter-spacing: -0.01em;
  border: 0.5px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.filter-tag:hover:not(.active) {
  background: rgba(255, 255, 255, 0.8);
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.filter-tag.active {
  background: linear-gradient(135deg, #007aff, #5856d6);
  color: #fff;
  border-color: transparent;
  box-shadow:
    0 2px 12px rgba(0, 122, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transform: scale(1.02);
}

.filter-tag:active {
  transform: scale(0.98);
}

/* ========================================
   加载/错误/空状态
   ======================================== */
.loading-container,
.error-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 1.5rem;
  border: 0.5px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.loading-spinner {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid rgba(0, 122, 255, 0.1);
  border-top-color: #007aff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 1rem;
  color: #86868b;
  font-size: 0.875rem;
  font-weight: 500;
}

.error-icon,
.empty-icon {
  font-size: 3.5rem;
  margin-bottom: 1.25rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.error-title,
.empty-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 0.5rem;
  letter-spacing: -0.01em;
}

.error-desc,
.empty-desc {
  color: #86868b;
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
  text-align: center;
}

.retry-btn {
  padding: 0.625rem 1.75rem;
  background: linear-gradient(135deg, #007aff, #5856d6);
  color: #fff;
  border: none;
  border-radius: 2rem;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.01em;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 122, 255, 0.3);
}

.retry-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 122, 255, 0.4);
}

.retry-btn:active {
  transform: scale(0.98);
}

/* ========================================
   动漫网格 - 使用容器查询实现响应式
   ======================================== */
.anime-grid {
  container-type: inline-size;
}

.anime-grid-inner {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

@container (min-width: 1200px) {
  .anime-grid-inner {
    grid-template-columns: repeat(5, 1fr);
  }
}

@container (min-width: 900px) and (max-width: 1199px) {
  .anime-grid-inner {
    grid-template-columns: repeat(4, 1fr);
  }
}

@container (min-width: 600px) and (max-width: 899px) {
  .anime-grid-inner {
    grid-template-columns: repeat(3, 1fr);
  }
}

@container (max-width: 599px) {
  .anime-grid-inner {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.75rem;
  }
}

/* ========================================
   动漫卡片 - Apple Liquid Glass 风格
   ======================================== */
.anime-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 0.5px solid rgba(255, 255, 255, 0.8);
  border-radius: 1.25rem;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 0 0 0.5px rgba(0, 0, 0, 0.02),
    0 2px 8px rgba(0, 0, 0, 0.04),
    inset 0 0.5px 0 rgba(255, 255, 255, 0.5);
}

.anime-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow:
    0 0 0 0.5px rgba(0, 0, 0, 0.02),
    0 12px 40px rgba(0, 0, 0, 0.12),
    inset 0 0.5px 0 rgba(255, 255, 255, 0.5);
  z-index: 10;
}

.anime-card.anime-hidden {
  display: none;
}

/* 封面容器 - 竖屏比例 2:3 */
.anime-cover-container {
  position: relative;
  aspect-ratio: 2/3;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f5f7, #e8e8ed);
}

.anime-cover-link {
  display: block;
  width: 100%;
  height: 100%;
  position: relative;
}

.anime-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.anime-cover-link:hover .anime-cover {
  transform: scale(1.08);
}

/* 悬停遮罩 */
.anime-cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent 60%);
  opacity: 0;
  transition: opacity 0.4s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.anime-cover-link:hover .anime-cover-overlay {
  opacity: 1;
}

.play-button {
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  transform: scale(0.7);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.anime-cover-link:hover .play-button {
  transform: scale(1);
}

.play-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: #1d1d1f;
  margin-left: 3px;
}

/* ========================================
   状态标签 - Liquid Glass 风格
   ======================================== */
.anime-status-tag {
  position: absolute;
  top: 0.625rem;
  left: 0.625rem;
  padding: 0.3rem 0.625rem;
  border-radius: 2rem;
  font-size: 0.6875rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  backdrop-filter: blur(10px) saturate(180%);
  -webkit-backdrop-filter: blur(10px) saturate(180%);
  letter-spacing: 0.01em;
}

.status-watching {
  background: rgba(52, 199, 89, 0.85);
  color: #fff;
  box-shadow: 0 2px 8px rgba(52, 199, 89, 0.3);
}

.status-completed {
  background: rgba(0, 122, 255, 0.85);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
}

.status-planned {
  background: rgba(255, 149, 0, 0.85);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 149, 0, 0.3);
}

.status-onhold {
  background: rgba(175, 82, 222, 0.85);
  color: #fff;
  box-shadow: 0 2px 8px rgba(175, 82, 222, 0.3);
}

.status-dropped {
  background: rgba(255, 59, 48, 0.85);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 59, 48, 0.3);
}

.status-icon {
  font-size: 0.625rem;
}

/* ========================================
   评分标签 - Liquid Glass 风格
   ======================================== */
.anime-rating-tag {
  position: absolute;
  top: 0.625rem;
  right: 0.625rem;
  padding: 0.3rem 0.625rem;
  border-radius: 2rem;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(10px) saturate(180%);
  -webkit-backdrop-filter: blur(10px) saturate(180%);
  color: #fff;
  font-size: 0.6875rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.star-icon {
  width: 0.75rem;
  height: 0.75rem;
  color: #ffd60a;
}

/* ========================================
   进度条
   ======================================== */
.anime-progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 0.75rem 0.875rem;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
}

.progress-track {
  width: 100%;
  height: 0.25rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2rem;
  overflow: hidden;
  margin-bottom: 0.375rem;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #34c759, #30d158);
  border-radius: 2rem;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.progress-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.6875rem;
  font-weight: 600;
  letter-spacing: 0.01em;
}

/* ========================================
   内容区域
   ======================================== */
.anime-content {
  padding: 0.875rem;
}

.anime-title {
  font-size: 0.8125rem;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: 0.375rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  letter-spacing: -0.01em;
}

.anime-description {
  font-size: 0.6875rem;
  color: #86868b;
  margin-bottom: 0.625rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

/* 详细信息 */
.anime-meta {
  font-size: 0.6875rem;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}

.meta-label {
  color: #86868b;
  font-weight: 500;
}

.meta-value {
  color: #1d1d1f;
  font-weight: 500;
}

.meta-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
  margin-top: 0.625rem;
}

.genre-tag {
  padding: 0.25rem 0.5rem;
  background: rgba(0, 122, 255, 0.08);
  color: #007aff;
  border-radius: 2rem;
  font-size: 0.625rem;
  font-weight: 500;
  transition: all 0.3s ease;
  border: 0.5px solid rgba(0, 122, 255, 0.12);
}

.genre-tag:hover {
  background: rgba(0, 122, 255, 0.15);
  transform: scale(1.02);
}

/* ========================================
   卡片动画
   ======================================== */
.anime-card-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.anime-card-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.anime-card-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(10px);
}

.anime-card-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* ========================================
   暗色主题 - Apple Dark Mode
   ======================================== */
:root.dark .bangumi-container,
[data-theme="dark"] .bangumi-container {
  .bangumi-title {
    color: #f5f5f7;
  }

  .bangumi-subtitle {
    color: #86868b;
  }

  .filter-container {
    background: rgba(44, 44, 46, 0.6);
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow:
      0 0 0 0.5px rgba(255, 255, 255, 0.05),
      0 2px 8px rgba(0, 0, 0, 0.2),
      inset 0 0.5px 0 rgba(255, 255, 255, 0.05);
  }

  .filter-tag {
    background: rgba(255, 255, 255, 0.08);
    color: #f5f5f7;
    border-color: rgba(255, 255, 255, 0.05);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  }

  .filter-tag:hover:not(.active) {
    background: rgba(255, 255, 255, 0.12);
  }

  .anime-card {
    background: rgba(44, 44, 46, 0.7);
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow:
      0 0 0 0.5px rgba(255, 255, 255, 0.05),
      0 2px 8px rgba(0, 0, 0, 0.2),
      inset 0 0.5px 0 rgba(255, 255, 255, 0.05);
  }

  .anime-cover-container {
    background: linear-gradient(135deg, #1c1c1e, #2c2c2e);
  }

  .anime-title {
    color: #f5f5f7;
  }

  .anime-description {
    color: #98989d;
  }

  .meta-label {
    color: #98989d;
  }

  .meta-value {
    color: #f5f5f7;
  }

  .genre-tag {
    background: rgba(10, 132, 255, 0.15);
    color: #0a84ff;
    border-color: rgba(10, 132, 255, 0.2);
  }

  .loading-container,
  .error-container,
  .empty-container {
    background: rgba(44, 44, 46, 0.6);
    border-color: rgba(255, 255, 255, 0.08);
  }

  .error-title,
  .empty-title {
    color: #f5f5f7;
  }
}

/* ========================================
   响应式设计
   ======================================== */
@media (max-width: 768px) {
  .filter-container {
    gap: 0.5rem;
    padding: 0.625rem;
    border-radius: 1rem;
  }

  .filter-tag {
    padding: 0.4375rem 0.875rem;
    font-size: 0.75rem;
  }
}

@media (max-width: 640px) {
  .bangumi-header {
    margin-bottom: 1.5rem;
  }

  .bangumi-title {
    font-size: 1.375rem;
  }

  .filter-container {
    gap: 0.375rem;
    padding: 0.5rem;
  }

  .filter-tag {
    padding: 0.375rem 0.75rem;
    font-size: 0.6875rem;
    border-radius: 1.5rem;
  }

  .anime-content {
    padding: 0.625rem;
  }

  .anime-title {
    font-size: 0.75rem;
  }

  .anime-description {
    font-size: 0.625rem;
  }

  .anime-status-tag,
  .anime-rating-tag {
    top: 0.5rem;
    padding: 0.25rem 0.5rem;
    font-size: 0.625rem;
  }

  .anime-status-tag {
    left: 0.5rem;
  }

  .anime-rating-tag {
    right: 0.5rem;
  }
}
</style>
