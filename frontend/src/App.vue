<script setup lang="ts">
import { ref } from 'vue';

const projectName = ref('DevFlow 团队协作平台')
const taskCount = ref(0)

function addTask() {
  taskCount.value += 1
}

</script>

<template>
  <main class="welcome">
    <p>全栈作品集</p>
    <h1>{{projectName}}</h1>
    <p>当前任务数量: {{taskCount}}</p>
    <button @click="addTask">添加一个任务</button>
  </main>
</template>

<style>
.welcome {
  padding: 40px;
}

button {
  padding: 10px 16px;
  cursor: pointer;
}
</style>