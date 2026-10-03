<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const items = ref([])
const groupBy = ref('category')
const loading = ref(false)

const dialog = ref(false)
const form = ref({ code: '', category: '瓷器', name: '', era: '', consignor: '', estimate: 0 })

// 改价对话框
const priceDialog = ref(false)
const priceTarget = ref(null)
const newEstimate = ref(0)

const groups = computed(() => {
  const map = {}
  for (const it of items.value) {
    const key = groupBy.value === 'status' ? it.status : it.category
    ;(map[key] = map[key] || []).push(it)
  }
  return Object.entries(map).map(([k, v]) => ({ key: k, list: v }))
})

async function load() {
  loading.value = true
  try {
    items.value = await api.listItems()
  } catch (e) { ElMessage.error(e.message) } finally { loading.value = false }
}

async function submit() {
  try {
    await api.createItem({ ...form.value })
    ElMessage.success('登记成功')
    dialog.value = false
    form.value = { code: '', category: '瓷器', name: '', era: '', consignor: '', estimate: 0 }
    await load()
    emit('changed')
  } catch (e) { ElMessage.error(e.message) }
}

function openPrice(it) {
  priceTarget.value = it
  newEstimate.value = it.estimate
  priceDialog.value = true
}

async function doChangePrice() {
  try {
    await api.updateItem(priceTarget.value.id, { estimate: newEstimate.value })
    ElMessage.success('改价成功')
    priceDialog.value = false
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
      <el-radio-group v-model="groupBy">
        <el-radio-button value="category">按品类折叠</el-radio-button>
        <el-radio-button value="status">按状态折叠</el-radio-button>
      </el-radio-group>
      <el-button type="primary" @click="dialog = true">登记寄卖品</el-button>
    </div>

    <el-collapse v-loading="loading" class="groups">
      <el-collapse-item v-for="g in groups" :key="g.key" :name="g.key" :title="`${g.key}（${g.list.length}）`">
        <el-table :data="g.list" size="small" border>
          <el-table-column prop="code" label="编号" width="100" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="era" label="年代" width="90" />
          <el-table-column prop="consignor" label="委托方" width="120" />
          <el-table-column prop="estimate" label="估价" width="110" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="openPrice(row)">改价</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <el-dialog v-model="dialog" title="登记寄卖品" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="寄卖编号"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="品类">
          <el-select v-model="form.category">
            <el-option v-for="c in ['瓷器','字画','玉器','杂项']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="年代"><el-input v-model="form.era" /></el-form-item>
        <el-form-item label="委托方"><el-input v-model="form.consignor" /></el-form-item>
        <el-form-item label="估价"><el-input-number v-model="form.estimate" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="priceDialog" title="改价" width="360px">
      <p>当前：{{ priceTarget?.name }}（{{ priceTarget?.status }}）</p>
      <el-input-number v-model="newEstimate" :min="1" />
      <template #footer>
        <el-button @click="priceDialog = false">取消</el-button>
        <el-button type="primary" @click="doChangePrice">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.groups { background: #fff; padding: 8px 12px; border-radius: 10px; }
</style>
