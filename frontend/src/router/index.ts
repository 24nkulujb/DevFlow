import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/projects' },
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
    {
      path: '/projects',
      name: 'projects',
      component: () => import('../views/ProjectsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/projects/:id',
      name: 'project-detail',
      component: () => import('../views/ProjectDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})
router.beforeEach(async (to) => {
  if (!to.meta.requiresAuth && to.name !== 'login') return true
  const auth = useAuthStore()
  try {
    const user = await auth.refresh()
    if (to.meta.requiresAuth && !user) return { name: 'login' }
    if (to.name === 'login' && user) return { name: 'projects' }
    return true
  } catch {
    auth.user = null
    return to.meta.requiresAuth ? { name: 'login', query: { reason: 'unavailable' } } : true
  }
})
export default router
