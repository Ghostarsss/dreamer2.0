<template>
  <div class="navbar">
    <div class="nav-row">

      <!-- 左侧：Logo + 导航 -->
      <div class="left">
        <el-space :size="10" alignment="center">
          <el-image
              src="/src/assets/icon.png"
              class="logo-img"
              fit="cover"
          />
          <span class="logo-text">dreamer2.0</span>
        </el-space>
      </div>

      <div class="menu-container">
        <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            :ellipsis="false"
            @select="handleSelect"
            class="menu"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/post">树洞</el-menu-item>
          <el-menu-item index="/futureLetter">未来信箱</el-menu-item>
          <el-menu-item index="/feedback">反馈</el-menu-item>
        </el-menu>
      </div>

      <!-- mobile menu button -->
      <div class="mobile-menu-btn" @click="mobileMenuOpen = true">
        <el-icon><Menu /></el-icon>
      </div>

      <!-- 右侧：登录 / 用户 -->
      <div style="margin-left: auto;margin-right: 10px;">
        <!-- 未登录 -->
        <el-space v-if="!isLogin">
          <el-button type="primary" text @click="goLogin">登录</el-button>
          <el-button type="primary" @click="goRegister">注册</el-button>
        </el-space>

        <!-- 已登录 -->
        <el-dropdown v-else>
          <el-space>
            <el-avatar :size="35" :src="user.avatar"/>
            <span>{{ user.username }}</span>
          </el-space>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goProfile">
                个人中心
              </el-dropdown-item>

              <el-dropdown-item @click="logout">
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

    </div>


  </div>

  <!-- 右下角邮件按钮 -->
  <transition name="mail-float">
    <div
        v-if="isLogin"
        class="mail-button"
        @click="goMessage"
    >
      <el-badge
          :is-dot="hasNewMessage"
          class="mail-badge"
      >
        <el-icon
            class="mail-icon"
            :class="{ shaking: hasNewMessage }"
        >
          <Message/>
        </el-icon>
      </el-badge>

      <span class="mail-text">
          {{ hasNewMessage ? '你有新消息' : '消息中心' }}
        </span>
    </div>
  </transition>

  <!--  消息弹窗-->
  <MessageModal v-model:visible="showDialog"/>

  <!-- Mobile Drawer Menu -->
  <el-drawer
    v-model="mobileMenuOpen"
    direction="ltr"
    size="70%"
  >
    <el-menu
      mode="vertical"
      :default-active="activeMenu"
      @select="handleMobileSelect"
    >
      <el-menu-item index="/home">首页</el-menu-item>
      <el-menu-item index="/post">树洞</el-menu-item>
      <el-menu-item index="/futureLetter">未来信箱</el-menu-item>
      <el-menu-item index="/feedback">反馈</el-menu-item>
    </el-menu>
  </el-drawer>

  <!-- 未登录提示 -->
  <transition name="popup-fade">
    <div
        v-if="!isLogin"
        class="login-popup"
        @click="goLogin"
    >
      <div class="popup-title">
        ✨ 登录后享受更多功能和权益
      </div>

      <div class="popup-desc">
        与其他「梦想家」互动、「质子」虚拟货币系统、发布文章、未来信箱...
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import {
  onMounted,
  onUnmounted,
  ref,
  computed
} from 'vue'

import {useRouter} from 'vue-router'

import {
  ElMessage
} from "element-plus";

import {
  Message,
  Menu
} from '@element-plus/icons-vue'

import {
  logoutApi
} from "@/api/authApi.ts";

import {
  me
} from "@/api/userApi.ts";

import request from '@/utils/request.ts'
import MessageModal from "@/components/MessageModal.vue";
import {countUnReadMessage} from "@/api/messageApi.ts";

const router = useRouter()

/* 当前激活菜单 */
const activeMenu = computed(() => {
  const path = router.currentRoute.value.path

  if (path.startsWith('/post')) return '/post'
  if (path.startsWith('/home')) return '/home'
  if (path.startsWith('/futureLetter')) return '/futureLetter'
  if (path.startsWith('/feedback')) return '/feedback'

  return '/home'
})

