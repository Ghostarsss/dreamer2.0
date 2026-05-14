<template>
  <div class="forum-page hide-scrollbar" ref="scrollContainer" @scroll="handleScroll">
    <!-- 发布区域 -->
    <div class="publish-card" v-if="isLogin">
      <el-input
          v-model="content"
          type="textarea"
          resize="none"
          :rows="5"
          maxlength="5000"
          show-word-limit
          placeholder="分享你的想法..."
      />
      <div class="publish-footer">
        <el-button
            type="primary"
            :loading="publishLoading"
            @click="publishPost"
        >
          发布
        </el-button>
      </div>
    </div>

    <!-- 文章列表 -->
    <div
        v-for="item in postList"
        :key="item.id"
        class="post-card"
    >
      <!-- 顶部 -->
      <div class="post-header">

        <!-- 新增：文章状态 -->
        <div
            class="post-status"
            :class="{
              reviewing: item.status === 0,
              approved: item.status === 1,
              rejected: item.status === 2
            }"
        >
          {{
            item.status === 0
                ? '审核中'
                : item.status === 1
                    ? '已通过'
                    : '已驳回'
          }}
        </div>

        <div class="time">{{ formatTime(item.createTime) }}</div>
      </div>

      <!-- 正文 -->
      <div class="post-content">
        <div
            class="content-text"
            :class="{ collapsed: !item.expanded }"
        >
          {{ item.content }}
        </div>

        <div
            v-if="item.content.length > 180"
            class="expand-btn"
            @click="item.expanded = !item.expanded"
        >
          {{ item.expanded ? '收起' : '展开全文' }}
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="post-actions">

        <!-- 支持 -->
        <div
            class="action-item support-item"
            @click="openSupportDialog(item)"
            v-if="item.status == 1"
        >
          <el-icon>
            <Star/>
          </el-icon>
          <span>支持 {{ item.protonCount }}</span>
        </div>

        <!-- 评论 -->
        <div
            class="action-item"
            :class="{'action-item-color': item.showComment}"
            @click="toggleComment(item)"
            v-if="item.status == 1"
        >
          <el-icon>
            <ChatDotRound/>
          </el-icon>
          <span>评论 {{ item.commentCount }}</span>
        </div>

        <!-- 点赞 -->
        <div
            class="action-item like-item"
            :class="{ liked: item.isLike === 1 }"
            @click="likePost(item)"
            v-if="item.status == 1"
        >
          <el-icon
              class="like-icon"
              :class="{ bounce: item.likeAnimating }"
          >
            <Pointer/>
          </el-icon>
          <span>点赞 {{ item.likeCount }}</span>
        </div>

        <!-- 修改 -->
        <div
            class="action-item edit-item"
            @click="openEditDialog(item)"
        >
          <el-icon>
            <Edit/>
          </el-icon>
          <span>修改</span>
        </div>

        <!-- 删除 -->
        <div
            class="action-item delete-item"
            @click="deletePost(item)"
        >
          <el-icon>
            <Delete/>
          </el-icon>
          <span>删除</span>
        </div>

      </div>

      <!-- 评论区域 -->
      <div v-if="item.showComment" class="comment-wrapper">
        <div class="mock-comment">
          <Comment :post-id="item.id"/>
        </div>
      </div>
    </div>

    <!-- 悬浮提示 -->
    <div
        v-if="showUserTip"
        class="user-tip-pop"
        :style="{ left: tipX + 10 + 'px', top: tipY - 35 + 'px' }"
    >
      点击进入用户主页
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading">
      <el-icon class="is-loading">
        <Loading/>
      </el-icon>
      <span>加载中...</span>
    </div>

    <!-- 没有更多 -->
    <div v-if="finished" class="finished">
      我们期待您分享更多想法
    </div>

    <!-- 支持弹窗 -->
    <el-dialog
        v-model="supportDialogVisible"
        title="支持文章"
        width="420px"
        destroy-on-close
        append-to-body
        align-center
        lock-scroll
    >
      <div class="support-dialog-content">
        <div class="support-tip">
          请输入要支持的「质子」数 (最多 20颗)
        </div>

        <el-input-number
            v-model="supportProtons"
            :min="1"
            :step="1"
            step-strictly
            controls-position="right"
            class="support-input"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeSupportDialog">取消</el-button>

          <el-button
              type="primary"
              :loading="supportLoading"
              @click="submitSupport"
          >
            打赏
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改文章弹窗 -->
    <el-dialog
        v-model="editDialogVisible"
        title="修改文章"
        width="600px"
        destroy-on-close
        append-to-body
        align-center
        lock-scroll
    >
      <div class="edit-dialog-content">

        <el-input
            v-model="editContent"
            type="textarea"
            resize="none"
            :rows="8"
            maxlength="5000"
            show-word-limit
            placeholder="请输入修改后的内容"
        />

      </div>

      <template #footer>
        <div class="dialog-footer">

          <el-button @click="closeEditDialog">
            取消
          </el-button>

          <el-button
              type="primary"
              :loading="editLoading"
              @click="submitEditPost"
          >
            保存修改
          </el-button>

        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, onUnmounted} from 'vue'

