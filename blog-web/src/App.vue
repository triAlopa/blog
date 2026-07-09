<template>
  <div id="app">
    <TheHeader />
    <MobileMenu />
    <SearchDialog />
    <router-view class="main-container" />
    <TheFooter />
    <FloatingButtons @sakura-change="onSakuraChange" />
    <SakuraFall :enabled="sakuraEnabled" :count="sakuraCount" />
    <Lantern />
    <RandomVideo />
    <div class="cursor-container"></div>
    <ContextMenu ref="contextMenuRef" />
  </div>
</template>

<script>
import TheHeader from '@/layout/Header/index.vue'
import TheFooter from '@/layout/Footer/index.vue'
import FloatingButtons from '@/components/common/FloatingButtons.vue'
import SakuraFall from '@/components/SakuraFall/index.vue'
import { getWebConfigApi, reportApi,getNoticeApi } from '@/api/site'
import { mapActions } from 'vuex'
import { initTheme, applyColorTheme } from '@/utils/theme'
import SearchDialog from '@/components/Search/index.vue'
import MobileMenu from '@/layout/MobileMenu/index.vue'
import Lantern from '@/components/Lanterns/index.vue'
import RandomVideo from '@/components/RandomVideo/index.vue'
import { getCookie,removeCookie } from '@/utils/cookie'
import ContextMenu from '@/components/ContextMenu/index.vue'

export default {
  name: 'App',
  components: {
    TheHeader,
    TheFooter,
    FloatingButtons,
    SakuraFall,
    SearchDialog,
    MobileMenu,
    Lantern,
    RandomVideo,
    ContextMenu,
  },
  data() {
    return {
      sakuraEnabled: false,
      sakuraCount: 15
    }
  },

  async created() {
    this.loadSakuraSettings()
    await reportApi()
    const res = await getWebConfigApi()
    this.setSiteInfo(res.data)
    this.$store.commit('setVisitorAccess', res.extra.visitorCount)
    this.$store.commit('setSiteAccess', res.extra.blogViewsCount)

    const noticeRes = await getNoticeApi()
    this.$store.commit('SET_NOTICE', noticeRes.data)

    // 应用后台配置的主题颜色
    const config = res.data
    if (config.primaryColor) {
      applyColorTheme({
        themeName: config.themeName || 'default',
        primaryColor: config.primaryColor,
        secondaryColor: config.secondaryColor,
        accentColor: config.accentColor,
        bgColor: config.bgColor,
        textColor: config.textColor,
        cardBgColor: config.cardBgColor,
        gradientStart: config.gradientStart,
        gradientEnd: config.gradientEnd,
        shadowColor: config.shadowColor
      })
    } else {
      initTheme()
    }

    await this.handleThirdPartyLogin()
    //这里等待第三方登录处理完成在获取用户信息
    await this.getUserInfo();

    //跳转到缓存地址
    let url = getCookie('redirectUrl')
    if (url) {
      removeCookie('redirectUrl')
      window.location.href = url
    }
  },
  methods: {
    ...mapActions(['setSiteInfo','getUserInfo']),

    /**
     * 樱花特效变化
     */
    onSakuraChange({ enabled, count }) {
      console.log('[App] Sakura change:', { enabled, count })
      this.sakuraEnabled = enabled
      this.sakuraCount = count
    },

    /**
     * 加载樱花设置
     */
    loadSakuraSettings() {
      const enabled = localStorage.getItem('sakura-enabled')
      const count = localStorage.getItem('sakura-count')
      if (enabled === '1') {
        this.sakuraEnabled = true
      }
      if (count) {
        this.sakuraCount = parseInt(count)
      }
    },

    /**
     * 处理第三方登录用回调逻辑
     */
     async handleThirdPartyLogin() {
      let flag = window.location.href.indexOf("token") != -1;
      if (flag) {
        let token = window.location.href.split("token=")[1];
        this.$store.commit('SET_TOKEN', token);
      }
    },

    /**
     * 初始化鼠标点击效果
     */
    initCursorEffect() {
      const container = document.querySelector('.cursor-container')
      
      document.addEventListener('click', (e) => {
        const cursor = document.createElement('div')
        cursor.className = 'cursor-fx'
        cursor.style.left = `${e.clientX}px`
        cursor.style.top = `${e.clientY}px`
        container.appendChild(cursor)
        
        cursor.addEventListener('animationend', () => {
          cursor.remove()
        })
      })
    },

    initContextMenu() {
      const handleContextMenu = (e) => {
        this.$refs.contextMenuRef.show(e)
      }

      const handleClick = () => {
        this.$refs.contextMenuRef.hide()
      }

      document.addEventListener('contextmenu', handleContextMenu)
      document.addEventListener('click', handleClick)

      // 在组件销毁时移除事件监听
      this.$once('hook:beforeDestroy', () => {
        document.removeEventListener('contextmenu', handleContextMenu)
        document.removeEventListener('click', handleClick)
      })
    }
  },
  mounted() {
    console.log('[App] Mounted, sakuraEnabled:', this.sakuraEnabled)
    this.initCursorEffect()
    this.initContextMenu()
  }
}
</script>

<style lang="scss">

@import 'animate.css';
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css');
* {
  margin: 0;
  padding: 0;
  font-family: "font";
  box-sizing: border-box;
}
</style> 