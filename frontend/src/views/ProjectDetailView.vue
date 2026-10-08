<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import {
  getProject,
  getMembers,
  getStats,
  getTasks,
  updateProject,
  updateTaskStatus,
} from '../api/projects'
import { useAuthStore } from '../stores/auth'
import ModalShell from '../components/ModalShell.vue'
import StatsPanel from '../components/StatsPanel.vue'
import TaskDrawer from '../components/TaskDrawer.vue'
import MembersPanel from '../components/MembersPanel.vue'
import {
  statusNames,
  priorityNames,
  isOverdue,
  type Project,
  type Member,
  type Task,
  type Stats,
  type TaskStatus,
} from '../types'
const route = useRoute(),
  auth = useAuthStore()
const projectId = computed(() => Number(route.params.id))
const project = ref<Project | null>(null),
  members = ref<Member[]>([]),
  tasks = ref<Task[]>([])
const stats = ref<Stats>({
  total: 0,
  todo: 0,
  inProgress: 0,
  done: 0,
  overdue: 0,
  completionRate: 0,
})
const loading = ref(true),
  error = ref(''),
  actionError = ref(''),
  taskLoading = ref(false),
  tab = ref('board')
const filters = reactive({ q: '', status: '', priority: '', assigneeId: '', overdue: false })
const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE']
const drawerOpen = ref(false),
  selectedTask = ref<number | null>(null),
  updating = ref<number[]>([])
const editOpen = ref(false),
  editName = ref(''),
  editDescription = ref(''),
  saving = ref(false),
  editError = ref('')
const admin = computed(() => project.value?.role === 'ADMIN')
let generation = 0,
  taskGeneration = 0
