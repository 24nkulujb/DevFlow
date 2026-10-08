<template>
  <section class="empty-state">
    <span class="eyebrow">404</span>
    <h1>这个页面暂时不存在</h1>
    <p>检查地址，或回到项目空间。</p>
    <RouterLink class="button primary" to="/projects">返回项目</RouterLink>
  </section>
</template>
<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>
