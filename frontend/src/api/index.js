import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

async function call(promise) {
  const response = await promise
  const body = response.data
  if (!body || body.ok === false) {
    throw new Error((body && body.message) || '请求失败')
  }
  return body.data
}

const params = (o) => ({ params: o })

export default {
  // ---------------- 寄卖品台账 ----------------
  listItems: (category, status) => call(http.get('/items', params(
    category ? { category, ...(status ? { status } : {}) }
             : (status ? { status } : {})))),
  createItem: (d) => call(http.post('/items', d)),
  updateItem: (id, d) => call(http.put(`/items/${id}`, d)),

  // ---------------- 估价记录 ----------------
  listAppraisals: (itemId, status) => call(http.get('/appraisals', params(
    itemId ? { itemId, ...(status ? { status } : {}) }
           : (status ? { status } : {})))),
  createAppraisal: (d) => call(http.post('/appraisals', d)),
  completeAppraisal: (id) => call(http.post(`/appraisals/${id}/complete`)),

  // ---------------- 寄卖状态流转 ----------------
  listFlowHistory: (itemId) => call(http.get('/flow/history', params(itemId ? { itemId } : {}))),
  transitionItem: (id, to, operator, remark) =>
    call(http.post(`/flow/items/${id}/transition`, { to, operator, remark })),

  // ---------------- 买家与结算 ----------------
  listBuyers: () => call(http.get('/settlement/buyers')),
  createBuyer: (d) => call(http.post('/settlement/buyers', d)),
  listDeals: (settlementStatus) => call(http.get('/settlement/deals', params(
    settlementStatus ? { settlementStatus } : {}))),
  createDeal: (d) => call(http.post('/settlement/deals', d)),
  settleDeal: (id) => call(http.post(`/settlement/deals/${id}/settle`))
}
