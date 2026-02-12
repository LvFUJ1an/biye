<template>
  <div class="dashboard-page">
    <el-card class="welcome-card">
      <template #header>
        <div class="welcome-header">
          <el-icon class="welcome-icon"><HomeFilled /></el-icon>
          <span>欢迎使用管理系统</span>
        </div>
      </template>
      <div class="welcome-content">
        <el-alert
          title="您已成功登录到IT知识分享平台管理员系统"
          type="success"
          :closable="false"
          show-icon
        />
        <div class="info-cards">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover" class="info-card">
                <el-icon class="info-icon"><User /></el-icon>
                <div class="info-detail">
                  <div class="info-title">用户总数</div>
                  <div class="info-value">{{ loading ? '加载中...' : dashboardData.userCount }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="info-card">
                <el-icon class="info-icon"><Document /></el-icon>
                <div class="info-detail">
                  <div class="info-title">文档数量</div>
                  <div class="info-value">{{ loading ? '加载中...' : dashboardData.ebookCount }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="info-card">
                <el-icon class="info-icon"><VideoPlay /></el-icon>
                <div class="info-detail">
                  <div class="info-title">视频数量</div>
                  <div class="info-value">{{ loading ? '加载中...' : dashboardData.videoCount }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="info-card">
                <el-icon class="info-icon"><ChatDotRound /></el-icon>
                <div class="info-detail">
                  <div class="info-title">博客数量</div>
                  <div class="info-value">{{ loading ? '加载中...' : dashboardData.blogCount }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div v-if="loading" class="loading-placeholder">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <div v-else ref="userChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>博客发布趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div v-if="loading" class="loading-placeholder">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span>加载中...</span>
            </div>
            <div v-else ref="blogChartRef" class="chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { 
  User, 
  Document, 
  VideoPlay, 
  HomeFilled,
  ChatDotRound,
  Loading
} from '@element-plus/icons-vue'
import { getDashboardData, DashboardData } from '../../api/dashboard'
import { message } from '../../utils/message'
import * as echarts from 'echarts'

// 加载状态
const loading = ref(true)

// 仪表盘数据
const dashboardData = reactive<DashboardData>({
  blogCount: 0,
  userCount: 0,
  videoCount: 0,
  ebookCount: 0,
  userGrowthData: [],
  userGrowthLabels: [],
  blogDailyData: [],
  blogDailyLabels: []
})

// 图表引用
const userChartRef = ref<HTMLElement | null>(null)
const blogChartRef = ref<HTMLElement | null>(null)

// 图表实例
let userChart: echarts.ECharts | null = null
let blogChart: echarts.ECharts | null = null

// 初始化用户增长图表
const initUserChart = () => {
  if (!userChartRef.value) return
  
  userChart = echarts.init(userChartRef.value)
  const option = {
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
      data: dashboardData.userGrowthLabels,
      axisLabel: {
        interval: Math.floor(dashboardData.userGrowthLabels.length / 6),
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: dashboardData.userGrowthData,
        smooth: true,
        symbolSize: 6,
        lineStyle: {
          width: 3,
          color: '#3b82f6'
        },
        itemStyle: {
          color: '#3b82f6'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(59, 130, 246, 0.5)'
              },
              {
                offset: 1,
                color: 'rgba(59, 130, 246, 0.1)'
              }
            ]
          }
        }
      }
    ]
  }
  
  userChart.setOption(option)
}

// 初始化博客发布图表
const initBlogChart = () => {
  if (!blogChartRef.value) return
  
  blogChart = echarts.init(blogChartRef.value)
  const option = {
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
      data: dashboardData.blogDailyLabels,
      axisLabel: {
        interval: Math.floor(dashboardData.blogDailyLabels.length / 6),
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '博客数量',
        type: 'bar',
        data: dashboardData.blogDailyData,
        barWidth: '60%',
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: '#67c23a'
              },
              {
                offset: 1,
                color: '#95d475'
              }
            ]
          }
        }
      }
    ]
  }
  
  blogChart.setOption(option)
}

// 获取仪表盘数据
const getData = async () => {
  loading.value = true
  try {
    const res = await getDashboardData()
    
    if (res.code === 0) {
      Object.assign(dashboardData, res.data)
      
      // 初始化图表
      setTimeout(() => {
        initUserChart()
        initBlogChart()
      }, 0)
    } else {
      message.error(res.message || '获取仪表盘数据失败')
    }
  } catch (error: any) {
    message.error(error.message || '获取仪表盘数据失败')
  } finally {
    loading.value = false
  }
}

// 窗口大小变化时重新调整图表大小
const handleResize = () => {
  userChart?.resize()
  blogChart?.resize()
}

// 组件挂载完成后获取数据
onMounted(() => {
  getData()
  window.addEventListener('resize', handleResize)
})

// 组件卸载前清理资源
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  userChart?.dispose()
  blogChart?.dispose()
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.welcome-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #3b82f6;
}

.welcome-content {
  padding: 10px 0;
}

.info-cards {
  margin-top: 20px;
}

.info-card {
  display: flex;
  align-items: center;
  padding: 10px;
  height: 100px;
}

.info-icon {
  font-size: 30px;
  margin-right: 15px;
  color: #3b82f6;
}

.info-detail {
  display: flex;
  flex-direction: column;
}

.info-title {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 5px;
}

.info-value {
  font-size: 24px;
  font-weight: bold;
  color: #1f2937;
}

.chart-card {
  margin-bottom: 20px;
  height: 380px;
}

.chart-header {
  font-weight: bold;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart {
  width: 100%;
  height: 100%;
}

.loading-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.loading-icon {
  font-size: 24px;
  margin-bottom: 10px;
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 