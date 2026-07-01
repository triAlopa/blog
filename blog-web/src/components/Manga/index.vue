<template>
  <div class="bangumi-container">
    <div class="bangumi-header">
      <h2 class="bangumi-title">
        <span class="title-bar"></span>
        <span class="title-text">我的漫画</span>
      </h2>
      <p class="bangumi-subtitle">漫画阅读记录</p>
    </div>

    <!-- 过滤按钮 -->
    <div class="filter-container">
      <button v-for="f in filters" :key="f.value"
        :class="['filter-tag', { active: currentFilter === f.value }]"
        @click="handleFilter(f.value)">
        {{ f.label }}
        <span class="filter-count">{{ getCount(f.value) }}</span>
      </button>
    </div>

    <!-- 加载/错误/空 -->
    <div v-if="loading" class="state-box">
      <div class="loading-spinner"></div>
      <p class="state-text">加载中...</p>
    </div>
    <div v-else-if="error" class="state-box">
      <div class="state-icon">😢</div>
      <h3>{{ error }}</h3>
      <p class="state-desc">请检查 API 配置是否正确</p>
      <button class="retry-btn" @click="fetchData">重试</button>
    </div>
    <div v-else-if="animeList.length === 0" class="state-box">
      <div class="state-icon">😢</div>
      <h3>暂无追番数据</h3>
      <p class="state-desc">请在后台配置 Bangumi 用户ID</p>
    </div>

    <!-- 追番列表 -->
    <div v-else class="anime-grid" ref="animeGrid">
      <div class="anime-grid-inner">
        <div v-for="anime in animeList" :key="anime.id"
          class="anime-card" :data-anime-status="anime.status">
          <div class="anime-cover-container">
            <a :href="anime.link" target="_blank" class="anime-cover-link">
              <img :src="anime.cover" :alt="anime.title" class="anime-cover" @error="onImgError" />
              <div class="cover-overlay">
                <div class="play-btn">
                  <svg width="18" height="18" fill="#fff" viewBox="0 0 24 24"><path d="M8 5v14l11-7z"/></svg>
                </div>
              </div>
            </a>
            <div :class="['status-tag', 'status-' + anime.status]">
              <span>{{ statusIcon(anime.status) }}</span> {{ statusText(anime.status) }}
            </div>
            <div v-if="anime.rating > 0" class="rating-tag">
              <svg width="10" height="10" fill="#ffc107" viewBox="0 0 20 20"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/></svg>
              {{ anime.rating }}
            </div>
            <div v-if="anime.status === 'watching'" class="progress-bar">
              <div class="progress-track">
                <div class="progress-fill" :style="{ width: pct(anime) + '%' }"></div>
              </div>
              <div class="progress-text">{{ anime.progress }}/{{ anime.totalEpisodes }} ({{ Math.round(pct(anime)) }}%)</div>
            </div>
          </div>
          <div class="anime-content">
            <h3 class="anime-title" :title="anime.title">{{ anime.title }}</h3>
            <p class="anime-desc" :title="anime.description">{{ anime.description || '暂无简介' }}</p>
            <div class="anime-meta">
              <div class="meta-row">
                <span class="meta-label">年份</span>
                <span class="meta-value">{{ anime.year }}</span>
              </div>
              <div class="meta-tags">
                <span v-for="g in anime.genre" :key="g" class="genre-tag">{{ g }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAllMangaCollectionsApi } from '@/api/bangumi'

