<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h2>用户注册</h2>
        <p>请填写以下信息创建账号</p>
      </div>

      <el-form
          ref="formRef"
          :model="registerForm"
          :rules="formRules"
          class="register-form"
          label-position="top"
          status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入3-12位的用户名"
              size="large"
              :prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="registerForm.email"
              placeholder="请输入有效的邮箱地址"
              size="large"
              :prefix-icon="Message"
              clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入至少6位的密码"
              size="large"
              :prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
          />
        </el-form-item>

        <el-form-item prop="agreed">
          <el-checkbox v-model="registerForm.agreed">
            我已阅读并同意<a href="#" @click.prevent="showAgreement">《用户协议》</a>和<a href="#" @click.prevent="showPrivacy">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              @click="onSubmit"
              size="large"
              class="submit-btn"
              :loading="loading"
          >
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p>已有账号？<el-link type="primary" @click="goToLogin">立即登录</el-link></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { User, Lock, Message } from '@element-plus/icons-vue';

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);

// 表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreed: false
});

// 表单验证规则[7,8](@ref)
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const validateAgreement = (rule: any, value: boolean, callback: any) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议和隐私政策'));
  } else {
    callback();
  }
};

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 12, message: '用户名长度在 3 到 12 个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, message: '密码需包含大小写字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  agreed: [
    { validator: validateAgreement, trigger: 'change' }
  ]
};

// 提交表单[1,5](@ref)
const onSubmit = async () => {
  if (!formRef.value) return;

  try {
    // 表单验证
    const valid = await formRef.value.validate();
    if (!valid) return;

    loading.value = true;

    // 发送注册请求[2,4](@ref)
    const response = await axios.post('http://localhost:8080/api/users', {
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    });

    console.log('注册响应:', response.data); // 查看实际返回的内容

    if (response.status === 200) {
      // 如果后端返回的是字符串
      if (typeof response.data === 'string') {
        if (response.data.includes('成功') || response.data.includes('成功')) {
          ElMessage.success('注册成功！');
          setTimeout(() => {
            router.push('/login');
          }, 1500);
        } else {
          ElMessage.error(response.data);
        }
      }
      // 如果后端返回的是JSON对象
      else if (typeof response.data === 'object') {
        if (response.data.code === 200 || response.data.success) {
          ElMessage.success(response.data.message || '注册成功！');
          setTimeout(() => {
            router.push('/login');
          }, 1500);
        } else {
          ElMessage.error(response.data.message || '注册失败');
        }
      }
      // 其他情况
      else {
        ElMessage.success('注册成功！');
        setTimeout(() => {
          router.push('/login');
        }, 1500);
      }
    } else {
      ElMessage.error('注册失败');
    }
  } catch (error: any) {
    console.error('注册请求失败:', error);

    // 错误处理[2](@ref)
    if (error.response) {
      const errorData = error.response.data;
      if (errorData.message) {
        ElMessage.error(errorData.message);
      } else if (error.response.status === 409) {
        ElMessage.error('用户名或邮箱已存在');
      } else if (error.response.status === 400) {
        ElMessage.error('请求参数错误');
      } else {
        ElMessage.error(`注册失败: ${error.response.status}`);
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查后端服务是否启动');
    } else {
      ElMessage.error('发生未知错误: ' + error.message);
    }
  } finally {
    loading.value = false;
  }
};

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login');
};

// 显示用户协议
const showAgreement = () => {
  ElMessage.info('用户协议内容');
};

// 显示隐私政策
const showPrivacy = () => {
  ElMessage.info('隐私政策内容');
};
</script>

<style scoped>
.register-container {
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

.register-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  color: #333;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.register-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.register-form {
  margin: 20px 0;
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
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  background: #a8abb2;
  cursor: not-allowed;
}

.reset-btn {
  width: 100%;
  border-radius: 12px;
  height: 48px;
  font-weight: 600;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.register-footer p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .register-card {
    padding: 30px 20px;
    margin: 0 10px;
  }

  .register-header h2 {
    font-size: 24px;
  }
}
</style>