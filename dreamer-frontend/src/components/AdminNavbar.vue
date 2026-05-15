<template>
  <div class="admin-layout">
    <!-- 侧边菜单 -->
    <aside class="sidebar" :class="{ mobileOpen: mobileMenuOpen }">
      <!-- Logo -->
      <div class="logo">
        <div class="logo-icon">✨</div>
        <div class="logo-text">
          <h2>Admin</h2>
          <span>管理后台</span>
        </div>
      </div>

      <!-- 菜单 -->
      <el-menu
          class="menu"
          :default-active="activeMenu"
          router
          @select="handleSelect"
      >
        <el-menu-item index="/admin/users">
          <el-icon>
            <User />
          </el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/posts">
          <el-icon>
            <Document />
          </el-icon>
          <span>文章审核</span>
        </el-menu-item>

        <el-menu-item index="/admin/feedback">
          <el-icon>
            <ChatDotRound />
          </el-icon>
          <span>用户反馈</span>
        </el-menu-item>

        <el-menu-item index="/admin/notice">
          <el-icon>
            <Bell />
          </el-icon>
          <span>公告管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/statistics">
          <el-icon>
            <DataAnalysis />
          </el-icon>
          <span>数据统计</span>
        </el-menu-item>
      </el-menu>

      <!-- 底部信息 -->
      <div class="bottom-card">
        <div class="bottom-title">管理员后台</div>
        <div class="bottom-desc">
          管理系统数据与社区内容
        </div>
      </div>
    </aside>

    <!-- 遮罩 -->
    <div
        v-if="mobileMenuOpen"
        class="mobile-mask"
        @click="mobileMenuOpen = false"
    />

    <!-- 主体 -->
    <main class="main">
      <!-- 顶部栏（移动端菜单按钮） -->
      <div class="topbar">
        <div class="left">
          <el-button
              class="menu-btn"
              @click="mobileMenuOpen = true"
              circle
              size="small"
          >
            <el-icon>
              <Menu />
            </el-icon>
          </el-button>

          <div class="page-title">
            {{ currentTitle }}
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import { useRoute } from "vue-router";

import {
  User,
  Document,
  ChatDotRound,
  Bell,
  DataAnalysis,
  Menu
} from "@element-plus/icons-vue";

const route = useRoute();

const mobileMenuOpen = ref(false);

const activeMenu = computed(() => route.path);

const titleMap: Record<string, string> = {
  "/admin/users": "用户管理",
  "/admin/articles": "文章审核",
  "/admin/feedback": "用户反馈",
  "/admin/notice": "公告管理",
  "/admin/statistics": "数据统计"
};

const currentTitle = computed(() => {
  return titleMap[route.path] || "管理员后台";
});

const handleSelect = () => {
  mobileMenuOpen.value = false;
};

onMounted(()=>{
  document.title = '管理员后台'
})
</script>

<style scoped>
:global(html),
:global(body),
:global(#app) {
  width: 100%;
  height: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
  background: #f5f7fb;
}

:global(body) {
  display: block;
}

.admin-layout {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  background: #f5f7fb;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: #ffffff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  padding: 20px 16px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  z-index: 1001;
}

/* logo */
.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 30px;
  padding: 0 6px;
}

.logo-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #409eff, #6366f1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: white;
  font-weight: bold;
}

.logo-text h2 {
  margin: 0;
  color: #111827;
  font-size: 20px;
}

.logo-text span {
  font-size: 12px;
  color: #94a3b8;
}

/* 菜单 */
.menu {
  flex: 1;
  border: none;
  background: transparent;
}

:deep(.el-menu) {
  background: transparent;
  border-right: none;
}

:deep(.el-menu-item) {
  height: 52px;
  border-radius: 14px;
  color: #475569;
  margin-bottom: 10px;
  transition: all 0.25s ease;
  font-size: 15px;
}

:deep(.el-menu-item:hover) {
  background: #f1f5f9;
  color: #111827;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #409eff, #6366f1);
  color: white;
  font-weight: 600;
}

:deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

/* 底部卡片 */
.bottom-card {
  padding: 18px;
  border-radius: 18px;
  background: #f8fafc;
  margin-top: 10px;
  border: 1px solid #ebeef5;
}

.bottom-title {
  color: #111827;
  font-size: 15px;
  font-weight: bold;
  margin-bottom: 6px;
}

.bottom-desc {
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

/* 主体 */
.main {
  flex: 1;
  width: 0;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f7fb;
}

/* 顶部栏 */
.topbar {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
  display: none;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-sizing: border-box;
}

.left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.page-title {
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

.menu-btn {
  display: inline-flex;
}

/* 内容 */
.content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 24px;
  box-sizing: border-box;
}

.content::-webkit-scrollbar {
  display: none;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 22px;
  padding: 28px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
}

.welcome-title {
  color: white;
  font-size: 26px;
  font-weight: bold;
  margin-bottom: 10px;
}

.welcome-desc {
  color: rgba(255, 255, 255, 0.65);
  line-height: 1.8;
  font-size: 15px;
}

/* 手机遮罩 */
.mobile-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
}

/* 手机端 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: -280px;
    top: 0;
    bottom: 0;
    height: 100vh;
    z-index: 2000;
  }

  .sidebar.mobileOpen {
    left: 0;
  }

  .menu-btn {
    display: inline-flex;
  }

  .topbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
  }

  .page-title {
    font-size: 18px;
  }

  .content {
    padding: 16px;
  }

  .welcome-card {
    padding: 20px;
    border-radius: 18px;
  }

  .welcome-title {
    font-size: 22px;
  }
}
</style>