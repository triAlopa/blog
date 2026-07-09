/**
 * 主题工具 - 处理暗色模式和配色方案
 */

// 默认配色方案
const defaultColorTheme = {
  themeName: 'default',
  primaryColor: '#6366f1',
  secondaryColor: '#8b5cf6',
  accentColor: '#f59e0b',
  bgColor: '#ffffff',
  textColor: '#1f2937',
  cardBgColor: '#ffffff',
  gradientStart: '#6366f1',
  gradientEnd: '#8b5cf6',
  shadowColor: 'rgba(99,102,241,0.1)'
}

/**
 * 获取暗色/亮色模式
 */
export function getThemeMode() {
  const savedMode = localStorage.getItem('theme-mode')
  if (savedMode) {
    return savedMode
  }

  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark'
  }

  return 'light'
}

/**
 * 设置暗色/亮色模式
 */
export function setThemeMode(mode) {
  localStorage.setItem('theme-mode', mode)

  if (mode === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark')
  } else {
    document.documentElement.removeAttribute('data-theme')
  }
}

/**
 * 应用配色方案到 CSS 变量
 */
export function applyColorTheme(theme) {
  const root = document.documentElement
  const merged = { ...defaultColorTheme, ...theme }

  // 设置 CSS 变量
  root.style.setProperty('--primary-color', merged.primaryColor)
  root.style.setProperty('--secondary-color', merged.secondaryColor)
  root.style.setProperty('--accent-color', merged.accentColor)
  root.style.setProperty('--bg-color', merged.bgColor)
  root.style.setProperty('--text-color', merged.textColor)
  root.style.setProperty('--card-bg-color', merged.cardBgColor)
  root.style.setProperty('--gradient-start', merged.gradientStart)
  root.style.setProperty('--gradient-end', merged.gradientEnd)
  root.style.setProperty('--shadow-color', merged.shadowColor)

  // 计算颜色变体
  root.style.setProperty('--primary-light-color', lightenColor(merged.primaryColor, 20))
  root.style.setProperty('--primary-dark-color', darkenColor(merged.primaryColor, 10))
  root.style.setProperty('--secondary-dark-color', darkenColor(merged.secondaryColor, 10))

  // 计算带透明度的颜色（用于 rgba）
  const primaryRgb = hexToRgb(merged.primaryColor)
  const secondaryRgb = hexToRgb(merged.secondaryColor)
  if (primaryRgb) {
    root.style.setProperty('--primary-rgb', `${primaryRgb.r}, ${primaryRgb.g}, ${primaryRgb.b}`)
  }
  if (secondaryRgb) {
    root.style.setProperty('--secondary-rgb', `${secondaryRgb.r}, ${secondaryRgb.g}, ${secondaryRgb.b}`)
  }

  // 保存到本地
  localStorage.setItem('color-theme', JSON.stringify(merged))
}

/**
 * Hex 转 RGB
 */
function hexToRgb(hex) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : null
}

/**
 * 颜色变暗
 */
function darkenColor(color, percent) {
  const num = parseInt(color.replace('#', ''), 16)
  const amt = Math.round(2.55 * percent)
  const R = Math.max(0, (num >> 16) - amt)
  const G = Math.max(0, ((num >> 8) & 0x00FF) - amt)
  const B = Math.max(0, (num & 0x0000FF) - amt)
  return `#${(1 << 24 | R << 16 | G << 8 | B).toString(16).slice(1)}`
}

/**
 * 从本地存储加载配色方案
 */
export function loadColorTheme() {
  const saved = localStorage.getItem('color-theme')
  if (saved) {
    try {
      return JSON.parse(saved)
    } catch {
      return defaultColorTheme
    }
  }
  return defaultColorTheme
}

/**
 * 初始化主题（暗色模式 + 配色方案）
 */
export function initTheme() {
  // 初始化暗色模式
  const mode = getThemeMode()
  setThemeMode(mode)

  // 初始化配色方案
  const colorTheme = loadColorTheme()
  applyColorTheme(colorTheme)

  return mode === 'dark'
}

/**
 * 颜色变亮
 */
function lightenColor(color, percent) {
  const num = parseInt(color.replace('#', ''), 16)
  const amt = Math.round(2.55 * percent)
  const R = Math.min(255, (num >> 16) + amt)
  const G = Math.min(255, ((num >> 8) & 0x00FF) + amt)
  const B = Math.min(255, (num & 0x0000FF) + amt)
  return `#${(1 << 24 | R << 16 | G << 8 | B).toString(16).slice(1)}`
}

/**
 * 生成渐变 CSS
 */
export function getGradient(direction = '135deg') {
  const theme = loadColorTheme()
  return `linear-gradient(${direction}, ${theme.gradientStart}, ${theme.gradientEnd})`
}

/**
 * 生成阴影 CSS
 */
export function getShadow(size = 'md') {
  const theme = loadColorTheme()
  const shadow = theme.shadowColor

  const shadows = {
    sm: `0 2px 4px ${shadow}`,
    md: `0 4px 12px ${shadow}`,
    lg: `0 8px 24px ${shadow}`
  }

  return shadows[size] || shadows.md
} 