<template>
  <div class="admin-page">
    <!-- 顶部 -->
    <div class="top-bar">
      <div class="title-box">
        <div class="title">用户管理</div>
        <div class="sub-title">USER MANAGEMENT</div>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-card">
      <el-table
          v-loading="loading"
          :data="userList"
          stripe
          border
          style="height: 100%"
      >
        <el-table-column label="头像" width="90">
          <template #default="{ row }">
            <div class="avatar-wrapper" @click="openUserHome(row)">
              <el-avatar :src="row.avatar" :size="46"/>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="用户名" min-width="160"/>

        <el-table-column prop="email" label="邮箱" min-width="220"/>

        <el-table-column label="性别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 1" type="primary">
              男
            </el-tag>

            <el-tag v-else-if="row.gender === 0" type="danger">
              女
            </el-tag>

            <el-tag v-else type="info">
              未知
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="身份" width="130">
          <template #default="{ row }">
            <el-tag
                :type="
                row.role === 3
                  ? 'danger'
                  : row.role === 2
                  ? 'warning'
                  : 'success'
              "
            >
              {{
                row.role === 3
                    ? "超级管理员"
                    : row.role === 2
                        ? "管理员"
                        : "普通用户"
              }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'">
              {{ row.status === 1 ? "封禁" : "正常" }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" fixed="right" width="360">
          <template #default="{ row }">
            <div class="action-group">
              <el-button
                  size="small"
                  type="warning"
                  @click="openWarnDialog(row)"
              >
                通知
              </el-button>

              <el-button
                  v-if="currentRole == '3'"
                  size="small"
                  type="primary"
                  @click="openRoleDialog(row)"
              >
                身份
              </el-button>

              <el-button
                  v-if="currentRole == '3'"
                  size="small"
                  type="info"
                  @click="openPasswordDialog(row)"
              >
                密码
              </el-button>

              <el-button
                  size="small"
                  :type="row.status === 1 ? 'success' : 'danger'"
                  @click="toggleBan(row)"
              >
                {{ row.status === 1 ? "解封" : "封禁" }}
              </el-button>

              <el-button
                  v-if="currentRole == '3'"
                  size="small"
                  type="danger"
                  plain
                  @click="deleteUser(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-box">
      <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="handlePageChange"
      />
    </div>

    <!-- 通知弹窗 -->
    <el-dialog
        v-model="warnDialog"
        title="发送通知"
        width="420px"
    >
      <el-input
          v-model="warnContent"
          type="textarea"
          :rows="5"
          placeholder="请输入通知内容"
      />

      <template #footer>
        <el-button @click="warnDialog = false">
          取消
        </el-button>

        <el-button
            type="primary"
            :loading="warnLoading"
            @click="sendWarning"
        >
          发送
        </el-button>
      </template>
    </el-dialog>

    <!-- 身份弹窗 -->
    <el-dialog
        v-model="roleDialog"
        title="修改身份"
        width="420px"
    >
      <el-select
          v-model="roleValue"
          style="width: 100%"
      >
        <el-option :value="1" label="普通用户"/>
        <el-option :value="2" label="管理员"/>
        <el-option :value="3" label="超级管理员"/>
      </el-select>

      <template #footer>
        <el-button @click="roleDialog = false">
          取消
        </el-button>

        <el-button
            type="primary"
            :loading="roleLoading"
            @click="updateRole"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码 -->
    <el-dialog
        v-model="passwordDialog"
        title="修改密码"
        width="420px"
    >
      <el-input
          v-model="newPassword"
          show-password
          placeholder="请输入新密码"
      />

      <template #footer>
        <el-button @click="passwordDialog = false">
          取消
        </el-button>

        <el-button
            type="primary"
            :loading="passwordLoading"
            @click="updatePassword"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import {onMounted, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {banUser, listUsers, noticeUser, removeUser, updateUserPassword, updateUserRole} from "@/api/adminApi.ts";

interface User {
  id: number;
  username: string;
  avatar: string;
  email: string;
  gender: number;
  role: number;
  proton: number;
  exp: number;
  status: number;
  createTime: string;
}

const currentRole = localStorage.getItem("role")

const loading = ref(false);

const page = ref(1);
const total = ref(0);
const size = ref(5);

const userList = ref<User[]>([]);

const currentUserId = ref<number>();

// 通知
const warnDialog = ref(false);
const warnContent = ref("");
const warnLoading = ref(false);

// 身份
const roleDialog = ref(false);
const roleValue = ref(1);
const roleLoading = ref(false);

// 密码
const passwordDialog = ref(false);
const newPassword = ref("");
const passwordLoading = ref(false);

const loadUsers = async () => {
  loading.value = true;

  try {
    const res = await listUsers({page: page.value});

    const data = res.data.data;

    userList.value = data.records;
    total.value = data.total;
    size.value = data.size;
  } catch (e) {
    ElMessage.error("获取用户失败");
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (p: number) => {
  page.value = p;
  loadUsers();
};

const formatTime = (time: string) => {
  return time.replace("T", " ");
};

const deleteUser = async (row: User) => {
  try {
    await ElMessageBox.confirm(
        `确认删除用户 ${row.username} ?`,
        "删除提示",
        {
          type: "warning",
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
    );

    const res = await removeUser(row.id);
    if (res.data.code != 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg)
    await loadUsers();
  } catch (e) {
  }
};

const toggleBan = async (row: User) => {
  try {

    const res = await banUser(row.id);
    if (res.data.code != 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(
        res.data.msg
    );

    await loadUsers();
  } catch (e) {
    ElMessage.error("操作失败");
  }
};

const openWarnDialog = (row: User) => {
  currentUserId.value = row.id;
  warnContent.value = "";
  warnDialog.value = true;
};

const sendWarning = async () => {
  if (!warnContent.value.trim()) {
    ElMessage.warning("请输入通知内容");
    return;
  }

  warnLoading.value = true;

  try {

    const res = await noticeUser(currentUserId.value, {
      content: warnContent.value
    });

    if (res.data.code != 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg);

    warnDialog.value = false;
  } catch (e) {
    ElMessage.error("发送失败");
  } finally {
    warnLoading.value = false;
  }
};

const openRoleDialog = (row: User) => {
  currentUserId.value = row.id;
  roleValue.value = row.role;
  roleDialog.value = true;
};

const updateRole = async () => {
  roleLoading.value = true;

  try {

    const res = await updateUserRole(currentUserId.value, {
      role: roleValue.value
    });

    if (res.data.code != 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg);

    roleDialog.value = false;

    await loadUsers();
  } catch (e) {
    ElMessage.error("修改失败");
  } finally {
    roleLoading.value = false;
  }
};

const openPasswordDialog = (row: User) => {
  currentUserId.value = row.id;
  newPassword.value = "";
  passwordDialog.value = true;
};

const updatePassword = async () => {
  if (!newPassword.value.trim()) {
    ElMessage.warning("请输入密码");
    return;
  }

  passwordLoading.value = true;

  try {

    const res = await updateUserPassword(currentUserId.value, {
      password: newPassword.value
    });

    if (res.data.code != 200) {
      ElMessage.error(res.data.msg)
      return
    }

    ElMessage.success(res.data.msg);
    passwordDialog.value = false;
  } catch (e) {
    ElMessage.error("修改失败");
  } finally {
    passwordLoading.value = false;
  }
};

onMounted(() => {
  loadUsers();
});

const openUserHome = (row: User) => {
  window.open(`/user/home/${row.id}`, '_blank');
};
</script>

<style scoped>
.admin-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

/* 顶部 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  gap: 12px;
  flex-wrap: wrap;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #111827;
  line-height: 1;
}

.sub-title {
  color: #94a3b8;
  margin-top: 2px;
  letter-spacing: 1px;
  font-size: 12px;
}

/* 表格 */
.table-card {
  flex: 1;
  min-height: 0;
  height: 0;
  border-radius: 18px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #ebeef5;
}

:deep(.el-table) {
  background: #ffffff;
  color: #303133;
}

:deep(.el-table th.el-table__cell) {
  background: #f8fafc;
  color: #111827;
}

:deep(.el-table tr) {
  background: #ffffff;
}

:deep(.el-table td.el-table__cell) {
  background: #ffffff;
  color: #303133;
}

:deep(.el-table__inner-wrapper::before) {
  display: none;
}

:deep(.el-table--border::after),
:deep(.el-table--group::after),
:deep(.el-table::before) {
  display: none;
}

:deep(.el-table td.el-table__cell),
:deep(.el-table th.el-table__cell) {
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar) {
  width: 0;
  height: 0;
}

.action-group {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 分页 */
.pagination-box {
  flex-shrink: 0;
  margin-top: 10px;
  display: flex;
  justify-content: center;
  padding-bottom: 2px;
}

/* 弹窗 */
:deep(.el-dialog) {
  border-radius: 22px;
  background: #ffffff;
}

:deep(.el-dialog__title) {
  color: #111827;
}

:deep(.el-dialog__body) {
  color: #606266;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  background: #ffffff;
  box-shadow: none;
  color: #303133;
  border: 1px solid #dcdfe6;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  color: #303133;
}

:deep(.el-overlay) {
  background: rgba(0, 0, 0, 0.35);
}

/* 手机端 */
@media (max-width: 768px) {
  .title {
    font-size: 20px;
  }

  .top-bar {
    margin-bottom: 10px;
  }

  .pagination-box {
    margin-top: 10px;
  }

  .table-card {
    overflow-x: auto;
  }

  .action-group {
    flex-direction: column;
  }

  :deep(.el-table) {
    min-width: 1200px;
  }
}

.avatar-wrapper {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
  transition: all 0.2s ease;
}
</style>