<template>
  <div class="mail-page">
    <!-- 顶部 -->
    <div class="page-header">
      <div class="title-box">
        <div class="title">时光信箱</div>
        <div class="sub-title">TIME LETTER BOX</div>
      </div>

      <el-button
          class="write-btn"
          type="primary"
          round
          @click="handleWrite"
      >
        ✨ 去写信
      </el-button>
    </div>

    <!-- 未开启提示 -->
    <div
        v-if="hasUnopened"
        class="unopened-banner"
        @click="goOpenPage"
    >
      <div class="banner-left">
        <span class="icon">📬</span>
        <div>
          <div class="banner-title">你有一封来自过去的信件</div>
          <div class="banner-desc">点击前往开启属于你的时光信封</div>
        </div>
      </div>

      <span class="banner-arrow">→</span>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="letter-list">
      <template v-if="list.length">
        <div
            v-for="item in list"
            :key="item.id"
            class="letter-card"
        >
          <!-- 信封顶部 -->
          <div class="letter-top">
            <div class="envelope-icon">
              ✉️
            </div>

            <div class="time-box">
              <div class="time-label">开启时间</div>
              <div class="time-value">
                {{ item.openTime }}
              </div>
            </div>
          </div>

          <!-- 内容 -->
          <div class="content">
            {{ getDisplayContent(item) }}

            <span
                v-if="item.content && item.content.length > CONTENT_LIMIT"
                class="expand-btn"
                @click="toggleExpand(item.id)"
            >
              {{ isExpanded(item.id) ? '收起' : '展开全文' }}
            </span>
          </div>

          <!-- 图片 -->
          <div
              v-if="item.img"
              class="img-box"
          >
            <el-image
                :src="item.img"
                fit="cover"
                :preview-src-list="[item.img]"
                preview-teleported
                class="letter-img"
            />
          </div>

          <!-- 底部 -->
          <div class="card-footer">
            <div class="create-time">
              写于 {{ formatTime(item.createTime) }}
            </div>

            <el-button
                v-if="Number(item.userId) === Number(userId) || role == '3' || role == '2' "
                type="danger"
                plain
                round
                :loading="deleteLoading === item.id"
                @click="handleDelete(item.id)"
            >
              删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty
          v-else
          description="暂无公开信件"
      />
    </div>

    <!-- 分页 -->
    <div class="pagination-box">
      <el-pagination
          background
          layout="prev, pager, next"
          :current-page="page"
          :page-size="size"
          :total="total"
          @current-change="handlePageChange"
      />
    </div>

    <!-- 写信弹窗 -->
    <LetterSubmitDialog
        v-model="writeDialog"
        @success="handleLetterSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {
  ElMessage,
  ElMessageBox
} from 'element-plus'
import {checkExistUnopenedLetter, deleteLetter, pageOpenedLetter} from "@/api/FutureLetterApi.ts";
import LetterSubmitDialog from "@/components/LetterSubmitDialog.vue";

interface LetterItem {
  id: number
  userId: number
  content: string
  img: string
  openTime: string
  isOpen: number
  isPublic: number
  deletedAt: string | null
  createTime: string
}

const role = localStorage.getItem('role')

const loading = ref(false)

const page = ref(1)
const size = ref(5)
const total = ref(0)

const list = ref<LetterItem[]>([])

const hasUnopened = ref(false)

const writeDialog = ref(false)

const deleteLoading = ref<number | null>(null)

const expandedMap = ref<Record<number, boolean>>({})

const CONTENT_LIMIT = 180

const isExpanded = (id: number) => {
  return !!expandedMap.value[id]
}

const toggleExpand = (id: number) => {
  expandedMap.value[id] = !expandedMap.value[id]
}

const getDisplayContent = (item: LetterItem) => {
  if (!item.content) return ''

  if (isExpanded(item.id)) {
    return item.content
  }

  if (item.content.length <= CONTENT_LIMIT) {
    return item.content
  }

  return item.content.slice(0, CONTENT_LIMIT) + '...'
}

const userId = localStorage.getItem('userId')

/**
 * 查询公开信件
 */
const getLetters = async () => {
  loading.value = true

  try {
    const res = await pageOpenedLetter(page.value, size.value);

    const data = res.data?.data

    list.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    ElMessage.error('获取信件失败')
  } finally {
    loading.value = false
  }
}

/**
 * 查询是否存在未开启信件
 */
const checkUnopened = async () => {
  try {

    //判断是否登录
    if (!localStorage.getItem("satoken")) {
      return
    }
    const res = await checkExistUnopenedLetter();

    const msg = Number(res.data?.msg)

    hasUnopened.value = msg == 1
  } catch (e) {
    console.error(e)
  }
}

/**
 * 删除信件
 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
        '确定删除这封信件吗？',
        '删除提示',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    deleteLoading.value = id

    const res = await deleteLetter(id);

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg)

    if (list.value.length === 1 && page.value > 1) {
      page.value--;
    }

    await getLetters()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  } finally {
    deleteLoading.value = null
  }
}

/**
 * 分页
 */