async function loadPage() {
  const g = ++generation
  taskGeneration++
  taskLoading.value = false
  loading.value = true
  error.value = ''
  if (!Number.isSafeInteger(projectId.value) || projectId.value <= 0) {
    error.value = '项目地址无效'
    loading.value = false
    return
  }
  try {
    const [p, m, s, t] = await Promise.all([
      getProject(projectId.value),
      getMembers(projectId.value),
      getStats(projectId.value),
      getTasks(projectId.value, filters),
    ])
    if (g !== generation) return
    project.value = p
    members.value = m
    stats.value = s
    tasks.value = t
  } catch (e) {
    if (g === generation) error.value = (e as Error).message
  } finally {
    if (g === generation) loading.value = false
  }
}
async function refreshBoard() {
  const g = ++taskGeneration
  taskLoading.value = true
  actionError.value = ''
  try {
    const [t, s] = await Promise.all([
      getTasks(projectId.value, filters),
      getStats(projectId.value),
    ])
    if (g === taskGeneration) {
      tasks.value = t
      stats.value = s
    }
  } catch (e) {
    if (g === taskGeneration) actionError.value = (e as Error).message
  } finally {
    if (g === taskGeneration) taskLoading.value = false
  }
}
watch(
  () => route.params.id,
  () => {
    drawerOpen.value = false
    project.value = null
    tab.value = 'board'
    Object.assign(filters, { q: '', status: '', priority: '', assigneeId: '', overdue: false })
    void loadPage()
  },
  { immediate: true },
)
function openTask(id: number | null) {
  selectedTask.value = id
  drawerOpen.value = true
}
function canEdit(task: Task) {
  return admin.value || task.creatorId === auth.user?.id || task.assigneeId === auth.user?.id
}
async function changeStatus(task: Task, event: Event) {
  const element = event.target as HTMLSelectElement
  updating.value.push(task.id)
  actionError.value = ''
  try {
    await updateTaskStatus(projectId.value, task, element.value as TaskStatus)
    await refreshBoard()
  } catch (e) {
    actionError.value = (e as Error).message
    element.value = task.status
  } finally {
    updating.value = updating.value.filter((id) => id !== task.id)
  }
}
function editProject() {
  editName.value = project.value?.name || ''
  editDescription.value = project.value?.description || ''
  editError.value = ''
  editOpen.value = true
}
async function saveProject() {
  if (saving.value) return
  if (!editName.value.trim()) {
    editError.value = '请输入项目名称'
    return
  }
  saving.value = true
  editError.value = ''
  try {
    project.value = await updateProject(projectId.value, {
      name: editName.value.trim(),
      description: editDescription.value.trim(),
    })
    editOpen.value = false
  } catch (e) {
    editError.value = (e as Error).message
  } finally {
    saving.value = false
  }
}
function clearFilters() {
  Object.assign(filters, { q: '', status: '', priority: '', assigneeId: '', overdue: false })
  void refreshBoard()
}
</script>
<template>
  <section class="page">
    <RouterLink to="/projects" class="back-link">← 返回项目空间</RouterLink>
    <div v-if="loading" class="loading-state">正在连接项目空间…</div>
    <div v-else-if="error" class="empty-state">
      <h2>无法打开项目</h2>
      <p role="alert">{{ error }}</p>
      <button class="button" @click="loadPage">重试</button>
    </div>
    <template v-else-if="project">
      <div class="page-heading detail-heading">
        <div>
          <div class="eyebrow">
            PROJECT / #{{ project.id }}
            <span class="badge">{{ admin ? '管理员' : '普通成员' }}</span>
          </div>
          <h1>{{ project.name }}</h1>
          <p class="muted project-description">{{ project.description || '暂无项目描述' }}</p>
        </div>
        <div class="toolbar-actions">
          <button v-if="admin" class="button" @click="editProject">编辑项目</button
          ><button class="button primary" @click="openTask(null)">＋ 新建任务</button>
        </div>
      </div>
      <StatsPanel :stats="stats" />
      <nav class="tabs" aria-label="项目视图">
        <button
          v-for="item in [
            { id: 'board', name: '任务看板' },
            { id: 'members', name: '团队成员' },
            { id: 'overview', name: '进度概览' },
          ]"
          :key="item.id"
          :class="{ active: tab === item.id }"
          :aria-current="tab === item.id ? 'page' : undefined"
          @click="tab = item.id"
        >
          {{ item.name }}</button
        ><span class="tab-note">{{ members.length }} 位成员协作中</span>
      </nav>
      <template v-if="tab === 'board'">
        <form class="filter-bar" @submit.prevent="refreshBoard">
          <input
            v-model="filters.q"
            maxlength="100"
            aria-label="搜索任务标题"
            placeholder="搜索任务…"
          /><select v-model="filters.status" aria-label="筛选状态">
            <option value="">全部状态</option>
            <option v-for="status in statuses" :key="status" :value="status">
              {{ statusNames[status] }}
            </option></select
          ><select v-model="filters.assigneeId" aria-label="筛选负责人">
            <option value="">全部负责人</option>
            <option v-for="member in members" :key="member.id" :value="String(member.id)">
              {{ member.displayName }}
            </option></select
          ><select v-model="filters.priority" aria-label="筛选优先级">
            <option value="">全部优先级</option>
            <option value="HIGH">高优先级</option>
            <option value="MEDIUM">中优先级</option>
            <option value="LOW">低优先级</option></select
          ><label class="checkbox"
            ><input v-model="filters.overdue" type="checkbox" />只看逾期</label
          ><button class="button" :disabled="taskLoading">
            {{ taskLoading ? '加载中…' : '筛选' }}</button
          ><button class="text-button" type="button" @click="clearFilters">重置</button>
        </form>
        <p v-if="actionError" class="notice error" role="alert">
          {{ actionError }} <button class="text-button" @click="refreshBoard">刷新看板</button>
        </p>
        <div class="board" :aria-busy="taskLoading">
          <section
            v-for="status in statuses"
            :key="status"
            :class="['board-column', 'column-' + status]"
          >
            <header class="column-heading">
              <span class="status-dot"></span>
              <h2>{{ statusNames[status] }}</h2>
              <span class="count">{{ tasks.filter((t) => t.status === status).length }}</span
              ><button class="icon-button" aria-label="新建待处理任务" @click="openTask(null)">
                ＋
              </button>
            </header>
            <div class="task-stack">
              <article
                v-for="task in tasks.filter((t) => t.status === status)"
                :key="task.id"
                class="task-card"
              >
                <div class="task-top">
                  <span :class="['priority', 'priority-' + task.priority]"
                    >{{ priorityNames[task.priority] }}优先级</span
                  ><span class="muted small">#{{ task.id }}</span>
                </div>
                <button class="task-title" @click="openTask(task.id)">{{ task.title }}</button>
                <p class="task-description">{{ task.description || '暂无描述' }}</p>
                <div class="task-meta">
                  <span :class="['due-date', { overdue: isOverdue(task) }]">{{
                    task.dueDate
                      ? (isOverdue(task) ? '逾期 · ' : '截止 · ') + task.dueDate.slice(5)
                      : '未设截止日期'
                  }}</span
                  ><span class="assignee" :title="task.assigneeName || '未指派'"
                    ><span class="mini-avatar">{{ task.assigneeName?.slice(0, 1) || '—' }}</span
                    >{{ task.assigneeName || '未指派' }}</span
                  >
                </div>
                <select
                  v-if="canEdit(task)"
                  class="status-select"
                  :value="task.status"
                  :disabled="updating.includes(task.id)"
                  :aria-label="'修改任务 ' + task.title + ' 的状态'"
                  @change="changeStatus(task, $event)"
                >
                  <option v-for="s in statuses" :key="s" :value="s">{{ statusNames[s] }}</option>
                </select>
              </article>
              <div v-if="!tasks.some((t) => t.status === status)" class="column-empty">
                这个阶段暂时没有任务
              </div>
            </div>
          </section>
        </div>
        <p class="page-footnote">
          显示 {{ tasks.length }} 条匹配任务 · 上方统计为整个项目 · 点击标题查看详情与讨论
        </p>
      </template>
      <MembersPanel
        v-else-if="tab === 'members'"
        :project-id="projectId"
        :members="members"
        :role="project.role"
        @changed="loadPage"
      />
      <section v-else class="panel overview-panel">
        <div>
          <span class="eyebrow">PROJECT MOMENTUM</span>
          <h2>每一步，都算进展。</h2>
          <p class="muted">已完成 {{ stats.done }} / {{ stats.total }} 项任务。</p>
          <div class="large-progress">
            <strong>{{ stats.completionRate }}<small>%</small></strong>
            <div class="progress-track">
              <span :style="{ width: stats.completionRate + '%' }"></span>
            </div>
          </div>
        </div>
        <div class="status-bars">
          <div
            v-for="item in [
              { label: '待处理', count: stats.todo, color: 'var(--amber)' },
              { label: '进行中', count: stats.inProgress, color: 'var(--blue)' },
              { label: '已完成', count: stats.done, color: 'var(--green)' },
            ]"
            :key="item.label"
          >
            <div class="progress-caption">
              <span>{{ item.label }}</span
              ><strong>{{ item.count }}</strong>
            </div>
            <div class="progress-track">
              <span
                :style="{
                  width: (stats.total ? (item.count / stats.total) * 100 : 0) + '%',
                  background: item.color,
                }"
              ></span>
            </div>
          </div>
          <p class="muted small">逾期统计以北京时间日期计算，已完成任务不会计入逾期。</p>
        </div>
      </section>
    </template>
  </section>
  <TaskDrawer
    :open="drawerOpen"
    :project-id="projectId"
    :task-id="selectedTask"
    :members="members"
    :role="project?.role || 'MEMBER'"
    @close="drawerOpen = false"
    @saved="refreshBoard"
  />
  <ModalShell :open="editOpen" title="编辑项目" :busy="saving" @close="editOpen = false"
    ><form @submit.prevent="saveProject">
      <fieldset class="form-stack" :disabled="saving">
        <label>项目名称<input v-model="editName" maxlength="50" required /></label
        ><label
          >项目描述<textarea v-model="editDescription" maxlength="500" rows="4"></textarea>
        </label>
      </fieldset>
      <p v-if="editError" class="notice error" role="alert">{{ editError }}</p>
      <footer class="modal-actions">
        <button class="button primary" :disabled="saving">
          {{ saving ? '保存中…' : '保存修改' }}
        </button>
      </footer>
    </form></ModalShell
  >
</template>
