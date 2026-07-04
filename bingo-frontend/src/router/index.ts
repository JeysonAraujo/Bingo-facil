import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/activate'
    },
    // Admin public auth routes
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../components/admin/AdminLogin.vue'),
      meta: { public: true }
    },
    {
      path: '/admin/forgot-password',
      name: 'admin-forgot-password',
      component: () => import('../components/admin/AdminForgotPassword.vue'),
      meta: { public: true }
    },
    // Admin protected routes
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('../components/admin/AdminDashboard.vue')
    },
    {
      path: '/admin/register',
      name: 'admin-register',
      component: () => import('../components/admin/AdminRegister.vue'),
      meta: { requiresSuperAdmin: true }
    },
    {
      path: '/admin/events/new',
      name: 'event-creator',
      component: () => import('../components/admin/EventCreator.vue')
    },
    {
      path: '/admin/events/:eventId/console',
      name: 'event-game-console',
      component: () => import('../components/admin/EventGameConsole.vue')
    },
    // Player routes (Story 2.1+)
    {
      path: '/activate',
      name: 'ticket-activation',
      component: () => import('../components/player/TicketActivation.vue'),
      meta: { public: true }
    },
    {
      path: '/game',
      name: 'game-room',
      component: () => import('../components/player/GameRoom.vue'),
      meta: { public: true }
    }
  ]
})

// Route navigation guard for admin endpoints
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // Apply guards only for routes under /admin
  if (to.path.startsWith('/admin')) {
    // If it's a public admin route (like /admin/login or /admin/forgot-password), let it pass
    if (to.meta.public) {
      if (authStore.isAuthenticated) {
        next('/admin')
      } else {
        next()
      }
      return
    }

    // Require authentication
    if (!authStore.isAuthenticated) {
      next('/admin/login')
      return
    }

    // Require SuperAdmin if route is flagged
    if (to.meta.requiresSuperAdmin && !authStore.isSuperAdmin) {
      next('/admin')
      return
    }
  }

  next()
})

export default router
