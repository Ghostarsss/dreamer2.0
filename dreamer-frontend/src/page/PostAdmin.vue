<template>
  <div class="audit-page">
    <!-- 顶部 -->
    <div class="page-header">
      <div>
        <div class="title">文章审核</div>
        <div class="sub-title">ARTICLE REVIEW</div>
      </div>
    </div>

    <!-- 内容 -->
    <div
        v-loading="loading"
        class="post-list"
    >
      <div
          v-for="item in postList"
          :key="item.id"
          class="post-card"
      >
        <!-- 用户信息 -->
        <div
            class="user-info"
            @click="goUserHome(item.userId)"
        >
          <el-avatar
              :src="item.avatar"
              :size="52"
          />

          <div class="user-meta">
            <div class="username-row">
              <span class="username">{{ item.username }}</span>

              <div class="level-tag">
                Lv.{{ item.level }}
              </div>
            </div>

            <div class="time">
              {{ formatTime(item.createTime) }}
            </div>
          </div>
        </div>

        <!-- 文章内容 -->
        <div class="post-content">
          {{ item.content }}
        </div>

        <!-- 操作 -->
        <div
            v-if="item.status === 0"
            class="action-row"
        >
          <el-button
              type="success"
              round
              @click="handleAudit(item.id, 1)"
          >
            审核通过
          </el-button>

          <el-button
              type="danger"
              round
              plain
              @click="handleAudit(item.id, 2)"
          >
            驳回文章
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
          v-if="!loading && postList.length === 0"
          description="暂无待审核文章"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination-box">
      <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="changePage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  Refresh,
  Pointer,
  ChatDotRound,
  EditPen
} from '@element-plus/icons-vue'
import axios from 'axios'
import {useRouter} from 'vue-router'
import {checkPost, listPosts} from "@/api/adminApi.ts";

const router = useRouter()

interface PostItem {
  id: string
  userId: string
  username: string
  avatar: string
  level: number
  content: string
  status: number
  protonCount: number
  likeCount: number
  commentCount: number | null
  editCount: number
  createTime: string
}

const loading = ref(false)

const page = ref(1)
const size = ref(5)
const total = ref(0)

const postList = ref<PostItem[]>([])

/**
 * 获取文章列表
 */
const loadPosts = async () => {
  loading.value = true

  try {

    const res = await listPosts({
      page: page.value
    });

    const data = res.data.data

    postList.value = data.records || []
    total.value = data.total || 0
    size.value = data.size || 5
  } catch (e) {
    ElMessage.error('获取文章失败')
  } finally {
    loading.value = false
  }
}

/**
 * 审核
 */
const handleAudit = async (
    postId: string,
    status: number
) => {
  const text = status === 1 ? '通过' : '驳回'

  try {
    await ElMessageBox.confirm(
        `确定${text}该文章吗？`,
        '审核确认',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
    )

    const res = await checkPost(postId, {
      status
    });

    if (res.data.code != 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg);

    await loadPosts()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

/**
 * 分页
 */
const changePage = (currentPage: number) => {
  page.value = currentPage
  loadPosts()
}

/**
 * 跳转用户主页
 */
const goUserHome = (userId: string) => {
  const url = router.resolve({
    path: `/user/home/${userId}`
  })

  window.open(url.href, '_blank')
}

/**
 * 时间格式化
 */
const formatTime = (time: string) => {
  if (!time) return ''

  return time.replace('T', ' ')
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.audit-page {
  min-height: 100vh;
  background: #f6f8fb;
  padding: 24px;
  box-sizing: border-box;
}

/* 顶部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #111827;
}

.sub-title {
  margin-top: 4px;
  color: #9ca3af;
  font-size: 13px;
  letter-spacing: 2px;
}

/* 列表 */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片 */
.post-card {
  background: white;
  border-radius: 22px;
  padding: 22px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.06);
}

/* 用户信息 */
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info:hover .username {
  color: #409eff;
}

.user-meta {
  margin-left: 14px;
  flex: 1;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-size: 17px;
  font-weight: 700;
  color: #111827;
  transition: 0.2s;
}

.level-tag {
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.time {
  margin-top: 6px;
  color: #9ca3af;
  font-size: 13px;
}

/* 内容 */
.post-content {
  margin-top: 18px;
  padding: 18px;
  background: #f9fafb;
  border-radius: 16px;
  color: #374151;
  line-height: 1.9;
  font-size: 15px;
  word-break: break-word;
  white-space: pre-wrap;
}

/* 底部 */
.post-footer {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.left-info {
  display: flex;
  align-items: center;
  gap: 18px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6b7280;
  font-size: 14px;
}

/* 操作栏 */
.action-row {
  margin-top: 22px;
  display: flex;
  justify-content: flex-end;
  gap: 14px;
}

/* 分页 */
.pagination-box {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 手机适配 */
@media (max-width: 768px) {
  .audit-page {
    padding: 14px;
  }

  .title {
    font-size: 24px;
  }

  .post-card {
    padding: 16px;
    border-radius: 18px;
  }

  .username {
    font-size: 15px;
  }

  .post-content {
    padding: 14px;
    font-size: 14px;
    line-height: 1.7;
  }

  .post-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .action-row {
    justify-content: space-between;
  }

  .action-row .el-button {
    flex: 1;
  }

  .left-info {
    gap: 12px;
    flex-wrap: wrap;
  }
}
</style>