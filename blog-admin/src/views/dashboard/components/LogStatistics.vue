<template>
  <div class="log-statistics">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #007aff, #5856d6);">
              <el-icon size="24">
                <View/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(summary.todayCount) }}</div>
              <div class="stat-label">今日访问</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #34c759, #30d158);">
              <el-icon size="24">
                <TrendCharts/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(summary.totalCount) }}</div>
              <div class="stat-label">总访问量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #ff9500, #ff6b00);">
              <el-icon size="24">
                <User/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(summary.todayUserCount) }}</div>
              <div class="stat-label">今日用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #ff3b30, #ff2d55);">
              <el-icon size="24">
                <UserFilled/>
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatNumber(summary.totalUserCount) }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="mb-4">
      <!-- 访问趋势 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>访问趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button label="7">近7天</el-radio-button>
                <el-radio-button label="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 设备分布 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>设备分布</span>
          </template>
          <div ref="deviceChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 中国地图 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>地区分布</span>
          </template>
          <div ref="mapChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>

      <!-- 浏览器分布 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>浏览器分布</span>
          </template>
          <div ref="browserChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>

      <!-- 操作系统分布 -->
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <span>操作系统分布</span>
          </template>
          <div ref="osChartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, onUnmounted, nextTick} from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import chinaMap from '@/assets/map/china.json'

// 统计摘要
const summary = ref<any>({
  todayCount: 0,
  totalCount: 0,
  todayUserCount: 0,
  totalUserCount: 0
})
// 访问趋势天数
const trendDays = ref(7)

// 数字格式化函数（防止 NaN）
const formatNumber = (value: any): number => {
  const num = Number(value)
  return isNaN(num) ? 0 : num
}

// 图表引用
const trendChartRef = ref<HTMLElement>()
const deviceChartRef = ref<HTMLElement>()
const browserChartRef = ref<HTMLElement>()
const osChartRef = ref<HTMLElement>()
const mapChartRef = ref<HTMLElement>()

// 图表实例
let trendChart: echarts.ECharts | null = null
let deviceChart: echarts.ECharts | null = null
let browserChart: echarts.ECharts | null = null
let osChart: echarts.ECharts | null = null
let mapChart: echarts.ECharts | null = null

// 加载统计摘要
const loadSummary = async () => {
  try {
    const {data} = await request({
      url: '/sys/operateLog/statistics/summary',
      method: 'get'
    })
    // 校验数据，确保所有值都是数字
    summary.value = {
      todayCount: Number(data?.todayCount) || 0,
      totalCount: Number(data?.totalCount) || 0,
      todayUserCount: Number(data?.todayUserCount) || 0,
      totalUserCount: Number(data?.totalUserCount) || 0
    }
  } catch (error) {
    console.error('加载统计摘要失败:', error)
    // 设置默认值
    summary.value = {
      todayCount: 0,
      totalCount: 0,
      todayUserCount: 0,
      totalUserCount: 0
    }
  }
}

// 加载访问趋势
const loadTrend = async () => {
  try {
    const {data} = await request({
      url: '/sys/operateLog/statistics/trend',
      method: 'get',
      params: {days: trendDays.value}
    })

    if (trendChart && data) {
      // 校验数据
      const dates = data.map((item: any) => item.date || '')
      const counts = data.map((item: any) => Number(item.count) || 0)

      trendChart.setOption({
        xAxis: {
          data: dates
        },
        series: [{
          data: counts
        }]
      })
    }
  } catch (error) {
    console.error('加载访问趋势失败:', error)
  }
}

// 加载设备分布
const loadDevice = async () => {
  try {
    const {data} = await request({
      url: '/sys/operateLog/statistics/device',
      method: 'get'
    })

    if (deviceChart && data) {
      // 校验数据
      const validData = data.map((item: any) => ({
        name: item.name || '未知',
        value: Number(item.value) || 0
      })).filter((item: any) => item.value > 0)

      deviceChart.setOption({
        series: [{
          data: validData
        }]
      })
    }
  } catch (error) {
    console.error('加载设备分布失败:', error)
  }
}

// 加载浏览器分布
const loadBrowser = async () => {
  try {
    const {data} = await request({
      url: '/sys/operateLog/statistics/browser',
      method: 'get'
    })

    if (browserChart && data) {
      // 校验数据
      const validData = data.map((item: any) => ({
        name: item.name || '未知',
        value: Number(item.value) || 0
      })).filter((item: any) => item.value > 0)

      browserChart.setOption({
        series: [{
          data: validData
        }]
      })
    }
  } catch (error) {
    console.error('加载浏览器分布失败:', error)
  }
}

