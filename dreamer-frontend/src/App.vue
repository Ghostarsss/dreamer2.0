<template>
  <el-container class="layout">

    <!-- 登录页不显示导航栏 -->
    <Navbar v-if="!(isOpenLetterPage ||isLoginPage || isRegisterPage || isUserProfilePage)" />

    <!-- 内容区域 -->
    <el-main class="main">
      <router-view />
    </el-main>

  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from "@/components/Navbar.vue";

const route = useRoute()

// 判断是否是登录页（根据你的路由路径改）
const isLoginPage = computed(() => route.path === '/login')
const isRegisterPage = computed(() => route.path === '/register')
const isUserProfilePage = computed(() => route.path.startsWith('/user'))
const isOpenLetterPage = computed(() => route.path === '/futureLetter/opening')
</script>

<style>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main {
  padding: 60px 0 0 0;
  --el-main-padding: 0px !important;
}

.no-padding {
  padding-top: 0;
}
</style>