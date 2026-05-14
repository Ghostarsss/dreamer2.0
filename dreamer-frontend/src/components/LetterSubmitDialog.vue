<template>
  <el-dialog
      v-model="visible"
      width="680px"
      destroy-on-close
      align-center
      class="write-letter-dialog"
      :show-close="false"
  >
    <!-- 顶部 -->
    <div class="dialog-header">
      <div>
        <div class="title">写一封给未来的信</div>
        <div class="sub-title">
          WRITE A LETTER TO THE FUTURE
        </div>
      </div>

      <div class="close-btn" @click="handleClose">
        ✕
      </div>
    </div>

    <!-- 内容 -->
    <div class="dialog-body">
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
      >


        <!-- 信件内容 -->
        <el-form-item
            label="信件内容"
            prop="content"
        >
          <el-input
              v-model="form.content"
              type="textarea"
              resize="none"
              :rows="4"
              maxlength="5000"
              show-word-limit
              placeholder="写下想对未来的自己说的话..."
          />
        </el-form-item>
        <!-- 上传图片 -->
        <el-form-item label="信封图片（用户「等级」到10级，并消耗 10颗「质子」上传）">
          <el-upload
              class="upload-box"
              action=""
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="uploadImage"
              accept="image/*"
          >
            <div v-if="form.img" class="preview-box">
              <img
                  :src="form.img"
                  class="preview-img"
                  alt=""
              />

              <div class="preview-mask">
                <span>重新上传</span>
              </div>
            </div>

            <div v-else class="upload-placeholder">
              <div class="upload-icon">🖼️</div>
              <div class="upload-text">
                点击上传图片
              </div>
              <div class="upload-tip">
                支持 jpg / png / webp
              </div>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 开启时间 -->
        <el-form-item
            label="开启时间"
            prop="openTime"
        >
          <el-date-picker
              v-model="form.openTime"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择开启日期"
              style="width: 100%"
              :disabled-date="disabledDate"
          />
        </el-form-item>

        <!-- 是否公开 -->
        <el-form-item label="是否公开">
          <div class="public-select">
            <div
                class="public-card"
                :class="{ active: form.isPublic === 0 }"
                @click="form.isPublic = 0"
            >
              <div class="public-icon">
                🔒
              </div>

              <div>
                <div class="public-title">
                  私密信件
                </div>

                <div class="public-desc">
                  只有你自己可以看到，且一旦查看后，系统自动删除该信件
                </div>
              </div>
            </div>

            <div
                class="public-card"
                :class="{ active: form.isPublic === 1 }"
                @click="form.isPublic = 1"
            >
              <div class="public-icon">
                🌍
              </div>

              <div>
                <div class="public-title">
                  公开信件
                </div>

                <div class="public-desc">
                  所有人都可以看到，会永久保存，您可以手动删除
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 底部 -->
    <div class="dialog-footer">
      <el-button
          round
          @click="handleClose"
      >
        取消
      </el-button>

      <el-button
          type="primary"
          round
          :loading="submitLoading"
          @click="handleSubmit"
      >
        ✨ 投递到未来
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import {computed, reactive, ref} from 'vue'
import type {
  FormInstance,
  FormRules,
  UploadRequestOptions
} from 'element-plus'

import {
  ElMessage
} from 'element-plus'

import axios from 'axios'
import {postLetter, uploadImg} from "@/api/FutureLetterApi.ts";

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'success'
])

const formRef = ref<FormInstance>()

const submitLoading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val: boolean) => {
    emit('update:modelValue', val)
  }
})

const form = reactive({
  content: '',
  img: '',
  openTime: '',
  isPublic: 0
})

/**
 * 校验规则
 */
const rules: FormRules = {
  content: [
    {
      required: true,
      message: '请输入信件内容',
      trigger: 'blur'
    },
    {
      min: 5,
      message: '至少输入 5 个字符',
      trigger: 'blur'
    }
  ],

  openTime: [
    {
      required: true,
      message: '请选择开启时间',
      trigger: 'change'
    }
  ]
}

/**
 * 限制日期
 * 至少 7 天后
 */
const disabledDate = (time: Date) => {
  const now = Date.now()

  const sevenDays =
      7 * 24 * 60 * 60 * 1000

  return time.getTime() < now + sevenDays
}

/**
 * 上传前校验
 */
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')

  if (!isImage) {
    ElMessage.error('只能上传图片')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isLt10M) {
    ElMessage.error('图片不能超过 10MB')
    return false
  }

  return true
}

/**
 * 上传图片
 */
const uploadImage = async (
    options: UploadRequestOptions
) => {
  try {
    const file = options.file

    const formData = new FormData()

    formData.append('img', file)

    const res = await uploadImg(formData);

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg)
      return
    }

    form.img = res.data?.msg || '';

    ElMessage.success("上传成功")
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

/**
 * 提交
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitLoading.value = true

    const res = await postLetter({
      content: form.content,
      img: form.img,
      openTime: form.openTime,
      isPublic: form.isPublic
    });

    if (res.data.code !== 200) {
      ElMessage.error(res.data.msg || '发布失败')
      return
    }

    ElMessage.success(res.data.msg);

    emit('success')

    handleReset()

    visible.value = false
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 重置
 */
