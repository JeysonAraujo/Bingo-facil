import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/admin'
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('../components/admin/AdminDashboard.vue')
    },
    {
      path: '/admin/events/new',
      name: 'event-creator',
      component: () => import('../components/admin/EventCreator.vue')
    }
  ]
})

export default router
