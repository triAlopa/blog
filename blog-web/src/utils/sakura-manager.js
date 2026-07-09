/**
 * 樱花飘落特效管理器
 * 基于 Canvas 实现，参考 BlogRepository 项目
 */

import sakuraImg from '@/assets/sakura.png'

// 默认配置（参考 BlogRepository）
const defaultConfig = {
  enable: true,
  sakuraNum: 5,
  limitTimes: -1, // -1 为无限循环
  size: {
    min: 0.5,
    max: 1.1
  },
  opacity: {
    min: 0.3,
    max: 0.9
  },
  speed: {
    horizontal: {
      min: -1.7,
      max: -1.2
    },
    vertical: {
      min: 1.5,
      max: 2.2
    },
    rotation: 0.03,
    fadeSpeed: 0.03 // 消失速度，不应大于最小不透明度
  },
  zIndex: 9999
}

// 樱花对象类
class Sakura {
  constructor(x, y, s, r, a, fn, idx, img, limitArray, config) {
    this.x = x
    this.y = y
    this.s = s
    this.r = r
    this.a = a
    this.fn = fn
    this.idx = idx
    this.img = img
    this.limitArray = limitArray
    this.config = config
  }

  draw(cxt) {
    cxt.save()
    cxt.translate(this.x, this.y)
    cxt.rotate(this.r)
    cxt.globalAlpha = this.a
    cxt.drawImage(this.img, 0, 0, 40 * this.s, 40 * this.s)
    cxt.restore()
  }

  update() {
    this.x = this.fn.x(this.x, this.y)
    this.y = this.fn.y(this.y, this.y)
    this.r = this.fn.r(this.r)
    this.a = this.fn.a(this.a)

    // 如果樱花越界或完全透明，重新调整位置
    if (
      this.x > window.innerWidth ||
      this.x < 0 ||
      this.y > window.innerHeight ||
      this.y < 0 ||
      this.a <= 0
    ) {
      // 如果樱花不做限制
      if (this.limitArray[this.idx] === -1) {
        this.resetPosition()
      } else {
        // 否则樱花有限制
        if (this.limitArray[this.idx] > 0) {
          this.resetPosition()
          this.limitArray[this.idx]--
        }
      }
    }
  }

  resetPosition() {
    this.r = getRandom('fnr', this.config)
    if (Math.random() > 0.4) {
      this.x = getRandom('x', this.config)
      this.y = 0
      this.s = getRandom('s', this.config)
      this.r = getRandom('r', this.config)
      this.a = getRandom('a', this.config)
    } else {
      this.x = window.innerWidth
      this.y = getRandom('y', this.config)
      this.s = getRandom('s', this.config)
      this.r = getRandom('r', this.config)
      this.a = getRandom('a', this.config)
    }
  }
}

// 樱花列表类
class SakuraList {
  constructor() {
    this.list = []
  }

  push(sakura) {
    this.list.push(sakura)
  }

  update() {
    for (let i = 0, len = this.list.length; i < len; i++) {
      this.list[i].update()
    }
  }

  draw(cxt) {
    for (let i = 0, len = this.list.length; i < len; i++) {
      this.list[i].draw(cxt)
    }
  }

  get(i) {
    return this.list[i]
  }

  size() {
    return this.list.length
  }
}

// 获取随机值的函数（完全参考 BlogRepository）
function getRandom(option, config) {
  let ret
  let random

  switch (option) {
    case 'x':
      ret = Math.random() * window.innerWidth
      break
    case 'y':
      ret = Math.random() * window.innerHeight
      break
    case 's':
      ret = config.size.min + Math.random() * (config.size.max - config.size.min)
      break
    case 'r':
      ret = Math.random() * 6
      break
    case 'a':
      ret = config.opacity.min + Math.random() * (config.opacity.max - config.opacity.min)
      break
    case 'fnx':
      random = config.speed.horizontal.min +
        Math.random() * (config.speed.horizontal.max - config.speed.horizontal.min)
      ret = (x, _y) => x + random
      break
    case 'fny':
      random = config.speed.vertical.min +
        Math.random() * (config.speed.vertical.max - config.speed.vertical.min)
      ret = (_x, y) => y + random
      break
    case 'fnr':
      ret = (r) => r + config.speed.rotation
      break
    case 'fna':
      ret = (alpha) => alpha - config.speed.fadeSpeed * 0.01
      break
  }
  return ret
}

// 樱花管理器类
export class SakuraManager {
  constructor(config) {
    this.config = { ...defaultConfig, ...config }
    this.canvas = null
    this.ctx = null
    this.sakuraList = null
    this.animationId = null
    this.img = null
    this.isRunning = false
    this.handleResize = this.handleResize.bind(this)
  }

