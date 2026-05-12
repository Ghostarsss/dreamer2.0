<script setup lang="ts">
import {
  nextTick,
  ref
} from "vue";

import {Icon} from "@iconify/vue";

import {
  ChatDotRound,
  Delete,
} from "@element-plus/icons-vue";

import {
  ElMessage,
  ElMessageBox
} from "element-plus";

import {
  addComment, likeCommentByCommentId, removeCommentByCommentId
} from "@/api/postApi.ts";

/**
 * 关键修复
 * 必须递归引用自身
 */
defineOptions({
  name: "CommentItem"
});

const props = defineProps({
  item: {
    type: Object,
    required: true
  },

  depth: {
    type: Number,
    default: 1
  }
});

const emit = defineEmits([
  "refresh"
]);

const userId = Number(
    localStorage.getItem(
        "userId"
    )
);

const replyContent = ref("");
const showReply = ref(false);
const showChildren = ref(false);

// 鼠标跟随提示
const showUserTip = ref(false)
const tipX = ref(0)
const tipY = ref(0)

// 鼠标移动
const handleMouseMove = (e: MouseEvent) => {
  tipX.value = e.pageX
  tipY.value = e.pageY
}

// 跳转到用户主页
const goToUserHome = (userId: string | number) => {
  if (!userId) return
  let userUrl = `/user/home/${userId}`;
  if (userId === localStorage.getItem("userId")) {
    userUrl = `/user/`;
  }
  window.open(userUrl, '_blank')
}

const toggleLike = async (item: any) => {
  if (props.item.isLike === 1) {
    props.item.isLike = 0;
    props.item.likeCount--;
  } else {
    props.item.isLike = 1;
    props.item.likeCount++;
  }

  const res = await likeCommentByCommentId(item.id);
  if (res.data.code === 200) {
    ElMessage.success(res.data.msg);
  } else {
    ElMessage.error(res.data.msg);
  }
};

const replyInputRef = ref<HTMLInputElement | null>(null)

const toggleReply = () => {
  showReply.value = !showReply.value;
  nextTick(() => {
    replyInputRef.value?.focus()
  })
};

const publishReply = async () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning("请输入回复内容");
    return;
  }

  const res = await addComment({
    postId: props.item.postId,
    parentId: props.item.id,
    content: replyContent.value
  });

  if (res.data.code !== 200) {
    ElMessage.error(res.data.msg);
    return;
  }

  ElMessage.success(res.data.msg);
  replyContent.value = "";
  showReply.value = false;
  emit("refresh");
};

const deleteComment = async () => {
  await ElMessageBox.confirm(
      "确定删除该评论吗？",
      "提示",
      {
        type: "warning",
        confirmButtonText: "确定",
        cancelButtonText: "取消"
      }
  );

  const res = await removeCommentByCommentId(props.item.id);
  if (res.data.code !== 200) {
    ElMessage.error(res.data.msg);
    return;
  }

  ElMessage.success(res.data.msg);
  emit("refresh");
};

const formatTime = (time: string) => {
  const create = new Date(time).getTime();
  const now = Date.now();
  const diff = now - create;
  const day = 24 * 60 * 60 * 1000;

  if (diff < day) {
    return "今天";
  }
  if (diff <= day * 3) {
    return `${Math.floor(diff / day)} 天前`;
  }
  return time.replace("T", " ");
};
</script>