import {
  Star,
  ChatDotRound,
  Pointer,
  Loading,
  Edit,
  Delete
} from '@element-plus/icons-vue'

import {ElMessage, ElMessageBox} from "element-plus"

import {
  addPost,
  like,
  listMinePosts,
  protonPostByPostId,
  deletePostByPostId,
  updatePostByPostId,
  getPostByPostId
} from "@/api/postApi.ts"

import Comment from "@/components/Comment.vue"

interface PostItem {
  id: number
  avatar: string
  userId: string
  username: string
  level: number
  content: string
  createTime: string
  protonCount: number
  commentCount: number
  likeCount: number
  isLike: number

  /* 新增 */
  status: number

  expanded?: boolean
  showComment?: boolean
  likeAnimating?: boolean
}

/* 悬浮提示 */
const showUserTip = ref(false)
const tipX = ref(0)
const tipY = ref(0)

const handleMouseMove = (e: MouseEvent) => {
  tipX.value = e.pageX
  tipY.value = e.pageY
}

/* 跳转主页 */
const goToUserHome = (userId: string) => {
  if (!userId) return

  let userUrl = `/user/home/${userId}`

  if (userId === localStorage.getItem("userId")) {
    userUrl = `/user/`
  }

  window.open(userUrl, '_blank')
}

/* 发布 */
const content = ref('')

const loading = ref(false)
const finished = ref(false)
const publishLoading = ref(false)

const supportDialogVisible = ref(false)
const supportLoading = ref(false)
const supportProtons = ref(1)
const currentSupportPost = ref<PostItem | null>(null)

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editContent = ref('')
const currentEditPost = ref<PostItem | null>(null)

const scrollContainer = ref<HTMLElement>()
const postList = ref<PostItem[]>([])

/* 发布文章 */
const publishPost = async () => {

  if (!content.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  try {

    publishLoading.value = true

    const res = await addPost({
      content: content.value
    })

    finished.value = false

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg)

    content.value = ''

    postList.value = []

    await loadPostList()

  } finally {
    publishLoading.value = false
  }
}

/* 查询文章 */
const loadPostList = async () => {

  if (loading.value || finished.value) return

  try {

    loading.value = true

    let cursor: number | null = null

    if (postList.value.length > 0) {
      const lastPost = postList.value[postList.value.length - 1]
      if (lastPost) cursor = lastPost.id
    }

    const res = await listMinePosts({cursor})

    const records = res.data.data || []

    if (records.length === 0) {
      finished.value = true
      return
    }

    const newList = records.map((item: PostItem) => ({
      ...item,
      expanded: false,
      showComment: false,
      likeAnimating: false
    }))

    postList.value.push(...newList)

  } finally {
    loading.value = false
  }
}

/* 打开支持弹窗 */
const openSupportDialog = (item: PostItem) => {

  if (!isLogin.value) {
    ElMessage.warning("请先登录")
    return
  }

  if (item.userId == localStorage.getItem("userId")) {
    ElMessage.warning("不能支持自己的文章")
    return
  }

  currentSupportPost.value = item
  supportProtons.value = 1
  supportDialogVisible.value = true
}

