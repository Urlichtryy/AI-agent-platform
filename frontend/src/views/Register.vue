<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('')
const password = ref('')
const email = ref('')
const phone = ref('')
const msg = ref('')
const err = ref('')

const submit = async () => {
  err.value = ''
  msg.value = ''
  if (!username.value || username.value.length < 3) { err.value='用户名至少3个字符'; return }
  if (!password.value || password.value.length < 6) { err.value='密码至少6位'; return }
  try {
    const ok = await auth.register({ username: username.value, password: password.value, email: email.value, phone: phone.value })
    if (ok) { msg.value = '注册成功，请登录'; setTimeout(() => router.push('/login'), 800) }
  } catch (e) {
    err.value = '注册失败，可能是用户名已存在'
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="card auth-card">
      <h2 class="title">注册</h2>
      <div class="form">
        <div class="row">
          <label>用户名</label>
          <input class="input" v-model="username" placeholder="用户名(3-50)" />
        </div>
        <div class="row">
          <label>密码</label>
          <input class="input" v-model="password" placeholder="密码(≥6)" type="password" />
        </div>
        <div class="row">
          <label>邮箱（可选）</label>
          <input class="input" v-model="email" placeholder="邮箱(可选)" />
        </div>
        <div class="row">
          <label>手机号（可选）</label>
          <input class="input" v-model="phone" placeholder="手机号(可选)" />
        </div>
        <button class="btn" @click="submit">注册</button>
        <p v-if="err" class="err">{{ err }}</p>
        <p v-if="msg" class="ok">{{ msg }}</p>
        <p class="hint">已有账号？<router-link to="/login">去登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap{min-height:100vh;display:flex;align-items:center;justify-content:center;padding:24px;background:var(--panel)}
.auth-card{width:100%;max-width:520px;padding:24px}
.title{text-align:center;margin:0 0 16px 0}
.title{font-size:22px}
.form{display:flex;flex-direction:column;gap:12px}
.row label{margin:0 0 0 2px}
.err{color:#b91c1c;margin:6px 0 0}
.ok{color:#065f46;margin:6px 0 0}
.hint{color:var(--muted);margin:0}
</style>
