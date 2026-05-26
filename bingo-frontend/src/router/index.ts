import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/activate'
    },
    // Admin routes
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('../components/admin/AdminDashboard.vue')
    },
    {
      path: '/admin/events/new',
      name: 'event-creator',
      component: () => import('../components/admin/EventCreator.vue')
    },
    // Player routes (Story 2.1+)
    {
      path: '/activate',
      name: 'ticket-activation',
      component: () => import('../components/player/TicketActivation.vue')
    },
    {
      path: '/game',
      name: 'game-room',
      component: () => import('../components/player/GameRoom.vue')
    }
  ]
})

export default router