const handlePageChange = (val: number) => {
  page.value = val
  getLetters()

  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

/**
 * 去开启信件页面
 */
const goOpenPage = () => {
  window.open('/futureLetter/opening', '_blank')
}

/**
 * 去写信
 */
const handleWrite = () => {
  if (!localStorage.getItem("satoken")) {
    ElMessage.warning('请先登录')
    return
  }

  writeDialog.value = true;
}

/**
 * 发布成功
 */
const handleLetterSuccess = async () => {
  page.value = 1

  await getLetters()
  await checkUnopened()
}

/**
 * 时间格式化
 */
const formatTime = (time: string) => {
  if (!time) return ''

  return time.replace('T', ' ')
}

onMounted(() => {
  getLetters()
  checkUnopened()
})
</script>

<style scoped>
.mail-page {
  min-height: 100vh;
  padding: 24px;
  background: radial-gradient(circle at top left, rgba(99, 102, 241, 0.15), transparent 260px),
  radial-gradient(circle at top right, rgba(59, 130, 246, 0.12), transparent 260px),
  #f5f7fb;
  box-sizing: border-box;
}

/* 顶部 */
.page-header {
  position: sticky;
  top: 0;
  z-index: 20;

  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 18px 24px;
  margin-bottom: 22px;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;

  border-radius: 24px;

  backdrop-filter: blur(18px);

  background: rgba(255, 255, 255, 0.72);

  border: 1px solid rgba(255, 255, 255, 0.8);

  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.title {
  font-size: 30px;
  font-weight: 800;
  color: #111827;
}

.sub-title {
  margin-top: 6px;
  font-size: 12px;
  letter-spacing: 4px;
  color: #94a3b8;
}

.write-btn {
  height: 44px;
  padding: 0 24px;
  font-size: 15px;
}

/* 未开启信件 */
.unopened-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;

  margin-bottom: 22px;
  padding: 20px 22px;

  border-radius: 22px;

  background: linear-gradient(
      135deg,
      #6366f1,
      #8b5cf6
  );

  color: white;

  cursor: pointer;

  transition: 0.3s;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.unopened-banner:hover {
  transform: translateY(-3px);
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon {
  font-size: 38px;
}

.banner-title {
  font-size: 18px;
  font-weight: 700;
}

.banner-desc {
  margin-top: 4px;
  font-size: 13px;
  opacity: 0.9;
}

.banner-arrow {
  font-size: 28px;
  font-weight: bold;
}

/* 列表 */
.letter-list {
  display: flex;
  flex-direction: column;
  gap: 22px;
  max-width: 1050px;
  margin: 0 auto;
}

/* 卡片 */
.letter-card {
  position: relative;

  overflow: hidden;

  padding: 26px;

  border-radius: 28px;

  background: rgba(255, 255, 255, 0.78);

  backdrop-filter: blur(18px);

  border: 1px solid rgba(255, 255, 255, 0.9);

  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);

  transition: 0.3s;
}

/* 顶部 */
.letter-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.envelope-icon {

  display: flex;
  align-items: center;
  justify-content: center;


  font-size: 30px;
}

.time-box {
  text-align: right;
}

.time-label {
  font-size: 12px;
  color: #94a3b8;
}

.time-value {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

/* 内容 */
.content {
  margin-top: 24px;

  line-height: 1.9;

  color: #374151;

  font-size: 16px;

  white-space: pre-wrap;
  word-break: break-word;
  position: relative;
}

.expand-btn {
  display: block;
  margin-top: 10px;
  color: #409eff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.expand-btn:hover {
  opacity: 0.8;
}

/* 图片 */
.img-box {
  margin-top: 20px;
}

.letter-img {
  width: 20%;
  max-height: 420px;

  border-radius: 20px;

  overflow: hidden;
}

/* 底部 */
.card-footer {
  margin-top: 22px;

  display: flex;
  align-items: center;
  justify-content: space-between;
}

.create-time {
  font-size: 13px;
  color: #94a3b8;
}

/* 分页 */
.pagination-box {
  margin-top: 34px;
  max-width: 920px;
  margin-left: auto;
  margin-right: auto;

  display: flex;
  justify-content: center;
}

/* 手机 */
@media (max-width: 768px) {
  .mail-page {
    padding: 14px;
  }

  .page-header {
    padding: 16px;
    border-radius: 20px;
  }

  .title {
    font-size: 22px;
  }

  .sub-title {
    letter-spacing: 2px;
  }

  .write-btn {
    height: 38px;
    padding: 0 16px;
    font-size: 13px;
  }

  .letter-card {
    padding: 18px;
    border-radius: 22px;
  }

  .letter-top {
    align-items: flex-start;
  }
  .envelope-icon {
    width: 44px;
    height: 44px;
    font-size: 22px;
    border-radius: 14px;
  }

  .time-value {
    font-size: 15px;
  }

  .content {
    font-size: 14px;
    line-height: 1.8;
  }

  .expand-btn {
    font-size: 13px;
  }

  .card-footer {
    gap: 12px;
    align-items: flex-start;
    flex-direction: column;
  }

  .unopened-banner {
    padding: 16px;
    border-radius: 18px;
  }

  .banner-title {
    font-size: 15px;
  }

  .banner-desc {
    font-size: 12px;
  }

  .icon {
    font-size: 30px;
  }
}
</style>