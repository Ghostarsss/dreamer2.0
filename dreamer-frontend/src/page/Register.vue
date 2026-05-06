<template>
  <div class="register-container">
    <el-button
        class="back-btn"
        :icon="ArrowLeft"
        circle
        @click="goBack"
    />
    <el-card class="register-card">
      <div class="title">注册账号</div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
      >
        <!-- 头像上传 -->
        <el-form-item prop="avatar">
          头像:
          <div class="avatar-box">
            <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :http-request="uploadAvatar"
            >
              <img v-if="form.avatar" :src="form.avatar" class="avatar"/>
              <el-icon v-else class="avatar-icon">
                <Plus/>
              </el-icon>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名"/>
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱"/>
        </el-form-item>

        <!-- 验证码 -->
        <el-form-item prop="verifyCode">
          <div class="code-row">
            <el-input v-model="form.verifyCode" placeholder="验证码"/>
            <el-button
                :disabled="countdown > 0"
                @click="sendCode"
            >
              {{ countdown > 0 ? countdown + 's' : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="密码"
          />
        </el-form-item>

        <!-- 性别 -->
        <el-form-item prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio label="1">男</el-radio>
            <el-radio label="2">女</el-radio>
            <el-radio label="3">未知</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import {ElMessage} from 'element-plus'
import type {FormInstance, FormRules} from 'element-plus'
import {Plus, ArrowLeft} from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'
import {avatarUpload, register, sendVerifyCode} from "@/api/auth.ts";

interface RegisterForm {
  avatar: string
  username: string
  email: string
  verifyCode: string
  password: string
  gender: string
}

const form = ref<RegisterForm>({
  avatar: '',
  username: '',
  email: '',
  verifyCode: '',
  password: '',
  gender: "3"
})

const rules: FormRules<RegisterForm> = {
  avatar: [{required: false}],
  username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '邮箱格式不正确', trigger: 'blur'}
  ],
  verifyCode: [{required: true, message: '请输入验证码', trigger: 'blur'}],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, message: '至少6位密码', trigger: 'blur'}
  ],
  gender: [{required: true, message: '请选择性别', trigger: 'change'}]
}

const formRef = ref<FormInstance>()
const loading = ref(false)

const router = useRouter()

/* ================= 上传头像 ================= */
const uploadAvatar = async (option: any) => {
  const file = option.file

  // 检查文件类型是否为图像
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图像文件')
    return
  }

  const formData = new FormData()
  formData.append('avatar', file)

  try {
    const res = await avatarUpload(formData);

    form.value.avatar = res.data.msg

    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

/* ================= 发送验证码 ================= */
const countdown = ref(0)

const sendCode = async () => {
  if (!formRef.value) return

  try {
    // 只校验邮箱字段（确保格式正确）
    await formRef.value.validateField('email')

    if (!form.value.email) {
      ElMessage.warning('请先输入邮箱')
      return
    }

    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)

    const data = await sendVerifyCode({email: form.value.email});

    if (data.data.code == 200) {
      ElMessage.success(data.data.msg);

    } else {
      ElMessage.error(data.data.msg);
    }

  } catch (e) {
    ElMessage.warning('发送失败')
  }
}

/* ================= 注册 ================= */
const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      loading.value = true

      const res = await register(form.value)

      if (res.data.code === 200) {
        ElMessage.success("注册成功")
        localStorage.setItem("satoken", res.data.msg)
        // 可跳转主页
        goBack()
      } else {
        ElMessage.error(res.data.msg)
      }

    } catch (e) {
      ElMessage.error('注册失败')
    } finally {
      loading.value = false
    }
  })
}

const goBack = () => {
  router.push('/')
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

.register-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;

  display: flex;
  justify-content: center;
  align-items: center;

  background: linear-gradient(135deg, #667eea, #764ba2);
}

.register-card {
  width: 400px;
  padding: 30px 25px;
  border-radius: 12px;
}

.title {
  text-align: center;
  font-size: 22px;
  margin-bottom: 20px;
}

/* 头像 */
.avatar-box {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 15px;
}

.avatar-uploader {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  border: 2px dashed rgba(255, 255, 255, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
}

.avatar-icon {
  font-size: 28px;
}


/* 验证码布局 */
.code-row {
  display: flex;
  gap: 10px;
}

/* 按钮 */
.register-btn {
  width: 100%;
}

/* 间距优化 */
:deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>