<template>
  <div class="puzzle-verify-container" :class="{ 'success': isVerified }">
    <div class="puzzle-bg-container">
      <!-- 显示背景图 -->
      <div class="puzzle-bg" ref="puzzleBg">
        <img :src="bgImgUrl" class="bg-img" ref="bgImg" @load="onImgLoad" />
        <!-- 拼图缺口位置 -->
        <div 
          class="puzzle-hole" 
          v-show="isImgLoaded" 
          :style="{
            left: `${targetX}px`,
            top: `${targetY}px`,
            width: `${puzzleSize}px`,
            height: `${puzzleSize}px`,
            backgroundImage: `url(${bgImgUrl})`,
            backgroundPosition: `-${targetX}px -${targetY}px`
          }"
        ></div>
      </div>
      
      <!-- 拼图滑块部分 -->
      <div 
        class="puzzle-piece" 
        ref="puzzlePiece" 
        v-show="isImgLoaded"
        :style="{
          left: `${moveX}px`,
          top: `${targetY}px`,
          width: `${puzzleSize}px`,
          height: `${puzzleSize}px`,
          backgroundImage: `url(${bgImgUrl})`,
          backgroundPosition: `-${targetX}px -${targetY}px`
        }"
        @mousedown="onMouseDown"
        @touchstart="onTouchStart"
      ></div>
      
      <!-- 加载状态 -->
      <div class="puzzle-loading" v-if="!isImgLoaded">
        <div class="loading-icon"></div>
        <span>加载中...</span>
      </div>
      
      <!-- 成功提示 -->
      <div class="verify-result" v-if="isVerified">
        <div class="success-icon"></div>
        <span>验证成功</span>
      </div>
    </div>
    
    <!-- 滑块条 -->
    <div class="slider-bar" ref="sliderBar">
      <div class="slider-tip" v-show="!isVerified && isImgLoaded">{{ tipText }}</div>
      <div class="slider-btn" 
        ref="sliderBtn"
        :style="{left: `${moveDistance}px`}"
        @mousedown="onMouseDown"
        @touchstart="onTouchStart"
      >
        <div class="slider-icon" :class="{'success-icon': isVerified}"></div>
      </div>
      <div class="slider-mask" :style="{width: `${moveDistance}px`}"></div>
    </div>
    
    <!-- 刷新按钮 -->
    <div class="refresh-btn" @click="refreshPuzzle" v-if="isImgLoaded && !isVerified">
      <div class="refresh-icon"></div>
    </div>
  </div>
</template>

<!-- 用于默认导出的普通script -->
<script lang="ts">
export default {
  name: 'SliderVerify'
}
</script>

<!-- 用于组件逻辑的script setup -->
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

// 组件接收的属性
const props = defineProps({
  tipText: {
    type: String,
    default: '拖动滑块完成拼图'
  },
  // 验证误差范围（像素）
  errorRange: {
    type: Number,
    default: 5
  },
  // 拼图大小（像素）
  puzzleSize: {
    type: Number,
    default: 42
  }
})

// 向父组件发送事件
const emit = defineEmits(['verified', 'fail'])

// 状态变量
const isMoving = ref(false)
const isVerified = ref(false)
const isImgLoaded = ref(false)
const moveDistance = ref(0)
const moveX = ref(0)
const startX = ref(0)
const startMoveX = ref(0)
const targetX = ref(0)
const targetY = ref(0)
const maxDistance = ref(0)

// DOM引用
const puzzleBg = ref<HTMLElement | null>(null)
const puzzlePiece = ref<HTMLElement | null>(null)
const sliderBar = ref<HTMLElement | null>(null)
const sliderBtn = ref<HTMLElement | null>(null)
const bgImg = ref<HTMLImageElement | null>(null)

// 随机背景图列表
const bgImages = [
  'https://picsum.photos/id/237/300/150',
  'https://picsum.photos/id/238/300/150',
  'https://picsum.photos/id/239/300/150',
  'https://picsum.photos/id/240/300/150',
  'https://picsum.photos/id/241/300/150',
  'https://picsum.photos/id/242/300/150',
  'https://picsum.photos/id/243/300/150',
  'https://picsum.photos/id/244/300/150',
]

// 当前使用的背景图URL
const bgImgUrl = ref('')

// 图片加载完成事件
const onImgLoad = () => {
  if (!bgImg.value || !puzzleBg.value) return
  
  const bgWidth = bgImg.value.clientWidth
  const bgHeight = bgImg.value.clientHeight
  
  // 生成拼图位置 (避免太靠边)
  const padding = props.puzzleSize + 10
  
  targetX.value = Math.floor(Math.random() * (bgWidth - padding - padding)) + padding
  targetY.value = Math.floor(Math.random() * (bgHeight - padding - padding)) + padding
  
  // 初始化拼图位置（左侧）
  moveX.value = 0
  
  // 图片已加载
  isImgLoaded.value = true
}

// 刷新拼图
const refreshPuzzle = () => {
  isImgLoaded.value = false
  moveX.value = 0
  moveDistance.value = 0
  
  // 随机选择一张背景图
  const randomIndex = Math.floor(Math.random() * bgImages.length)
  bgImgUrl.value = bgImages[randomIndex] + '?t=' + new Date().getTime()
}

// 监听鼠标按下事件
const onMouseDown = (e: MouseEvent) => {
  if (isVerified.value) return
  isMoving.value = true
  startX.value = e.clientX
  startMoveX.value = moveX.value
  
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
  
  // 阻止默认行为
  e.preventDefault()
}

