<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
const router = useRouter(),
  route = useRoute(),
  auth = useAuthStore()
const username = ref(''),
  password = ref(''),
  submitting = ref(false)
const error = ref(route.query.reason === 'unavailable' ? '暂时无法连接后端，请确认服务已启动' : '')
const demo = import.meta.env.VITE_DEMO !== 'false'
function selectDemo(name: string) {
  username.value = name
  password.value = 'DevFlow123!'
  error.value = ''
}
async function login() {
  if (submitting.value) return
  error.value = ''
  if (!username.value.trim() || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  submitting.value = true
  try {
    await auth.login(username.value.trim(), password.value)
    password.value = ''
    await router.replace('/projects')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    submitting.value = false
  }
}
</script>
<template>
  <main class="login-layout">
    <section class="login-story">
      <a href="/" class="logo"
        ><span class="logo-mark">↗</span>DevFlow<span class="logo-tag">WORKSPACE</span></a
      >
      <div class="login-story-content">
        <span class="badge light">为有想法的团队而造</span>
        <h1>把灵感，<br />变成下一步。</h1>
        <p>从一个想法到一次交付。<br />清晰的任务，连接默契的团队。</p>
        <div class="login-preview" aria-hidden="true">
          <div class="preview-row">
            <span class="preview-dot"></span><span>项目规划</span><small>已完成 ✓</small>
          </div>
          <div class="preview-row">
            <span class="preview-dot blue"></span><span>团队协作</span><small>进行中 ↗</small>
          </div>
          <div class="preview-row">
            <span class="preview-dot amber"></span><span>下一个好想法</span><small>待处理</small>
          </div>
        </div>
      </div>
      <p class="login-copyright">Vue · Java · MyBatis / Built for collaboration</p>
    </section>
    <section class="login-form-side">
      <form class="login-form" @submit.prevent="login">
        <span class="eyebrow">WELCOME BACK</span>
        <h2>欢迎回到工作空间</h2>
        <p class="muted">登录，继续推进你的项目。</p>
        <fieldset class="form-stack" :disabled="submitting">
          <label for="username"
            >用户名<input
              id="username"
              v-model="username"
              maxlength="50"
              autocomplete="username"
              required
              placeholder="输入用户名" /></label
          ><label for="password"
            >密码<input
              id="password"
              v-model="password"
              type="password"
              autocomplete="current-password"
              required
              placeholder="输入密码" /></label
          ><button class="button primary login-submit" :disabled="submitting">
            {{ submitting ? '正在登录…' : '进入工作空间 →' }}
          </button>
        </fieldset>
        <p v-if="error" class="notice error" role="alert">{{ error }}</p>
        <div v-if="demo" class="demo-accounts">
          <span class="eyebrow">本地演示账号</span>
          <p>选择账号后点击登录。密码均为 DevFlow123!</p>
          <div>
            <button
              v-for="name in ['alice', 'bob', 'charlie']"
              :key="name"
              class="button subtle"
              type="button"
              :disabled="submitting"
              @click="selectDemo(name)"
            >
              {{ name }}
            </button>
          </div>
          <small>Alice 管理团队项目；Bob 另有独立项目；Charlie 用于成员权限演示。</small>
        </div>
      </form>
    </section>
  </main>
</template>
