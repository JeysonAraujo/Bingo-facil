import { defineStore } from 'pinia'
import api, { type ApiResponse } from '../services/api'

const TOKEN_KEY = 'admin_session_token'
const USERNAME_KEY = 'admin_username'
const ROLE_KEY = 'admin_role'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) as string | null,
    username: localStorage.getItem(USERNAME_KEY) as string | null,
    role: localStorage.getItem(ROLE_KEY) as string | null,
    isLoading: false,
    error: null as string | null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isSuperAdmin: (state) => state.role === 'ROLE_SUPER_ADMIN'
  },

  actions: {
    async login(username: string, password: String): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<any>>('/auth/login', { username, password })
        if (response.data.success && response.data.data) {
          const { token, username: user, role } = response.data.data
          this.token = token
          this.username = user
          this.role = role

          localStorage.setItem(TOKEN_KEY, token)
          localStorage.setItem(USERNAME_KEY, user)
          localStorage.setItem(ROLE_KEY, role)
          return true
        } else {
          this.error = response.data.error?.message || 'Erro ao realizar login.'
          return false
        }
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao se conectar ao servidor.'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async signup(payload: any): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<string>>('/auth/signup', payload)
        return response.data.success
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao cadastrar administrador.'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async forgotPassword(email: string): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<string>>('/auth/forgot-password', { email })
        return response.data.success
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao solicitar código.'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async resetPassword(payload: any): Promise<boolean> {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post<ApiResponse<string>>('/auth/reset-password', payload)
        return response.data.success
      } catch (err: any) {
        console.error(err)
        this.error = err.response?.data?.error?.message || err.message || 'Erro ao redefinir senha.'
        return false
      } finally {
        this.isLoading = false
      }
    },

    async logout(): Promise<void> {
      try {
        if (this.token) {
          await api.post('/auth/logout')
        }
      } catch (err) {
        console.error('Logout error:', err)
      } finally {
        this.token = null
        this.username = null
        this.role = null
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(USERNAME_KEY)
        localStorage.removeItem(ROLE_KEY)
      }
    }
  }
})
