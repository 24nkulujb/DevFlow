<script setup lang="ts">
import type { Stats } from '../types'
defineProps<{ stats: Stats }>()
</script>
<template>
  <div class="stats-grid">
    <article class="stat-card">
      <span>全部任务</span><strong>{{ stats.total }}</strong
      ><small>项目内的完整工作量</small>
    </article>
    <article class="stat-card">
      <span>进行中</span><strong class="blue-text">{{ stats.inProgress }}</strong
      ><small>正在推进的事项</small>
    </article>
    <article class="stat-card">
      <span>已完成</span><strong class="green-text">{{ stats.done }}</strong
      ><small>完成率 {{ stats.completionRate }}%</small>
    </article>
    <article class="stat-card">
      <span>逾期任务</span
      ><strong :class="{ 'red-text': stats.overdue > 0 }">{{ stats.overdue }}</strong
      ><small>截止日期早于今天且未完成</small>
    </article>
  </div>
</template>
