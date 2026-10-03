<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import router from './router'
import api from './api'

import ConsignmentItemView from './views/ConsignmentItemView.vue'
import AppraisalView from './views/AppraisalView.vue'
import StatusFlowView from './views/StatusFlowView.vue'
import SettlementView from './views/SettlementView.vue'

// ------------------------------------------------------------------
// 导航形态：顶部图标条。四个业务模块各占一个图标按钮，
// 页面操作范式：分组折叠（按品类 / 状态分组展开看明细）。
// ------------------------------------------------------------------

const modules = [
  { path: '/items',      label: '寄卖品台账', icon: '🏺', comp: ConsignmentItemView },
  { path: '/appraisals', label: '估价记录',   icon: '🔍', comp: AppraisalView },
  { path: '/flow',       label: '状态流转',   icon: '🔄', comp: StatusFlowView },
  { path: '/settlement', label: '买家结算',   icon: '💰', comp: SettlementView }
]

const currentPath = ref(router.currentRoute.value.path)
const counts = ref({})

const currentView = computed(() => {
  const m = modules.find((mm) => mm.path === currentPath.value)
  return m ? m.comp : ConsignmentItemView
})

async function loadCounts() {
  try {
    const [i, a, f, s] = await Promise.all([
      api.listItems(), api.listAppraisals(),
      api.listFlowHistory(), api.listDeals()
    ])
    counts.value = {
      '/items': i.length,
      '/appraisals': a.length,
      '/flow': f.length,
      '/settlement': s.length
    }
  } catch (e) { /* 计数失败不影响主流程 */ }
}

function go(path) {
  if (path !== currentPath.value) router.push(path)
}

watch(() => router.currentRoute.value.path, (p) => { currentPath.value = p })

onMounted(async () => {
  currentPath.value = router.currentRoute.value.path
  await loadCounts()
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <span class="brand-mark">鼎</span>
        <div class="brand-text">
          <div class="brand-title">古玩寄卖行</div>
          <div class="brand-sub">赭红主题 · 顶部图标条导航 · 分组折叠看明细</div>
        </div>
      </div>

      <nav class="icon-bar">
        <button
          v-for="m in modules"
          :key="m.path"
          class="icon-btn"
          :class="{ active: currentPath === m.path }"
          @click="go(m.path)"
        >
          <span class="icon">{{ m.icon }}</span>
          <span class="icon-label">{{ m.label }}</span>
          <span class="icon-count">{{ counts[m.path] ?? 0 }}</span>
        </button>
      </nav>
    </header>

    <main class="content">
      <component :is="currentView" @changed="loadCounts" />
    </main>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; display: flex; flex-direction: column; }

.topbar {
  display: flex; align-items: center; gap: 28px;
  padding: 0 22px; height: 64px;
  background: linear-gradient(100deg, var(--primary-dark-2) 0%, var(--primary) 70%, var(--primary-light-3) 100%);
  color: #fff; box-shadow: 0 2px 12px rgba(139, 58, 58, 0.3);
}
.brand { display: flex; align-items: center; gap: 12px; }
.brand-mark {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  background: #fff; color: var(--primary-dark-2); font-weight: 700; font-size: 20px;
}
.brand-title { font-size: 18px; font-weight: 600; }
.brand-sub { font-size: 12px; opacity: 0.85; }

.icon-bar { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.icon-btn {
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  padding: 6px 16px; border: none; border-radius: 10px;
  background: rgba(255, 255, 255, 0.12); color: #fff; cursor: pointer;
  transition: all 0.18s ease;
}
.icon-btn:hover { background: rgba(255, 255, 255, 0.24); }
.icon-btn.active { background: #fff; color: var(--primary-dark-2); }
.icon { font-size: 20px; line-height: 1; }
.icon-label { font-size: 12px; font-weight: 600; }
.icon-count {
  font-size: 11px; opacity: 0.8;
  background: rgba(0, 0, 0, 0.18); border-radius: 8px; padding: 0 6px;
}
.icon-btn.active .icon-count { background: var(--primary-wash); color: var(--primary-dark-2); }

.content { flex: 1; padding: 18px 22px 28px; }
</style>
