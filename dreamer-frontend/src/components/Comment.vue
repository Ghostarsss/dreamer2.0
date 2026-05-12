<script setup lang="ts">
import {
  ref,
  onMounted,
  computed
} from "vue";

import { ElMessage } from "element-plus";

import {
  addComment,
  listCommentsByPostId
} from "@/api/postApi.ts";

import CommentItem from "./CommentItem.vue";

export interface CommentType {
  id: number;
  userId: number;
  postId: number;
  postUserId: string;
  parentId: number | null;
  content: string;
  likeCount: number;
  createTime: string;
  username: string;
  avatar: string;
  role: number;
  level: number;
  isLike: number;

  replyToUsername?: string | null;

  children: CommentType[];

  showReply: boolean;
  replyContent: string;
}

const props = defineProps({
  postId: {
    type: Number,
    required: true
  }
});

const commentContent = ref("");

const publishLoading = ref(false);

const commentList = ref<CommentType[]>([]);

/**
 * 新增
 * 一级评论分页显示
 */
const visibleCount = ref(5);

const visibleComments = computed(() => {
  return commentList.value.slice(
      0,
      visibleCount.value
  );
});

const loadMore = () => {
  visibleCount.value += 5;
};

const initComment = (
    item: any
): CommentType => {

  return {
    ...item,

    children: [],

    showReply: false,

    replyContent: ""
  };
};

/**
 * 构建树
 */
const buildTree = (
    list: CommentType[]
) => {

  const map = new Map<
      number,
      CommentType
  >();

  const roots: CommentType[] = [];

  list.forEach(item => {

    map.set(item.id, item);

    item.children = [];
  });

  const findRootParent = (
      item: CommentType
  ): CommentType | null => {

    if (
        item.parentId === null
    ) {

      return item;
    }

    const parent =
        map.get(item.parentId);

    if (!parent) {

      return null;
    }

    if (
        parent.parentId === null
    ) {

      return parent;
    }

    return findRootParent(parent);
  };

  list.forEach(item => {

    if (
        item.parentId === null
    ) {

      roots.push(item);
    }
  });

  roots.forEach(root => {

    const replies =
        list.filter(
            item =>
                item.parentId !== null &&
                findRootParent(item)?.id === root.id
        );

    const result: CommentType[] = [];

    const appendReplies = (
        parentId: number
    ) => {

      const children =
          replies.filter(
              item =>
                  item.parentId === parentId
          );

      children.forEach(child => {

        result.push(child);

        appendReplies(child.id);
      });
    };

    appendReplies(root.id);

    root.children = result;
  });

  return roots;
};

const loadComments = async () => {

  try {

    const res =
        await listCommentsByPostId(
            props.postId
        );

    const data =
        res.data.data || [];

    const list =
        data.map((item: any) =>
            initComment(item)
        );

    commentList.value =
        buildTree(list);

    /**
     * 每次重新加载
     * 默认只显示 5 条一级评论
     */
    visibleCount.value = 5;

  } catch (e) {

    console.log(e);

    ElMessage.error(
        "加载评论失败"
    );
  }
};

const publishComment = async (
    content: string,
    parentId: number | null
) => {

  if (!content.trim()) {

    ElMessage.warning(
        "请输入评论内容"
    );

    return;
  }

  publishLoading.value = true;

  try {

    const res = await addComment({
      postId: props.postId,
      parentId,
      content
    });

    if (res.data.code !== 200) {

      ElMessage.error(
          res.data.msg
      );

      return;
    }

    ElMessage.success(
        res.data.msg
    );

    commentContent.value = "";

    await loadComments();

  } finally {

    publishLoading.value = false;
  }
};

onMounted(() => {
  loadComments();
});
</script>

<template>
  <div class="comment-container">

    <!-- 发布评论 -->
    <el-card
        shadow="hover"
        class="publish-card"
    >

      <el-input
          v-model="commentContent"
          type="textarea"
          :rows="2"
          maxlength="1000"
          resize="none"
          show-word-limit
          placeholder="写下你的评论..."
      />

      <div class="publish-footer">

        <el-button
            type="primary"
            round
            :loading="publishLoading"
            @click="
            publishComment(
              commentContent,
              null
            )
          "
        >
          发布评论
        </el-button>

      </div>

    </el-card>

    <!-- 评论列表 -->
    <div class="comment-list">

      <CommentItem
          v-for="item in visibleComments"
          :key="item.id"
          :item="item"
          :depth="1"
          @refresh="loadComments"
      />

      <!-- 加载更多 -->
      <div
          v-if="
          commentList.length >
          visibleCount
        "
          class="load-more"
      >

        <el-button
            round
            plain
            @click="loadMore"
        >
          加载更多评论
        </el-button>

      </div>

      <el-empty
          v-if="commentList.length === 0"
          description="暂无评论"
      />

    </div>

  </div>
</template>

<style scoped>
.comment-container {
  width: 100%;
}

.publish-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 20px;
}

.publish-footer {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 6px;
}
</style>