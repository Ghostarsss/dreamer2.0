<template>
  <div class="user-layout">

    <!-- 左侧导航栏 -->
    <div class="profile-sidebar">
      <div class="sidebar-header">
        <div class="avatar">
          <el-icon :size="32">
            <UserFilled />
          </el-icon>
        </div>

        <div class="user-info">
          <div class="username">Dreamer</div>
          <div class="desc">个人中心</div>
        </div>
      </div>

      <el-menu
          class="sidebar-menu"
          :default-active="route.path"
          router
      >
        <el-menu-item :index="buildPath('/user/profile', '/user/home')">
          <el-icon>
            <User />
          </el-icon>
          <span>个人资料</span>
        </el-menu-item>

        <el-menu-item :index="buildPath('/user/following')">
          <el-icon>
            <Star />
          </el-icon>
          <span>关注</span>
        </el-menu-item>

        <el-menu-item :index="buildPath('/user/fans')">
          <el-icon>
            <UserFilled />
          </el-icon>
          <span>粉丝</span>
        </el-menu-item>

        <el-menu-item :index="buildPath('/user/vp')">
          <el-icon>
            <Coin />
          </el-icon>
          <span>虚拟财产</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧内容 -->
    <div class="content-wrapper">
      <router-view />
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

import {
  User,
  UserFilled,
  Star,
  Coin
} from "@element-plus/icons-vue";

const route = useRoute();

/**
 * 获取当前路径中的 userId
 * 例如：
 * /user/home/123
 * /user/following/123
 */
const userId = computed(() => {
  const match = route.path.match(/\/(\d+)$/);
  return match ? match[1] : null;
});

/**
 * 构建菜单路径
 * 有 userId:
 *   /user/home/123
 *   /user/following/123
 *
 * 无 userId:
 *   /user/profile
 *   /user/following
 */
const buildPath = (basePath: string, customPath?: string) => {
  const finalPath = customPath || basePath;

  if (userId.value) {
    return `${finalPath}/${userId.value}`;
  }

  return basePath;
};
</script>

<style scoped>
:global(html),
:global(body),
:global(#app) {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.user-layout {
  position: fixed;
  inset: 0;
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
  z-index: 1;
}

.content-wrapper {
  flex: 1;
  height: 100vh;
  padding: 24px;
  box-sizing: border-box;
  overflow-x: hidden;
  overflow-y: auto;
}

.profile-sidebar {
  width: 240px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
}

.sidebar-header {
  padding: 32px 20px 24px;
  display: flex;
  align-items: center;
  gap: 14px;
  border-bottom: 1px solid #f2f2f2;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.desc {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.sidebar-menu {
  border-right: none;
  padding-top: 12px;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 52px;
  font-size: 15px;
  margin: 4px 12px;
  border-radius: 10px;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #ecf5ff;
  color: #409eff;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background: #f5f7fa;
}
@media screen and (max-width: 768px) {
  .user-layout {
    flex-direction: column;
    overflow: hidden;
  }

  .profile-sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #ebeef5;
  }

  .sidebar-header {
    padding: 16px;
  }

  .avatar {
    width: 46px;
    height: 46px;
  }

  .username {
    font-size: 16px;
  }

  .desc {
    font-size: 12px;
  }

  .sidebar-menu {
    display: flex;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 8px;
    gap: 8px;
    white-space: nowrap;
  }

  .sidebar-menu :deep(.el-menu) {
    display: flex;
    border-bottom: none;
  }

  .sidebar-menu :deep(.el-menu-item) {
    flex-shrink: 0;
    margin: 0;
    height: 44px;
    padding: 0 16px;
    border-radius: 8px;
  }

  .content-wrapper {
    flex: 1;
    width: 100%;
    height: auto;
    padding: 14px;
    overflow-y: auto;
  }
}
</style>