/* 是否登录 */
const isLogin = ref(!!localStorage.getItem('satoken'))

/* 是否有新消息 */
const hasNewMessage = ref(false)

/* 定时器 */
let timer: number | null = null

/* 用户信息 */
const user = ref({
  username: 'dreamer',
  avatar: '',
})

/* 路由跳转 */
const handleSelect = (index: string) => {
  router.push(index)

}

/* 登录 / 注册 */
const goLogin = () => router.push('/login')

const goRegister = () => router.push('/register')

const showDialog = ref(false)

const mobileMenuOpen = ref(false)

/* 消息页面 */
const goMessage = () => {
  showDialog.value = true
  hasNewMessage.value = false
}

const handleMobileSelect = (index: string) => {
  router.push(index)
  mobileMenuOpen.value = false
}

/* 用户操作 */
const goProfile = () => {
  window.open('/user', '_blank')
}

/* 退出登录 */
const logout = async () => {

  const res = await logoutApi();

  if (res.data.code === 200) {
    ElMessage.success(res.data.msg)
  } else {
    ElMessage.error(res.data.msg)
  }

  localStorage.removeItem('satoken');
  localStorage.removeItem('userId');

  isLogin.value = false
  hasNewMessage.value = false

  //刷新页面
  window.location.reload()
}

/* 获取用户信息 */
const getUserInfo = async () => {

  if (!isLogin.value) return

  try {

    const res = await me()

    if (res.data.code === 200) {

      user.value = res.data.data
      localStorage.setItem('userId', res.data.data.id)

    } else {

      localStorage.clear()

      ElMessage.error("登录已过期，请重新登录")

      await router.push('/login')
    }

  } catch (err) {
    console.error('获取用户信息失败', err)
  }
}

/*
 * 检查是否有新消息
 *
 * 后端返回格式示例：
 * {
 *   code: 200,
 *   data: true
 * }
 */
const checkNewMessage = async () => {

  if (!isLogin.value) {
    return
  }

  try {

    const res = await countUnReadMessage()

    if (res.data.code === 200 && res.data.msg != "0") {
      hasNewMessage.value = true
    }

  } catch (e) {
    console.error('消息检测失败', e)
  }
}

/* 启动轮询 */
const startMessagePolling = () => {

  checkNewMessage()

  timer = window.setInterval(() => {
    checkNewMessage()
  }, 10000)
}


// 默认标题
document.title = "「dreamer」梦想家";

// 监听路由变化设置标题
import { watch } from 'vue'

watch(
  () => router.currentRoute.value.path,
  (newPath) => {
    if (newPath.endsWith('/feedback')) {
      document.title = "「dreamer」反馈中心";
    } else if (newPath.includes('/post')) {
      document.title = "「dreamer」树洞论坛";
    } else if (newPath.endsWith('/futureLetter')) {
      document.title = "「dreamer」未来信箱";
    } else if (newPath.endsWith('/home')){
      document.title = "「dreamer」梦想家首页";
    }
  },
  { immediate: true }
)

onMounted(() => {

  getUserInfo()

  if (isLogin.value) {
    startMessagePolling()
  }
})

onUnmounted(() => {

  if (timer) {
    clearInterval(timer)
  }
})

</script>

<style scoped>
.mobile-menu-btn {
  display: none;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
}

.logo-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
}

.menu-container {
  flex: 1;
  display: flex;
  justify-content: center;
}

.menu {
  width: 600px;
  display: flex;
  justify-content: space-between;
}

.navbar {

  position: fixed;

  top: 0;

  left: 0;

  width: 100%;

  height: 70px;

  z-index: 999;

  background: rgba(255, 255, 255, 0.85);

  backdrop-filter: blur(5px);

  -webkit-backdrop-filter: blur(10px);
}

