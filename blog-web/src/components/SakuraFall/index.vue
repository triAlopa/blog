<template>
  <!-- Canvas 版本的樱花特效，无需 DOM 元素 -->
</template>

<script>
import { initSakura, getSakuraManager } from '@/utils/sakura-manager'

export default {
  name: 'SakuraFall',
  props: {
    enabled: {
      type: Boolean,
      default: false
    },
    count: {
      type: Number,
      default: 21
    }
  },
  watch: {
    enabled: {
      handler(val) {
        console.log('[SakuraFall] enabled changed:', val)
        this.updateSakura()
      },
      immediate: false
    },
    count(val) {
      console.log('[SakuraFall] count changed:', val)
      const manager = getSakuraManager()
      if (manager && this.enabled) {
        manager.updateCount(val)
      }
    }
  },
  methods: {
    updateSakura() {
      const manager = initSakura({
        enable: this.enabled,
        sakuraNum: this.count
      })
      if (this.enabled && !manager.getIsRunning()) {
        manager.init()
      } else if (!this.enabled && manager.getIsRunning()) {
        manager.stop()
      }
    }
  },
  mounted() {
    console.log('[SakuraFall] Mounted, enabled:', this.enabled)
    if (this.enabled) {
      this.updateSakura()
    }
  },
  beforeDestroy() {
    const manager = getSakuraManager()
    if (manager) {
      manager.stop()
    }
  }
}
</script>
