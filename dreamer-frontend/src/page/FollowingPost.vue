<template>
  <div class="forum-page hide-scrollbar" ref="scrollContainer" @scroll="handleScroll">

    <div class="hot-tip">关注的「作者」越多，「帖子」也越多</div>

    <!-- 文章列表 -->
    <div
        v-for="item in postList"
        :key="item.id"
        class="post-card"
    >
      <!-- 顶部 -->
      <div class="post-header">
        <div class="left">
          <div
              class="user-hover-wrap"
              @mousemove="handleMouseMove($event, item)"
              @mouseenter="showUserTip = true; currentTipUser = item"
              @mouseleave="showUserTip = false"
              @click="goToUserHome(item.userId)"
          >
            <el-avatar :size="46" :src="item.avatar"/>
            <div class="user-info">
              <div class="username">{{ item.username }}</div>
              <div class="level">Lv.{{ item.level }}</div>
            </div>
          </div>
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
        <div
            class="action-item support-item"
            @click="openSupportDialog(item)"
        >
          <el-icon>
            <Star/>
          </el-icon>
          <span>支持 {{ item.protonCount }}</span>
        </div>
        <div
            class="action-item"
            :class="{'action-item-color': item.showComment}"
            @click="toggleComment(item)"
        >
          <el-icon>
            <ChatDotRound/>
          </el-icon>
          <span>评论 {{ item.commentCount }}</span>
        </div>
        <div
            class="action-item like-item"
            :class="{ liked: item.isLike === 1 }"
            @click="likePost(item)"
        >
          <el-icon
              class="like-icon"
              :class="{ bounce: item.likeAnimating }"
          >
            <Pointer/>
          </el-icon>
          <span>点赞 {{ item.likeCount }}</span>
        </div>
      </div>

      <!-- 评论区域 -->
      <div v-if="item.showComment" class="comment-wrapper">
        <div class="mock-comment">
          <Comment :post-id="item.id"/>
        </div>
      </div>
    </div>

    <!-- 悬浮提示 - 跟随鼠标 -->
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
      没有更多了，快去关注更多的「梦想家」吧
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
          >打赏
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, onUnmounted} from 'vue'
import {Star, ChatDotRound, Pointer, Loading} from '@element-plus/icons-vue'
import {ElMessage} from "element-plus";
import {addPost, like, listFollowingPosts, listNewPosts, protonPostByPostId} from "@/api/postApi.ts";
import Comment from "@/components/Comment.vue";

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
  expanded?: boolean
  showComment?: boolean
  likeAnimating?: boolean
}

// 悬浮提示相关
const showUserTip = ref(false)
const tipX = ref(0)
const tipY = ref(0)
const currentTipUser = ref<PostItem | null>(null)

// 跟随鼠标位置
const handleMouseMove = (e: MouseEvent, item?: PostItem) => {
  tipX.value = e.pageX
  tipY.value = e.pageY
}

// 跳转用户主页 新标签页
const goToUserHome = (userId: string) => {
  if (!userId) return

  let userUrl = `/user/home/${userId}`;
  if (userId === localStorage.getItem("userId")) {
    userUrl = `/user/`;
  }
  window.open(userUrl, '_blank')
}

/* 发布内容 */
const content = ref('')
const loading = ref(false)
const finished = ref(false)
const publishLoading = ref(false)
const supportDialogVisible = ref(false)
const supportLoading = ref(false)
const supportProtons = ref(1)
const currentSupportPost = ref<PostItem | null>(null)
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
    const res = await addPost({content: content.value});
    finished.value = false
    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }
    ElMessage.success(res.data.msg)
    content.value = '';
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
    const res = await listFollowingPosts({cursor})
    const records = res.data.data || []
    if (records.length === 0) {
      finished.value = true
      return
    }
    const followingList = records.map((item: PostItem) => ({
      ...item,
      expanded: false,
      showComment: false,
      likeAnimating: false
    }))
    postList.value.push(...followingList)
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
    const res = await protonPostByPostId(currentSupportPost.value.id, {protons: supportProtons.value});
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
    const res = await like(item.id);
    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
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
  const distance = el.scrollHeight - el.scrollTop - el.clientHeight
  if (distance < 300) {
    loadPostList()
  }
}

/* 全局滚轮控制 */
const handleGlobalWheel = (e: WheelEvent) => {
  // mobile端不接管全局滚动
  if (window.innerWidth <= 768) return

  const target = e.target as HTMLElement
  if (target.closest('.message-scroll')) return
  if (!scrollContainer.value) return

  e.preventDefault()
  scrollContainer.value.scrollTop += e.deltaY
}

const isLogin = ref(!!localStorage.getItem("satoken"))

onMounted(() => {
  loadPostList()
  window.addEventListener('wheel', handleGlobalWheel, {passive: false})
})

onUnmounted(() => {
  window.removeEventListener('wheel', handleGlobalWheel)
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

.left {
  display: flex;
  align-items: center;
}

.user-hover-wrap {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-hover-wrap:hover {
  opacity: 0.85;
}

.user-info {
  margin-left: 14px;
}

.username {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.level {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.time {
  font-size: 13px;
  color: #909399;
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

.support-dialog-content {
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

/* 自定义跟随鼠标的悬浮提示气泡 */
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

.hot-tip {
  background-color: rgba(108, 182, 255, 0.09);
  border-radius: 12px;
  padding: 16px 20px;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #eee;
  margin-bottom: 20px;
  text-align: center;
}

@media screen and (max-width: 768px) {

  .forum-page {
    height: auto;
    min-height: 100vh;
    padding: 16px 12px;
  }

  .hot-tip {
    padding: 12px 14px;
    font-size: 13px;
    margin-bottom: 14px;
    margin-top: 35px;
    border-radius: 10px;
  }

  .post-card {
    padding: 16px;
    border-radius: 16px;
    margin-bottom: 18px;
  }

  .post-header {
    align-items: flex-start;
  }

  .user-info {
    margin-left: 10px;
  }

  .username {
    font-size: 14px;
  }

  .level {
    font-size: 12px;
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

  .post-actions {
    gap: 10px;
    flex-wrap: wrap;
  }

  .action-item {
    font-size: 13px;
    gap: 6px;
  }

  .support-dialog-content {
    gap: 14px;
  }

  .user-tip-pop {
    display: none;
  }
}
</style>