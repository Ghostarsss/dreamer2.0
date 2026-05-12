<template>
  <div class="feedback-page">

    <!-- 顶部 -->
    <div class="top-banner">

      <div class="banner-content">

        <div class="icon-box">
          <el-icon>
            <ChatLineRound/>
          </el-icon>
        </div>

        <div class="banner-text">
          <div class="title">问题反馈中心</div>
          <div class="desc">
            遇到 Bug、卡顿、异常问题，或者有新的建议想法，
            都欢迎提交给我们。
          </div>
        </div>

      </div>

    </div>

    <!-- 内容 -->
    <div class="feedback-card">

      <!-- 类型 -->
      <div class="section">

        <div class="section-title">
          反馈类型
        </div>

        <div class="type-list">

          <div
              class="type-item"
              :class="{ active: form.type === 0 }"
              @click="form.type = 0"
          >
            <el-icon class="type-icon">
              <WarningFilled/>
            </el-icon>

            <div class="type-info">
              <div class="type-name">Bug 反馈</div>
              <div class="type-desc">
                功能异常、页面卡顿、无法操作等问题
              </div>
            </div>
          </div>

          <div
              class="type-item"
              :class="{ active: form.type === 1 }"
              @click="form.type = 1"
          >
            <el-icon class="type-icon">
              <Opportunity/>
            </el-icon>

            <div class="type-info">
              <div class="type-name">功能建议</div>
              <div class="type-desc">
                新功能、交互优化、体验提升建议
              </div>
            </div>
          </div>

        </div>

      </div>

      <!-- 内容 -->
      <div class="section">

        <div class="section-title">
          反馈内容
        </div>

        <el-input
            v-model="form.content"
            type="textarea"
            resize="none"
            :rows="5"
            maxlength="2000"
            show-word-limit
            placeholder="请详细描述您遇到的问题或建议..."
            class="feedback-input"
        />

      </div>

      <!-- 提示 -->
      <div class="tips-box">

        <div class="tips-title">
          提示
        </div>

        <div class="tips-item">
          • 描述越详细，我们处理问题会越快
        </div>

        <div class="tips-item">
          • 如果是 Bug，建议说明复现步骤
        </div>

        <div class="tips-item">
          • 您的反馈将帮助我们持续优化体验
        </div>

      </div>

      <!-- 按钮 -->
      <div class="submit-wrapper">

        <el-button
            type="primary"
            class="submit-btn"
            :loading="submitLoading"
            @click="submitFeedback"
        >
          提交反馈
        </el-button>

      </div>

    </div>

  </div>
</template>

<script setup lang="ts">
import {reactive, ref} from "vue"

import {
  ChatLineRound,
  WarningFilled,
  Opportunity
} from "@element-plus/icons-vue"

import {ElMessage} from "element-plus"

import {addFeedback} from "@/api/feedbackApi.ts";

interface FeedbackForm {
  type: number
  content: string
}

const submitLoading = ref(false)

const form = reactive<FeedbackForm>({
  type: 0,
  content: ''
})

/* 提交反馈 */
const submitFeedback = async () => {

  if (!form.content.trim()) {
    ElMessage.warning("请输入反馈内容")
    return
  }

  try {

    submitLoading.value = true

    const res = await addFeedback({
      type: form.type,
      content: form.content
    })

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg || "提交失败")
      return
    }

    ElMessage.success(
        res.data.msg || "反馈提交成功，感谢您的建议"
    )

    form.content = ''
    form.type = 0

  } catch (e) {

    console.error(e)

    ElMessage.error("反馈提交失败")
  } finally {
    submitLoading.value = false
  }
}

</script>

<style scoped>
.feedback-page {
  width: 100%;
  min-height: 100vh;
  padding: 32px;
  box-sizing: border-box;
  background: linear-gradient(
      180deg,
      #eef5ff 0%,
      #f7f9fc 35%,
      #ffffff 100%
  );
}

/* 顶部 */
.top-banner {
  max-width: 1020px;
  margin: 0 auto 28px auto;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 20px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(14px);
  border-radius: 28px;
  padding: 28px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
}

.icon-box {
  width: 72px;
  height: 72px;
  border-radius: 22px;
  background: linear-gradient(
      135deg,
      #409eff,
      #79bbff
  );
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 34px;
  flex-shrink: 0;
}

.banner-text {
  flex: 1;
}

.title {
  font-size: 30px;
  font-weight: 700;
  color: #303133;
}

.desc {
  margin-top: 10px;
  color: #606266;
  line-height: 1.8;
  font-size: 15px;
}

/* 卡片 */
.feedback-card {
  max-width: 950px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  border-radius: 30px;
  padding: 34px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.06);
}

.section + .section {
  margin-top: 34px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 18px;
}

/* 类型 */
.type-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.type-item {
  flex: 1;
  min-width: 260px;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px;
  border-radius: 22px;
  background: #f7f9fc;
  border: 2px solid transparent;
  cursor: pointer;
  transition: 0.25s;
}

.type-item:hover {
  transform: translateY(-3px);
  background: #edf4ff;
}

.type-item.active {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.08);
}

.type-icon {
  font-size: 34px;
  color: #409eff;
}

.type-info {
  flex: 1;
}

.type-name {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
}

.type-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.7;
  color: #909399;
}

/* 输入框 */
.feedback-input {
  width: 100%;
}

/* 提示 */
.tips-box {
  margin-top: 34px;
  padding: 24px;
  border-radius: 22px;
  background: linear-gradient(
      135deg,
      rgba(64, 158, 255, 0.08),
      rgba(121, 187, 255, 0.12)
  );
}

.tips-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}

.tips-item {
  line-height: 2;
  color: #606266;
  font-size: 14px;
}

/* 按钮 */
.submit-wrapper {
  margin-top: 38px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.reset-btn,
.submit-btn {
  min-width: 120px;
  height: 44px;
  border-radius: 12px;
  font-size: 15px;
}

.submit-btn {
  box-shadow: 0 8px 18px rgba(64, 158, 255, 0.28);
}

/* 响应式 */
@media (max-width: 768px) {

  .feedback-page {
    padding: 18px;
  }

  .banner-content,
  .feedback-card {
    padding: 22px;
    border-radius: 24px;
  }

  .title {
    font-size: 24px;
  }

  .type-list {
    flex-direction: column;
  }

  .submit-wrapper {
    flex-direction: column;
  }

  .reset-btn,
  .submit-btn {
    width: 100%;
  }
}
</style>