  // 初始化樱花特效
  async init() {
    if (!this.config.enable || this.isRunning) {
      return
    }

    // 创建图片对象
    this.img = new Image()
    this.img.src = sakuraImg

    // 等待图片加载完成
    await new Promise((resolve, reject) => {
      if (this.img) {
        this.img.onload = () => resolve()
        this.img.onerror = () => reject(new Error('Failed to load sakura image'))
      }
    })

    this.createCanvas()
    this.createSakuraList()
    this.startAnimation()
    this.isRunning = true
  }

  // 创建画布
  createCanvas() {
    this.canvas = document.createElement('canvas')
    this.canvas.height = window.innerHeight
    this.canvas.width = window.innerWidth
    this.canvas.setAttribute(
      'style',
      `position: fixed; left: 0; top: 0; pointer-events: none; z-index: ${this.config.zIndex};`
    )
    this.canvas.setAttribute('id', 'canvas_sakura')
    document.body.appendChild(this.canvas)
    this.ctx = this.canvas.getContext('2d')

    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
  }

  // 创建樱花列表
  createSakuraList() {
    if (!this.img || !this.ctx) return

    this.sakuraList = new SakuraList()
    const limitArray = new Array(this.config.sakuraNum).fill(this.config.limitTimes)

    for (let i = 0; i < this.config.sakuraNum; i++) {
      const randomX = getRandom('x', this.config)
      const randomY = getRandom('y', this.config)
      const randomS = getRandom('s', this.config)
      const randomR = getRandom('r', this.config)
      const randomA = getRandom('a', this.config)
      const randomFnx = getRandom('fnx', this.config)
      const randomFny = getRandom('fny', this.config)
      const randomFnR = getRandom('fnr', this.config)
      const randomFnA = getRandom('fna', this.config)

      const sakura = new Sakura(
        randomX,
        randomY,
        randomS,
        randomR,
        randomA,
        {
          x: randomFnx,
          y: randomFny,
          r: randomFnR,
          a: randomFnA
        },
        i,
        this.img,
        limitArray,
        this.config
      )

      sakura.draw(this.ctx)
      this.sakuraList.push(sakura)
    }
  }

  // 开始动画
  startAnimation() {
    if (!this.ctx || !this.canvas || !this.sakuraList) return

    const animate = () => {
      if (!this.ctx || !this.canvas || !this.sakuraList) return

      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)
      this.sakuraList.update()
      this.sakuraList.draw(this.ctx)
      this.animationId = requestAnimationFrame(animate)
    }

    this.animationId = requestAnimationFrame(animate)
  }

  // 处理窗口大小变化
  handleResize() {
    if (this.canvas) {
      this.canvas.width = window.innerWidth
      this.canvas.height = window.innerHeight
    }
  }

  // 停止樱花特效
  stop() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
      this.animationId = null
    }

    if (this.canvas) {
      document.body.removeChild(this.canvas)
      this.canvas = null
    }

    window.removeEventListener('resize', this.handleResize)
    this.isRunning = false
  }

  // 切换樱花特效
  toggle() {
    if (this.isRunning) {
      this.stop()
    } else {
      this.init()
    }
  }

  // 更新配置
  updateConfig(newConfig) {
    const wasRunning = this.isRunning
    if (wasRunning) {
      this.stop()
    }
    this.config = { ...defaultConfig, ...newConfig }
    if (wasRunning && newConfig.enable) {
      this.init()
    }
  }

  // 更新数量
  updateCount(count) {
    this.updateConfig({ ...this.config, sakuraNum: count })
  }

  // 获取运行状态
  getIsRunning() {
    return this.isRunning
  }
}

// 全局樱花管理器实例
let globalSakuraManager = null

// 初始化樱花特效
export function initSakura(config) {
  if (globalSakuraManager) {
    globalSakuraManager.updateConfig(config)
  } else {
    globalSakuraManager = new SakuraManager(config)
    if (config.enable) {
      globalSakuraManager.init()
    }
  }
  return globalSakuraManager
}

// 切换樱花特效
export function toggleSakura() {
  if (globalSakuraManager) {
    globalSakuraManager.toggle()
  }
}

// 停止樱花特效
export function stopSakura() {
  if (globalSakuraManager) {
    globalSakuraManager.stop()
    globalSakuraManager = null
  }
}

// 获取樱花管理器实例
export function getSakuraManager() {
  return globalSakuraManager
}

// 获取樱花特效运行状态
export function getSakuraStatus() {
  return globalSakuraManager ? globalSakuraManager.getIsRunning() : false
}
