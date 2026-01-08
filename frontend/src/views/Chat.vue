<script setup>
import { onMounted, ref, nextTick } from 'vue'
import http from '@/api/http'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const sessions = ref([])
const activeId = ref(null)
const messages = ref([])
const messagesEl = ref(null)
const input = ref('')
const sending = ref(false)

const fetchSessions = async () => {
  const { data } = await http.get('/api/chat/sessions')
  if (data.code === 0) sessions.value = data.data
}
const createSession = async () => {
  const title = ''
  const { data } = await http.post('/api/chat/session', { title })
  if (data.code === 0) {
    await fetchSessions()
    activeId.value = data.data.id
    await fetchMessages()
  }
}
const deleteSession = async (id) => {
  await http.delete(`/api/chat/session/${id}`)
  await fetchSessions()
  if (activeId.value === id) { activeId.value = null; messages.value = [] }
}
const fetchMessages = async () => {
  if (!activeId.value) return
  const { data } = await http.get(`/api/chat/messages/${activeId.value}`)
  if (data.code === 0) messages.value = data.data
  await nextTick()
  scrollBottom()
}
const send = async () => {
  if (!input.value || !activeId.value || sending.value) return
  const content = input.value
  const startedAt = Date.now()
  // 乐观更新：立即展示用户消息
  messages.value.push({ id: startedAt, role: 'user', content })
  input.value = ''
  await nextTick(); scrollBottom()
  sending.value = true
  try {
    // 单次发送请求提高超时（覆盖全局）
    const { data } = await http.post('/api/chat/send', { sessionId: activeId.value, content }, { timeout: 120000 })
    if (data.code === 0 && data.data && data.data.reply !== undefined) {
      messages.value.push({ id: startedAt + 1, role: 'assistant', content: String(data.data.reply) })
      await nextTick(); scrollBottom()
      await fetchMessages()
    } else {
      await fetchMessages()
    }
  } catch (e) {
    // 若因超时/网络错误未拿到回复，启动轮询直到助手消息出现或超时
    await pollUntilAssistantAppears(startedAt, 90000)
  } finally {
    sending.value = false
  }
}

const pollUntilAssistantAppears = async (sinceTs, maxWaitMs = 60000) => {
  const deadline = Date.now() + maxWaitMs
  while (Date.now() < deadline) {
    try {
      await fetchMessages()
      const found = messages.value.some(m => {
        if (m.role !== 'assistant') return false
        if (m.createdAt) {
          const t = new Date(m.createdAt).getTime()
          return t >= sinceTs
        }
        return true
      })
      if (found) return
      await new Promise(r => setTimeout(r, 2000))
    } catch (_) {
      await new Promise(r => setTimeout(r, 2000))
    }
  }
}

const scrollBottom = () => {
  const el = messagesEl.value
  if (el) el.scrollTop = el.scrollHeight
}

const renderMd = (text) => {
  const html = marked.parse(text || '', { breaks: true })
  return DOMPurify.sanitize(html)
}

onMounted(async () => {
  await fetchSessions()
  if (sessions.value.length) {
    activeId.value = sessions.value[0].id
    await fetchMessages()
  }
})
</script>

<template>
  <div class="grid" style="gap:16px; grid-template-columns:280px 1fr;">
    <div class="card" style="padding:12px;">
      <div style="display:flex;justify-content:space-between;align-items:center;">
        <h3 class="section-title" style="margin:0">会话</h3>
        <button class="btn" @click="createSession">新建</button>
      </div>
      <div style="margin-top:10px; display:flex; flex-direction:column; gap:8px;">
        <div v-for="s in sessions" :key="s.id" class="session-item" :class="{active: s.id===activeId}" @click="activeId=s.id; fetchMessages()">
          <div>{{ s.title }}</div>
          <div style="display:flex; gap:6px;">
            <button class="btn" style="padding:6px 10px" @click.stop="deleteSession(s.id)">删除</button>
          </div>
        </div>
      </div>
    </div>
    <div class="card chat-card">
      <div ref="messagesEl" class="messages">
        <div v-for="m in messages" :key="m.id" :class="['msg-bubble', m.role==='user' ? 'msg-user' : 'msg-assistant']">
          <template v-if="m.role==='assistant'">
            <div v-html="renderMd(m.content)"></div>
          </template>
          <template v-else>
            {{ m.content }}
          </template>
        </div>
      </div>
      <div class="compose">
        <input class="input" v-model="input" placeholder="说点什么..." @keyup.enter="send" :disabled="sending" />
        <button class="btn" @click="send" :disabled="sending || !activeId || !input.trim()">
          <span v-if="!sending">发送</span>
          <span v-else class="spinner"></span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid{display:grid}
.session-item{display:flex;justify-content:space-between;align-items:center;padding:10px;border:1px solid var(--border);border-radius:10px;cursor:pointer}
.session-item.active,.session-item:hover{background:rgba(255,255,255,0.04)}
.chat-card{padding:12px;display:flex;flex-direction:column;height:calc(100vh - 60px - 36px);}
.messages{flex:1; overflow:auto; display:flex; flex-direction:column; gap:10px; padding:6px;}
.compose{display:flex; gap:8px; width:100%;}
:global(.content){overflow:hidden}
</style>