// 监听触摸开始事件
const onTouchStart = (e: TouchEvent) => {
  if (isVerified.value) return
  isMoving.value = true
  startX.value = e.touches[0].clientX
  startMoveX.value = moveX.value
  
  document.addEventListener('touchmove', onTouchMove)
  document.addEventListener('touchend', onTouchEnd)
  
  // 阻止默认行为
  e.preventDefault()
}

// 监听鼠标移动事件
const onMouseMove = (e: MouseEvent) => {
  if (!isMoving.value) return
  const distance = e.clientX - startX.value
  moveSlider(distance)
}

// 监听触摸移动事件
const onTouchMove = (e: TouchEvent) => {
  if (!isMoving.value) return
  const distance = e.touches[0].clientX - startX.value
  moveSlider(distance)
}

// 移动滑块
const moveSlider = (distance: number) => {
  if (!sliderBar.value || !sliderBtn.value) return
  
  const sliderWidth = sliderBar.value.clientWidth
  const btnWidth = sliderBtn.value.clientWidth
  maxDistance.value = sliderWidth - btnWidth
  
  // 计算滑动距离（滑块条）
  let newMoveDistance = startMoveX.value + distance
  
  // 限制范围
  if (newMoveDistance < 0) newMoveDistance = 0
  if (newMoveDistance > maxDistance.value) newMoveDistance = maxDistance.value
  
  moveDistance.value = newMoveDistance
  
  // 计算拼图位置
  if (puzzleBg.value && bgImg.value) {
    const bgWidth = bgImg.value.clientWidth
    const ratio = bgWidth / sliderWidth
    moveX.value = moveDistance.value * ratio
  }
}

// 鼠标释放
const onMouseUp = () => {
  if (!isMoving.value) return
  isMoving.value = false
  
  // 验证是否滑到正确位置
  verifyPosition()
  
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
}

// 触摸结束
const onTouchEnd = () => {
  if (!isMoving.value) return
  isMoving.value = false
  
  // 验证是否滑到正确位置
  verifyPosition()
  
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
}

// 验证拼图位置
const verifyPosition = () => {
  const distance = Math.abs(moveX.value - targetX.value)
  
  if (distance <= props.errorRange) {
    // 验证通过
    isVerified.value = true
    moveX.value = targetX.value // 吸附到正确位置
    emit('verified')
  } else {
    // 验证失败，拼图和滑块回到起点
    moveX.value = 0
    moveDistance.value = 0
    emit('fail')
  }
}

// 重置验证
const reset = () => {
  isVerified.value = false
  moveX.value = 0
  moveDistance.value = 0
  refreshPuzzle()
}

// 组件挂载完成后初始化
onMounted(() => {
  refreshPuzzle()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 组件卸载前移除事件监听
onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
  window.removeEventListener('resize', handleResize)
})

// 窗口大小变化处理
const handleResize = () => {
  if (isVerified.value) return
  
  // 重置位置
  moveX.value = 0
  moveDistance.value = 0
}

// 对外暴露的方法
defineExpose({
  reset,
  isVerified,
  refreshPuzzle
})
</script>

<style scoped>
.puzzle-verify-container {
  width: 100%;
  margin: 10px 0;
  position: relative;
}

.puzzle-bg-container {
  position: relative;
  width: 100%;
  height: 150px;
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f0f0f0;
}

.puzzle-bg {
  position: relative;
  width: 100%;
  height: 100%;
}

.bg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.puzzle-hole {
  position: absolute;
  box-sizing: border-box;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5) inset;
  background-repeat: no-repeat;
  background-size: 300px 150px;
  border-radius: 4px;
}

.puzzle-piece {
  position: absolute;
  box-sizing: border-box;
  border: 1px solid #fff;
  cursor: pointer;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.5);
  background-repeat: no-repeat;
  background-size: 300px 150px;
  border-radius: 4px;
  transition: box-shadow 0.2s;
}

.puzzle-piece:hover {
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.8);
}

.puzzle-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.2);
  color: white;
}

.loading-icon {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
}

.verify-result {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(16, 185, 129, 0.6);
  color: white;
  font-weight: bold;
}

.success-icon {
  width: 24px;
  height: 24px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ffffff'%3E%3Cpath d='M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z'/%3E%3C/svg%3E");
  background-size: contain;
  margin-bottom: 8px;
}

/* 滑块条 */
.slider-bar {
  position: relative;
  height: 40px;
  background-color: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(95, 145, 255, 0.1);
  overflow: hidden;
}

.slider-tip {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  user-select: none;
}

.slider-btn {
  position: absolute;
  width: 40px;
  height: 38px;
  top: 0;
  left: 0;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(139, 92, 246, 0.3));
  border-radius: 8px;
  transition: background 0.3s;
  z-index: 2;
}

.slider-btn:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.5), rgba(139, 92, 246, 0.5));
}

.slider-icon {
  width: 16px;
  height: 16px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ffffff'%3E%3Cpath d='M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6-1.41-1.41z'/%3E%3C/svg%3E");
  background-size: contain;
  background-repeat: no-repeat;
}

.slider-mask {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: rgba(95, 145, 255, 0.2);
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
  z-index: 1;
}

/* 刷新按钮 */
.refresh-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  cursor: pointer;
  z-index: 5;
  transition: transform 0.3s;
}

.refresh-btn:hover {
  transform: rotate(30deg);
}

.refresh-icon {
  width: 16px;
  height: 16px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ffffff'%3E%3Cpath d='M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 12h7V5l-2.35 1.35z'/%3E%3C/svg%3E");
  background-size: contain;
}

.success .verify-result {
  display: flex;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style> 