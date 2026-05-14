<template>
  <div class="profile-container">

    <!-- 标题 -->
    <div class="page-header">
      <div class="title">个人资料</div>
      <div class="sub-title">USER PROFILE</div>
    </div>

    <!-- 表单 -->
    <el-form
        ref="formRef"
        :model="user"
        :rules="rules"
        label-width="90px"
        class="profile-form"
    >

      <!-- 头像 -->
      <el-form-item label="头像">
        <div class="avatar-box">

          <el-avatar
              :size="90"
              :src="user.avatar"
          />

          <!-- 隐藏上传组件 -->
          <el-upload
              ref="uploadRef"
              class="hidden-upload"
              :show-file-list="false"
              :http-request="handleAvatarChange"
              :auto-upload="true"
          >
          </el-upload>

          <!-- 按钮 + 确认框 -->
          <el-popconfirm
              title="修改头像需要消耗10颗「质子」，是否继续？"
              confirm-button-text="继续"
              cancel-button-text="取消"
              @confirm="triggerUpload"
          >
            <template #reference>
              <el-button type="primary">
                修改头像
              </el-button>
            </template>
          </el-popconfirm>

        </div>
      </el-form-item>

      <!-- 用户名 -->
      <el-form-item label="用户名" prop="username">
        <el-input
            v-model="user.username"
            placeholder="请输入用户名"
        />
      </el-form-item>

      <!-- 性别 -->
      <el-form-item label="性别">
        <el-radio-group v-model="user.gender">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
          <el-radio :value="3">未知</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item label="邮箱">
        <el-input
            v-model="user.email"
            disabled
        />
      </el-form-item>

      <!-- 修改密码 -->
      <el-form-item prop="password" label="密码">
        <el-input
            v-model="user.password"
            type="password"
            show-password
            placeholder="修改密码（需要验证码）"
        />
      </el-form-item>

      <!-- 验证码 -->
      <el-form-item prop="verifyCode">
        <div class="code-row">
          <el-input v-model="user.verifyCode" placeholder="验证码"/>
          <el-button
              :disabled="countdown > 0"
              @click="sendCode"
          >
            {{ countdown > 0 ? countdown + 's' : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 个人介绍 -->
      <el-form-item label="个人介绍" prop="bio">
        <el-input
            v-model="user.bio"
            type="textarea"
            :rows="5"
            maxlength="200"
            show-word-limit
            placeholder="介绍一下自己吧"
        />
      </el-form-item>

      <!-- 保存按钮 -->
      <el-form-item>
        <el-button
            type="primary"
            size="large"
            @click="saveProfile"
        >
          保存更改
        </el-button>
      </el-form-item>

    </el-form>

  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import type {FormInstance, FormRules, UploadInstance} from "element-plus";
import {avatarUpload, me, sendVerifyCode, updateUser} from "@/api/userApi.ts";
import router from "@/router/router.ts";

const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()

const countdown = ref(0)

const sendCode = async () => {
  try {

    countdown.value = 60;

    const timer = setInterval(() => {

      countdown.value--

      if (countdown.value <= 0) {
        clearInterval(timer)
      }

    }, 1000)

    const data = await sendVerifyCode();

    if (data.data.code == 200) {
      ElMessage.success(data.data.msg);

    } else {
      ElMessage.error(data.data.msg);
    }

  } catch (e) {
    ElMessage.warning('发送失败')
  }
}

// 用户数据
const user = reactive({
  avatar: "",
  username: "",
  gender: "",
  email: "",
  bio: "",
  verifyCode: "",
  password: "",
});

// 表单验证规则
const rules = reactive<FormRules>({
  username: [
    {required: true, message: '用户名不能为空', trigger: 'blur'},
    {
      pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/,
      message: '用户名只能是中文、字母、数字和下划线',
      trigger: 'blur'
    }
  ],
  password: [
    {
      validator: (rule: any, value: any, callback: any) => {

        const hasPassword = value && value.length > 0
        const hasVerifyCode = user.verifyCode && user.verifyCode.length > 0

        if (hasPassword && !hasVerifyCode) {
          callback(new Error('请填写验证码'))
        } else if (hasPassword && value.includes(' ')) {
          callback(new Error('密码不能包含空格'))
        } else if (hasPassword && value.length < 6) {
          callback(new Error('密码长度至少6位'))
        } else {
          callback()
        }

      },
      trigger: 'blur'
    }
  ],
  verifyCode: [
    {
      validator: (rule: any, value: any, callback: any) => {

        const hasPassword = user.password && user.password.length > 0
        const hasVerifyCode = value && value.length > 0

        if (hasVerifyCode && !hasPassword) {
          callback(new Error('请填写修改后的密码'))
        } else {
          callback()
        }

      },
      trigger: 'blur'
    }
  ],
  bio: [
    {required: true, message: '个人介绍不能为空', trigger: 'blur'}
  ]
})

// 获取用户信息
const getUserInfo = async () => {

  try {

    const res = await me();

    if (res.data.code === 200) {
      Object.assign(user, res.data.data);
    } else {
      ElMessage.error(res.data.msg);
    }

  } catch (e) {
    ElMessage.error("出错啦，请重试");
  }

};

// 点击确认后再触发文件选择
const triggerUpload = () => {

  const input = uploadRef.value?.$el.querySelector('input')

  if (input) {
    input.click()
  }

}

// 修改头像
const handleAvatarChange = async (option: any) => {

  const file = option.file

  // 检查文件类型是否为图像
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图像文件')
    return
  }

  try {

    // 上传头像
    const formData = new FormData()
    formData.append('avatar', file)

    const res = await avatarUpload(formData);

    if (res.data.code != 200) {
      ElMessage.error(res.data.msg);
      return;
    }

    user.avatar = res.data.msg;

    ElMessage.success("头像上传成功");

  } catch (e) {
    ElMessage.error("头像上传失败");
  }

};

// 保存修改
const saveProfile = async () => {

  if (!formRef.value) return

  try {

    await formRef.value.validate()

    const res = await updateUser(user)

    if (res.data.code === 200) {

      ElMessage.success(res.data.msg);

      // 如果 password 有值，跳转登录界面
      if (user.password) {

        localStorage.removeItem("satoken");

        await router.push("/login")

      }

    } else {
      ElMessage.error(res.data.msg);
    }

  } catch (e) {
    ElMessage.error("保存失败");
  }

};

onMounted(() => {
  document.title = '个人中心'
  getUserInfo();
});
</script>

<style scoped>
.profile-container {
  width: 100%;
  min-height: 100%;
  height: 100%;
  padding: 40px;
  box-sizing: border-box;
  background: #f5f7fa;
  overflow-y: auto;

  /* 隐藏滚动条（兼容各浏览器） */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.profile-container::-webkit-scrollbar {
  display: none; /* Chrome / Safari */
}

.page-header {
  margin-bottom: 28px;
}

.title {
  font-size: 34px;
  font-weight: 700;
  color: #111827;
  letter-spacing: 1px;
}

.sub-title {
  margin-top: 6px;
  font-size: 13px;
  color: #94a3b8;
  letter-spacing: 3px;
}

.profile-form {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 21px;
}

/* 头像区域 */
.avatar-box {
  display: flex;
  align-items: center;
  gap: 30px;
}

/* 隐藏上传组件 */
.hidden-upload {
  display: none;
}

/* textarea 禁止拖拽 */
:deep(.el-textarea__inner) {
  resize: none;
}

/* 验证码布局 */
.code-row {
  display: flex;
  gap: 10px;
}
</style>