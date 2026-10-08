<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { createProjectApi, getProjects } from '../api/projects'
import ModalShell from '../components/ModalShell.vue'
import { useAuthStore } from '../stores/auth'
import type { Project } from '../types'
const auth = useAuthStore()
const projects = ref<Project[]>([]),
  loading = ref(true),
  error = ref(''),
  search = ref(''),
  showCreate = ref(false)
const name = ref(''),
  description = ref(''),
  saving = ref(false),
  formError = ref(''),
  notice = ref('')
const filtered = computed(() =>
  projects.value.filter((p) =>
    (p.name + ' ' + p.description).toLowerCase().includes(search.value.toLowerCase()),
  ),
)
const totals = computed(() => ({
  tasks: projects.value.reduce((n, p) => n + p.taskCount, 0),
  done: projects.value.reduce((n, p) => n + p.doneCount, 0),
}))
let loadVersion = 0
async function load() {
  const version = ++loadVersion
  loading.value = true
  error.value = ''
  try {
    const data = await getProjects()
    if (version === loadVersion) projects.value = data
  } catch (e) {
    if (version === loadVersion) error.value = (e as Error).message
  } finally {
    if (version === loadVersion) loading.value = false
  }
}
function openCreate() {
  name.value = ''
  description.value = ''
  formError.value = ''
  showCreate.value = true
}
async function create() {
  if (saving.value) return
  formError.value = ''
  if (!name.value.trim()) {
    formError.value = '请输入项目名称'
    return
  }
  saving.value = true
  try {
    await createProjectApi({ name: name.value.trim(), description: description.value.trim() })
    showCreate.value = false
    notice.value = '项目已创建，你已成为该项目管理员'
    await load()
  } catch (e) {
    formError.value = (e as Error).message
  } finally {
    saving.value = false
  }
}
onMounted(load)
</script>
<template>
  <section class="page">
    <div class="page-heading">
      <div>
        <span class="eyebrow">YOUR WORKSPACE</span>
        <h1>项目空间<span class="heading-dot">.</span></h1>
        <p class="muted">你好，{{ auth.user?.displayName }}。把想法变成有序的行动。</p>
      </div>
      <button class="button primary" @click="openCreate"><span>＋</span> 新建项目</button>
    </div>
    <div class="workspace-hero">
      <div>
        <span class="badge light">一起，让工作向前</span>
        <h2>清晰的目标，<br />从容的协作。</h2>
        <p>项目、任务与讨论，在同一个空间连接起来。</p>
      </div>
      <div class="hero-orbit" aria-hidden="true">
        <span class="orbit-ring"></span><span class="orbit-ring two"></span
        ><span class="orbit-mark">↗</span><span class="orbit-dot"></span>
      </div>
      <div class="hero-numbers">
        <strong>{{ projects.length }}<small>参与项目</small></strong
        ><strong>{{ totals.tasks }}<small>全部任务</small></strong
        ><strong>{{ totals.done }}<small>已完成</small></strong>
      </div>
    </div>
    <div class="section-toolbar">
      <h2>
        我的项目 <span class="count">{{ projects.length }}</span>
      </h2>
      <div class="toolbar-actions">
        <input
          v-model="search"
          class="search-input"
          aria-label="搜索项目"
          placeholder="搜索项目名称或描述…"
        /><button class="button subtle" :disabled="loading" @click="load">刷新</button>
      </div>
    </div>
    <p v-if="notice" class="notice success" role="status">{{ notice }}</p>
    <div v-if="loading" class="skeleton-grid" aria-label="正在加载项目">
      <div v-for="i in 3" :key="i" class="skeleton-card"></div>
    </div>
    <div v-else-if="error" class="empty-state">
      <h3>暂时无法加载</h3>
      <p role="alert">{{ error }}</p>
      <button class="button" @click="load">重试</button>
    </div>
    <div v-else-if="!filtered.length" class="empty-state">
      <span class="empty-icon">▦</span>
      <h3>{{ search ? '没有找到匹配项目' : '从第一个项目开始' }}</h3>
      <p>{{ search ? '试试其他关键词。' : '创建一个项目，邀请成员，一起推进任务。' }}</p>
      <button v-if="!search" class="button primary" @click="openCreate">创建项目</button>
    </div>
    <div v-else class="project-grid">
      <RouterLink
        v-for="(project, index) in filtered"
        :key="project.id"
        :to="'/projects/' + project.id"
        class="project-card"
      >
        <div class="card-top">
          <span :class="['project-symbol', 'symbol-' + (index % 4)]">{{
            project.name.slice(0, 1)
          }}</span
          ><span class="badge">{{ project.role === 'ADMIN' ? '管理员' : '成员' }}</span>
        </div>
        <h3>{{ project.name }}</h3>
        <p class="card-description">{{ project.description || '这个项目正在等待一个好故事。' }}</p>
        <div class="progress-caption">
          <span>任务进度</span><strong>{{ project.doneCount }} / {{ project.taskCount }}</strong>
        </div>
        <div class="progress-track">
          <span
            :style="{
              width: (project.taskCount ? (project.doneCount / project.taskCount) * 100 : 0) + '%',
            }"
          ></span>
        </div>
        <div class="card-footer">
          <span
            ><span class="mini-avatar">{{ project.memberCount }}</span> 位协作成员</span
          ><span class="card-arrow">↗</span>
        </div>
      </RouterLink>
    </div>
    <p class="page-footnote">只展示你参与的项目 · 权限由后端验证</p>
  </section>
  <ModalShell :open="showCreate" title="创建一个新项目" :busy="saving" @close="showCreate = false">
    <form @submit.prevent="create">
      <fieldset :disabled="saving" class="form-stack">
        <label
          >项目名称<input
            v-model="name"
            required
            maxlength="50"
            autofocus
            placeholder="给项目起个清晰的名字" /></label
        ><label
          >项目描述<textarea
            v-model="description"
            maxlength="500"
            rows="4"
            placeholder="目标是什么？团队需要完成什么？"
          ></textarea>
        </label>
      </fieldset>
      <p v-if="formError" class="notice error" role="alert">{{ formError }}</p>
      <footer class="modal-actions">
        <button type="button" class="button" :disabled="saving" @click="showCreate = false">
          取消</button
        ><button class="button primary" :disabled="saving">
          {{ saving ? '创建中…' : '创建项目' }}
        </button>
      </footer>
    </form>
  </ModalShell>
</template>
