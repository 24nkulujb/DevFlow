<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { addMember, changeRole, getCandidates, removeMember } from '../api/projects'
import { useAuthStore } from '../stores/auth'
import type { Member, Role, UserOption } from '../types'
const props = defineProps<{ projectId: number; members: Member[]; role: string }>()
const emit = defineEmits<{ changed: [] }>()
const auth = useAuthStore()
const options = ref<UserOption[]>([]),
  query = ref(''),
  selected = ref(''),
  newRole = ref<Role>('MEMBER'),
  busy = ref(false),
  searching = ref(false),
  error = ref('')
async function search() {
  if (props.role !== 'ADMIN') return
  searching.value = true
  try {
    options.value = await getCandidates(props.projectId, query.value)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    searching.value = false
  }
}
async function add() {
  if (busy.value) return
  error.value = ''
  if (!selected.value) {
    error.value = '请选择要加入的用户'
    return
  }
  busy.value = true
  try {
    await addMember(props.projectId, Number(selected.value), newRole.value)
    selected.value = ''
    emit('changed')
    await search()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    busy.value = false
  }
}
async function roleChange(member: Member, event: Event) {
  const element = event.target as HTMLSelectElement
  const next = element.value as Role
  if (busy.value) {
    element.value = member.role
    return
  }
  if (member.id === auth.user?.id && !window.confirm('降为普通成员后将失去管理权限，是否继续？')) {
    element.value = member.role
    return
  }
  error.value = ''
  busy.value = true
  try {
    await changeRole(props.projectId, member.id, next)
    emit('changed')
  } catch (e) {
    error.value = (e as Error).message
    element.value = member.role
  } finally {
    busy.value = false
  }
}
async function remove(member: Member) {
  if (
    busy.value ||
    !window.confirm(
      '移除 ' + member.displayName + '？其未完成任务将取消负责人，已完成任务保留历史负责人。',
    )
  )
    return
  error.value = ''
  busy.value = true
  try {
    await removeMember(props.projectId, member.id)
    emit('changed')
    await search()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    busy.value = false
  }
}
watch(
  () => props.members,
  () => {
    if (props.role === 'ADMIN') void search()
  },
)
onMounted(search)
</script>
<template>
  <section class="panel">
    <div class="section-toolbar">
      <div>
        <h2>
          团队成员 <span class="count">{{ members.length }}</span>
        </h2>
        <p class="muted small">管理员管理项目；成员协作任务与讨论。</p>
      </div>
    </div>
    <p v-if="error" class="notice error" role="alert">{{ error }}</p>
    <form v-if="role === 'ADMIN'" class="member-invite" @submit.prevent="add">
      <div class="invite-search">
        <input
          v-model="query"
          maxlength="100"
          aria-label="搜索已有用户"
          placeholder="搜索用户名或显示名称"
        /><button type="button" class="button" :disabled="searching || busy" @click="search">
          搜索
        </button>
      </div>
      <label
        >已有用户<select v-model="selected" :disabled="busy">
          <option value="">选择一个用户</option>
          <option v-for="option in options" :key="option.id" :value="String(option.id)">
            {{ option.displayName }} · @{{ option.username }}
          </option>
        </select></label
      >
      <label
        >项目角色<select v-model="newRole" :disabled="busy">
          <option value="MEMBER">普通成员</option>
          <option value="ADMIN">管理员</option>
        </select></label
      >
      <button class="button primary" :disabled="busy || !selected">
        {{ busy ? '处理中…' : '添加成员' }}
      </button>
    </form>
    <div class="member-list">
      <article v-for="member in members" :key="member.id" class="member-row">
        <span class="avatar">{{ member.displayName.slice(0, 1) }}</span>
        <div class="member-name">
          <strong
            >{{ member.displayName }}
            <small v-if="member.id === auth.user?.id" class="muted">（你）</small></strong
          ><span class="muted small">@{{ member.username }}</span>
        </div>
        <select
          v-if="role === 'ADMIN'"
          :value="member.role"
          :aria-label="'修改 ' + member.displayName + ' 的角色'"
          :disabled="busy"
          @change="roleChange(member, $event)"
        >
          <option value="ADMIN">管理员</option>
          <option value="MEMBER">普通成员</option></select
        ><span v-else class="badge">{{ member.role === 'ADMIN' ? '管理员' : '成员' }}</span
        ><button
          v-if="role === 'ADMIN' && member.id !== auth.user?.id && member.role === 'MEMBER'"
          class="text-button danger"
          :disabled="busy"
          @click="remove(member)"
        >
          移除
        </button>
      </article>
    </div>
  </section>
</template>
