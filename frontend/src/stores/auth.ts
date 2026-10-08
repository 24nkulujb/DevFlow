import { defineStore } from "pinia";
import { ref } from "vue";
import {
    getCurrentUser,
    loginApi,
    logoutApi,
    type CurrentUser,
} from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null)

  async function refresh() {
    user.value = await getCurrentUser()
    return user.value
  }

  async function login(username: string, password: string) {
    await loginApi(username, password)

    if (!(await refresh())) {
      throw new Error('登录状态未建立，请检查浏览器 Cookie 设置')
    }
  }

  async function logout() {
    await logoutApi()
    user.value = null
  }

  return { user, refresh, login, logout }
})
