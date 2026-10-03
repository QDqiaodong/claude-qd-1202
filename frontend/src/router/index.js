import { createRouter, createWebHashHistory } from 'vue-router'

// 注意：main.js 里没有 app.use(router)，路由实例由 App.vue 直接驱动，
// 所以这里全部用静态 import —— <component :is> 拿到的是组件对象本身。
import ConsignmentItemView from '../views/ConsignmentItemView.vue'
import AppraisalView from '../views/AppraisalView.vue'
import StatusFlowView from '../views/StatusFlowView.vue'
import SettlementView from '../views/SettlementView.vue'

const routes = [
  { path: '/', redirect: '/items' },
  { path: '/items', name: 'items', component: ConsignmentItemView },
  { path: '/appraisals', name: 'appraisals', component: AppraisalView },
  { path: '/flow', name: 'flow', component: StatusFlowView },
  { path: '/settlement', name: 'settlement', component: SettlementView },
  { path: '/:pathMatch(.*)*', redirect: '/items' }
]

export default createRouter({
  history: createWebHashHistory(),
  routes
})
