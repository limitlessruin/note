<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>用户登录</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <el-form
          ref="formRef"
          :model="loginForm"
          :rules="formRules"
          class="login-form"
          label-position="top"
          @keyup.enter="onSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-container">
            <el-input
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                size="large"
                class="captcha-input"
                @keyup.enter="onSubmit"
            />
            <div class="captcha-image-container">
              <img 
                  :src="captchaImage" 
                  alt="验证码" 
                  class="captcha-image"
                  @click="refreshCaptcha"
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="remember-forgot">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-link type="primary" @click="handleForgotPassword">忘记密码？</el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="onSubmit"
              size="large"
              class="submit-btn"
              :loading="loading"
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>还没有账号？<el-link type="primary" @click="goToRegister">立即注册</el-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false,
  sessionId: ''
});

const loading = ref(false);
const formRef = ref();
const captchaImage = ref('');

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度不能少于3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码必须为4位字符', trigger: 'blur' }
  ]
};

// 检查本地存储的记住我信息
onMounted(() => {
  const rememberedUsername = localStorage.getItem('rememberedUsername');
  if (rememberedUsername) {
    loginForm.username = rememberedUsername;
    loginForm.rememberMe = true;
  }
  // 初始化验证码
  refreshCaptcha();
});

// 刷新验证码
const refreshCaptcha = async () => {
  try {
    const response = await axios.get('/api/captcha/image');
    console.log('验证码响应:', response.data); // 调试日志
    
    // 更灵活的响应处理
    if (response.data && (response.data.success || response.data.code === 200)) {
      // 处理不同的响应格式
      const captchaData = response.data.data || response.data;
      
      if (captchaData.captchaImage) {
        captchaImage.value = captchaData.captchaImage;
      } else if (captchaData.image) {
        captchaImage.value = captchaData.image;
      } else if (captchaData.base64) {
        captchaImage.value = captchaData.base64;
      }
      
      // 更新sessionId
      if (captchaData.sessionId) {
        loginForm.sessionId = captchaData.sessionId;
      }
    } else {
      throw new Error('验证码响应格式不正确');
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
    // 使用默认验证码或显示错误信息
    captchaImage.value = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjQwIiB2aWV3Qm94PSIwIDAgMTAwIDQwIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjQwIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAiIHk9IjIwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjNjY2IiBmb250LXNpemU9IjE0Ij7nrKzkuozml6DnlKjlpLQ8L3RleHQ+PC9zdmc+';
    ElMessage.warning('验证码加载失败，请检查后端服务');
  }
};

const onSubmit = async () => {
  if (!formRef.value) return;

  try {
    // 表单验证
    const valid = await formRef.value.validate();
    if (!valid) return;

    loading.value = true;

    // 发送登录请求（添加超时设置）
    const response = await axios.post('/api/users/login', {
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      sessionId: loginForm.sessionId
    }, {
      timeout: 10000 // 10秒超时
    });

    console.log('登录响应:', response.data); // 调试日志

    // 更灵活的成功状态判断
    const isSuccess = response.data &&
        (response.data.code === 200 || response.data.success);

    if (isSuccess) {
      // 确保消息框显示为绿色
      ElMessage({
        message: '登录成功！',
        type: 'success',
        duration: 2000
      });

      // 处理记住我功能
      if (loginForm.rememberMe) {
        localStorage.setItem('rememberedUsername', loginForm.username);
      } else {
        localStorage.removeItem('rememberedUsername');
      }

      // 更安全的数据处理
      const authData = response.data.data || response.data;
      if (authData.token) {
        localStorage.setItem('authToken', authData.token);
      }
      if (authData.user) {
        localStorage.setItem('userInfo', JSON.stringify(authData.user));
      } else if (authData.userInfo) {
        localStorage.setItem('userInfo', JSON.stringify(authData.userInfo));
      } else {
        // 如果没有用户信息，从响应中提取基本信息
        const userInfo = {
          username: loginForm.username,
          loginTime: new Date().toISOString()
        };
        localStorage.setItem('userInfo', JSON.stringify(userInfo));
      }

      // 处理登录成功后的跳转逻辑
      const redirectPath = route.query.redirect || '/home';
      
      // 直接使用router.replace进行跳转
      router.replace(redirectPath).then(() => {
        console.log('成功跳转到:', redirectPath);
      }).catch(error => {
        console.error('路由跳转失败:', error);
        // 如果路由跳转失败，使用router.push作为备选方案
        router.push(redirectPath);
      });
    } else {
      const errorMsg = response.data.message || '登录失败，请检查账号密码';
      ElMessage.error(errorMsg);
      // 登录失败时刷新验证码
      refreshCaptcha();
      loginForm.captcha = '';
    }
  } catch (error) {
    console.error('登录请求失败:', error);

    // 更详细的错误处理
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接');
    } else if (error.response) {
      // 根据HTTP状态码处理
      const status = error.response.status;
      const message = error.response.data?.message || `服务器错误 (${status})`;

      if (status === 401) {
        ElMessage.error('用户名或密码错误');
      } else if (status >= 500) {
        ElMessage.error('服务器内部错误，请稍后再试');
      } else {
        ElMessage.error(message);
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查后端服务是否启动');
    } else {
      ElMessage.error('发生未知错误: ' + error.message);
    }
    // 登录失败时刷新验证码
    refreshCaptcha();
    loginForm.captcha = '';
  } finally {
    loading.value = false;
  }
};

const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能尚未实现');
};

const goToRegister = () => {
  router.push('/register');
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background:
    linear-gradient(rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.7)),
    url(/images/background.png);
  background-size:cover;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 420px;
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.login-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin: 20px 0;
}

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.2);
}

.submit-btn {
  width: 100%;
  border-radius: 12px;
  height: 48px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.captcha-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.captcha-image {
  width: 100px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.captcha-image:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.refresh-btn {
  font-size: 12px;
  color: #666;
}

.refresh-btn:hover {
  color: #409eff;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.login-footer p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 30px 20px;
    margin: 0 10px;
  }

  .login-header h2 {
    font-size: 24px;
  }

  .remember-forgot {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}

:deep(.el-message--success) {
  background-color: var(--el-color-success) !important;
}
:deep(.el-message--error) {
  background-color: var(--el-color-error) !important;
}
</style>