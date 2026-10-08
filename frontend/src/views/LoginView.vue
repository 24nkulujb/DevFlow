<script setup lang="ts">
import { useAuthStore } from '../stores/auth';
import { ref } from 'vue';
import { useRoute, useRouter} from 'vue-router';

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const submitting = ref(false)
const error = ref(
    route.query.reason === 'unavailable'
        ? '暂时无法连接后端，请确认服务已启动'
        : '',
)

async function submitLogin() {
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
    } catch (err) {
        error.value = err instanceof Error ? err.message : "登陆失败"
    } finally {
        submitting.value = false
    }
}
</script>

<template>
    <main class="login-page">
        <form @submit.prevent="submitLogin">
            <h1>登陆 DevFlow</h1>

            <fieldset :disabled="submitting">
                <label for="username">用户名</label>
                <input
                    id="username"
                    v-model="username"
                    autocomplete="username"
                    required
                />

                <label for="password">密码</label>
                <input 
                    id="password"
                    v-model="password"
                    type="pasword"
                    autocomplete="current-password"
                    required
                />

                <button type="submit">
                    {{ submitting ? '登陆中...' : '登陆'}}
                </button>
            </fieldset>

            <p v-if="error" class="error" role="alert">{{error}}</p>
        </form>
    </main>
</template>

<style scoped>
.login-page {
  max-width: 420px;
  margin: 80px auto;
  padding: 24px;
}

form {
  padding: 28px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: white;
}

fieldset {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
  padding: 0;
  margin: 0;
  border: 0;
}

input {
  padding: 10px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font: inherit;
}

button {
  margin-top: 12px;
  padding: 10px;
  border: 0;
  border-radius: 6px;
  background: #4f46e5;
  color: white;
  cursor: pointer;
}

.error {
  color: #b91c1c;
}
</style>