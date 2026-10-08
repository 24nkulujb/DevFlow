<script setup lang="ts">
import { ref } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loggingOut = ref(false)
const logoutError = ref('')

async function logout() {
  if (loggingOut.value) return

  loggingOut.value = true
  logoutError.value = ''

  try {
    await auth.logout()
    await router.replace('/login')
  } catch (err) {
    logoutError.value = err instanceof Error ? err.message : '退出失败'
  } finally {
    loggingOut.value = false
  }
}
</script>

<template>
  <header v-if="auth.user" class="user-bar">
    <span>当前用户：{{ auth.user.displayName }}</span>
    <button :disabled="loggingOut" @click="logout">
      {{ loggingOut ? '退出中…' : '退出登录' }}
    </button>
    <span v-if="logoutError" role="alert">{{ logoutError }}</span>
  </header>

  <RouterView />
</template>

<style scoped>
.user-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
}

button {
  padding: 6px 12px;
  cursor: pointer;
}
</style>