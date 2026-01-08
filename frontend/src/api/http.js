import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

const http = axios.create({
  baseURL,
  // 提高全局超时，避免 LLM 长响应被前端过早取消
  timeout: 120000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default http
