<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const buyers = ref([])
const deals = ref([])
const items = ref([])
const loading = ref(false)
const dialog = ref(false)
const form = ref({ buyerId: null, itemId: null, dealPrice: 0, commissionRate: 0.1, dealDate: '' })

const groups = computed(() => {
  const map = {}
  for (const d of deals.value) {
    const key = d.settlementStatus || '未分类'
    ;(map[key] = map[key] || []).push(d)
  }
  return Object.entries(map).map(([k, v]) => ({ key: k, list: v }))
})

async function load() {
  loading.value = true
  try {
    ;[buyers.value, deals.value, items.value] = await Promise.all([
      api.listBuyers(), api.listDeals(), api.listItems()
    ])
  } catch (e) { ElMessage.error(e.message) } finally { loading.value = false }
}

function buyerName(id) {
  const b = buyers.value.find((x) => x.id === id)
  return b ? b.name : `#${id}`
}
function itemName(id) {
  const it = items.value.find((x) => x.id === id)
  return it ? `${it.name}（${it.code}）` : `#${id}`
}

async function submit() {
  try {
    await api.createDeal({ ...form.value })
    ElMessage.success('成交已登记，佣金自动计算')
    dialog.value = false
    form.value = { buyerId: null, itemId: null, dealPrice: 0, commissionRate: 0.1, dealDate: '' }
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}

async function settle(d) {
  try {
    await api.settleDeal(d.id)
    ElMessage.success('已结算')
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
      <span class="hint">佣金=成交价×比例自动算；成交价不得低于估价红线；仅「已售出」可结算</span>
      <el-button type="primary" @click="dialog = true">登记成交</el-button>
    </div>

    <el-collapse v-loading="loading" class="groups">
      <el-collapse-item v-for="g in groups" :key="g.key" :name="g.key" :title="`${g.key}（${g.list.length}）`">
        <el-table :data="g.list" size="small" border>
          <el-table-column label="买家" width="130">
            <template #default="{ row }">{{ buyerName(row.buyerId) }}</template>
          </el-table-column>
          <el-table-column label="寄卖品" min-width="160">
            <template #default="{ row }">{{ itemName(row.itemId) }}</template>
          </el-table-column>
          <el-table-column prop="dealPrice" label="成交价" width="100" />
          <el-table-column prop="commissionRate" label="佣金比例" width="90" />
          <el-table-column prop="commissionAmount" label="佣金金额" width="100" />
          <el-table-column prop="settlementStatus" label="结算" width="90" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.settlementStatus === '未结算'" size="small" type="success" @click="settle(row)">结算</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <el-dialog v-model="dialog" title="登记成交" width="440px">
      <el-form :model="form" label-width="84px">
        <el-form-item label="买家">
          <el-select v-model="form.buyerId" filterable>
            <el-option v-for="b in buyers" :key="b.id" :label="`${b.name}（${b.level}）`" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="寄卖品">
          <el-select v-model="form.itemId" filterable>
            <el-option v-for="it in items.filter(i => i.status === '已售出')" :key="it.id" :label="`${it.name}（${it.code}·估${it.estimate}）`" :value="it.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="成交价"><el-input-number v-model="form.dealPrice" :min="1" /></el-form-item>
        <el-form-item label="佣金比例"><el-input-number v-model="form.commissionRate" :min="0" :max="1" :step="0.05" /></el-form-item>
        <el-form-item label="成交日期"><el-date-picker v-model="form.dealDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
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
