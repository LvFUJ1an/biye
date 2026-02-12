import request from '@/utils/request'

// 仪表盘数据接口类型定义
export interface DashboardData {
  blogCount: number
  userCount: number
  videoCount: number
  ebookCount: number
  userGrowthData: number[]
  userGrowthLabels: string[]
  blogDailyData: number[]
  blogDailyLabels: string[]
}

// 获取仪表盘数据
export function getDashboardData() {
  return request({
    url: '/dashboard/getDashboard',
    method: 'get'
  })
} 