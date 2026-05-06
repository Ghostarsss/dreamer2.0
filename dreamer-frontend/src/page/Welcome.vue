<template>
  <div class="page">
    <!-- 顶部欢迎区 -->
    <section class="hero">
      <div class="hero-left fade-in">
        <h1 class="title">欢迎来到 dreamer 大家庭！</h1>
        <p class="sub">{{ subSlogan }}</p>
      </div>

      <div class="top-card">
        <div class="slogan-title">每日 slogan</div>
        <div class="slogan-text">{{ slogan }}</div>

        <el-button type="primary" class="sign-btn" @click="handleSign">
          签到
        </el-button>
      </div>
    </section>

    <!-- cosmic icons -->
    <div class="icon-row fade-in">
      <span class="icon">
        {{ randomIcon }}
      </span>
    </div>
    <!-- 介绍卡片 -->
    <section class="cards fade-in">
      <h2 class="section-title">功能介绍</h2>

      <el-row :gutter="20" justify="center">
        <el-col :xs="24" :sm="12" :md="10">
          <el-card class="card">
            <h3>🌳 树洞功能</h3>
            <p>
              你可以在这里匿名倾诉情绪，把不想说出口的想法写进树洞，
              它会安静地倾听你的一切。
            </p>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="10">
          <el-card class="card">
            <h3>📮 未来信箱</h3>
            <p>
              写一封信给未来的自己，设定时间后自动开启，
              让时间替你保存期待与成长。
            </p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 升级内容 -->
    <section class="upgrade">
      <h2 class="section-title">Dreamer2.0 升级内容</h2>

      <el-card class="upgrade-card">
        <ul>
          <li>🚀 微服务架构重构，系统更稳定</li>
          <li>⚡ 性能优化，响应速度更快</li>
          <li>🎨 UI 全面升级，更现代化设计</li>
          <li>🔐 权限系统增强，更安全可靠</li>
        </ul>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue'


// slogan
const slogans = [
  '今天也要保持好奇心 ✨',
  '宇宙在等你发光 🌌',
  '每一步都在靠近未来 🚀',
  '你比想象中更强大 💡',
  '梦境正在生成中...'
]

const subSlogans = [
  '在每一次沉默与表达之间，总有一些难以言说的情绪正在被世界悄然接住，它们不会被打断，也不会被评判，而是在这里慢慢沉淀成可以被理解的回声。',
  '有些心事并不需要得到答案，它们只需要一个可以安全停留的空间，在那里被时间轻轻抚平，然后慢慢变成可以再次回望的安静片段。',
  '当你无法向世界完整解释自己的时候，这里允许你只是存在，不需要证明价值，也不需要迎合任何目光，只需要安静地成为你自己。',
  '那些没有说出口的话语从不会真正消失，它们只是换了一种方式存在，在时间的缝隙中慢慢发酵，最终成为理解自己的另一种路径。',
  '当世界的声音过于喧闹时，可以把自己放进这里的安静角落，让思绪重新排列，让呼吸重新变得清晰，让内心重新找到节奏与方向。',
  '每一个看似微不足道的瞬间，其实都藏着情绪的波动与心境的变化，而这里的意义，就是允许这些细微的变化被完整地保留下来。',
  '有时候人并不需要被解决问题，而只是需要被理解，这里所存在的意义，就是让那些无法表达的部分也能被温柔地看见。',
  '你所经历的一切，无论是明亮还是低落，都不会被轻易抹去，它们会在这里留下痕迹，并在未来某一刻重新与你相遇。',
  '世界不会总是给出回应，但你可以在这里为自己留下些什么，让未来的自己在某一刻读到现在的心境，并轻轻点头。',
  '在不断流动的时间里，这里像一个缓冲地带，让情绪不必急于消散，也不必急于解释，只需要被允许自然地存在。'
]

const slogan = ref('')
const subSlogan = ref('')

const icons = [
  '🌌', '✨', '🚀', '🌙', '⭐', '🪐', '☄️', '🌟', '🔭'
]

const randomIcon = ref<string>('')

onMounted(() => {
  const randomIndex = Math.floor(Math.random() * slogans.length)
  slogan.value = slogans[randomIndex] ?? '今天也要保持好奇心 ✨'

  const subIndex = Math.floor(Math.random() * subSlogans.length)
  subSlogan.value = subSlogans[subIndex] ?? ''

  const iconIndex = Math.floor(Math.random() * icons.length)
  randomIcon.value = icons[iconIndex] ?? '🌌'
})

// 签到
const handleSign = () => {
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f6f8ff, #ffffff);
  padding: 40px 20px;
  box-sizing: border-box;
}

/* hero */
.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1100px;
  margin: 60px auto 80px auto;
  padding: 20px 10px;
  background: transparent;
}

.title {
  font-size: 80px;
  font-weight: 800;
  margin: 0;
  line-height: 1.2;
}

.sub {
  margin-top: 60px;
  color: #666;
}

/* section */
.section-title {
  text-align: center;
  font-size: 24px;
  margin: 40px 0 20px;
}

.cards {
  max-width: 1100px;
  margin: 0 auto;
}

.card {
  height: 180px;
  transition: 0.3s;
}

.card:hover {
  transform: translateY(-5px);
}

/* upgrade */
.upgrade {
  max-width: 1100px;
  margin: 120px auto 20px;
}

.upgrade-card {
  padding: 20px;
}

.upgrade-card ul {
  line-height: 2;
  padding-left: 20px;
}

/* fade-in animation */
.fade-in {
  animation: fadeIn 1.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* right-top floating card (fixed) */
.top-card {
  position: fixed;
  top: 150px;
  right: 60px;
  width: 150px;
  padding: 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  gap: 15px;
  z-index: 999;
}

.slogan-title {
  font-size: 12px;
  color: #888;
}

.slogan-text {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.proton {
  font-size: 13px;
  color: #409eff;
  font-weight: 600;
}

/* button */
.sign-btn {
  width: 120px;
  height: 44px;
}

.icon-row {
  max-width: 1100px;
  margin: 20px auto 10px auto;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 64px;
  opacity: 0.9;
}

.icon {
  display: inline-block;
  transition: transform 0.3s ease;
}

.icon:hover {
  transform: scale(1.3) rotate(10deg);
}
</style>