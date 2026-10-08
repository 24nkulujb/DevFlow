<script setup lang="ts">
import { nextTick, onBeforeUnmount, ref, watch } from 'vue'
const props = defineProps<{ open: boolean; title: string; drawer?: boolean; busy?: boolean }>()
const emit = defineEmits<{ close: [] }>()
const element = ref<HTMLDialogElement | null>(null)
watch(
  () => props.open,
  async (open) => {
    await nextTick()
    if (open && element.value && !element.value.open) element.value.showModal()
    else if (!open) element.value?.close()
  },
  { immediate: true },
)
onBeforeUnmount(() => element.value?.close())
function close() {
  if (!props.busy) emit('close')
}
function outside(event: MouseEvent) {
  if (event.target === element.value) close()
}
</script>
<template>
  <Teleport to="body">
    <dialog
      ref="element"
      :class="['modal', { drawer }]"
      :aria-label="title"
      @cancel.prevent="close"
      @click="outside"
    >
      <section class="modal-panel">
        <header class="modal-heading">
          <div>
            <span class="eyebrow">DEVFLOW WORKSPACE</span>
            <h2>{{ title }}</h2>
          </div>
          <button
            class="icon-button"
            type="button"
            :disabled="busy"
            aria-label="关闭对话框"
            @click="close"
          >
            ×
          </button>
        </header>
        <slot />
      </section>
    </dialog>
  </Teleport>
</template>
