<template>
  <div class="notice-page">
    <!-- 顶部 -->
    <div class="page-header">
      <div>
        <div class="title">公告管理</div>
        <div class="sub-title">NOTICE MANAGEMENT</div>
      </div>

      <el-button
          type="primary"
          round
          @click="openPublishDialog"
      >
        <el-icon>
          <Plus/>
        </el-icon>
        发布公告
      </el-button>
    </div>

    <!-- 公告列表 -->
    <div
        v-loading="loading"
        class="notice-list"
    >
      <div
          v-for="item in noticeList"
          :key="item.id"
          class="notice-card"
      >
        <!-- 顶部 -->
        <div class="card-top">
          <div class="left-top">
            <div
                class="user-link"
                @click="goUserHome(item.sendId)"
            >
              <el-icon>
                <User/>
              </el-icon>

              <span>查看发布者</span>
            </div>
          </div>

          <div class="right-top">
            {{ formatTime(item.createTime) }}
          </div>
        </div>

        <!-- 内容 -->
        <div class="notice-content">
          {{ item.content }}
        </div>

        <!-- 操作 -->
        <div class="action-row">
          <el-button
              type="danger"
              round
              plain
              @click="deleteNotice(item.id)"
          >
            删除公告
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
          v-if="!loading && noticeList.length === 0"
          description="暂无公告内容"
      />
    </div>

    <!-- 发布公告弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        title="发布公告"
        width="520px"
    >
      <el-input
          v-model="noticeContent"
          type="textarea"
          :rows="7"
          maxlength="500"
          show-word-limit
          placeholder="请输入公告内容..."
      />

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>

        <el-button
            type="primary"
            :loading="submitLoading"
            @click="submitNotice"
        >
          发布公告
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue'
import axios from 'axios'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, User} from '@element-plus/icons-vue'
import {useRouter} from 'vue-router'
import {listNotices, publishNotice, removeNotice} from "@/api/adminApi.ts";

interface NoticeItem {
  id: number
  type: number
  sendId: number | string
  content: string
  isBroadcast: number
  createTime: string
}

const router = useRouter()

const loading = ref(false)

const noticeList = ref<NoticeItem[]>([])

/**
 * 发布公告
 */
const dialogVisible = ref(false)
const submitLoading = ref(false)
const noticeContent = ref('')

/**
 * 获取公告列表
 */
const loadNoticeList = async () => {
  loading.value = true

  try {
    const res = await listNotices();
    noticeList.value = res.data.data || []
  } catch (e) {
    ElMessage.error('获取公告失败')
  } finally {
    loading.value = false
  }
}

/**
 * 打开发布弹窗
 */
const openPublishDialog = () => {
  noticeContent.value = ''
  dialogVisible.value = true
}

/**
 * 发布公告
 */
const submitNotice = async () => {
  if (!noticeContent.value.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }

  submitLoading.value = true

  try {
    await publishNotice({
      content: noticeContent.value
    })

    ElMessage.success('公告发布成功')

    dialogVisible.value = false

    await loadNoticeList()
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除公告
 */
const deleteNotice = async (noticeId: number) => {
  try {
    await ElMessageBox.confirm(
        '确定删除该公告吗？',
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: "确定",
          cancelButtonText: "取消"
        }
    )

    const res = await removeNotice(noticeId);

    if(res.data.code != 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg)

    await loadNoticeList()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
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
  loadNoticeList()
})
</script>

<style scoped>
.notice-page {
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
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 卡片 */
.notice-card {
  background: white;
  border-radius: 22px;
  padding: 22px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
}

.notice-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.06);
}

/* 顶部 */
.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.left-top {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 用户 */
.user-link {
  height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  background: #f3f6fb;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #374151;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.25s;
}

.user-link:hover {
  background: #409eff;
  color: white;
}

/* 时间 */
.right-top {
  color: #9ca3af;
  font-size: 13px;
}

/* 内容 */
.notice-content {
  margin-top: 18px;
  padding: 18px;
  border-radius: 18px;
  background: #f9fafb;
  line-height: 1.9;
  color: #374151;
  font-size: 15px;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 操作 */
.action-row {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 手机适配 */
@media (max-width: 768px) {
  .notice-page {
    padding: 14px;
  }

  .title {
    font-size: 24px;
  }

  .notice-card {
    padding: 16px;
    border-radius: 18px;
  }

  .card-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .notice-content {
    padding: 14px;
    font-size: 14px;
    line-height: 1.7;
  }

  .action-row {
    justify-content: stretch;
  }

  .action-row .el-button {
    width: 100%;
  }
}
</style>