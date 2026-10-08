<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
const router = useRouter(),
  route = useRoute(),
  auth = useAuthStore()
const working = computed(() => !!auth.user && route.meta.requiresAuth)
const loggingOut = ref(false),
  error = ref('')
async function logout() {
  if (loggingOut.value) return
  loggingOut.value = true
  error.value = ''
  try {
    await auth.logout()
    await router.replace('/login')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loggingOut.value = false
  }
}
function expired() {
  auth.user = null
  void router.replace('/login')
}
onMounted(() => window.addEventListener('auth:expired', expired))
onUnmounted(() => window.removeEventListener('auth:expired', expired))
</script>
<template>
  <div v-if="working" class="app-shell">
    <aside class="sidebar">
      <RouterLink class="logo" to="/projects"><span class="logo-mark">↗</span>DevFlow</RouterLink>
      <div class="workspace-label">团队工作空间<span class="workspace-status"></span></div>
      <span class="sidebar-caption">WORKSPACE</span>
      <nav aria-label="主导航">
        <RouterLink to="/projects" class="nav-item active"
          ><span>▦</span>项目空间<span class="nav-arrow">↗</span></RouterLink
        >
      </nav>
      <div class="sidebar-tip">
        <span class="eyebrow">ONE STEP AT A TIME</span>
        <h3>让每一步更清晰。</h3>
        <p>把大目标拆成小任务，与团队一起向前。</p>
        <span class="tip-decoration">↗</span>
      </div>
      <div class="sidebar-bottom">
        <span class="version-pill">v1.0</span><span>全栈协作作品集</span>
      </div>
    </aside>
    <div class="workspace-main">
      <header class="topbar">
        <div class="breadcrumbs">
          <RouterLink to="/projects">工作空间</RouterLink><span>/</span
          ><strong>{{ route.name === 'project-detail' ? '项目详情' : '项目空间' }}</strong>
        </div>
        <div class="topbar-user">
          <span class="online-dot"></span><span>{{ auth.user?.displayName }}</span
          ><span class="avatar">{{ auth.user?.displayName.slice(0, 1) }}</span
          ><button class="text-button" :disabled="loggingOut" @click="logout">
            {{ loggingOut ? '退出中…' : '退出' }}
          </button>
        </div>
      </header>
      <p v-if="error" class="notice error" role="alert">{{ error }}</p>
      <RouterView />
    </div>
  </div>
  <RouterView v-else />
</template>
