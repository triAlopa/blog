<template>
  <div class="floating-buttons" :class="{ 'show-top': showBackToTop }">

    <!-- 樱花飘落控制 -->
    <div class="sakura-control-wrapper">
      <transition name="slide-fade">
        <div v-show="showSakuraSlider" class="sakura-slider-panel">
          <span class="slider-label">飘落数量</span>
          <el-slider
            v-model="sakuraCount"
            :min="5"
            :max="50"
            :step="5"
            size="small"
            :show-tooltip="true"
          />
        </div>
      </transition>
      <el-tooltip :content="sakuraEnabled ? '关闭樱花' : '开启樱花'" placement="left">
        <button
          class="float-btn sakura-btn"
          :class="{ active: sakuraEnabled }"
          @click="toggleSakura"
        >
          <i class="fas fa-leaf"></i>
        </button>
      </el-tooltip>
    </div>

    <el-tooltip content="聊天" placement="left">
      <router-link to="/chat" class="float-btn chat-btn" title="聊天">
      <i class="fas fa-comments"></i>
    </router-link>
    </el-tooltip>

    <el-tooltip content="切换主题" placement="left">
      <button class="float-btn theme-btn" @click="toggleTheme" title="切换主题">
        <i :class="['fas', isDarkMode ? 'fa-sun' : 'fa-moon']"></i>
      </button>
    </el-tooltip>

    <el-tooltip content="回到顶部" placement="left">
      <button
        v-show="showBackToTop"
        class="float-btn top-btn"
        @click="scrollToTop"
        title="回到顶部"
      >
        <i class="fas fa-arrow-up"></i>
      </button>
    </el-tooltip>
  </div>
</template>

<script>
import { getThemeMode, setThemeMode, initTheme } from '@/utils/theme'

export default {
  name: 'FloatingButtons',
  data() {
    return {
      isDarkMode: false,
      showBackToTop: false,
      sakuraEnabled: false,
      sakuraCount: 15,
      showSakuraSlider: false,
      hideSliderTimer: null
    }
  },
  computed: {
    themeIcon() {
      return ['fas', this.isDarkMode ? 'fa-sun' : 'fa-moon']
    }
  },
  watch: {
    sakuraEnabled(val) {
      localStorage.setItem('sakura-enabled', val ? '1' : '0')
    },
    sakuraCount: {
      handler(val) {
        localStorage.setItem('sakura-count', val)
        this.emitSakuraChange()
      },
      immediate: false
    }
  },
  methods: {
    toggleTheme() {
      this.isDarkMode = !this.isDarkMode
      const mode = this.isDarkMode ? 'dark' : 'light'
      setThemeMode(mode)
    },
    scrollToTop() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    },
    handleScroll() {
      this.showBackToTop = window.pageYOffset > 300
    },
    toggleSakura() {
      console.log('[FloatingButtons] toggleSakura, current:', this.sakuraEnabled)
      this.sakuraEnabled = !this.sakuraEnabled
      this.showSakuraSlider = this.sakuraEnabled
      this.startHideSliderTimer()
      this.emitSakuraChange()
    },
    startHideSliderTimer() {
      if (this.hideSliderTimer) {
        clearTimeout(this.hideSliderTimer)
      }
      this.hideSliderTimer = setTimeout(() => {
        this.showSakuraSlider = false
      }, 3000)
    },
    emitSakuraChange() {
      console.log('[FloatingButtons] Emitting sakura-change:', {
        enabled: this.sakuraEnabled,
        count: this.sakuraCount
      })
      this.$emit('sakura-change', {
        enabled: this.sakuraEnabled,
        count: this.sakuraCount
      })
    },
    loadSakuraSettings() {
      const enabled = localStorage.getItem('sakura-enabled')
      const count = localStorage.getItem('sakura-count')
      if (enabled === '1') {
        this.sakuraEnabled = true
      }
      if (count) {
        this.sakuraCount = parseInt(count)
      }
      // 加载后通知父组件
      this.$nextTick(() => {
        this.emitSakuraChange()
      })
    }
  },
  mounted() {
    console.log('[FloatingButtons] Mounted')
    this.isDarkMode = initTheme()
    this.loadSakuraSettings()
    window.addEventListener('scroll', this.handleScroll)
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll)
    if (this.hideSliderTimer) {
      clearTimeout(this.hideSliderTimer)
    }
  }
}
</script>

<style lang="scss" scoped>
.floating-buttons {
  position: fixed !important;
  right: 20px !important;
  bottom: 100px !important;
  display: flex !important;
  flex-direction: column !important;
  gap: 8px !important;
  z-index: 9998 !important;
}

.float-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: #6366f1;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  opacity: 1;

  &:hover {
    opacity: 0.9;
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  }

  &.chat-btn {
    background: #10b981;
    text-decoration: none;
    animation: pulse 2s infinite;
  }

  &.theme-btn {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
  }

  &.sakura-btn {
    background: linear-gradient(135deg, #f093fb, #f5576c);

    &:hover {
      opacity: 0.9;
    }

    &.active {
      background: linear-gradient(135deg, #f5576c, #f093fb);
      animation: pulse-pink 2s infinite;
    }
  }

  &.top-btn {
    transform: translateY(100px);
    opacity: 0;
    visibility: hidden;
    background: #6366f1;

    .show-top & {
      transform: translateY(0);
      opacity: 1;
      visibility: visible;
      animation: bounceIn 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
    }
  }

  i {
    font-size: 1.2em;
  }
}

.sakura-control-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.sakura-slider-panel {
  position: absolute;
  right: 50px;
  top: 50%;
  transform: translateY(-50%);
  background: var(--card-bg);
  border-radius: 8px;
  padding: 8px 16px;
  box-shadow: $shadow-lg;
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
  min-width: 200px;

  .slider-label {
    font-size: 12px;
    color: var(--text-secondary);
  }

  :deep(.el-slider) {
    flex: 1;
  }
}

.slide-fade-enter-active {
  transition: all 0.3s ease;
}
.slide-fade-leave-active {
  transition: all 0.3s ease;
}
.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateY(-50%) translateX(10px);
  opacity: 0;
}

@keyframes bounceIn {
  0% {
    transform: translateY(100px) scale(0.3);
    opacity: 0;
  }
  50% {
    transform: translateY(-10px) scale(1.1);
  }
  70% {
    transform: translateY(5px) scale(0.95);
  }
  100% {
    transform: translateY(0) scale(1);
    opacity: 0.8;
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(99, 102, 241, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(99, 102, 241, 0);
  }
}

@keyframes pulse-pink {
  0% {
    box-shadow: 0 0 0 0 rgba(240, 147, 251, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(240, 147, 251, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(240, 147, 251, 0);
  }
}

@include responsive(sm) {
  .floating-buttons {
    right: 15px;
    bottom: 80px;
  }

  .float-btn {
    width: 36px;
    height: 36px;
  }

  .sakura-slider-panel {
    min-width: 160px;
    padding: 6px 12px;
  }
}
</style> 