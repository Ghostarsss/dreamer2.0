<template>
  <div class="page">
    <!-- 顶部欢迎区 -->
    <section class="hero">
      <div class="hero-left fade-in">
        <h1 class="title">欢迎来到 dreamer 大家庭！</h1>
        <p class="sub">{{ subSlogan }}</p>
      </div>

      <div class="top-card" v-if=isLogin>
        <div class="slogan-title">每日 slogan</div>
        <div class="slogan-text">{{ slogan }}</div>

        <el-button
            :type="isSigned ? 'success' : 'primary'"
            class="sign-btn"
            @click="handleSign"
            :disabled="isSigned"
        >
          {{ isSigned ? '已签到' : '签到' }}
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
      <h2 class="section-title">网站功能介绍</h2>

      <el-row :gutter="20" justify="center">
        <el-col :xs="24" :sm="12" :md="10">
          <el-card class="card">
            <h3>🌳 树洞功能</h3>
            <p>
              这里是专属你的情绪树洞，不用伪装、不必顾虑，所有没说出口的心事、无处安放的情绪，都可以放心写在这里，它会安静倾听、妥善珍藏，做你最忠实的倾听者。
            </p>
            <p>
              无需登录即可浏览全站最新、最热内容，轻松邂逅他人的故事与心声；登录即可解锁完整社交体验，点赞、评论、投币互动，自由发布专属文字动态，还能关注心仪作者、实时查看TA的更新，一站式管理自己发布的所有内容。
            </p>
            <p>
              这里没有陌生与隔阂，所有用户都是彼此的同行者，你可以把这里当成专属朋友圈，自在分享、真诚交流，在温柔的氛围里，遇见同频的人。
            </p>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="10">
          <el-card class="card">
            <h3>📮 未来信箱</h3>
            <p>写给未来的一封信，封存当下心情，预约时光回响。你可以随心寄信给自己，设定指定开启时间，让岁月替你珍藏期许，见证一路成长，如同专属时光胶囊。</p>

            <p>支持信件搭配专属配图，自由选择公开或私密模式：<br>
              · 公开信件：永久留存，所有用户均可浏览查阅，也可手动删除；<br>
              · 私密信件：仅本人可见，一旦查看便自动销毁，守护专属心事。</p>

            <p>信件到期后会准时提醒，搭配精致仪式感开信体验，治愈走心、情绪满满，用温柔仪式，遇见时光里更好的自己。</p>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 升级内容 -->
    <section class="upgrade">
      <h2 class="section-title">Dreamer2.0 升级内容</h2>

      <el-card class="upgrade-card">
        <div style="font-weight: bold">功能升级内容：</div>
        <ul>
          <li>🚀 微服务架构重构，系统更稳定</li>
          <li>⚡ 性能优化，响应速度更快</li>
          <li>🎨 UI 全面升级，更简约化设计</li>
          <li>🔐 用户系统增强，采用密码加密技术，更安全可靠</li>
        </ul>

        <div style="font-weight: bold">系统升级内容：</div>
        <ul>
          <li>🚀 微服务架构重构，系统更稳定</li>
          <li>⚡ 性能优化，响应速度更快</li>
          <li>🎨 UI 全面升级，更简约化设计</li>
          <li>🔐 用户系统增强，采用密码加密技术，更安全可靠</li>
          <li>💻 使用的新技术栈：Redis、SpringCloud、mybatis-plus、RabbitMQ、OpenFeign、Nacos、Sentinel、Seata、Sa-token</li>
        </ul>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {checkSign, sign} from "@/api/userApi.ts";
import {ElMessage} from "element-plus";


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

const isLogin = ref(false)

//是否签到
const isSigned = ref(false)

onMounted(() => {
  //判断是否登录
  if (localStorage.getItem("satoken")) {
    isLogin.value = true
    //判断签到状态
    isSign()
  }
  const randomIndex = Math.floor(Math.random() * slogans.length)
  slogan.value = slogans[randomIndex] ?? '今天也要保持好奇心 ✨'

  const subIndex = Math.floor(Math.random() * subSlogans.length)
  subSlogan.value = subSlogans[subIndex] ?? ''

  const iconIndex = Math.floor(Math.random() * icons.length)
  randomIcon.value = icons[iconIndex] ?? '🌌'
})

// 签到
const handleSign = async () => {

  const res = await sign();
  if (res.data.code === 200) {
    ElMessage.success({message: res.data.msg, duration: 1000})
    isSigned.value = true
  } else {
    ElMessage.error(res.data.msg)
  }

}

//判断是否签到
const isSign = async () => {

  const res = await checkSign();
  isSigned.value = res.data.msg === "1";
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
  height: 380px;
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

@media screen and (max-width: 768px) {

  .page {
    padding: 20px 12px;
  }

  .hero {
    flex-direction: column;
    align-items: flex-start;
    margin: 30px auto 40px auto;
    gap: 20px;
  }

  .title {
    font-size: 34px;
    line-height: 1.3;
  }

  .sub {
    margin-top: 20px;
    font-size: 14px;
  }

  .top-card {
    display: none;
  }

  .icon-row {
    font-size: 42px;
    margin: 10px auto 20px auto;
  }

  .section-title {
    font-size: 18px;
  }

  .card {
    height: auto;
  }

  .upgrade {
    margin: 60px auto 20px;
  }
}
</style>