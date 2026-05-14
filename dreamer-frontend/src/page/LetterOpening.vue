<template>
  <div class="space-container">
    <!-- 背景星空 -->
    <div class="stars"></div>

    <!-- 滚动宇宙背景文本 -->
    <div class="crawl" v-if="phase === 'intro'">
      <div class="crawl-inner">
        <p v-for="(line, i) in introLines" :key="i">{{ line }}</p>
      </div>
    </div>

    <!-- 信封开启仪式 -->
    <div v-if="phase === 'intro'" class="intro">
      <div class="terminal">
        <div class="scan-line"></div>
        <p class="glow">正在连接旅行者8号……</p>
        <p class="sub">正在建立虫洞连接……</p>
        <el-progress :percentage="progress" :stroke-width="10" striped animated />
      </div>
    </div>

    <!-- 打开信件阶段 -->
    <div v-if="phase === 'reading'" class="mail-stage">
      <transition name="fade" mode="out-in">
        <div class="letter-card" :key="current?.id">
          <div class="letter-header">
            <span>📡 旅行者8号档案</span>
            <span class="index">{{ currentIndex + 1 }} / {{ letters.length }}</span>
          </div>

          <div class="letter-content">
            {{ current?.content }}
          </div>

          <div v-if="current?.img" class="img-box">
            <img :src="current.img" />
          </div>

          <div class="letter-meta">
            <span>开启时间: {{ current?.openTime }}</span>
            <span>创建时间: {{ formatDate(current?.createTime) }}</span>
          </div>
        </div>
      </transition>

      <div class="actions">
        <el-button
          type="default"
          @click="prevLetter"
          :disabled="currentIndex === 0"
        >
          上一封信件
        </el-button>

        <el-button type="primary" @click="nextLetter">
          {{ isLast ? "完成接收" : "下一封信件" }}
        </el-button>
      </div>
    </div>

    <!-- 完成阶段 -->
    <div v-if="phase === 'done'" class="done">
      <div class="done-box">
        <h2>📡 连接稳定</h2>
        <p>所有信件已通过 Voyager 8 接收完毕</p>
        <el-button type="danger" size="large" @click="closeTab">
          断开连接
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { openLetters } from "@/api/FutureLetterApi";

interface Letter {
  id: number;
  userId: number;
  content: string;
  img: string;
  openTime: string;
  isOpen: number;
  isPublic: number;
  deletedAt: any;
  createTime: string;
}

const phase = ref<"intro" | "reading" | "done">("intro");
const progress = ref(0);

const letters = ref<Letter[]>([]);
const currentIndex = ref(0);

const current = computed(() => letters.value[currentIndex.value]);
const isLast = computed(() => currentIndex.value >= letters.value.length - 1);

const introLines = [
  "你好，这里是旅行者8号",
  "自NASA成立以来，我们致力于突破已知边界……",
  "2061年10月24日，旅行者8号探测器成功发射……",
  "虫洞系统已激活",
  "正在接收跨时空信件数据……",
];

const formatDate = (dateStr?: string) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
};

const fetchLetters = async () => {
  const res = await openLetters();
  if (res.data?.code === 200) {
    const list = res.data.data || [];

    // 如果没有信件，直接关闭当前页面
    if (!list.length) {
      window.close()
      return;
    }

    letters.value = list;
  }
};

const startIntro = () => {
  const timer = setInterval(() => {
    progress.value += 5;
    if (progress.value >= 100) {
      clearInterval(timer);
      phase.value = "reading";
    }
  }, 120);
};

const prevLetter = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

const nextLetter = () => {
  if (isLast.value) {
    phase.value = "done";
    return;
  }
  currentIndex.value++;
};

const closeTab = () => {
  window.close();
};

onMounted(async () => {
  await fetchLetters();
  startIntro();
});
</script>

