<template>
  <div class="statistics-page">
    <!-- 顶部 -->
    <div class="page-header">
      <div>
        <div class="title">数据统计</div>
        <div class="sub-title">DATA STATISTICS</div>
      </div>

      <el-button
          round
          @click="loadStatistics"
      >
        <el-icon>
          <Refresh />
        </el-icon>
        刷新数据
      </el-button>
    </div>

    <!-- 数据卡片 -->
    <div
        v-loading="loading"
        class="statistics-grid"
    >
      <div class="statistics-card">
        <div class="card-icon user-icon">
          <el-icon>
            <User />
          </el-icon>
        </div>

        <div class="card-info">
          <div class="card-title">总用户数</div>
          <div class="card-value">
            {{ statistics.userCount || 0 }}
          </div>
        </div>
      </div>

      <div class="statistics-card">
        <div class="card-icon post-icon">
          <el-icon>
            <Document />
          </el-icon>
        </div>

        <div class="card-info">
          <div class="card-title">总文章数</div>
          <div class="card-value">
            {{ statistics.postCount || 0 }}
          </div>
        </div>
      </div>

      <div class="statistics-card">
        <div class="card-icon comment-icon">
          <el-icon>
            <ChatDotRound />
          </el-icon>
        </div>

        <div class="card-info">
          <div class="card-title">总评论数</div>
          <div class="card-value">
            {{ statistics.commentCount || 0 }}
          </div>
        </div>
      </div>

      <div class="statistics-card">
        <div class="card-icon letter-icon">
          <el-icon>
            <Message />
          </el-icon>
        </div>

        <div class="card-info">
          <div class="card-title">总信件数（包括未开启信件）</div>
          <div class="card-value">
            {{ statistics.letterCount || 0 }}
          </div>
        </div>
      </div>

      <div class="statistics-card views-card">
        <div class="card-icon views-icon">
          <el-icon>
            <View />
          </el-icon>
        </div>

        <div class="card-info">
          <div class="card-title">网站浏览量</div>
          <div class="card-value">
            {{ statistics.totalViews || 0 }}
          </div>
        </div>
      </div>
    </div>

    <!-- 底部统计信息 -->
    <div
        v-if="statistics.createTime || statistics.updateTime"
        class="statistics-footer"
    >
      <div class="footer-time-item">
        <el-icon>
          <Clock />
        </el-icon>

        <span>
          创建时间：{{ formatTime(statistics.createTime || '') }}
        </span>
      </div>

      <div
          v-if="statistics.updateTime"
          class="footer-time-item"
      >
        <el-icon>
          <Refresh />
        </el-icon>

        <span>
          更新时间：{{ formatTime(statistics.updateTime) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  User,
  Document,
  Message,
  ChatDotRound,
  View,
  Clock
} from '@element-plus/icons-vue'
import {getStatistics} from "@/api/adminApi.ts";

interface StatisticsData {
  id: number | string
  userCount: number
  postCount: number
  letterCount: number
  commentCount: number
  totalViews: number
  createTime: string
  updateTime: string
}

const loading = ref(false)

const statistics = ref<Partial<StatisticsData>>({})

/**
 * 获取统计数据
 */
const loadStatistics = async () => {
  loading.value = true

  try {

    const res = await getStatistics();
    statistics.value = res.data.data || {}
  } catch (e) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 时间格式化
 */
const formatTime = (time: string) => {
  if (!time) return ''

  return time.replace('T', ' ')
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-page {
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
  margin-bottom: 28px;
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

/* 网格 */
.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 22px;
}

/* 卡片 */
.statistics-card {
  background: white;
  border-radius: 24px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.25s ease;
}

.statistics-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.06);
}

/* 图标 */
.card-icon {
  width: 68px;
  height: 68px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.user-icon {
  background: #eff6ff;
  color: #2563eb;
}

.post-icon {
  background: #f5f3ff;
  color: #7c3aed;
}

.letter-icon {
  background: #ecfeff;
  color: #0891b2;
}

.comment-icon {
  background: #fef3c7;
  color: #d97706;
}

.views-icon {
  background: #dcfce7;
  color: #16a34a;
}

/* 信息 */
.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #9ca3af;
}

.card-value {
  margin-top: 10px;
  font-size: 34px;
  font-weight: 700;
  color: #111827;
  line-height: 1;
}

/* 浏览量大卡片 */
.views-card {
  grid-column: span 2;
}

/* 底部 */
.statistics-footer {
  margin-top: 28px;
  background: white;
  border-radius: 20px;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  gap: 22px;
  color: #6b7280;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  flex-wrap: wrap;
}

.footer-time-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 手机适配 */
@media (max-width: 768px) {
  .statistics-page {
    padding: 14px;
  }

  .title {
    font-size: 24px;
  }

  .statistics-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .statistics-card {
    padding: 18px;
    border-radius: 20px;
  }

  .card-icon {
    width: 56px;
    height: 56px;
    font-size: 24px;
    border-radius: 16px;
  }

  .card-value {
    font-size: 28px;
  }

  .views-card {
    grid-column: span 1;
  }

  .statistics-footer {
    padding: 16px;
    border-radius: 18px;
    font-size: 13px;
    line-height: 1.7;
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }

  .footer-time-item {
    width: 100%;
    word-break: break-word;
  }
}
</style>