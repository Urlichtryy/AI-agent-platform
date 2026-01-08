<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const err = ref('')

const submit = async () => {
  err.value = ''
  if (!username.value || !password.value) { err.value = '请输入用户名和密码'; return }
  try {
    const ok = await auth.login(username.value, password.value)
    if (ok) router.push('/app/profile')
  } catch (e) {
    err.value = '登录失败，请检查用户名或密码'
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="card auth-card">
      <h2 class="title">登录</h2>
      <div class="form">
        <div class="row">
          <label>用户名</label>
          <input class="input" v-model="username" placeholder="输入用户名" />
        </div>
        <div class="row">
          <label>密码</label>
          <input class="input" v-model="password" placeholder="输入密码" type="password" />
        </div>
        <button class="btn" @click="submit">登录</button>
        <p v-if="err" class="err">{{ err }}</p>
        <p class="hint">没有账号？<router-link to="/register">去注册</router-link></p>
      </div>
    </div>
  </div>
  
</template>

<style scoped>
.auth-wrap{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;background:var(--panel)}
.auth-card{width:100%;max-width:480px;padding:24px}
.title{text-align:center;margin:0 0 16px 0}
.title{font-size:22px}
.form{display:flex;flex-direction:column;gap:12px}
.row label{margin:0 0 0 2px}
.err{color:#b91c1c;margin:6px 0 0}
.hint{color:var(--muted);margin:0}
</style>
