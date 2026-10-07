<script setup lang="ts">
import { ref } from 'vue';

const message = ref("尚未连接后端")
const loading = ref(false)
const error = ref('')

async function checkBackend() {
  loading.value = true
  error.value = ''
  message.value = ''

  try {
    const response = await fetch("/api/health")

    if (!response.ok) {
      throw new Error(`请求失败，状态码：${response.status}`)
    }

    message.value = await response.text()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '连接失败'
  } finally {
    loading.value = false
  }
}

</script>

<template>
  <main class="welcome">
    <h1>DevFlow 团队协作平台</h1>
    <p>前后端联调</p>

    <button :disabled="loading" @click="checkBackend">
      {{ loading ? '连接中...' : '检查后端连接'}}
    </button>

    <p v-if="error" class="error">{{error}}</p>
    <p v-else>{{message}}</p>
  </main>
</template>

<style>
.welcome {
  padding: 40px;
}

button {
  padding: 10px 16px;
  cursor: pointer;
}

button:disabled {
  cursor: wait;
}

.error {
  color: #dc2626;
}
</style>