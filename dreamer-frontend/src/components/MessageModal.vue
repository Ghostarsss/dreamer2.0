<template>
  <!-- 消息弹窗：绑定 innerVisible，不再用自己的 visible -->
  <el-dialog
      v-model="innerVisible"
      title="消息通知"
      width="700px"
      destroy-on-close
  >
    <div class="message-scroll">
      <!-- 顶部操作 -->
      <div class="header-actions">
        <el-button
            type="danger"
            plain
            :loading="clearLoading"
            @click="clearNotifications"
        >
          清空通知
        </el-button>
      </div>

      <!-- 空状态 -->
      <el-empty
          v-if="messageList.length === 0 && !loading"
          description="暂未收到消息"
      />

      <!-- 加载 -->
      <div v-loading="loading" class="message-list">
        <div
            v-for="item in messageList"
            :key="item.id"
            class="message-item"
        >
          <!-- 左侧头像 -->
          <div class="avatar-box">
            <!-- 系统消息 -->
            <div
                v-if="isSystemMessage(item.type)"
                class="system-avatar"
            >
              <el-icon>
                <BellFilled/>
              </el-icon>
            </div>

            <div
                v-if="item.type == 1"
                class="system-avatar"
            >
              <el-icon>
                <Promotion/>
              </el-icon>
            </div>

            <!-- 用户头像 -->
            <el-avatar
                v-else-if="isSocialMessage(item.type)"
                :src="item.avatar"
                :size="48"
            >
              {{ item.username?.charAt(0) }}
            </el-avatar>
          </div>

          <!-- 右侧内容 -->
          <div class="content-box">

            <!-- 顶部 -->
            <div class="top-row">
              <span
                  v-if="isSystemMessage(item.type)"
                  class="system-title"
              >
                系统消息
              </span>

              <span
                  v-if="item.type == 1"
                  class="system-title"
              >
                公告
              </span>

              <span
                  v-if="isSocialMessage(item.type)"
                  class="social-title"
              >
                互动消息
              </span>
            </div>

            <!-- 中部内容 -->
            <div class="message-content">
              {{ item.content }}
            </div>

            <!-- 底部时间 -->
            <div class="time">
              {{ formatTime(item.createTime) }}
            </div>

          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import {ref, onMounted, computed, watch} from "vue";
import {
  BellFilled,
  Promotion
} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {clearMessage, listMessage} from "@/api/messageApi.ts";

/**
 * 消息类型
 */
interface NotificationItem {
  id: string;
  type: number;
  sendId: string;
  content: string;
  readStatus: number;
  createTime: string;
  avatar: string;
  username: string;
  level: number;
}

/**
 * 加载状态
 */
const loading = ref(false);
const clearLoading = ref(false);

/**
 * 消息列表
 */
const messageList = ref<NotificationItem[]>([]);

/**
 * 是否系统消息
 */
const isSystemMessage = (type: number) => {
  return [0, 2, 3, 8, 9, 10].includes(type);
};

const isSocialMessage = (type: number) => {
  return [4, 5, 6, 7].includes(type);
};

/**
 * 获取通知
 */
const loadNotifications = async () => {
  loading.value = true;

  try {
    // 这里替换成你的接口
    const res = await listMessage()
    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    messageList.value = res.data.data || [];

  } catch (e) {
    ElMessage.error("获取消息失败");
  } finally {
    loading.value = false;
  }
};

/**
 * 清空通知
 */
const clearNotifications = async () => {
  try {
    await ElMessageBox.confirm(
        "确定清空所有通知吗？",
        "提示",
        {
          type: "warning",
          confirmButtonText: "确定",
          cancelButtonText: "取消"
        }
    );

    clearLoading.value = true;

    // delete 请求
    await clearMessage();

    innerVisible.value = false;
    ElMessage.success("通知已清空");

  } catch (e) {

  } finally {
    clearLoading.value = false;
  }
};

/**
 * 时间格式化
 */
const formatTime = (time: string) => {
  if (!time) return "";

  return time.replace("T", " ");
};

// 接收父组件传的显示状态
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

// 向父组件发送更新事件
const emit = defineEmits(['update:visible'])

// 双向绑定给 el-dialog 使用
const innerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 监听弹窗打开 → 刷新消息
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadNotifications()
  }
})

onMounted(() => {
  // 打开时才加载，不提前请求
});
</script>

<style scoped>
/* 真正滚动的盒子，在这里隐藏滚动条 */
.message-list {
  max-height: 500px;
  overflow-y: auto;

  /* 隐藏滚动条 */
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 适配 Chrome / Edge / Safari */
.message-list::-webkit-scrollbar {
  display: none;
}

.message-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid #f2f2f2;
}

.message-item:last-child {
  border-bottom: none;
}

.avatar-box {
  flex-shrink: 0;
}

.system-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.content-box {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.top-row {
  display: flex;
  align-items: center;
}

.system-title {
  font-size: 14px;
  color: #409eff;
  font-weight: 600;
}

.social-title {
  font-size: 14px;
  color: #818080;
  font-weight: 600;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  color: #555;
  word-break: break-word;
}

.time {
  font-size: 12px;
  color: #999;
}

.header-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.message-scroll {
  /* 这里不需要滚动了，删掉多余样式 */
  max-height: unset;
  overflow: visible;
}
</style>