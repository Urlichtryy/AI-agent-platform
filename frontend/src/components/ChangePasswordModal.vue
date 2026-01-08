<script setup>
import { ref } from 'vue'
import http from '@/api/http'

const show = defineModel('show', { type: Boolean, default: false })
const oldPassword = ref('')
const newPassword = ref('')
const loading = ref(false)
const msg = ref('')

const close = () => { show.value = false; oldPassword.value=''; newPassword.value=''; msg.value='' }
const submit = async () => {
  if (!newPassword.value || newPassword.value.length < 6) { msg.value='新密码至少6位'; return }
  loading.value = true
  try {
    const { data } = await http.post('/api/user/password/change', { oldPassword: oldPassword.value, newPassword: newPassword.value })
    if (data.code === 0) { msg.value='修改成功'; setTimeout(close, 600) } else { msg.value=data.message||'修改失败' }
  } catch(e) {
    msg.value = '修改失败，检查原密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="show" class="mask" @click.self="close">
    <div class="card box">
      <h3 class="section-title">修改密码</h3>
      <div class="form-grid" style="grid-template-columns:1fr;">
        <div>
          <label>原密码</label>
          <input v-model="oldPassword" type="password" />
        </div>
        <div>
          <label>新密码</label>
          <input v-model="newPassword" type="password" />
        </div>
      </div>
      <div class="form-actions">
        <button class="btn" @click="submit" :disabled="loading">确认修改</button>
      </div>
      <div style="color:var(--muted)">{{ msg }}</div>
    </div>
  </div>
</template>

<style scoped>
.mask{position:fixed;inset:0;background:rgba(0,0,0,.4);display:flex;align-items:center;justify-content:center;z-index:100}
.box{width:420px;padding:16px}
label{display:block;margin:0 0 6px 2px;color:var(--muted)}
</style>