<style scoped>
:global(body) {
  margin: 0;
  overflow: hidden;
}
.space-container {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  z-index: 999999;
  overflow: hidden;
  background: radial-gradient(circle at 30% 20%, #1b2a4a, #05070f);
  color: #d6eaff;
  font-family: "Microsoft YaHei", sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 星空 */
.stars::before,
.stars::after {
  pointer-events: none;
  content: "";
  position: absolute;
  width: 200%;
  height: 200%;
  background-image: radial-gradient(white 1px, transparent 1px);
  background-size: 50px 50px;
  animation: moveStars 60s linear infinite;
  opacity: 0.2;
}

.stars {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

@keyframes moveStars {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-500px);
  }
}

/* intro */
.intro {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.terminal {
  width: 500px;
  padding: 30px;
  border: 1px solid #3bd6ff;
  box-shadow: 0 0 30px #3bd6ff;
  background: rgba(0, 0, 0, 0.5);
  position: relative;
}

.glow {
  font-size: 18px;
  color: #3bd6ff;
  text-shadow: 0 0 10px #3bd6ff;
}

.sub {
  opacity: 0.7;
  margin-bottom: 10px;
}

.scan-line {
  position: absolute;
  width: 100%;
  height: 2px;
  background: #3bd6ff;
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% {
    top: 0;
  }
  100% {
    top: 100%;
  }
}

/* crawl */
.crawl {
  position: fixed;
  inset: 0;
  overflow: hidden;
  perspective: 400px;
}

.crawl-inner {
  position: absolute;
  bottom: -100%;
  width: 100%;
  text-align: center;
  transform: rotateX(25deg);
  animation: crawl 12s linear forwards;
}

@keyframes crawl {
  to {
    bottom: 120%;
  }
}

/* mail */
.mail-stage {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding-top: 0;
}

.letter-card {
  width: 700px;
  max-height: 75vh;
  padding: 20px;
  border: 1px solid #3bd6ff;
  background: rgba(0, 0, 0, 0.6);
  box-shadow: 0 0 20px #3bd6ff;
  overflow-y: auto;
}

.letter-card::-webkit-scrollbar {
  width: 6px;
}

.letter-card::-webkit-scrollbar-thumb {
  background: rgba(59, 214, 255, 0.6);
  border-radius: 10px;
}

.letter-card::-webkit-scrollbar-track {
  background: transparent;
}

.letter-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #3bd6ff;
}

.letter-content {
  min-height: 200px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.img-box img {
  width: 100%;
  margin-top: 10px;
  border-radius: 8px;
}

.letter-meta {
  margin-top: 10px;
  font-size: 12px;
  opacity: 0.6;
  display: flex;
  justify-content: space-between;
}

.actions {
  margin-top: 20px;
}

/* done */
.done {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.done-box {
  text-align: center;
  border: 1px solid red;
  padding: 40px;
  box-shadow: 0 0 30px red;
}

.warn {
  color: orange;
  margin-bottom: 20px;
}

/* fade */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.5s;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
:global(.el-container) {
  overflow: hidden !important;
}

.lost {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.lost-box {
  text-align: center;
  border: 1px solid #3bd6ff;
  padding: 40px;
  box-shadow: 0 0 30px #3bd6ff;
  background: rgba(0,0,0,0.6);
}

.lost .warn {
  color: #ffb347;
  margin-bottom: 20px;
}

.pulse {
  width: 80px;
  height: 80px;
  margin: 20px auto;
  border-radius: 50%;
  border: 2px solid #3bd6ff;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 0.2; }
  100% { transform: scale(0.8); opacity: 0.8; }
}

.mail-stage,
.intro,
.done,
.lost {
  position: relative;
  z-index: 2;
}

@media (max-width: 768px) {
  .letter-card {
    width: 92vw;
  }
  .letter-card {
    max-height: 70vh;
  }

  .terminal {
    width: 92vw;
    padding: 20px;
  }

  .crawl-inner {
    font-size: 14px;
    padding: 0 10px;
  }

  .letter-content {
    min-height: auto;
  }

  .actions {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }

  .actions .el-button {
    width: 90%;
  }

  .actions {
    margin-top: 20px;
  }

  .done-box,
  .lost-box {
    width: 90vw;
    padding: 20px;
  }

  .letter-meta {
    flex-direction: column;
    gap: 6px;
  }
}
</style>