/* 关闭支持弹窗 */
const closeSupportDialog = () => {
  supportDialogVisible.value = false
}

/* 提交支持 */
const submitSupport = async () => {

  if (!currentSupportPost.value) return

  if (!supportProtons.value || supportProtons.value <= 0) {
    ElMessage.warning("请输入大于 0 的数字")
    return
  }

  try {

    supportLoading.value = true

    const res = await protonPostByPostId(
        currentSupportPost.value.id,
        {
          protons: supportProtons.value
        }
    )

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    currentSupportPost.value.protonCount += supportProtons.value

    ElMessage.success(res.data.msg || "支持成功")

    supportDialogVisible.value = false

  } catch (e) {

    console.error(e)

    ElMessage.error("支持失败")

  } finally {
    supportLoading.value = false
  }
}

/* 点赞 */
const likePost = async (item: PostItem) => {

  if (!isLogin.value) {
    ElMessage.warning("请先登录")
    return
  }

  if (item.isLike === 1) {
    item.isLike = 0
    item.likeCount--
  } else {
    item.isLike = 1
    item.likeCount++
  }

  item.likeAnimating = true

  setTimeout(() => {
    item.likeAnimating = false
  }, 300)

  try {

    const res = await like(item.id)

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg)

  } catch (e) {

    if (item.isLike === 1) {
      item.isLike = 0
      item.likeCount--
    } else {
      item.isLike = 1
      item.likeCount++
    }

    console.error(e)

    ElMessage.error("操作失败")
  }
}

/* 打开修改弹窗 */
const openEditDialog = async (item: PostItem) => {

  try {

    const res = await getPostByPostId(item.id)

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    currentEditPost.value = item

    editContent.value = res.data.data.content || ''

    editDialogVisible.value = true

  } catch (e) {

    console.error(e)

    ElMessage.error("获取文章失败")
  }
}

/* 关闭修改弹窗 */
const closeEditDialog = () => {
  editDialogVisible.value = false
}

/* 提交修改 */
const submitEditPost = async () => {

  if (!currentEditPost.value) return

  if (!editContent.value.trim()) {
    ElMessage.warning("请输入内容")
    return
  }

  try {

    editLoading.value = true

    const res = await updatePostByPostId(
        currentEditPost.value.id,
        {
          content: editContent.value
        }
    )

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    currentEditPost.value.content = editContent.value

    ElMessage.success(res.data.msg || "修改成功")

    editDialogVisible.value = false

  } catch (e) {

    console.error(e)

    ElMessage.error("修改失败")

  } finally {
    editLoading.value = false
  }
}

