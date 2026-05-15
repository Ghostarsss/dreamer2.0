<template>
  <div class="login-container">
    <el-button
        class="back-btn"
        :icon="ArrowLeft"
        circle
        @click="goBack"
    />
    <el-card class="login-card">
      <h2 class="title">登录</h2>

      <el-form
          :model="form"
          :rules="rules"
          ref="formRef"
          label-width="0"
      >
        <el-form-item prop="email">
          <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import {ElMessage} from 'element-plus'
import {useRouter} from 'vue-router'
import {ArrowLeft} from '@element-plus/icons-vue'
import type {FormInstance, FormRules} from 'element-plus'
import {login} from "@/api/authApi.ts";

// 表单数据类型
interface LoginForm {
  email: string
  password: string
}

// 表单数据
const form = ref<LoginForm>({
  email: '',
  password: ''
})

// 表单校验
const rules: FormRules<LoginForm> = {
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '邮箱格式不正确', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    { pattern: /^\S*$/, message: '密码不能包含空格', trigger: 'blur' }
  ]
}

// 表单实例
const formRef = ref<FormInstance>()

// loading 状态
const loading = ref(false)

const router = useRouter()

const goBack = () => {
  router.push('/')
}

// 登录逻辑
const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      loading.value = true

      const res = await login({
        email: form.value.email,
        password: form.value.password
      })

      if (res.data.code === 200) {
        ElMessage.success("登录成功")
        localStorage.setItem("satoken", res.data.msg)
        goBack()
      } else {
        ElMessage.error(res.data.msg)
      }

    } catch (error) {
      ElMessage.error('请求失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.back-btn {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 20;
  width: 40px;
  height: 40px;
  font-size: 18px;
}

/* 强制占满整个屏幕，并覆盖所有父布局影响 */
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;

  display: flex;
  justify-content: center;
  align-items: center;

  background: linear-gradient(135deg, #667eea, #764ba2);

  /* 禁止页面滚动 */
  overflow: hidden;
}

/* 登录卡片 */
.login-card {
  width: 360px;
  padding: 30px 25px 10px;
  border-radius: 12px;
}

/* 表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

/* 最后一个按钮间距更大 */
:deep(.el-form-item:last-child) {
  margin-top: 10px;
}

/* 标题 */
.title {
  text-align: center;
  margin-bottom: 35px;
}

/* 按钮 */
.login-btn {
  width: 100%;
}
</style>