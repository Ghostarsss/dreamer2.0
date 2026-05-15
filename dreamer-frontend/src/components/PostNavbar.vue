把下面这个文件直接整体覆盖即可，我已经帮你加好了：

* 点击右侧头像打开新标签页
* 跳转到 /user

你只需要替换原文件。

<template>
  <div class="forum-layout">
    <!-- 左侧导航栏 -->
    <aside class="sidebar">
      <div class="sidebar-card">
        <el-menu
            class="menu"
            :default-active="activeMenu"
            @select="handleSelect"
            router
        >
          <el-menu-item index="new">
            <el-icon>
              <Clock/>
            </el-icon>
            <span>最新文章</span>
          </el-menu-item>
          <el-menu-item index="hot">
            <el-icon>
              <TrendCharts/>
            </el-icon>
            <span>热门文章</span>
          </el-menu-item>
          <el-menu-item index="follow" v-if="isLogin">
            <el-icon>
              <Star/>
            </el-icon>
            <span>关注新作</span>
          </el-menu-item>
          <el-menu-item index="mine" v-if="isLogin">
            <el-icon>
              <User/>
            </el-icon>
            <span>我的文章</span>
          </el-menu-item>
        </el-menu>
      </div>
    </aside>
    <!-- 中间内容区域 -->
    <main class="content">
      <div class="content-card">
        <router-view/>
      </div>
    </main>
    <!-- 右侧用户信息卡片 -->
    <aside class="right-panel" v-if="isLogin">
      <div class="user-card">
        <div class="user-top">
          <!-- 可点击头像 -->
          <el-avatar
              class="avatar-clickable"
              :size="64"
              :src="user.avatar"
              @click="openPage('/user')"
          />
          <div class="user-info">
            <div class="username">
              {{ user.username }}
            </div>
            <div class="level">
              Lv.{{ user.level }}
            </div>
          </div>
        </div>
        <div class="user-stats">
          <div
              class="stat-item clickable"
              @click="openPage('/user/vp')"
          >
            <span class="label">质子数</span>
            <span class="value">{{ user.proton }}</span>
          </div>
          <div
              class="stat-item clickable"
              @click="openPage('/user/following')"
          >
            <span class="label">关注数</span>
            <span class="value">{{ user.followingCount }}</span>
          </div>
          <div
              class="stat-item clickable"
              @click="openPage('/user/fans')"
          >
            <span class="label">粉丝数</span>
            <span class="value">{{ user.fansCount }}</span>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>
