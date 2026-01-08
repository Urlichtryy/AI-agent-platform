<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const open = ref(false)
const showChange = ref(false)

const goto = (path) => router.push(path)
const logout = () => { auth.logout(); router.push('/login') }
</script>

<template>
  <div>
    <header class="header">
      <div class="brand">
        <span class="dot"></span>
        <span>AI 代码助手</span>
      </div>
      <div class="user">
        <div class="dropdown">
          <div class="item" style="cursor:pointer" @click="open=!open">
            {{ auth.user?.nickname || auth.user?.username }}
          </div>
          <div v-if="open" class="dropdown-menu card" @mouseleave="open=false">
            <div class="item" @click="goto('/app/profile')">个人中心</div>
            <div class="item" @click="showChange=true">修改密码</div>
            <div class="item" @click="logout">登出</div>
          </div>
        </div>
      </div>
    </header>
    <div class="layout">
      <aside class="sidebar">
        <div class="item" :class="{active: route.path.startsWith('/app/profile') }" @click="goto('/app/profile')">个人中心</div>
        <div class="item" :class="{active: route.path.startsWith('/app/agent') }" @click="goto('/app/agent')">智能体应用</div>
      </aside>
      <main class="content">
        <router-view />
      </main>
    </div>
    <ChangePasswordModal v-model:show="showChange" />
  </div>
</template>

<style scoped>
.item{color:var(--text)}
</style>
