<template>
  <div class="following-page" ref="scrollRef" @scroll="handleScroll">
    <!-- 顶部 -->
    <div class="page-header">
      <div class="title">关注</div>
      <div class="sub-title">FOLLOWING LIST</div>
    </div>

    <!-- 空状态 -->
    <el-empty
        v-if="!loading && followList.length === 0"
        description="没有关注任何用户"
    />

    <!-- 用户列表 -->
    <div class="follow-list">
      <div
          class="follow-item"
          v-for="item in followList"
          :key="item.id"
      >
        <!-- 左侧用户信息 -->
        <div
            class="user-info"
            @click="openUserHome(item.id)"
        >
          <!-- 头像 -->
          <el-avatar
              class="avatar"
              :src="item.avatar || ''"
              :size="56"
          >
            {{ item.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>

          <!-- 信息 -->
          <div class="info">
            <div class="username">
              {{ item.username }}
            </div>

            <div class="meta">
              <span class="level">
                Lv.{{ item.level || 1 }}
              </span>
            </div>

            <div
                class="bio"
                v-if="item.bio"
            >
              {{ item.bio }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载 -->
    <div
        class="loading-box"
        v-if="loading"
    >
      <el-icon class="is-loading">
        <Loading/>
      </el-icon>

      <span>正在加载...</span>
    </div>

    <!-- 没有更多 -->
    <div
        class="no-more"
        v-if="finished && followList.length > 0"
    >
      已经到底啦～
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {Loading} from "@element-plus/icons-vue";
import {listUserFollowing} from "@/api/userApi.ts";
import {useRoute} from "vue-router";

interface UserItem {
  id: string;
  username: string;
  avatar: string | null;
  bio: string | null;
  exp: number;
  level: number;
}

const route = useRoute()
const userId = route.params.userId as string
const followList = ref<UserItem[]>([]);

const loading = ref(false);

const finished = ref(false);

const cursor = ref<string | null>(null);

const offset = ref<number | null>(null);

const scrollRef = ref<HTMLElement>();

/**
 * 查询关注列表
 */
const loadFollowingList = async () => {

  if (loading.value || finished.value) {
    return;
  }

  loading.value = true;

  try {

    const res = await listUserFollowing(cursor.value, offset.value, userId)

    const data = res.data?.data;

    const list = data?.list || [];

    if (list.length === 0) {
      finished.value = true;
      return;
    }

    followList.value.push(...list);

    cursor.value = data.cursor;

    offset.value = data.offset;

  } catch (e) {

    console.error(e);

    ElMessage.error("加载关注列表失败");

  } finally {

    loading.value = false;

  }
};


/**
 * 打开用户主页
 */
const openUserHome = (id: string) => {

  if (id != localStorage.getItem("userId")) {
    window.open(`/user/home/${id}`, "_blank");
  }
  window.open(`/user/`, "_blank");
};

/**
 * 滚动加载
 */
const handleScroll = () => {

  const el = scrollRef.value;

  if (!el || loading.value || finished.value) {
    return;
  }

  const distance =
      el.scrollHeight - el.scrollTop - el.clientHeight;

  if (distance < 200) {
    loadFollowingList();
  }
};

onMounted(() => {
  document.title = '用户关注'
  loadFollowingList();
});
</script>

<style scoped>
.following-page {
  width: 100%;
  height: 100vh;
  overflow-y: auto;
  box-sizing: border-box;
  padding: 32px;
  background: radial-gradient(circle at top left, #f4f7ff 0%, transparent 30%),
  radial-gradient(circle at bottom right, #eef2ff 0%, transparent 35%),
  #f8fafc;
}

/* 顶部 */
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

/* 列表 */
.follow-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* item */
.follow-item {
  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 20px 24px;

  border-radius: 20px;

  background: rgba(255, 255, 255, 0.88);

  backdrop-filter: blur(18px);

  border: 1px solid rgba(255, 255, 255, 0.6);

  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.06);

  transition: all 0.25s ease;
}

.follow-item:hover {
  transform: translateY(-3px);

  box-shadow: 0 14px 38px rgba(15, 23, 42, 0.1);
}

/* 左侧 */
.user-info {
  display: flex;
  align-items: center;
  gap: 18px;
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.avatar {
  flex-shrink: 0;
  border: 2px solid #e5e7eb;
}

/* 用户信息 */
.info {
  min-width: 0;
}

.username {
  font-size: 19px;
  font-weight: 700;
  color: #111827;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;

  margin-top: 6px;
}

.level {
  padding: 2px 10px;

  border-radius: 999px;

  font-size: 12px;
  font-weight: 600;

  color: #4338ca;

  background: #eef2ff;
}

.bio {
  margin-top: 8px;

  font-size: 14px;
  color: #64748b;

  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 加载 */
.loading-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;

  padding: 26px 0;

  color: #64748b;
  font-size: 14px;
}

/* 没有更多 */
.no-more {
  text-align: center;

  padding: 30px 0 40px;

  color: #94a3b8;

  font-size: 14px;
}

/* 移动端 */
@media (max-width: 768px) {

  .following-page {
    padding: 20px;
  }

  .follow-item {
    padding: 16px;
  }

  .title {
    font-size: 28px;
  }

  .username {
    font-size: 17px;
  }

  .follow-btn {
    min-width: 88px;
  }
}
</style>