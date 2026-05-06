<template>
  <div class="navbar">
    <div class="nav-row">

      <!-- 左侧：Logo + 导航 -->
      <div class="left">
        <!-- Logo -->
        <el-space :size="10" alignment="center">
          <el-image
              src="/src/assets/icon.jpeg"
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
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/post">树洞</el-menu-item>
          <el-menu-item index="/futureLetter">未来信箱</el-menu-item>
          <el-menu-item index="/feedback">反馈</el-menu-item>
        </el-menu>
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
            <el-avatar :size="32" :src="user.avatar"/>
            <span>{{ user.name }}</span>
          </el-space>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goProfile">个人中心</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {logoutApi} from "@/api/auth.ts";
import {ElMessage} from "element-plus";

const router = useRouter()

// 当前激活菜单
const activeMenu = ref('/')

// 是否登录
const isLogin = ref(!!localStorage.getItem('satoken'))

// 用户信息
const user = ref({
  name: 'dreamer',
  avatar: 'https://via.placeholder.com/40'
})

// 路由跳转
const handleSelect = (index: string) => {
  router.push(index)
}

// 登录 / 注册
const goLogin = () => router.push('/login')
const goRegister = () => router.push('/register')

// 用户操作
const goProfile = () => router.push('/profile')
const logout = async () => {

  const res = await logoutApi();
  if (res.data.code === 200) {
    ElMessage.success(res.data.msg)
  } else {
    ElMessage.error(res.data.msg)
  }
  localStorage.removeItem('satoken');
  isLogin.value = false
}

if (isLogin.value) {
  //TODO 获取用户信息
}

</script>

<style scoped>
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
</style>