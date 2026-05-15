<template>
  <div class="feedback-page">
    <!-- 顶部 -->
    <div class="page-header">
      <div>
        <div class="title">用户反馈</div>
        <div class="sub-title">USER FEEDBACK</div>
      </div>
    </div>

    <!-- 列表 -->
    <div
        v-loading="loading"
        class="feedback-list"
    >
      <div
          v-for="item in feedbackList"
          :key="item.id"
          class="feedback-card"
      >
        <!-- 顶部 -->
        <div class="card-top">
          <div class="left-top">
            <div
                class="user-link"
                @click="goUserHome(item.userId)"
            >
              <el-icon>
                <User />
              </el-icon>

              <span>查看反馈者</span>
            </div>

            <el-tag
                v-if="item.type === 0"
                type="danger"
                round
            >
              Bug反馈
            </el-tag>

            <el-tag
                v-else
                type="success"
                round
            >
              功能建议
            </el-tag>
          </div>

          <div class="right-top">
            <el-tag
                v-if="item.status === 0"
                type="warning"
                round
            >
              未处理
            </el-tag>

            <el-tag
                v-else
                type="success"
                round
            >
              已处理
            </el-tag>
          </div>
        </div>

        <!-- 内容 -->
        <div class="content-box">
          {{ item.content }}
        </div>

        <!-- 回复 -->
        <div
            v-if="item.reply"
            class="reply-box"
        >
          <div class="reply-title">
            管理员回复
          </div>

          <div class="reply-content">
            {{ item.reply }}
          </div>
        </div>

        <!-- 时间 -->
        <div class="time-row">
          <div>
            反馈时间：{{ formatTime(item.createTime) }}
          </div>

          <div>
            受理时间：{{ formatTime(item.updateTime) }}
          </div>
        </div>

        <!-- 操作 -->
        <div
            v-if="item.status === 0"
            class="action-row"
        >
          <el-button
              type="primary"
              round
              @click="openReplyDialog(item)"
          >
            受理反馈
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
          v-if="!loading && feedbackList.length === 0"
          description="暂无反馈内容"
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

    <!-- 回复弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        width="500px"
        title="处理反馈"
    >
      <el-input
          v-model="replyContent"
          type="textarea"
          :rows="6"
          maxlength="300"
          show-word-limit
          placeholder="请输入处理回复内容..."
      />

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>

        <el-button
            type="primary"
            :loading="submitLoading"
            @click="submitReply"
        >
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  User
} from '@element-plus/icons-vue'
import {handleFeedback, listFeedbacks} from "@/api/adminApi.ts";

interface FeedbackItem {
  id: number
  userId: number | string
  type: number
  content: string
  status: number
  reply: string | null
  createTime: string
  updateTime: string
}

const router = useRouter()

const loading = ref(false)

const page = ref(1)
const size = ref(5)
const total = ref(0)

const feedbackList = ref<FeedbackItem[]>([])

/**
 * 弹窗
 */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const replyContent = ref('')
const currentFeedbackId = ref<number | null>(null)

/**
 * 获取反馈列表
 */
const loadFeedbackList = async () => {
  loading.value = true

  try {

    const res = await listFeedbacks({
      page: page.value
    });

    const data = res.data.data

    feedbackList.value = data.records || []
    total.value = data.total || 0
    size.value = data.size || 5
  } catch (e) {
    ElMessage.error('获取反馈失败')
  } finally {
    loading.value = false
  }
}

/**
 * 分页
 */
const changePage = (currentPage: number) => {
  page.value = currentPage
  loadFeedbackList()
}

/**
 * 打开回复弹窗
 */
const openReplyDialog = (item: FeedbackItem) => {
  currentFeedbackId.value = item.id
  replyContent.value = ''
  dialogVisible.value = true
}

/**
 * 提交回复
 */
const submitReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  if (!currentFeedbackId.value) {
    return
  }

  submitLoading.value = true

  try {

    const res = await handleFeedback(currentFeedbackId.value,{
      reply: replyContent.value
    });

    if (res.data.code != 200) {
      ElMessage.error(res.data.msg)
    }

    ElMessage.success(res.data.msg);

    dialogVisible.value = false

    await loadFeedbackList()
  } catch (e) {
    ElMessage.error('处理失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 跳转用户主页
 */
const goUserHome = (userId: number | string) => {
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
  loadFeedbackList()
})
</script>

<style scoped>
.feedback-page {
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
.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片 */
.feedback-card {
  background: white;
  border-radius: 22px;
  padding: 22px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
}

.feedback-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.06);
}

/* 顶部 */
.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.left-top {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 用户链接 */
.user-link {
  height: 38px;
  padding: 0 16px;
  background: #f3f6fb;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #374151;
  transition: 0.25s;
  font-size: 14px;
  font-weight: 600;
}

.user-link:hover {
  background: #409eff;
  color: white;
}

/* 内容 */
.content-box {
  margin-top: 18px;
  background: #f9fafb;
  border-radius: 18px;
  padding: 18px;
  line-height: 1.9;
  color: #374151;
  font-size: 15px;
  word-break: break-word;
  white-space: pre-wrap;
}

/* 回复 */
.reply-box {
  margin-top: 18px;
  background: #eff6ff;
  border-radius: 18px;
  padding: 18px;
}

.reply-title {
  font-size: 14px;
  font-weight: 700;
  color: #2563eb;
}

.reply-content {
  margin-top: 10px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 时间 */
.time-row {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #9ca3af;
  font-size: 13px;
  gap: 12px;
}

/* 操作 */
.action-row {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 分页 */
.pagination-box {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 手机适配 */
@media (max-width: 768px) {
  .feedback-page {
    padding: 14px;
  }

  .title {
    font-size: 24px;
  }

  .feedback-card {
    padding: 16px;
    border-radius: 18px;
  }

  .card-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .time-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .content-box,
  .reply-box {
    padding: 14px;
    font-size: 14px;
  }

  .action-row {
    justify-content: stretch;
  }

  .action-row .el-button {
    width: 100%;
  }
}
</style>