export default {
  name: 'Manga',
  data() {
    return {
      loading: false,
      error: null,
      animeList: [],
      currentFilter: 'all',
      filters: [
        { label: '全部', value: 'all' },
        { label: '在读', value: 'reading' },
        { label: '想看', value: 'planned' },
        { label: '看过', value: 'completed' },
        { label: '搁置', value: 'onhold' },
        { label: '抛弃', value: 'dropped' }
      ]
    }
  },
  mounted() { this.fetchData() },
  methods: {
    async fetchData() {
      this.loading = true
      this.error = null
      try {
        const res = await getAllMangaCollectionsApi()
        if (res.code === 200) {
          console.log('Bangumi API 数据:', res.data)
          const list = []
          const map = { reading: 3, completed: 2, planned: 1, onhold: 4, dropped: 5 }
          Object.entries(map).forEach(([status]) => {
            if (res.data[status] && res.data[status].data) {
              res.data[status].data.forEach(item => {
                list.push(this.toAnime(item, status))
              })
            }
          })
          console.log('处理后列表:', list)
          this.animeList = list
        } else {
          this.error = res.message || '获取数据失败'
        }
      } catch (e) {
        console.error(e)
        this.error = '获取数据失败，请检查 API 配置'
      } finally {
        this.loading = false
      }
    },
    toAnime(item, status) {
      const s = item.subject || {}
      const infobox = s.infobox || []

      // 调试日志
      if (infobox.length > 0) {
        console.log('条目:', s.name_cn, 'infobox 长度:', infobox.length)
        console.log('infobox keys:', infobox.map(x => x.key))
      }

      // 从 infobox 提取制作方
      let studio = '未知'
      const keys = ['动画制作', '动画工作室', '制作公司', '製作', '主动画师', '动画制片人']
      for (const k of keys) {
        const found = infobox.find(x => x.key === k)
        if (found && found.value) {
          if (typeof found.value === 'string') { studio = found.value; break }
          if (Array.isArray(found.value)) {
            const names = found.value.map(v => (typeof v === 'object' && v) ? (v.v || v.name || '') : String(v)).filter(Boolean)
            if (names.length) { studio = names.join('、'); break }
          }
        }
      }

      if (studio !== '未知') {
        console.log('条目:', s.name_cn, '制作方:', studio)
      }

      return {
        id: item.subject_id || s.id,
        title: s.name_cn || s.name || '未知标题',
        status, rating: item.rate ? +item.rate.toFixed(1) : 0,
        cover: s.images?.medium || s.images?.large || '',
        description: (s.short_summary || '').trim(),
        year: s.date ? s.date.split('-')[0] : '未知',
        genre: s.tags ? s.tags.slice(0, 3).map(t => t.name) : [],
        studio, link: s.id ? `https://bgm.tv/subject/${s.id}` : '#',
        progress: item.ep_status || 0, totalEpisodes: s.eps || item.ep_status || 0
      }
    },
    handleFilter(val) {
      if (this.currentFilter === val) return
      const self = this
      const grid = this.$refs.animeGrid
      if (!grid) { this.currentFilter = val; return }
      // Step1: 缩小消失
      grid.querySelectorAll('.anime-card').forEach(el => {
        el.style.transition = 'all 0.15s ease-out'
        el.style.opacity = '0'
        el.style.transform = 'scale(0.3)'
      })
      setTimeout(() => {
        self.currentFilter = val
        self.$nextTick(() => {
          self.$nextTick(() => {
            const g = self.$refs.animeGrid; if (!g) return
            // 先全部隐藏
            g.querySelectorAll('.anime-card').forEach(el => { el.classList.add('anime-hidden'); el.style.cssText = '' })
            // 找出应显示的
            const show = []
            g.querySelectorAll('.anime-card').forEach(el => {
              const st = el.getAttribute('data-anime-status')
              if (self.currentFilter === 'all' || st === self.currentFilter) { el.classList.remove('anime-hidden'); show.push(el) }
            })
            // 初始状态
            show.forEach(el => { el.style.opacity = '0'; el.style.transform = 'scale(0.3)'; el.style.transition = 'none' })
            g.offsetHeight
            // 放大出现
            show.forEach((el, i) => {
              setTimeout(() => {
                el.style.transition = 'all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1)'
                el.style.opacity = '1'
                el.style.transform = 'scale(1)'
              }, i * 20)
            })
            setTimeout(() => { show.forEach(el => { el.style.cssText = '' }) }, 300)
          })
        })
      }, 150)
    },
    getCount(v) { return v === 'all' ? this.animeList.length : this.animeList.filter(a => a.status === v).length },
    pct(a) { return a.totalEpisodes ? (a.progress / a.totalEpisodes * 100) : 0 },
    statusText(s) { return { reading: '在读', completed: '看过', planned: '想看', onhold: '搁置', dropped: '抛弃' }[s] || s },
    statusIcon(s) { return { reading: '📖', completed: '✓', planned: '⏰', onhold: '⏸', dropped: '✗' }[s] || '?' },
    onImgError(e) { e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjMwMCIgZmlsbD0iI2YzZjRmNiIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIGZpbGw9IiM5Y2EzYWYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7mmoLml6Dlm77niYc8L3RleHQ+PC9zdmc+' }
  }
}
</script>

<style scoped>
.bangumi-container { padding: 20px; }
.bangumi-header { margin-bottom: 20px; }
.bangumi-title { font-size: 24px; font-weight: 700; color: #333; margin-bottom: 6px; display: flex; align-items: center; gap: 10px; }
.title-bar { width: 4px; height: 20px; border-radius: 2px; background: #409eff; flex-shrink: 0; }
.bangumi-subtitle { color: #999; font-size: 14px; padding-left: 14px; }

.filter-container { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }
.filter-tag { padding: 8px 16px; border: 1px solid #e0e0e0; border-radius: 6px; background: #fff; color: #666; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px; }
.filter-tag:hover:not(.active) { background: #f5f5f5; border-color: #409eff; }
.filter-tag.active { background: #409eff; color: #fff; border-color: #409eff; }
.filter-count { font-size: 12px; background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 10px; }
.filter-tag.active .filter-count { background: rgba(255,255,255,0.2); }

.state-box { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 20px; background: #fff; border-radius: 8px; border: 1px solid #e8e8e8; }
.state-icon { font-size: 48px; margin-bottom: 12px; }
.state-text { margin-top: 16px; color: #999; font-size: 14px; }
.state-box h3 { font-size: 16px; color: #333; margin-bottom: 4px; }
.state-desc { color: #999; font-size: 14px; margin-bottom: 16px; }
.retry-btn { padding: 8px 20px; background: #409eff; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; }
.retry-btn:hover { background: #337ecc; }
.loading-spinner { width: 36px; height: 36px; border: 3px solid #e8e8e8; border-top-color: #409eff; border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* 网格 - 默认一行5个 */
.anime-grid { container-type: inline-size; }
.anime-grid-inner { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; }
@container (max-width: 850px) { .anime-grid-inner { grid-template-columns: repeat(4, 1fr); gap: 10px; } }
@container (max-width: 650px) { .anime-grid-inner { grid-template-columns: repeat(3, 1fr); gap: 8px; } }
@container (max-width: 450px) { .anime-grid-inner { grid-template-columns: repeat(2, 1fr); gap: 8px; } }

/* 卡片容器 */
.anime-card { background: #fff; border: 1px solid #e8e8e8; border-radius: 8px; overflow: hidden; transition: all 0.3s; box-shadow: 0 2px 4px rgba(0,0,0,0.05); display: flex; flex-direction: column; position: relative; }
.anime-card:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); border-color: #d0d0d0; }
.anime-hidden { display: none !important; }

/* 封面容器 - 竖屏 3:5 比例，更矮 */
.anime-cover-container { position: relative; aspect-ratio: 3/4.5; overflow: hidden; background: #f5f5f5; }
.anime-cover-link { display: block; width: 100%; height: 100%; }
.anime-cover { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.anime-cover-link:hover .anime-cover { transform: scale(1.05); }
.cover-overlay { position: absolute; inset: 0; background: linear-gradient(to top, rgba(0,0,0,0.5), transparent 60%); opacity: 0; transition: opacity 0.3s; display: flex; align-items: center; justify-content: center; }
.anime-cover-link:hover .cover-overlay { opacity: 1; }
.play-btn { width: 44px; height: 44px; border-radius: 50%; background: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; transform: scale(0.8); transition: transform 0.3s; }
.anime-cover-link:hover .play-btn { transform: scale(1); }

/* 标签 */
.status-tag { position: absolute; top: 8px; left: 8px; padding: 2px 6px; border-radius: 3px; font-size: 11px; font-weight: 500; display: flex; align-items: center; gap: 3px; }
.status-reading { background: #e8eaf6; color: #283593; }
.status-completed { background: #e3f2fd; color: #1565c0; }
.status-planned { background: #fff3e0; color: #e65100; }
.status-onhold { background: #f3e5f5; color: #7b1fa2; }
.status-dropped { background: #ffebee; color: #c62828; }

.rating-tag { position: absolute; top: 8px; right: 8px; padding: 2px 5px; border-radius: 3px; background: rgba(0,0,0,0.55); color: #fff; font-size: 11px; display: flex; align-items: center; gap: 2px; }

/* 进度条 */
.progress-bar { position: absolute; bottom: 0; left: 0; right: 0; padding: 6px 8px; background: linear-gradient(to top, rgba(0,0,0,0.55), transparent); }
.progress-track { width: 100%; height: 3px; background: rgba(255,255,255,0.2); border-radius: 2px; overflow: hidden; margin-bottom: 2px; }
.progress-fill { height: 100%; background: #4caf50; border-radius: 2px; transition: width 0.3s; }
.progress-text { color: rgba(255,255,255,0.9); font-size: 10px; }

/* 内容 */
.anime-content { padding: 10px; flex: 1; display: flex; flex-direction: column; }
.anime-title { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 4px; line-height: 1.3; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.anime-desc { font-size: 12px; color: #888; margin-bottom: 6px; line-height: 1.5; flex: 1; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.anime-meta { font-size: 11px; margin-top: auto; }
.meta-row { display: flex; justify-content: space-between; margin-bottom: 2px; }
.meta-label { color: #999; }
.meta-value { color: #666; }
.meta-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 6px; }
.genre-tag { padding: 2px 6px; background: #f5f5f5; color: #666; border-radius: 3px; font-size: 10px; }
.genre-tag:hover { background: #e8e8e8; }

/* 暗色 */
:root.dark [data-theme="dark"] .bangumi-container,
.bangumi-container { }
:root.dark .bangumi-container, [data-theme="dark"] .bangumi-container {
  .bangumi-title, .anime-title { color: #e0e0e0; }
  .bangumi-subtitle, .anime-desc, .meta-label { color: #999; }
  .filter-tag { background: #2a2a2a; border-color: #404040; color: #ccc; }
  .anime-card { background: #2a2a2a; border-color: #404040; }
  .anime-cover-container { background: #333; }
  .meta-value { color: #bbb; }
  .genre-tag { background: #333; color: #aaa; }
}

@media (max-width: 640px) {
  .bangumi-container { padding: 12px; }
  .bangumi-title { font-size: 20px; }
  .filter-tag { padding: 6px 12px; font-size: 13px; }
  .anime-content { padding: 8px; }
}
</style>
