<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import http from '@/api/http'

const auth = useAuthStore()
const loading = ref(false)
const editing = ref(false)
const form = reactive({ nickname: '', email: '', phone: '' })
let snapshot = { nickname: '', email: '', phone: '' }

const load = async () => {
  loading.value = true
  await auth.fetchUserInfo()
  const u = auth.user || {}
  form.nickname = u.nickname || ''
  form.email = u.email || ''
  form.phone = u.phone || ''
  snapshot = { nickname: form.nickname, email: form.email, phone: form.phone }
  loading.value = false
}

const startEdit = () => { editing.value = true }
const cancel = () => {
  editing.value = false
  form.nickname = snapshot.nickname
  form.email = snapshot.email
  form.phone = snapshot.phone
}
const save = async () => {
  await http.put('/api/user/profile', form)
  await auth.fetchUserInfo()
  snapshot = { nickname: form.nickname, email: form.email, phone: form.phone }
  editing.value = false
}

onMounted(load)
</script>

<template>
  <div class="card" style="padding:16px;">
    <div class="card-header">
      <h3 class="section-title" style="margin:0">个人中心</h3>
      <div>
        <button v-if="!editing" class="btn" @click="startEdit">修改</button>
        <template v-else>
          <button class="btn" style="margin-right:8px" @click="save">保存</button>
          <button class="btn" style="background:#fff;color:var(--text);border-color:var(--border)" @click="cancel">取消</button>
        </template>
      </div>
    </div>
    <div class="form-grid">
      <div>
        <label>用户名</label>
        <div class="static-input">{{ auth.user?.username }}</div>
        <div class="muted" style="font-size:12px;margin-top:4px">用户名暂不支持在线修改</div>
      </div>
      <div>
        <label>昵称</label>
        <template v-if="editing">
          <input class="input" v-model="form.nickname" />
        </template>
        <template v-else>
          <div class="static-input">{{ form.nickname || '—' }}</div>
        </template>
      </div>
      <div>
        <label>邮箱</label>
        <template v-if="editing">
          <input class="input" v-model="form.email" />
        </template>
        <template v-else>
          <div class="static-input">{{ form.email || '—' }}</div>
        </template>
      </div>
      <div>
        <label>手机号</label>
        <template v-if="editing">
          <input class="input" v-model="form.phone" />
        </template>
        <template v-else>
          <div class="static-input">{{ form.phone || '—' }}</div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
label{display:block;margin:0 0 6px 2px;color:var(--muted)}
</style>
