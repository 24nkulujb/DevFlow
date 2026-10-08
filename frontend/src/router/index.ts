import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.ts' 

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path:'/',
      redirect:'/projects',
    },
    {
      path:'/login',
      name:'login',
      component: () => import("../views/LoginView.vue"),
    },
    {
      path:'/projects',
      name:'projects',
      component: () => import ("../views/ProjectsView.vue"),
      meta: { requiresAuth: true},
    },
  ],
})

router.beforeEach(async (to) => {
  if (!to.meta.requiresAuth) return true

  const auth = useAuthStore()

  try {
    const user = await auth.refresh()
    return user ? true : { name: 'login' }
  } catch {
    return {
      name: 'login',
      query: { reason: 'unavailable' },
    }
  }
})

export default router
