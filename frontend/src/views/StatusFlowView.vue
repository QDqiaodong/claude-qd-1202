<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const items = ref([])
const history = ref([])
const loading = ref(false)
const targetId = ref(null)
const toStatus = ref('')
const operator = ref('店长')

const statusOptions = ['在售', '已预订', '已售出', '已撤回']

async function load() {
  loading.value = true
  try {
    ;[items.value, history.value] = await Promise.all([api.listItems(), api.listFlowHistory()])
    if (!targetId.value && items.value.length) targetId.value = items.value[0].id
  } catch (e) { ElMessage.error(e.message) } finally { loading.value = false }
}

// 按寄卖品分组展示流转留痕
const groups = computed(() => {
  const map = {}
  for (const h of history.value) {
    const key = `寄卖品#${h.itemId}`
    ;(map[key] = map[key] || []).push(h)
  }
  return Object.entries(map).map(([k, v]) => ({ key: k, list: v }))
})

function currentStatus(id) {
  const it = items.value.find((x) => x.id === id)
  return it ? it.status : '—'
}

async function doTransition() {
  if (!targetId.value || !toStatus.value) { ElMessage.warning('请选择寄卖品与目标状态'); return }
  try {
    await api.transitionItem(targetId.value, toStatus.value, operator.value, '前端发起流转')
    ElMessage.success('流转成功')
    toStatus.value = ''
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <span class="hint">状态机：已售出锁价、已撤回不可再上架；非法跳转（如已售出→在售）将被拦截</span>
    </div>

    <el-card class="panel" shadow="never">
      <div class="op">
        <el-select v-model="targetId" placeholder="选择寄卖品" style="width: 240px">
          <el-option v-for="it in items" :key="it.id" :label="`${it.name}（${it.code}·${it.status}）`" :value="it.id" />
        </el-select>
        <el-select v-model="toStatus" placeholder="目标状态" style="width: 140px">
          <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
        </el-select>
        <el-input v-model="operator" placeholder="操作人" style="width: 140px" />
        <el-button type="primary" @click="doTransition">发起流转</el-button>
      </div>
    </el-card>

    <el-collapse v-loading="loading" class="groups">
      <el-collapse-item v-if="!groups.length" name="empty" title="暂无流转留痕">
        <span class="hint">选择寄卖品并发起一次合法流转后会在此留痕。</span>
      </el-collapse-item>
      <el-collapse-item v-for="g in groups" :key="g.key" :name="g.key" :title="`${g.key}（${g.list.length} 次）`">
        <el-timeline>
          <el-timeline-item
            v-for="h in g.list"
            :key="h.id"
            :timestamp="(h.createdAt || '') + ' · ' + (h.operator || '')"
          >
            {{ h.fromStatus }} → <b>{{ h.toStatus }}</b>
            <span class="remark">{{ h.remark }}</span>
          </el-timeline-item>
        </el-timeline>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<style scoped>
.toolbar { margin-bottom: 12px; }
.hint { color: var(--muted); font-size: 13px; }
.panel { margin-bottom: 12px; background: #fff; }
.op { display: flex; gap: 10px; flex-wrap: wrap; align-items: center; }
.groups { background: #fff; padding: 8px 12px; border-radius: 10px; }
.remark { color: var(--muted); margin-left: 8px; }
</style>
