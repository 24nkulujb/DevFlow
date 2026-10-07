<script setup lang="ts">
import { onMounted, ref } from 'vue';

interface Project {
  id: number
  name: string
  description: string
}

const projects = ref<Project[]>([])
const loading = ref(false)
const error = ref('')

async function loadProjects() {
  loading.value = true
  error.value = ''

  try {
    const response = await fetch("/api/projects")

    if (!response.ok) {
      throw new Error(`加载失败，状态码：${response.status}`)
    }

    projects.value = await response.json()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '加载失败'
  } finally {
    loading.value = false
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

      <button :disabled="loading" @click="loadProjects">
        {{ loading ? '加载中...' : '刷新列表'}}
      </button>
    </header>

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

@media (max-width: 600px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>