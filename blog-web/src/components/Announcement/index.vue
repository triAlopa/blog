<template>
  <transition name="slide-fade">
    <div v-if="visible" class="announcement-container">
      <div class="announcement-content">
        <div class="announcement-wrapper">
          <div class="announcement-icon">
            <i class="fas fa-bullhorn"></i>
          </div>
          <div class="announcement-text">
            <span v-html="notice.content"></span>
          </div>
        </div>
        <div class="announcement-close" @click="close">
          <i class="fas fa-times"></i>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import { setCookieExpires, getCookie } from '@/utils/cookie'

// 前端本地配置（独立于后台 admin）
const ANNOUNCEMENT_CONFIG = {
  // 是否启用公告
  enabled: true,
  // 内边距（参考苹果圆角图标距离控制）
  padding: {
    horizontal: '1rem',  // 水平内边距
    vertical: '0.625rem' // 垂直内边距
  },
  // 圆角
  borderRadius: '0.75rem',
  // 字体大小
  fontSize: '0.875rem',
  // 图标大小
  iconSize: '1rem',
  // 关闭按钮大小
  closeBtnSize: '1.5rem',
  // 滚动速度（秒）
  scrollDuration: 20,
  // 自动关闭时间（毫秒，0 表示不自动关闭）
  autoClose: 0
}

export default {
  name: 'Announcement',
  data() {
    return {
      visible: false,
      notice: {},
      config: ANNOUNCEMENT_CONFIG,
      autoCloseTimer: null
    }
  },
  watch: {
    '$store.state.notice'(val) {
      if (val && val.top) {
        this.notice = val.top[0]
        if (getCookie('notice') == this.notice.id) return
        if (!this.config.enabled) return
        this.visible = true

        // 自动关闭
        if (this.config.autoClose > 0) {
          this.autoCloseTimer = setTimeout(() => {
            this.close()
          }, this.config.autoClose)
        }
      }
    }
  },
  beforeDestroy() {
    if (this.autoCloseTimer) {
      clearTimeout(this.autoCloseTimer)
    }
  },
  methods: {
    close() {
      setCookieExpires('notice', this.notice.id, 365)
      this.visible = false
      if (this.autoCloseTimer) {
        clearTimeout(this.autoCloseTimer)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.announcement-container {
  width: 100%;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.9), rgba(88, 86, 214, 0.9));
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  padding: 0.625rem 0;
  position: relative;
  overflow: hidden;
  box-shadow:
    0 0 0 0.5px rgba(255, 255, 255, 0.2),
    0 2px 8px rgba(0, 0, 0, 0.1);
}

.announcement-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 2rem;
  font-size: 0.875rem;
  position: relative;
  gap: 1rem;
}

.announcement-wrapper {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
  max-width: 700px;
  position: relative;
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 0.75rem;
  border: 0.5px solid rgba(255, 255, 255, 0.15);
  gap: 0.75rem;
}

.announcement-icon {
  font-size: 1rem;
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  z-index: 1;
  color: #ffd60a;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 214, 10, 0.15);
  border-radius: 0.5rem;
}

.announcement-text {
  flex: 1;
  overflow: hidden;
  position: relative;
  display: flex;
  justify-content: center;
  letter-spacing: 0.01em;

  span {
    white-space: nowrap;
    animation: scroll 20s linear infinite;
    padding-left: 100%;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.95);
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    font-size: 0.8125rem;

    &:hover {
      animation-play-state: paused;
    }
  }
}

.announcement-close {
  cursor: pointer;
  opacity: 0.8;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.15);
  border: 0.5px solid rgba(255, 255, 255, 0.1);

  &:hover {
    opacity: 1;
    background: rgba(255, 255, 255, 0.25);
    transform: rotate(90deg);
  }

  i {
    font-size: 0.75rem;
    color: rgba(255, 255, 255, 0.9);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
}

@keyframes scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-100%);
  }
}

.slide-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-content {
    padding: 0 1rem;
  }

  .announcement-wrapper {
    padding: 0.375rem 0.75rem;
    border-radius: 0.625rem;
  }

  .announcement-icon {
    width: 1.25rem;
    height: 1.25rem;
    font-size: 0.875rem;
  }

  .announcement-text span {
    font-size: 0.75rem;
  }

  .announcement-close {
    width: 1.25rem;
    height: 1.25rem;

    i {
      font-size: 0.625rem;
    }
  }
}
</style>