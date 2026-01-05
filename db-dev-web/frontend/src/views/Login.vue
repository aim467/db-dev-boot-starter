<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="background-decoration">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="grid-pattern"></div>
    </div>
    
    <div class="login-content">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo">
            <div class="logo-icon-large">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M4 7C4 5.89543 4.89543 5 6 5H18C19.1046 5 20 5.89543 20 7V17C20 18.1046 19.1046 19 18 19H6C4.89543 19 4 18.1046 4 17V7Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 9H16M8 13H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <h1 class="brand-title">DB Dev</h1>
            <p class="brand-subtitle">Database Development Assistant</p>
          </div>
          
          <div class="feature-list">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="feature-text">
                <span class="feature-name">多数据源管理</span>
                <span class="feature-desc">支持多种数据库类型</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
                <el-icon><Grid /></el-icon>
              </div>
              <div class="feature-text">
                <span class="feature-name">表结构浏览</span>
                <span class="feature-desc">可视化查看元数据</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563);">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="feature-text">
                <span class="feature-name">SQL 执行器</span>
                <span class="feature-desc">在线编写执行SQL</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon" style="background: linear-gradient(135deg, #f56c6c, #f89898);">
                <el-icon><Tools /></el-icon>
              </div>
              <div class="feature-text">
                <span class="feature-name">代码生成</span>
                <span class="feature-desc">一键生成Mapper代码</span>
              </div>
            </div>
          </div>
          
          <div class="brand-footer">
            <el-tag size="small" type="info" effect="plain">Version 1.0.0</el-tag>
            <span class="copyright">© 2026 DB Dev Team</span>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录区域 -->
      <div class="login-section">
        <div class="login-box">
          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账户以继续</p>
          </div>
      
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                clearable>
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                @keyup.enter="handleLogin">
                <template #prefix>
                  <el-icon class="input-icon"><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe" class="remember-me">
                <span>记住我</span>
              </el-checkbox>
            </div>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-button"
                @click="handleLogin">
                <span v-if="!loading">登 录</span>
                <span v-else>登录中...</span>
              </el-button>
            </el-form-item>
          </el-form>
          
         
          
          <div v-if="errorMessage" class="error-message">
            <el-alert
              :title="errorMessage"
              type="error"
              :closable="false"
              show-icon />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Connection, Grid, Lightning, Tools } from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    errorMessage.value = ''
    
    try {
      const response = await request.post('/auth/login', {
        username: loginForm.username,
        password: loginForm.password
      })
      
      if (response.code === 200 && response.data) {
        // 根据记住我选项保存token
        if (rememberMe.value) {
          localStorage.setItem('db-dev-token', response.data.token)
          localStorage.setItem('db-dev-username', response.data.username)
        } else {
          sessionStorage.setItem('db-dev-token', response.data.token)
          sessionStorage.setItem('db-dev-username', response.data.username)
        }
        
        ElMessage.success('登录成功')
        
        // 跳转到首页
        router.push('/dashboard')
        
      } else {
        errorMessage.value = response.message || '登录失败'
      }
    } catch (error) {
      errorMessage.value = error.response?.data?.message || error.message || '登录失败，请检查网络连接'
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 50%, #7e8ba3 100%);
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

/* 背景装饰 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.grid-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  opacity: 0.5;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.6;
  animation: float 20s infinite ease-in-out;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.4) 0%, transparent 70%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.orb-2 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.4) 0%, transparent 70%);
  bottom: -150px;
  right: -150px;
  animation-delay: 7s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.4) 0%, transparent 70%);
  top: 50%;
  right: 10%;
  animation-delay: 14s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* 主内容区域 */
.login-content {
  display: flex;
  max-width: 1100px;
  width: 100%;
  position: relative;
  z-index: 1;
  gap: 60px;
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.brand-content {
  text-align: left;
}

.brand-logo {
  margin-bottom: 48px;
}

.logo-icon-large {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  animation: pulse 3s ease-in-out infinite;
}

.logo-icon-large svg {
  width: 48px;
  height: 48px;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.3);
  }
  50% {
    box-shadow: 0 0 0 20px rgba(255, 255, 255, 0);
  }
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.8;
  font-weight: 400;
}

/* 功能特性列表 */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 48px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(8px);
}

.feature-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #3b82f6 100%);
  border-radius: 12px;
  color: white;
  font-size: 20px;
}

.feature-text {
  display: flex;
  flex-direction: column;
}

.feature-name {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 2px;
}

.feature-desc {
  font-size: 13px;
  opacity: 0.7;
}

/* 品牌底部 */
.brand-footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.copyright {
  font-size: 13px;
  opacity: 0.6;
}

/* 右侧登录区域 */
.login-section {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 440px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #6b7280;
}

/* 表单 */
.login-form {
  margin-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #ffffff;
  padding: 12px 16px;
}

:deep(.el-input__wrapper:hover) {
  border-color: #6366f1;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
}

:deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1), 0 4px 12px rgba(99, 102, 241, 0.2);
}

.input-icon {
  color: #9ca3af;
  font-size: 18px;
}

:deep(.el-input__inner) {
  color: #1f2937;
  font-size: 15px;
}

:deep(.el-input__inner::placeholder) {
  color: #9ca3af;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.remember-me {
  user-select: none;
}

.remember-me span {
  color: #6b7280;
  font-size: 14px;
}

.forgot-link {
  font-size: 14px;
}

.forgot-link:hover {
  text-decoration: underline !important;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #1aa8dc 0%, #5cb6f6 50%, #3b82f6 100%);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(77, 142, 211, 0.4);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.5);
  background: linear-gradient(135deg, #66b9f0 0%, #414fce 50%, #4d92f7 100%);
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
}

.login-button.is-loading {
  opacity: 0.8;
}

/* 快捷登录 */
.quick-login {
  margin-top: 32px;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}

.divider span {
  font-size: 13px;
  color: #9ca3af;
}

.social-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.social-btn {
  width: 48px;
  height: 48px;
  border: 1px solid #e5e7eb;
  background: #fff;
  transition: all 0.3s ease;
}

.social-btn:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #6366f1;
}

/* 错误消息 */
.error-message {
  margin-top: 20px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-alert) {
  border-radius: 12px;
  border: 1px solid #fecaca;
  background: #fef2f2;
}

:deep(.el-alert__title) {
  color: #dc2626;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 900px) {
  .login-content {
    flex-direction: column;
    gap: 40px;
  }
  
  .brand-section {
    text-align: center;
  }
  
  .brand-content {
    text-align: center;
  }
  
  .feature-list {
    display: none;
  }
  
  .brand-footer {
    justify-content: center;
  }
  
  .login-section {
    min-width: auto;
  }
  
  .login-box {
    padding: 40px 32px;
  }
}
</style>

