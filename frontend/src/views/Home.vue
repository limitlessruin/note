<template>
  <el-scrollbar height="800px">
  <div class="home">
    <!-- 用户信息显示 -->
    <div v-if="isAuthenticated" class="user-info">
      <h2>欢迎，{{ userInfo.username }}！</h2>
      <button @click="logout" style="margin-left: 10px; background: #f56c6c; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer;">
        退出登录
      </button>
    </div>
    
    <!-- 图片上传区域 -->
    <div class="upload-section" v-if="isAuthenticated">
      <h3>图片上传</h3>
      <input 
        type="file" 
        ref="fileInput" 
        @change="handleFileSelect" 
        accept="image/*"
        style="margin: 10px 0;"
      >
      <button @click="uploadImage" style="margin-left: 10px;">上传图片</button>
      
      <!-- 预览区域 -->
      <div v-if="selectedFile" style="margin-top: 10px;">
        <p>已选择文件: {{ selectedFile.name }}</p>
        <img v-if="imagePreview" :src="imagePreview" alt="预览" style="max-width: 300px; max-height: 200px; margin-top: 10px;">
      </div>
      
      <!-- 上传结果 -->
      <div v-if="uploadResult" style="margin-top: 10px; color: green;">
        {{ uploadResult }}
      </div>
      
      <!-- 图片识别功能 -->
      <div v-if="imagePreview" style="margin-top: 20px; border-top: 1px solid #eee; padding-top: 20px;">
        <h3>AI图片分析</h3>
        
        <div style="margin-bottom: 15px;">
          <label for="questionInput" style="display: block; margin-bottom: 5px; font-weight: bold;">询问图片内容：</label>
          <textarea 
            id="questionInput"
            v-model="questionText" 
            placeholder="请输入您想询问的图片内容，例如：这张图片中有什么？描述一下场景和物体。"
            style="width: 100%; min-height: 80px; padding: 8px; border: 1px solid #ddd; border-radius: 4px; font-family: inherit; resize: vertical;"
          ></textarea>
          <p style="font-size: 12px; color: #666; margin-top: 5px;">留空将使用默认问题分析图片</p>
        </div>
        
        <button 
          @click="analyzeImage" 
          :disabled="isAnalyzing"
          style="margin: 10px 0; background: #67c23a; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;"
        >
          {{ isAnalyzing ? '识别中...' : '分析图片' }}
        </button>
        
        <!-- 识别结果 -->
        <div v-if="analysisResult" style="margin-top: 10px;">
          <h5>识别结果：</h5>
          <div>
            <p style="white-space: pre-wrap; margin: 0;">{{ analysisResult }}</p>
          </div>
        </div>
        
        <!-- 识别错误 -->
        <div v-if="analysisError" style="margin-top: 10px; color: #f56c6c;">
          <strong>识别失败：</strong> {{ analysisError }}
        </div>
      </div>
    </div>
    
    <!-- 未登录提示 -->
    <div v-else class="login-prompt">
      <p>您需要登录后才能使用图片上传功能</p>
      <button @click="redirectToLogin" style="margin-top: 10px; background: #409eff; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer;">
        前往登录
      </button>
    </div>

  </div>
  </el-scrollbar>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const fileInput = ref(null)
const selectedFile = ref(null)
const imagePreview = ref(null)
const uploadResult = ref('')
const isAuthenticated = ref(false)
const userInfo = ref({})
const isAnalyzing = ref(false)
const analysisResult = ref('')
const analysisError = ref('')
const questionText = ref('')

// 检查认证状态
const checkAuthStatus = () => {
  const token = localStorage.getItem('authToken')
  const user = localStorage.getItem('userInfo')
  
  if (token && user) {
    try {
      userInfo.value = JSON.parse(user)
      isAuthenticated.value = true
    } catch (error) {
      console.error('解析用户信息失败:', error)
      clearAuthData()
    }
  } else {
    isAuthenticated.value = false
  }
}

// 清除认证数据
const clearAuthData = () => {
  localStorage.removeItem('authToken')
  localStorage.removeItem('userInfo')
  isAuthenticated.value = false
  userInfo.value = {}
}

// 退出登录
const logout = () => {
  clearAuthData()
  uploadResult.value = '已退出登录'
}

// 重定向到登录页面
const redirectToLogin = () => {
  router.push('/login')
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    selectedFile.value = file
    
    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
      imagePreview.value = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const uploadImage = async () => {
  if (!selectedFile.value) {
    uploadResult.value = '请先选择图片文件'
    return
  }
  
  try {
    // 这里可以添加实际的上传逻辑
    // 目前只是模拟上传
    uploadResult.value = `图片 "${selectedFile.value.name}" 上传成功！`
    
    // 清空选择
    selectedFile.value = null
    imagePreview.value = null
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    
  } catch (error) {
    uploadResult.value = '上传失败: ' + error.message
  }
}

// 图片识别功能
const analyzeImage = async () => {
  if (!selectedFile.value || !imagePreview.value) {
    analysisError.value = '请先选择并上传图片'
    return
  }
  
  isAnalyzing.value = true
  analysisResult.value = ''
  analysisError.value = ''
  
  try {
    // 将图片转换为base64格式（去掉data:image/...前缀）
    const base64Image = imagePreview.value.split(',')[1]
    
    // 使用用户自定义的问题或默认问题
    const question = questionText.value.trim() || 
      '请详细分析这张图片的内容，包括：1. 图片中的主要物体和场景 2. 颜色和风格特征 3. 可能的用途或含义 4. 任何可见的文字内容'
    
    // 调用后端API进行图片识别
    const response = await fetch('http://localhost:8080/api/vision/analyze', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`
      },
      body: JSON.stringify({
        imageBase64: base64Image,
        question: question
      })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    
    if (result.success) {
      analysisResult.value = result.data
    } else {
      analysisError.value = result.message || '识别失败'
    }
    
  } catch (error) {
    console.error('图片识别错误:', error)
    analysisError.value = `识别失败: ${error.message}。请确保后端服务已启动且配置正确。`
  } finally {
    isAnalyzing.value = false
  }
}

// 组件加载时检查认证状态
onMounted(() => {
  checkAuthStatus()
})
</script>

<style scoped>
.scrollbar-demo-item {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
  margin: 10px;
  text-align: center;
  border-radius: 4px;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}
/*.user-info {
  background: #f0f9ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.login-prompt {
  background: #fff2e8;
  border: 1px solid #ffbb96;
  border-radius: 4px;
  padding: 20px;
  text-align: center;
  margin-top: 20px;
}*/
</style>