const handleReset = () => {
  form.content = ''
  form.img = ''
  form.openTime = ''
  form.isPublic = 1
}

/**
 * 关闭
 */
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
:deep(.write-letter-dialog) {
  border-radius: 32px;
  overflow: hidden;
  backdrop-filter: blur(20px);
}

:deep(.el-dialog) {
  border-radius: 32px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.94);
}

:deep(.el-dialog__body) {
  padding: 0;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 22px 30px 20px;

  background: linear-gradient(
      135deg,
      rgba(99, 102, 241, 0.08),
      rgba(59, 130, 246, 0.08)
  );
}

.title {
  font-size: 30px;
  font-weight: 800;
  color: #111827;
}

.sub-title {
  margin-top: 8px;

  font-size: 12px;
  letter-spacing: 4px;

  color: #94a3b8;
}

.close-btn {
  width: 42px;
  height: 42px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;

  background: #f3f4f6;

  cursor: pointer;

  transition: 0.25s;

  font-size: 18px;
}

.close-btn:hover {
  transform: rotate(90deg);
  background: #e5e7eb;
}

.dialog-body {
  padding: 26px 30px;
}

.letter-notice {
  display: flex;
  align-items: center;
  gap: 14px;

  margin-bottom: 20px;
  padding: 16px 18px;

  border-radius: 20px;

  background: linear-gradient(
      135deg,
      rgba(99, 102, 241, 0.08),
      rgba(59, 130, 246, 0.08)
  );

  border: 1px solid rgba(99, 102, 241, 0.12);
}

.notice-icon {
  width: 46px;
  height: 46px;

  flex-shrink: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 14px;

  background: white;

  font-size: 22px;
}

.notice-title {
  font-size: 15px;
  font-weight: 700;

  color: #111827;
}

.upload-notice-title {
  margin-top: 10px;
}

.notice-desc {
  margin-top: 4px;

  font-size: 13px;
  line-height: 1.7;

  color: #64748b;
}

.upload-box {
  width: 100%;
}

.upload-placeholder {
  width: 100%;
  height: 160px;

  border-radius: 26px;
  border: 2px dashed #d1d5db;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  cursor: pointer;

  transition: 0.3s;

  background: linear-gradient(
      135deg,
      rgba(99, 102, 241, 0.04),
      rgba(59, 130, 246, 0.04)
  );
}

.upload-placeholder:hover {
  border-color: #6366f1;
  transform: translateY(-2px);
}

.upload-icon {
  font-size: 46px;
}

.upload-text {
  margin-top: 12px;

  font-size: 16px;
  font-weight: 700;

  color: #374151;
}

.upload-tip {
  margin-top: 6px;

  font-size: 13px;
  color: #9ca3af;
}

.preview-box {
  position: relative;

  overflow: hidden;

  width: 100%;
  height: 220px;

  border-radius: 26px;

  cursor: pointer;
}

.preview-img {
  width: 100%;
  height: 100%;

  object-fit: cover;
}

.preview-mask {
  position: absolute;
  inset: 0;

  display: flex;
  align-items: center;
  justify-content: center;

  background: rgba(0, 0, 0, 0.45);

  opacity: 0;

  transition: 0.3s;

  color: white;

  font-size: 16px;
  font-weight: 700;
}

.preview-box:hover .preview-mask {
  opacity: 1;
}

.public-select {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.public-card {
  display: flex;
  align-items: center;
  gap: 14px;

  padding: 10px;

  border-radius: 22px;

  border: 2px solid transparent;

  background: #f8fafc;

  cursor: pointer;

  transition: 0.25s;
}

.public-card:hover {
  transform: translateY(-2px);
}

.public-card.active {
  border-color: #6366f1;

  background: rgba(99, 102, 241, 0.08);
}

.public-icon {
  width: 52px;
  height: 52px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 18px;

  background: white;

  font-size: 24px;
}

.public-title {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.public-desc {

  font-size: 12px;
  color: #94a3b8;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 14px;

  padding: 0 30px 30px;
}

:deep(.el-textarea__inner) {
  border-radius: 22px;
  padding: 16px;

  font-size: 15px;
  line-height: 1.8;

  min-height: 180px !important;
}

:deep(.el-input__wrapper) {
  min-height: 46px;
  border-radius: 16px;
}

:deep(.el-form-item__label) {
  font-size: 15px;
  font-weight: 700;

  color: #374151;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: calc(100vw - 20px) !important;
    margin: 0 auto;
  }

  .dialog-header {
    padding: 22px 20px 16px;
  }

  .dialog-body {
    padding: 20px;
  }

  .dialog-footer {
    padding: 0 20px 20px;
  }

  .title {
    font-size: 24px;
  }

  .public-select {
    grid-template-columns: 1fr;
  }

  .upload-placeholder {
    height: 140px;
  }

  .preview-box {
    height: 180px;
  }
  .letter-notice {
    align-items: flex-start;
    padding: 14px;
  }

  .notice-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
}
</style>