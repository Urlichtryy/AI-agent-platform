import { defineStore } from 'pinia'
import http from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null,
  }),
  actions: {
    async login(username, password) {
      const { data } = await http.post('/api/user/login', { username, password })
      if (data.code === 0) {
        this.token = data.data.token
        this.user = data.data.user
        localStorage.setItem('token', this.token)
        return true
      }
      return false
    },
    async register(payload) {
      const { data } = await http.post('/api/user/register', payload)
      return data.code === 0
    },
    async fetchUserInfo() {
      const { data } = await http.get('/api/user/info')
      if (data.code === 0) {
        this.user = data.data
      }
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    }
  }
})
