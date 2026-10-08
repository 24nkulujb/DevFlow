import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path:'/',
      redirect:'/projects',
    },
    {
      path:'/projects',
      name:'projects',
      component: () => import ("../views/ProjectsView.vue")
    },
  ],
})

export default router
