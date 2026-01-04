<template>
  <div class="not-found-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <span v-for="n in 20" :key="n" class="floating-emoji" :style="getRandomStyle(n)">
        {{ getRandomEmoji(n) }}
      </span>
    </div>

    <div class="not-found-content">
      <!-- 骨架屏加载效果 -->
      <div class="skeleton-wrapper">
        <div class="skeleton-illustration">
          <div class="skeleton-line code-line-1"></div>
          <div class="skeleton-line code-line-2"></div>
          <div class="skeleton-line code-line-3"></div>
          <div class="error-overlay">😵‍💫</div>
        </div>
      </div>

      <div class="error-code">404</div>
      <div class="error-message">
        <span class="emoji">🚫</span>
        页面迷失在代码的星空中...
        <span class="emoji">🚀</span>
      </div>
      <div class="error-detail">
        <el-skeleton :rows="2" animated :loading="false">
          <template #template>
            <el-skeleton-item variant="text" style="width: 60%" />
          </template>
          <template #default>
            <p>您要访问的页面可能已被删除、重命名或暂时不可用</p>
          </template>
        </el-skeleton>
      </div>

      <div class="action-buttons">
        <el-button type="primary" size="large" @click="goHome" class="home-btn">
          <span class="btn-emoji">🏠</span>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack" class="back-btn">
          <span class="btn-emoji">⬅️</span>
          返回上一页
        </el-button>
      </div>

      <!-- 搜索框骨架 -->
      <div class="search-skeleton">
        <el-skeleton :rows="1" animated :loading="false">
          <template #template>
            <div class="search-box">
              <el-skeleton-item variant="text" style="width: 100%" />
            </div>
          </template>
          <template #default>
            <p class="hint-text">💡 建议：检查 URL 是否正确，或返回首页重新导航</p>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 底部装饰 -->
    <div class="bottom-decoration">
      <span class="deco-emoji">💻</span>
      <span class="deco-emoji">🔧</span>
      <span class="deco-emoji">⚡</span>
      <span class="deco-emoji">🎯</span>
      <span class="deco-emoji">🌟</span>
    </div>
  </div>
</template>

<script setup>
const goHome = () => {
  window.location.href = '/'
}

const goBack = () => {
  window.history.back()
}

const emojis = ['🚀', '??', '⭐', '🌙', '☀️', '🔥', '💥', '🎨', '🔮', '✨', '⚡', '💡', '🎯', '🌈', '🎪']
const getRandomEmoji = (n) => {
  return emojis[n % emojis.length]
}

const getRandomStyle = (n) => {
  return {
    left: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 3}s`,
    animationDuration: `${3 + Math.random() * 2}s`,
    fontSize: `${16 + Math.random() * 24}px`
  }
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-emoji {
  position: absolute;
  animation: float 4s ease-in-out infinite;
  opacity: 0.3;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

.not-found-content {
  text-align: center;
  color: #fff;
  z-index: 1;
  padding: 40px;
  max-width: 600px;
}

/* 骨架屏包装器 */
.skeleton-wrapper {
  margin-bottom: 30px;
}

.skeleton-illustration {
  position: relative;
  display: inline-block;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  min-width: 200px;
}

.skeleton-line {
  height: 12px;
  background: linear-gradient(90deg, rgba(255,255,255,0.1) 25%, rgba(255,255,255,0.3) 50%, rgba(255,255,255,0.1) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
  margin: 8px 0;
}

.skeleton-line.code-line-1 { width: 80%; }
.skeleton-line.code-line-2 { width: 60%; }
.skeleton-line.code-line-3 { width: 70%; }

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.error-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 48px;
  animation: bounce 1s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.2); }
}

.error-code {
  font-size: 140px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
  text-shadow: none;
  position: relative;
}

.error-code::after {
  content: '404';
  position: absolute;
  left: 0;
  top: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: blur(20px);
  opacity: 0.5;
  z-index: -1;
}

.error-message {
  font-size: 28px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  opacity: 1;
}

.emoji {
  animation: wobble 2s ease-in-out infinite;
}

.emoji:nth-child(2) { animation-delay: 0.2s; }
.emoji:nth-child(3) { animation-delay: 0.4s; }

@keyframes wobble {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.error-detail {
  margin-bottom: 32px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 24px;
}

.home-btn, .back-btn {
  padding: 16px 32px;
  font-size: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.home-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.home-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.back-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.btn-emoji {
  margin-right: 8px;
}

.search-skeleton {
  margin-top: 20px;
}

.hint-text {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  margin-top: 8px;
}

/* 底部装饰 */
.bottom-decoration {
  position: absolute;
  bottom: 30px;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 20px;
  opacity: 0.4;
}

.deco-emoji {
  font-size: 24px;
  animation: pulse 2s ease-in-out infinite;
}

.deco-emoji:nth-child(1) { animation-delay: 0s; }
.deco-emoji:nth-child(2) { animation-delay: 0.3s; }
.deco-emoji:nth-child(3) { animation-delay: 0.6s; }
.deco-emoji:nth-child(4) { animation-delay: 0.9s; }
.deco-emoji:nth-child(5) { animation-delay: 1.2s; }

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.4; }
  50% { transform: scale(1.2); opacity: 0.7; }
}

/* 响应式 */
@media (max-width: 768px) {
  .error-code {
    font-size: 100px;
  }

  .error-message {
    font-size: 20px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: center;
  }

  .home-btn, .back-btn {
    width: 200px;
  }
}
</style>