<script setup lang="ts">
import {ref, reactive, onMounted, computed} from 'vue'
import {
  Clock,
  TrendCharts,
  Star,
  User
} from '@element-plus/icons-vue'
import {me} from "@/api/userApi.ts";
import {useRouter} from "vue-router";
const router = useRouter()
/* 菜单 */
const activeMenu = computed(() => {
  const path = router.currentRoute.value.path
  // 1. 如果以 /post 开头，截取后面的部分（new / hot / latest 等）
  if (path.startsWith('/post')) {
    // 截取 /post/ 后面的内容
    const suffix = path.split('/post/')[1] || 'new'
    return suffix
  }
  return 'new'
})
const handleSelect = (index: string) => {
  // 跳转路由
  router.push(index)
}
/* 新标签页打开页面 */
const openPage = (path: string) => {
  const url = router.resolve(path).href
  window.open(url, '_blank')
}
/* 用户信息类型 */
interface UserInfo {
  avatar: string
  username: string
  level: number
  proton: number
  followingCount: number
  fansCount: number
}
/* 用户数据 */
const user = reactive<UserInfo>({
  avatar: "",
  username: '加载中...',
  level: 0,
  proton: 0,
  followingCount: 0,
  fansCount: 0
})
/* 获取当前用户信息 */
const getCurrentUserInfo = async () => {
  try {
    const res = await me()
    user.avatar = res.data.data.avatar
    user.username = res.data.data.username
    user.level = res.data.data.level
    user.proton = res.data.data.proton
    user.followingCount = res.data.data.followingCount
    user.fansCount = res.data.data.fansCount
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
}
const isLogin = ref(false)
/* 页面加载完成自动请求 */
onMounted(() => {
  if (localStorage.getItem("satoken")) {
    getCurrentUserInfo();
    isLogin.value = true
  }
})
</script>
<style scoped>
/* 全局禁止滚动 */
:global(html),
:global(body),
:global(#app) {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden !important;
  background: #f5f7fb;
}
/* 根布局 */
.forum-layout {
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden !important;
  background: #f5f7fb;
  position: fixed;
  inset: 0;
}
/* 左侧导航 */
.sidebar {
  width: 320px;
  height: 100vh;
  padding: 24px 0 24px 80px;
  box-sizing: border-box;
  position: fixed;
  top: 60px;
  left: 0;
  overflow: hidden !important;
}
.sidebar-card {
  width: 204px;
  background: white;
  border-radius: 20px;
  padding: 12px;
  box-sizing: border-box;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.04);
  overflow: hidden !important;
}
.menu {
  border-right: none;
  background: transparent;
  overflow: hidden !important;
}
.menu :deep(.el-menu-item) {
  height: 54px;
  font-size: 16px;
  border-radius: 12px;
  margin: 6px 0;
}
.menu :deep(.el-menu-item.is-active) {
  background: #ecf5ff;
}
/* 中间内容 */
.content {
  position: fixed;
  top: 60px;
  left: 320px;
  right: 320px;
  bottom: 0;
  padding: 0 24px;
  box-sizing: border-box;
  overflow: hidden !important;
}
.content-card {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  padding: 0 20px 20px 20px;
  box-sizing: border-box;
  overflow: hidden !important;
}
/* router-view 内部也禁止滚动 */
.content-card :deep(*) {
  overflow: hidden;
}
/* 右侧用户卡片 */
.right-panel {
  width: 320px;
  height: 100vh;
  padding: 24px 24px 24px 0;
  box-sizing: border-box;
  position: fixed;
  top: 60px;
  right: 0;
  overflow: hidden !important;
}
.user-card {
  width: 100%;
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-sizing: border-box;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.04);
  overflow: hidden !important;
}
/* 用户顶部 */
.user-top {
  display: flex;
  align-items: center;
}
/* 头像可点击 */
.avatar-clickable {
  cursor: pointer;
  transition: all 0.2s ease;
}
.avatar-clickable:hover {
  transform: scale(1.05);
  opacity: 0.9;
}
.user-info {
  margin-left: 16px;
}
.username {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.level {
  margin-top: 6px;
  font-size: 14px;
  color: #909399;
}
/* 底部统计 */
.user-stats {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.clickable {
  cursor: pointer;
  transition: all 0.2s;
}
.clickable:hover {
  opacity: 0.8;
}
.label {
  font-size: 15px;
  color: #606266;
}
.value {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}
@media screen and (max-width: 768px) {
  .forum-layout {
    position: relative;
    width: 100%;
    height: auto;
    min-height: 100vh;
    flex-direction: column;
    overflow: visible !important;
  }
  .sidebar {
    position: sticky;
    top: 60px;
    left: 0;
    width: 100%;
    height: auto;
    padding: 12px;
    z-index: 20;
    background: #f5f7fb;
  }
  .sidebar-card {
    width: 100%;
    border-radius: 16px;
    padding: 10px;
    overflow-x: auto !important;
    overflow-y: hidden !important;
  }
  .menu {
    width: max-content;
    display: flex;
    flex-wrap: nowrap;
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
  }
  .menu :deep(.el-menu) {
    display: flex;
    flex-wrap: nowrap;
    width: max-content;
    min-width: max-content;
    border-right: none;
  }
  .menu :deep(.el-menu-item) {
    height: 40px;
    margin: 0 4px 0 0;
    padding: 0 12px;
    border-radius: 10px;
    flex-shrink: 0;
    font-size: 13px;
  }
  .menu :deep(.el-menu-item span) {
    display: inline-block;
    white-space: nowrap;
  }
  .menu::-webkit-scrollbar {
    display: none;
  }
  .content {
    position: relative;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    padding: 12px;
    overflow: visible !important;
  }
  .content-card {
    height: auto;
    min-height: calc(100vh - 140px);
    padding: 0;
    border-radius: 16px;
    overflow: visible !important;
  }
  .content-card :deep(*) {
    overflow: visible;
  }
  .right-panel {
    display: none;
  }
}
</style>