<template>
  <div class="profile-container">

    <!-- 标题 -->
    <div class="card-header">
      <div class="title">个人资料</div>
      <div class="sub-title">USER PROFILE</div>
    </div>

    <!-- 主卡片 -->
    <div class="profile-card">

      <!-- 顶部用户信息 -->
      <div class="top-user-info">
        <div class="avatar-wrapper">
          <el-avatar
              class="user-avatar"
              :size="72"
              :src="user.avatar"
          />
        </div>

        <div class="top-info-right">
          <div class="username">
            {{ user.username || '未知用户' }}
          </div>

          <div class="user-tags">
            <div class="tag">
              {{ genderText }}
            </div>

            <div class="tag email-tag">
              {{ user.email || '暂无邮箱' }}
            </div>
          </div>
        </div>
      </div>

      <!-- 信息列表 -->
      <div class="profile-form">

        <!-- 用户名 -->
        <div class="form-card">
          <div class="form-label">用户名</div>

          <div class="form-content">
            {{ user.username || '暂无用户名' }}
          </div>
        </div>

        <!-- 性别 -->
        <div class="form-card">
          <div class="form-label">性别</div>

          <div class="form-content">
            {{ genderText }}
          </div>
        </div>

        <!-- 邮箱 -->
        <div class="form-card">
          <div class="form-label">邮箱</div>

          <div class="form-content">
            {{ user.email || '暂无邮箱' }}
          </div>
        </div>

        <!-- 个人介绍 -->
        <div class="form-card bio-card">
          <div class="form-label">个人介绍</div>

          <div class="form-content bio-text">
            {{ user.bio || '这个人很懒，还没有留下介绍...' }}
          </div>
        </div>
      </div>

      <!-- 按钮 -->
      <div class="follow-btn-box">
        <el-button
            class="follow-btn"
            :type="isFollowed == 1 ? 'danger' : 'primary'"
            @click="handleFollow"
            :loading="followLoading"
        >
          {{isFollowed == 1 ? '取消关注' : '关注该用户'}}
        </el-button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref, computed} from "vue";
import {ElMessage} from "element-plus";
import {queryUserInfo, followUser, checkFollowStatus, unfollowUser} from "@/api/userApi.ts";
import {useRoute} from "vue-router";

const route = useRoute()
const userId = route.params.userId as string

// 用户数据
const user = reactive({
  avatar: "",
  username: "",
  gender: 0 as number,
  email: "",
  bio: "",
});

// 关注相关
const isFollowed = ref(0) // 1=已关注 0=未关注
const followLoading = ref(false)

// 性别文本转换
const genderText = computed(() => {
  if (user.gender === 1) return '男'
  if (user.gender === 2) return '女'
  return '未知'
})

// 获取用户信息
const getUserInfo = async () => {
  try {
    const res = await queryUserInfo(userId);
    document.title = "「" + res.data.data.username + '」的个人中心'
    if (res.data.code === 200) {
      Object.assign(user, res.data.data);
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    ElMessage.error("获取用户信息失败");
  }
};

// 查询是否已关注
const checkFollow = async () => {
  try {
    const res = await checkFollowStatus(userId)
    // 后端返回 1=已关注 0=未关注
    if (res.data.code === 200) {
      isFollowed.value = res.data.msg
    }
  } catch (e) {
    console.error('查询关注状态失败')
  }
}

// 关注 / 取消关注
const handleFollow = async () => {
  followLoading.value = true
  try {

    if (isFollowed.value == 0) {
      const res = await followUser(userId);
      if (res.data.code === 200) {
        // 切换状态
        isFollowed.value = 1
        ElMessage.success(res.data.msg)
      } else {
        ElMessage.error(res.data.msg)
      }
    }else {
      const res = await unfollowUser(userId);
      if (res.data.code === 200) {
        // 切换状态
        isFollowed.value = 0
        ElMessage.success(res.data.msg)
      } else {
        ElMessage.error(res.data.msg)
      }
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

onMounted(() => {
  getUserInfo();
  checkFollow();
});
</script>

<style scoped>
.profile-container {
  width: 100%;
  height: 100%;
  padding: 100px 24px;
  box-sizing: border-box;
  background: #f5f7fb;
  overflow-y: auto;
}


/* 标题 */
.card-header {
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.sub-title {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 2px;
  color: #94a3b8;
}

/* 顶部用户 */
.top-user-info {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px;
  border-radius: 16px;
  background: #f8fafc;
  margin-bottom: 24px;
}

.user-avatar {
  border: 3px solid white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.top-info-right {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 10px;
  word-break: break-word;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag {
  padding: 6px 12px;
  border-radius: 999px;
  background: white;
  border: 1px solid #e5e7eb;
  color: #475569;
  font-size: 13px;
}

.email-tag {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 信息区域 */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-card {
  display: flex;
  gap: 18px;
  padding: 18px;
  border-radius: 14px;
  background: #fafbfd;
  border: 1px solid #eef2f7;
}

.form-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
}

.form-content {
  flex: 1;
  color: #1e293b;
  line-height: 1.7;
  word-break: break-word;
}

.bio-text {
  white-space: pre-wrap;
}

/* 按钮 */
.follow-btn-box {
  margin-top: 50px;
  display: flex;
  justify-content: center;
}

.follow-btn {
  min-width: 170px;
  height: 50px;
  border-radius: 15px;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-container {
    padding: 14px;
  }

  .profile-card {
    padding: 18px;
  }

  .top-user-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .username {
    font-size: 22px;
  }

  .form-card {
    flex-direction: column;
    gap: 8px;
  }

  .form-label {
    width: 100%;
  }

  .follow-btn {
    width: 100%;
  }
}
</style>