<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const appraisals = ref([])
const items = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({ itemId: null, appraiser: '', amount: 0, appraisalDate: '', status: '待评' })

const groups = computed(() => {
  const map = {}
  for (const a of appraisals.value) {
    const key = a.status || '未分类'
    ;(map[key] = map[key] || []).push(a)
  }
  return Object.entries(map).map(([k, v]) => ({ key: k, list: v }))
})

async function load() {
  loading.value = true
  try {
    ;[appraisals.value, items.value] = await Promise.all([api.listAppraisals(), api.listItems()])
  } catch (e) { ElMessage.error(e.message) } finally { loading.value = false }
}

function itemName(id) {
  const it = items.value.find((x) => x.id === id)
  return it ? `${it.name}（${it.code}）` : `#${id}`
}

async function submit() {
  try {
    await api.createAppraisal({ ...form.value })
    ElMessage.success('估价已留痕')
    dialog.value = false
    form.value = { itemId: null, appraiser: '', amount: 0, appraisalDate: '', status: '待评' }
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}

async function complete(a) {
  try {
    await api.completeAppraisal(a.id)
    ElMessage.success('已标记为已评')
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
      <span class="hint">同一寄卖品可多次估价留痕，按「待评 / 已评」分组折叠</span>
      <el-button type="primary" @click="dialog = true">新增估价</el-button>
    </div>

    <el-collapse v-loading="loading" class="groups">
      <el-collapse-item v-for="g in groups" :key="g.key" :name="g.key" :title="`${g.key}（${g.list.length}）`">
        <el-table :data="g.list" size="small" border>
          <el-table-column label="寄卖品" min-width="160">
            <template #default="{ row }">{{ itemName(row.itemId) }}</template>
          </el-table-column>
          <el-table-column prop="appraiser" label="评估师" width="100" />
          <el-table-column prop="amount" label="估价金额" width="110" />
          <el-table-column prop="appraisalDate" label="估价日期" width="120" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.status === '待评'" size="small" type="success" @click="complete(row)">完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <el-dialog v-model="dialog" title="新增估价" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="寄卖品">
          <el-select v-model="form.itemId" filterable>
            <el-option v-for="it in items" :key="it.id" :label="`${it.name}（${it.code}）`" :value="it.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估师"><el-input v-model="form.appraiser" /></el-form-item>
        <el-form-item label="估价金额"><el-input-number v-model="form.amount" :min="1" /></el-form-item>
        <el-form-item label="估价日期"><el-date-picker v-model="form.appraisalDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.hint { color: var(--muted); font-size: 13px; }
.groups { background: #fff; padding: 8px 12px; border-radius: 10px; }
</style>
