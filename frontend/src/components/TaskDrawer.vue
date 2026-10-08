<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import ModalShell from './ModalShell.vue'
import { createTask, updateTask, getTask, getComments, addComment } from '../api/projects'
import { useAuthStore } from '../stores/auth'
import { formatDate, type Member, type Task, type TaskComment, type TaskInput } from '../types'
const props = defineProps<{
  open: boolean
  projectId: number
  taskId: number | null
  members: Member[]
  role: string
}>()
const emit = defineEmits<{ close: []; saved: [] }>()
const auth = useAuthStore()
const task = ref<Task | null>(null),
  comments = ref<TaskComment[]>([]),
  loading = ref(false),
  saving = ref(false),
  posting = ref(false)
const error = ref(''),
  commentError = ref(''),
  commentText = ref('')
const form = reactive({
  title: '',
  description: '',
  status: 'TODO',
  priority: 'MEDIUM',
  assigneeId: '',
  dueDate: '',
})
const isNew = computed(() => props.taskId === null)
const editable = computed(
  () =>
    isNew.value ||
    props.role === 'ADMIN' ||
    task.value?.creatorId === auth.user?.id ||
    task.value?.assigneeId === auth.user?.id,
)
let generation = 0
async function load() {
  const current = ++generation
  task.value = null
  comments.value = []
  error.value = ''
  commentError.value = ''
  commentText.value = ''
  Object.assign(form, {
    title: '',
    description: '',
    status: 'TODO',
    priority: 'MEDIUM',
    assigneeId: '',
    dueDate: '',
  })
  if (props.taskId === null) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const [data, discussion] = await Promise.all([
      getTask(props.projectId, props.taskId),
      getComments(props.projectId, props.taskId),
    ])
    if (current !== generation) return
    task.value = data
    comments.value = discussion
    Object.assign(form, {
      title: data.title,
      description: data.description,
      status: data.status,
      priority: data.priority,
      assigneeId: data.assigneeId === null ? '' : String(data.assigneeId),
      dueDate: data.dueDate || '',
    })
  } catch (e) {
    if (current === generation) error.value = (e as Error).message
  } finally {
    if (current === generation) loading.value = false
  }
}
watch(
  () => [props.open, props.taskId, props.projectId],
  () => {
    if (props.open) void load()
    else generation++
  },
  { immediate: true },
)
async function save() {
  if (saving.value || !editable.value) return
  error.value = ''
  if (!form.title.trim()) {
    error.value = '任务标题不能为空'
    return
  }
  saving.value = true
  const input: TaskInput = {
    title: form.title.trim(),
    description: form.description.trim(),
    status: form.status as TaskInput['status'],
    priority: form.priority as TaskInput['priority'],
    assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
    dueDate: form.dueDate || null,
  }
  if (task.value) input.version = task.value.version
  try {
    if (props.taskId === null) await createTask(props.projectId, input)
    else await updateTask(props.projectId, props.taskId, input)
    emit('saved')
    emit('close')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    saving.value = false
  }
}
async function postComment() {
  if (posting.value || props.taskId === null) return
  commentError.value = ''
  if (!commentText.value.trim()) {
    commentError.value = '请输入评论内容'
    return
  }
  posting.value = true
  try {
    comments.value = await addComment(props.projectId, props.taskId, commentText.value.trim())
    commentText.value = ''
  } catch (e) {
    commentError.value = (e as Error).message
  } finally {
    posting.value = false
  }
}
function refreshTask() {
  if (window.confirm('重新加载会覆盖未保存的编辑，是否继续？')) void load()
}
</script>
<template>
  <ModalShell
    :open="open"
    :title="isNew ? '创建任务' : '任务详情 · #' + taskId"
    drawer
    :busy="saving || posting"
    @close="emit('close')"
  >
    <p v-if="loading" class="loading-state">正在加载任务…</p>
    <template v-else>
      <p v-if="error" class="notice error" role="alert">
        {{ error
        }}<button
          v-if="!isNew"
          type="button"
          class="text-button"
          :disabled="saving || posting"
          @click="refreshTask"
        >
          重新加载任务
        </button>
      </p>
      <form v-if="isNew || task" @submit.prevent="save">
        <p v-if="!editable" class="notice">
          你可以查看和评论；修改权限属于管理员、创建者或负责人。
        </p>
        <fieldset class="form-stack" :disabled="saving || !editable">
          <label
            >任务标题<input
              v-model="form.title"
              required
              maxlength="100"
              placeholder="用一句话描述要完成的事情"
          /></label>
          <label
            >任务描述<textarea
              v-model="form.description"
              maxlength="2000"
              rows="5"
              placeholder="补充背景、要求和验收标准…"
            ></textarea>
          </label>
          <div class="form-row">
            <label
              >优先级<select v-model="form.priority">
                <option value="LOW">低优先级</option>
                <option value="MEDIUM">中优先级</option>
                <option value="HIGH">高优先级</option>
              </select></label
            ><label
              >负责人<select v-model="form.assigneeId">
                <option value="">暂不指派</option>
                <option v-for="member in members" :key="member.id" :value="String(member.id)">
                  {{ member.displayName }}
                </option>
                <option
                  v-if="task?.assigneeId && !members.some((m) => m.id === task?.assigneeId)"
                  :value="String(task.assigneeId)"
                >
                  {{ task.assigneeName }}（已离开项目）
                </option>
              </select></label
            >
          </div>
          <div class="form-row">
            <label>截止日期<input v-model="form.dueDate" type="date" /></label
            ><label v-if="!isNew"
              >任务状态<select v-model="form.status">
                <option value="TODO">待处理</option>
                <option value="IN_PROGRESS">进行中</option>
                <option value="DONE">已完成</option>
              </select></label
            >
          </div>
        </fieldset>
        <p v-if="task" class="muted small">
          由 {{ task.creatorName }} 创建于 {{ formatDate(task.createdAt) }} · 版本
          {{ task.version }}
        </p>
        <footer v-if="editable" class="modal-actions">
          <button class="button primary" :disabled="saving || posting">
            {{ saving ? '保存中…' : isNew ? '创建任务' : '保存修改' }}
          </button>
        </footer>
      </form>
      <section v-if="task" class="discussion">
        <h3>
          讨论 <span class="count">{{ comments.length }}</span>
        </h3>
        <div v-if="!comments.length" class="muted small">还没有评论，说说你的想法。</div>
        <article v-for="comment in comments" :key="comment.id" class="comment">
          <span class="avatar small-avatar">{{ comment.authorName.slice(0, 1) }}</span>
          <div>
            <div class="comment-heading">
              <strong>{{ comment.authorName }}</strong
              ><time>{{ formatDate(comment.createdAt) }}</time>
            </div>
            <p>{{ comment.content }}</p>
          </div>
        </article>
        <form @submit.prevent="postComment">
          <label class="form-stack"
            >发表评论<textarea
              v-model="commentText"
              :disabled="posting || saving"
              maxlength="500"
              rows="3"
              required
              placeholder="留言与团队沟通…"
            ></textarea>
          </label>
          <p v-if="commentError" class="notice error" role="alert">{{ commentError }}</p>
          <div class="modal-actions">
            <button class="button" :disabled="posting || saving">
              {{ posting ? '发送中…' : '发送评论' }}
            </button>
          </div>
        </form>
      </section>
    </template>
  </ModalShell>
</template>