/* 删除文章 */
const deletePost = async (item: PostItem) => {

  try {

    await ElMessageBox.confirm(
        '确定删除这篇文章吗？',
        '删除提示',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const res = await deletePostByPostId(item.id)

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    postList.value = postList.value.filter(post => post.id !== item.id)

    ElMessage.success(res.data.msg || "删除成功")

  } catch (e: any) {

    if (e !== 'cancel') {
      console.error(e)
      ElMessage.error("删除失败")
    }
  }
}

/* 评论展开 */
const toggleComment = (item: PostItem) => {
  item.showComment = !item.showComment
}

/* 时间格式化 */
const formatTime = (time: string) => {

  const createTime = new Date(time).getTime()

  const now = Date.now()

  const diff = now - createTime

  const day = 1000 * 60 * 60 * 24

  const days = Math.floor(diff / day)

  if (days < 1) return '发布于今天'

  if (days <= 3) return `发布于 ${days} 天前`

  const date = new Date(time)

  const year = date.getFullYear()

  const month = String(date.getMonth() + 1).padStart(2, '0')

  const dayStr = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${dayStr}`
}

/* 滚动加载 */
const handleScroll = () => {

  if (!scrollContainer.value) return

  const el = scrollContainer.value

  const distance =
      el.scrollHeight -
      el.scrollTop -
      el.clientHeight

  if (distance < 300) {
    loadPostList()
  }
}

/* 全局滚轮 */
const handleGlobalWheel = (e: WheelEvent) => {
  // mobile端不接管全局滚动
  if (window.innerWidth <= 768) return

  const target = e.target as HTMLElement
  if (target.closest('.message-scroll')) return
  if (!scrollContainer.value) return
  e.preventDefault()
  scrollContainer.value.scrollTop += e.deltaY
}

const isLogin = ref(
    !!localStorage.getItem("satoken")
)

onMounted(() => {

  loadPostList()

  window.addEventListener(
      'wheel',
      handleGlobalWheel,
      {passive: false}
  )
})

onUnmounted(() => {

  window.removeEventListener(
      'wheel',
      handleGlobalWheel
  )
})
</script>

<style scoped>
.forum-page {
  width: 100%;
  height: 100vh;
  overflow-y: auto;
  padding: 30px 24px 24px 24px;
  box-sizing: border-box;
}

.publish-card {
  background: rgba(224, 234, 244, 0.71);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 20px;
}

.publish-footer {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.post-card {
  background: white;
  border-radius: 20px;
  padding: 22px;
  margin-bottom: 40px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 新增 */
.post-status {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 600;
}

.post-status.reviewing {
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.12);
}

.post-status.approved {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.12);
}

.post-status.rejected {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.12);
}

.time {
  font-size: 13px;
  color: #909399;
  margin-left: auto;
}

.post-content {
  margin-top: 20px;
}

.content-text {
  font-size: 15px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
}

.collapsed {
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
}

.expand-btn {
  margin-top: 10px;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}

.post-actions {
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
  color: #606266;
  transition: 0.2s;
}

.action-item:hover {
  color: #409eff;
}

.action-item-color {
  color: #409eff;
}

.support-item:hover {
  color: #e6a23c;
}

.like-item.liked {
  color: #409eff;
  font-weight: 600;
}

.edit-item:hover {
  color: #67c23a;
}

.delete-item:hover {
  color: #f56c6c;
}

.like-icon {
  transition: transform 0.2s;
}

.bounce {
  animation: bounce-animation 0.3s;
}

@keyframes bounce-animation {

  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.35);
  }

  100% {
    transform: scale(1);
  }
}

.comment-wrapper {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #f2f3f5;
}

.mock-comment {
  background: #f5f7fb;
  border-radius: 12px;
  padding: 16px;
  color: #909399;
}

.support-dialog-content,
.edit-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.support-tip {
  font-size: 14px;
  color: #606266;
}

.support-input {
  width: 100%;
}

.loading,
.finished {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 8px;
  margin-bottom: 60px;
}

.hide-scrollbar {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

.user-tip-pop {
  position: fixed;
  background: #303133;
  color: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 9999;
}

@media screen and (max-width: 768px) {

  .forum-page {
    height: auto;
    min-height: 100vh;
    padding: 16px 12px;
  }

  .publish-card {
    padding: 14px;
    border-radius: 16px;
    margin-bottom: 16px;
    margin-top: 35px;
  }

  .post-card {
    padding: 16px;
    border-radius: 16px;
    margin-bottom: 18px;
  }

  .post-header {
    align-items: flex-start;
    gap: 10px;
  }

  .post-status {
    font-size: 12px;
    padding: 3px 8px;
  }

  .time {
    font-size: 12px;
  }

  .post-content {
    margin-top: 14px;
  }

  .content-text {
    font-size: 14px;
    line-height: 1.7;
  }

  .expand-btn {
    font-size: 13px;
  }

  .post-actions {
    margin-top: 16px;
    gap: 10px;
    flex-wrap: wrap;
  }

  .action-item {
    font-size: 13px;
    gap: 6px;
  }

  .comment-wrapper {
    margin-top: 14px;
    padding-top: 14px;
  }

  .mock-comment {
    padding: 12px;
    border-radius: 10px;
  }

  .support-dialog-content,
  .edit-dialog-content {
    gap: 14px;
  }

  .support-tip {
    font-size: 13px;
  }

  .user-tip-pop {
    display: none;
  }
}
</style>