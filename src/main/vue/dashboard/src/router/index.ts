import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/saf-dashboard-bff',
      redirect: '/login'
    },
    {
      path: '/home',
      name: 'Home',
      component: () => import('../components/Home.vue')
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../components/Login.vue')
    }
  ],
})

export default router
