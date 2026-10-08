<script setup lang="ts">
import { onMounted, ref } from 'vue';

import {
  getProjects,
  createProjectApi,
  HttpError,
  type Project,
} from '../api/projects'

const projects = ref<Project[]>([])
const loading = ref(false)
const error = ref('')

async function loadProjects() {
  loading.value = true
  error.value = ''

  try {
    projects.value = await getProjects()

  } catch (err) {
    error.value = err instanceof Error ? err.message : '加载失败'
  } finally {
    loading.value = false
  }
}

const projectName = ref('')
const projectDescription = ref('')
const submitting = ref(false)
const submitError = ref('')
const successMessage = ref('')

async function createProject() {
  if (submitting.value || loading.value) return 

  submitError.value = ''
  successMessage.value = ''

  const name = projectName.value.trim()
  const description = projectDescription.value.trim()

  if (!name) {
    submitError.value = '请输入项目名称'
    return

  }

  if (name.length > 50 || description.length > 500) {
    submitError.value = '名称最多50个字符, 描述最多500个字符'
    return
  }

  submitting.value = true

  try {
    await createProjectApi({ name, description })

    projectName.value = ''
    projectDescription.value = ''
    successMessage.value = '项目创建成功'

    await loadProjects()

  } catch (err) {
    if (err instanceof HttpError) {
      submitError.value = 
        err.status === 400
          ? '提交内容不符合要求，请检查名称和描述' : `创建失败，状态码：${err.status}`
    } else {
      submitError.value = '未能确认创建结果，请检查网络或后端服务。重试前先刷新列表，确认是否已创建。'
    } 
  } finally {
    submitting.value = false
  }

}

onMounted(loadProjects)

</script>

<template>
  <main class="project-page">
    <header class="page-header">
      <div>
        <p class="brand">DEVFLOW</p>
        <h1>我的项目</h1>
        <p class="subtitle">集中管理项目 让团队协作更清晰</p>
      </div>

      <button :disabled="loading || submitting" @click="loadProjects">
        {{ loading ? '加载中...' : '刷新列表'}}
      </button>
    </header>

    <form class="create-form" @submit.prevent="createProject">
      <h2>创建项目</h2>

      <fieldset :disabled="submitting">
        <div class="form-field">
          <label for="project-name">项目名称</label>
          <input
            id="project-name"
            v-model="projectName"
            type="text"
            maxlength="50"
            required
            placeholder="例如 团队官网开发"
            />
        </div>

        <div class="form-field">
          <label for="project-description">项目描述(可选)</label>
          <textarea
            id="project-description"
            v-model="projectDescription"
            maxlength="500"
            rows="3"
            placeholder="描述项目目标"
            >
          </textarea>
        </div>

        <button type="submit" :disabled="submitting || loading">
          {{ submitting ? '创建中...' : '创建项目'}}
        </button>
      </fieldset>

      <p v-if="submitError" class="error" role="alert">
        {{submitError}}
      </p>

      <p v-if="successMessage" class="success" role="status">
        {{successMessage}}
      </p>
    </form>

    <p v-if="loading" role="status">正在加载项目...</p>

    <div v-else-if="error" class="error" role="alert">
      {{error}} 请确认后端服务正常运行 再点击刷新
    </div>

    <p v-else-if="projects.length === 0">还没有项目</p>

    <section v-else class="project-grid" aria-label="项目列表">
      <article
        v-for="project in projects"
        :key="project.id"
        class="project-card"
      >
        <span class="project-label">项目 #{{ project.id }}</span>
        <h2>{{ project.name }}</h2>
        <p>{{ project.description || '暂无描述' }}</p>
      </article>
    </section>
  </main>
</template>

<style scoped>
.project-page {
  width: 100%;
  max-width: 1080px;
  margin: 0 auto;
  padding: 48px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
}

.brand {
  color: #4f46e5;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
}

h1 {
  margin: 8px 0;
  font-size: 32px;
}

.subtitle,
.project-card p {
  color: #64748b;
}

button {
  padding: 10px 16px;
  border: 0;
  border-radius: 8px;
  background: #4f46e5;
  color: white;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: wait;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.project-card {
  padding: 24px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: white;
  overflow-wrap: anywhere;
}

.project-label {
  color: #64748b;
  font-size: 12px;
}

h2 {
  margin: 12px 0;
  font-size: 20px;
}

.error {
  padding: 16px;
  border-radius: 8px;
  background: #fef2f2;
  color: #b91c1c;
}

.create-form {
  margin-bottom: 32px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: white;
}

fieldset {
  min-width: 0;
  margin: 0;
  padding: 0;
  border: 0;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

label {
  font-size: 14px;
  font-weight: 600;
}

input,
textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font: inherit;
}

textarea {
  resize: vertical;
}

input:focus,
textarea:focus {
  outline: 2px solid #4f46e5;
  outline-offset: 2px;
}

.success {
  margin-top: 16px;
  color: #15803d;
}

@media (max-width: 600px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }

}
</style>