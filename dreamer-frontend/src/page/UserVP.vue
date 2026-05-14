<template>
  <div class="assets-page">

    <!-- 标题 -->
    <div class="page-header">
      <div class="title">虚拟财产</div>
      <div class="sub-title">VIRTUAL ASSETS</div>
    </div>

    <!-- 卡片区域 -->
    <div class="card-grid">

      <!-- 等级卡片 -->
      <div class="asset-card level-card">
        <div class="card-top">
          <div class="icon-box level-icon">
            <Icon icon="mdi:medal" width="30" height="30"/>
          </div>

          <div class="info">
            <div class="label">「等级」</div>
            <div class="value level-value">
              LV.{{ user.level }}
            </div>
          </div>
        </div>

        <div class="exp-section">

          <div class="exp-text">
            <span>{{ currentLevelExp }}/{{ currentLevelExp + remainExp }} EXP</span>
            <span>{{ expPercent }}%</span>
          </div>

          <el-progress
              :percentage="expPercent"
              :stroke-width="16"
              :show-text="false"
              striped
              striped-flow
          />

          <div class="exp-desc">
            距离下一级还需要
            <span class="highlight">
              {{ remainExp }}
            </span>
            EXP
          </div>

        </div>
      </div>

      <!-- 质子卡片 -->
      <div class="asset-card proton-card">
        <div class="card-top">
          <div class="icon-box proton-icon">
            <Icon icon="mdi:atom-variant" width="30" height="30"/>
          </div>

          <div class="info">
            <div class="label">「质子」</div>
            <div class="value proton-value">
              {{ user.proton }} 个
            </div>
          </div>
        </div>

        <div class="proton-tip">
          可用于兑换 EXP、修改头像、打赏帖子、帖子上热门......
        </div>
      </div>

    </div>

  </div>
</template>

<script setup lang="ts">
import {ref, reactive, computed, onMounted} from "vue";
import axios from "axios";
import {ElMessage, ElMessageBox} from "element-plus";
import {Icon} from "@iconify/vue";
import {buyEXP, me, queryUserInfo} from "@/api/userApi.ts";
import {useRoute} from "vue-router";

interface UserInfo {
  id: string;
  username: string;
  avatar: string;
  proton: number;
  exp: number;
  level: number;
}

const user = reactive<UserInfo>({
  id: "",
  username: "",
  avatar: "",
  proton: 0,
  exp: 0,
  level: 1
});

const route = useRoute()
const userId = route.params.userId as string
const loading = ref(false);

const exchangeValue = ref(1);

const maxExchangeValue = computed(() => {
  return Math.max(user.proton, 0);
});

/**
 * 后端等级算法：
 * level = sqrt(exp / K) + 1
 */
const K = 100;

/**
 * 当前等级起始 EXP
 */
const currentLevelMinExp = computed(() => {
  return Math.pow(user.level - 1, 2) * K;
});

/**
 * 下一级需要 EXP
 */
const nextLevelNeedExp = computed(() => {
  return Math.pow(user.level, 2) * K;
});

/**
 * 当前等级已获得 EXP
 */
const currentLevelExp = computed(() => {
  return user.exp - currentLevelMinExp.value;
});

/**
 * 当前等级总进度
 */
const currentLevelTotalExp = computed(() => {
  return nextLevelNeedExp.value - currentLevelMinExp.value;
});

/**
 * 剩余 EXP
 */
const remainExp = computed(() => {
  return nextLevelNeedExp.value - user.exp;
});

/**
 * 百分比
 */
const expPercent = computed(() => {
  const percent =
      (currentLevelExp.value / currentLevelTotalExp.value) * 100;

  return Number(percent.toFixed(1));
});

/**
 * 获取用户信息
 */
const getUserInfo = async () => {
  try {
    const res = await queryUserInfo(userId);

    if (res.data.code === 200) {
      Object.assign(user, res.data.data);
      if (user.proton <= 0) {
        exchangeValue.value = 0;
      } else {
        exchangeValue.value = 1;
      }
    }
  } catch (e) {
    ElMessage.error("获取用户信息失败");
  }
};

/**
 * 兑换 EXP
 */
const handleExchange = async () => {

  if (exchangeValue.value <= 0 || user.proton <= 0) {
    ElMessage.warning("请输入合规的质子数");
    return;
  }

  if (exchangeValue.value > user.proton) {
    ElMessage.warning("质子不足");
    return;
  }

  try {

    await ElMessageBox.confirm(
        `确认使用 ${exchangeValue.value} 个质子兑换 ${exchangeValue.value * 10} EXP 吗？`,
        "升级确认",
        {
          type: "warning",
          confirmButtonText: "确认升级",
          cancelButtonText: "取消"
        }
    );

    loading.value = true;

    const res = await buyEXP(exchangeValue.value);
    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg);
      return
    }
    ElMessage.success(res.data.msg);

    setTimeout(() => {

      user.proton -= exchangeValue.value;
      user.exp += exchangeValue.value * 10;

      /**
       * 前端同步等级
       */
      user.level =
          Math.floor(Math.sqrt(user.exp / K)) + 1;

      loading.value = false;

    }, 800);

  } catch (e) {
    loading.value = false;
  }

};

onMounted(() => {
  getUserInfo();
});
</script>

<style scoped>
.assets-page {
  width: 100%;
  min-height: 100vh;
  overflow-y: auto;
  box-sizing: border-box;
  padding: 32px;
  background: radial-gradient(circle at top left, #f4f7ff 0%, transparent 30%),
  radial-gradient(circle at bottom right, #eef2ff 0%, transparent 35%),
  #f8fafc;
}

/* 标题 */
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

/* 卡片布局 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

/* 卡片 */
.asset-card {
  position: relative;
  overflow: hidden;
  padding: 28px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.06);
  transition: all 0.25s ease;
}

.asset-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 38px rgba(15, 23, 42, 0.1);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 18px;
}

/* 图标 */
.icon-box {
  width: 68px;
  height: 68px;
  border-radius: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 30px;
}

.level-icon {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.proton-icon {
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
}

/* 信息 */
.info {
  flex: 1;
}

.level-value {
  color: #111827;
}

.label {
  color: #64748b;
  font-size: 14px;
}

.value {
  margin-top: 6px;
  font-size: 38px;
  font-weight: 800;
  color: #111827;
}

.proton-value {
  color: #070707;
}

/* EXP */
.exp-section {
  margin-top: 26px;
}

.exp-text {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #64748b;
}

.exp-desc {
  margin-top: 12px;
  font-size: 14px;
  color: #64748b;
}

.highlight {
  color: #2563eb;
  font-weight: 700;
}

/* 提示 */
.proton-tip {
  margin-top: 40px;
  padding: 14px;
  border-radius: 16px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 14px;
}

/* element */
:deep(.el-progress-bar__outer) {
  background: #e5e7eb;
  border-radius: 999px;
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #3b82f6, #6366f1);
  border-radius: 999px;
}

:deep(.el-input-number) {
  width: 220px;
}

:deep(.el-input__wrapper) {
  background: #f8fafc;
  box-shadow: none !important;
  border: 1px solid #e2e8f0;
}

:deep(.el-input__inner) {
  color: #111827;
}

@media (max-width: 768px) {

  .assets-page {
    padding: 20px;
  }

  .title {
    font-size: 28px;
  }

  .value {
    font-size: 30px;
  }

}
</style>