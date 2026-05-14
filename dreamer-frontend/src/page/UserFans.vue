<template>
  <div class="fans-page" ref="scrollRef" @scroll="handleScroll">
    <!-- 顶部 -->
    <div class="page-header">
      <div class="title">粉丝</div>
      <div class="sub-title">FANS LIST</div>
    </div>

    <!-- 空状态 -->
    <el-empty
        v-if="!loading && fansList.length === 0"
        description="这里空空的，像我的心一样"
    />

    <!-- 用户列表 -->
    <div class="fans-list">
      <div
          class="fans-item"
          v-for="item in fansList"
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
        v-if="finished && fansList.length > 0"
    >
      已经到底啦～
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {Loading} from "@element-plus/icons-vue";
import {listUserFans, removeFansUser} from "@/api/userApi.ts";
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
const fansList = ref<UserItem[]>([]);

const loading = ref(false);

const finished = ref(false);

const cursor = ref<string | null>(null);

const offset = ref<number | null>(null);

const scrollRef = ref<HTMLElement>();

/**
 * 查粉丝列表
 */
const loadFollowingList = async () => {

  if (loading.value || finished.value) {
    return;
  }

  loading.value = true;

  try {

    const res = await listUserFans(cursor.value, offset.value,userId)

    const data = res.data?.data;

    const list = data?.list || [];

    if (list.length === 0) {
      finished.value = true;
      return;
    }

    fansList.value.push(...list);

    cursor.value = data.cursor;

    offset.value = data.offset;

  } catch (e) {

    console.error(e);

    ElMessage.error("加粉丝列表失败");

  } finally {

    loading.value = false;

  }
};

/**
 * 移除粉丝
 */
const handleRemoveFans = async (item: UserItem) => {

  try {

    await ElMessageBox.confirm(
        `确定移除粉丝 ${item.username} 吗？`,
        "提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }
    );

    const res = await removeFansUser(item.id);

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg);
      return
    }

    fansList.value = fansList.value.filter(
        user => user.id !== item.id
    );
    ElMessage.success(res.data.msg);
  } catch (e: any) {

    if (e !== "cancel") {
      ElMessage.error("移除粉丝失败");
    }
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
  document.title = '用户粉丝'
  loadFollowingList();
});
</script>

<style scoped>
.fans-page {
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
.fans-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* item */
.fans-item {
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

.fans-item:hover {
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

  .fans-page {
    padding: 20px;
  }

  .fans-item {
    padding: 16px;
  }

  .title {
    font-size: 28px;
  }

  .username {
    font-size: 17px;
  }
}
</style>