.nav-row {
  height: 80px;

  width: 100%;

  display: flex;

  align-items: center;

  justify-content: space-between;

  padding: 0 20px;

  box-sizing: border-box;
}

.left {
  display: flex;
  align-items: center;
  margin-left: 10px;
}

:deep(.el-tooltip__trigger:focus-visible) {
  outline: none !important;
}

:deep(.el-tooltip__trigger) {
  border: none !important;
  box-shadow: none !important;
}

/* ===================== */
/* 右下角邮件按钮 */
/* ===================== */

.mail-button {

  position: fixed;

  right: 100px;

  bottom: 50px;

  width: 80px;

  height: 80px;

  border-radius: 50%;

  background: linear-gradient(
      135deg,
      rgb(64, 158, 255),
      rgba(108, 182, 255, 0.86)
  );

  display: flex;

  flex-direction: column;

  align-items: center;

  justify-content: center;

  gap: 1px;

  color: white;

  cursor: pointer;

  user-select: none;

  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.35);

  transition: 0.25s;

  z-index: 2000;
}

.mail-button:hover {
  transform: translateY(-4px) scale(1.04);
}

.mail-icon {
  font-size: 26px;
}

.mail-text {
  font-size: 10px;
}

/* 新消息动画 */
.shaking {
  animation: shake 1s infinite;
}

@keyframes shake {

  0% {
    transform: rotate(0deg);
  }

  20% {
    transform: rotate(-10deg);
  }

  40% {
    transform: rotate(10deg);
  }

  60% {
    transform: rotate(-8deg);
  }

  80% {
    transform: rotate(8deg);
  }

  100% {
    transform: rotate(0deg);
  }
}

/* ===================== */
/* 未登录提示 */
/* ===================== */

.login-popup {

  position: fixed;

  right: 50px;

  bottom: 30px;

  width: 250px;

  padding: 16px;

  border-radius: 18px;

  background: rgba(255, 255, 255, 0.95);

  backdrop-filter: blur(10px);

  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);

  cursor: pointer;

  transition: 0.25s;

  z-index: 2000;
}

.login-popup:hover {
  transform: translateY(-4px);
}

.popup-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
}

.popup-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #909399;
}

/* ===================== */
/* 动画 */
/* ===================== */

.mail-float-enter-active,
.mail-float-leave-active,
.popup-fade-enter-active,
.popup-fade-leave-active {
  transition: all 0.25s ease;
}

.mail-float-enter-from,
.mail-float-leave-to,
.popup-fade-enter-from,
.popup-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
@media screen and (max-width: 768px) {

  .navbar {
    height: auto;
    padding-bottom: 10px;
  }

  .nav-row {
    height: auto;
    flex-wrap: wrap;
    padding: 10px 12px;
    gap: 10px;
  }

  .left {
    margin-left: 0;
  }

  .logo-text {
    font-size: 16px;
  }

  .logo-img {
    width: 34px;
    height: 34px;
  }

  .menu-container {
    display: none;
  }

  .mobile-menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    margin-left: 10px;
    cursor: pointer;
  }

  .menu {
    width: max-content;
    min-width: 100%;
    justify-content: flex-start;
    gap: 6px;
  }

  :deep(.el-menu--horizontal) {
    border-bottom: none;
  }

  :deep(.el-menu--horizontal > .el-menu-item) {
    height: 42px;
    line-height: 42px;
    padding: 0 14px;
    border-radius: 10px;
    flex-shrink: 0;
  }

  .mail-button {
    right: 20px;
    bottom: 20px;
    width: 62px;
    height: 62px;
  }

  .mail-icon {
    font-size: 20px;
  }

  .mail-text {
    display: none;
  }

  .login-popup {
    right: 15px;
    left: 15px;
    bottom: 15px;
    width: auto;
    padding: 14px;
  }

  .popup-title {
    font-size: 14px;
  }

  .popup-desc {
    font-size: 12px;
    line-height: 1.5;
  }
}
</style>