// 加载操作系统分布
const loadOs = async () => {
  try {
    const {data} = await request({
      url: '/sys/operateLog/statistics/os',
      method: 'get'
    })

    if (osChart && data) {
      // 校验数据
      const validData = data.map((item: any) => ({
        name: item.name || '未知',
        value: Number(item.value) || 0
      })).filter((item: any) => item.value > 0)

      osChart.setOption({
        series: [{
          data: validData
        }]
      })
    }
  } catch (error) {
    console.error('加载操作系统分布失败:', error)
  }
}

// 加载地区分布
const loadRegion = async () => {
  try {
    const {data} = await request({
      url: '/sys/operateLog/statistics/region',
      method: 'get'
    })

    console.log('地区分布数据:', data)  // 调试日志

    if (mapChart && Array.isArray(data) && data.length > 0) {
      // 校验数据，确保 value 不为 NaN 或 null
      const validData: { name: string; value: number }[] = []

      for (const item of data) {
        // 严格校验每个字段
        const name = typeof item === 'object' && item !== null ? String(item.name || '') : ''
        const value = typeof item === 'object' && item !== null ? Number(item.value) : 0

        if (name && name !== '' && !isNaN(value) && value > 0) {
          validData.push({name, value})
        }
      }

      console.log('校验后的数据:', validData)  // 调试日志

      if (validData.length > 0) {
        // 更新 visualMap 的最大值
        const maxValue = Math.max(...validData.map(item => item.value), 1)

        mapChart.setOption({
          visualMap: {
            max: maxValue
          },
          series: [{
            data: validData
          }]
        })
      }
    }
  } catch (error) {
    console.error('加载地区分布失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  // 访问趋势图
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        },
        axisLabel: {
          color: '#666'
        }
      },
      yAxis: {
        type: 'value',
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        },
        axisLabel: {
          color: '#666'
        },
        splitLine: {
          lineStyle: {
            color: '#f0f0f0'
          }
        }
      },
      series: [{
        name: '访问量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#007aff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              {offset: 0, color: 'rgba(0, 122, 255, 0.3)'},
              {offset: 1, color: 'rgba(0, 122, 255, 0.05)'}
            ]
          }
        },
        data: []
      }]
    })
  }

  // 设备分布饼图
  if (deviceChartRef.value) {
    deviceChart = echarts.init(deviceChartRef.value)
    deviceChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        bottom: 10,
        left: 'center'
      },
      series: [{
        name: '设备分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: []
      }]
    })
  }

  // 浏览器分布饼图
  if (browserChartRef.value) {
    browserChart = echarts.init(browserChartRef.value)
    browserChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        bottom: 10,
        left: 'center'
      },
      series: [{
        name: '浏览器分布',
        type: 'pie',
        radius: '60%',
        data: [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // 操作系统分布饼图
  if (osChartRef.value) {
    osChart = echarts.init(osChartRef.value)
    osChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        bottom: 10,
        left: 'center'
      },
      series: [{
        name: '操作系统',
        type: 'pie',
        radius: '60%',
        data: [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // 中国地图
  if (mapChartRef.value) {
    mapChart = echarts.init(mapChartRef.value)
    // 注册中国地图
    echarts.registerMap('china', chinaMap as any)
    mapChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: (params: any) => {
          // 👇 在这里添加校验
          const data = params.data
          if (!data || data.value === undefined || data.value === null || isNaN(data.value)) {
            return `${params.name}: 0`
          }
          return `${params.name}: ${data.value}`
        }
      },
      visualMap: {
        min: 0,
        max: 100,
        left: 'left',
        top: 'bottom',
        text: ['高', '低'],
        inRange: {
          color: ['#e0f3f8', '#007aff']
        },
        calculable: true
      },
      series: [{
        name: '访问量',
        type: 'map',
        map: 'china',
        roam: true,
        emphasis: {
          label: {
            show: true
          }
        },
        data: []
      }]
    })
  }
}

const processMapData = (data: any[]) => {
  if (!data || !Array.isArray(data)) {
    return []
  }

  return data.map(item => {
    // 确保每个数据项都有有效的value
    let value = item.value
    if (value === null || value === undefined || isNaN(value)) {
      value = 0
    }

    return {
      ...item,
      value: Number(value) // 确保是数字类型
    }
  })
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  trendChart?.resize()
  deviceChart?.resize()
  browserChart?.resize()
  osChart?.resize()
  mapChart?.resize()
}

onMounted(async () => {
  await nextTick()
  initCharts()

  // 加载数据
  await Promise.all([
    loadSummary(),
    loadTrend(),
    loadDevice(),
    loadBrowser(),
    loadOs(),
    loadRegion()
  ])

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  deviceChart?.dispose()
  browserChart?.dispose()
  osChart?.dispose()
  mapChart?.dispose()
})
</script>

<style scoped>
.log-statistics {
  padding: 20px;
}

.mb-4 {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #86868b;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