<template>

  <!-- 鼠标跟随提示 -->
  <div
      v-if="showUserTip"
      class="user-tip-pop"
      :style="{ left: tipX + 10 + 'px', top: tipY - 35 + 'px' }"
  >
    点击进入用户主页
  </div>

  <div
      class="comment-item"
      :class="{
      root: depth === 1
    }"
  >
    <!-- 头像 + 用户名 可点击 + 鼠标悬浮提示 -->
    <div
        class="user-hover-wrap"
        @mousemove="handleMouseMove($event)"
        @mouseenter="showUserTip = true"
        @mouseleave="showUserTip = false"
        @click="goToUserHome(item.userId)"
    >
      <el-avatar
          :src="item.avatar"
          :size="42"
          class="avatar"
      />
    </div>

    <div class="comment-main">
      <div class="top-row">
        <div class="user-info">
          <!-- 用户名也加入悬浮区域 -->
          <span
              class="user-hover-wrap username"
              @mousemove="handleMouseMove($event)"
              @mouseenter="showUserTip = true"
              @mouseleave="showUserTip = false"
              @click="goToUserHome(item.userId)"
          >
            {{ item.username }}
          </span>

          <el-tag
              size="small"
              type="primary"
              effect="plain"
              round
          >
            Lv.{{ item.level }}
          </el-tag>

          <span
              v-if="item.replyToUsername"
              class="reply-user"
          >
            回复 @{{ item.replyToUsername }}
          </span>
        </div>

        <span class="time">
          {{ formatTime(item.createTime) }}
        </span>
      </div>

      <div class="content">
        {{ item.content }}
      </div>

      <div class="action-row">
        <div
            class="action-btn"
            :class="{
            active: item.isLike === 1
          }"
            @click="toggleLike(item)"
        >
          <Icon icon="mdi:heart-outline"/>
          <span>{{ item.likeCount }}</span>
        </div>

        <div class="action-btn" @click="toggleReply">
          <el-icon>
            <ChatDotRound/>
          </el-icon>
          回复
        </div>

        <div
            v-if="item.userId == userId || userId == item.postUserId"
            class="action-btn delete-btn"
            @click="deleteComment"
        >
          <el-icon>
            <Delete/>
          </el-icon>
          删除
        </div>
      </div>

      <div v-if="showReply" class="reply-box">
        <el-input
            ref="replyInputRef"
            v-model="replyContent"
            type="textarea"
            :rows="2"
            resize="none"
            maxlength="500"
            show-word-limit
            placeholder="输入回复内容..."
        />
        <div class="reply-footer">
          <el-button size="small" @click="showReply = false">
            取消
          </el-button>
          <el-button type="primary" size="small" @click="publishReply">
            回复
          </el-button>
        </div>
      </div>

      <div
          v-if="item.children && item.children.length"
          class="expand-reply"
          @click="showChildren = !showChildren"
      >
        {{ showChildren ? '收起回复' : `展开其他 ${item.children.length} 条回复` }}
      </div>

      <div
          v-if="showChildren && item.children && item.children.length"
          :class="depth === 1 ? 'children-list' : 'children-flat'"
      >
        <CommentItem
            v-for="child in item.children"
            :key="child.id"
            :item="child"
            :depth="depth + 1"
            @refresh="emit('refresh')"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-item {
  display: flex;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
  transition: all 0.25s ease;
}

.comment-item.root {
  background: #fff;
  border: 1px solid #ebeef5;
}

.comment-item.root:hover {
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}

.avatar {
  flex-shrink: 0;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.username {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
}

.reply-user {
  color: #409EFF;
  font-size: 13px;
}

.time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.content {
  margin-top: 10px;
  line-height: 1.8;
  color: #303133;
  word-break: break-word;
  font-size: 14px;
}

.action-row {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #909399;
  font-size: 13px;
  transition: all 0.2s ease;
}

.action-btn:hover {
  color: #409EFF;
}

.action-btn.active {
  color: #409EFF;
}

.delete-btn:hover {
  color: #f56c6c;
}

.reply-box {
  margin-top: 14px;
  padding: 14px;
  border-radius: 12px;
  background: #f8fafc;
}

.reply-footer {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.expand-reply {
  margin-top: 12px;
  color: #409EFF;
  font-size: 13px;
  cursor: pointer;
  width: fit-content;
}

.expand-reply:hover {
  opacity: 0.8;
}

.children-list {
  margin-top: 18px;
  padding-left: 18px;
  border-left: 2px solid #ebeef5;
}

.children-flat {
  margin-top: 16px;
  padding-left: 12px;
}

/* 点击悬浮样式 */
.user-hover-wrap {
  cursor: pointer;
}
.user-hover-wrap:hover {
  opacity: 0.85;
}

/* 鼠标跟随提示气泡 */
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
</style>