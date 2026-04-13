const BASE_URL = 'http://localhost:8080'

function getAuthToken() {
  const app = getApp ? getApp() : null
  const tokenFromStore = app && app.$vm && app.$vm.$store && app.$vm.$store.state ? app.$vm.$store.state.token : ''
  const tokenFromStorage = wx.getStorageSync('token') || wx.getStorageSync('authentication') || ''
  return tokenFromStore || tokenFromStorage || ''
}

function request({ url, method = 'GET', data }) {
  const authentication = getAuthToken()
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        authentication,
      },
      success(res) {
        resolve(res.data)
      },
      fail(err) {
        reject(err)
      },
    })
  })
}

Page({
  data: {
    loading: false,
    coupons: [],
  },
  onLoad() {
    this.fetchList()
  },
  onPullDownRefresh() {
    this.fetchList().finally(() => wx.stopPullDownRefresh())
  },
  goBack() {
    wx.navigateBack({ delta: 1 })
  },
  async fetchList() {
    this.setData({ loading: true })
    try {
      const res = await request({ url: '/user/coupon/available', method: 'GET' })
      if (String(res && res.code) === '1') {
        const list = Array.isArray(res.data) ? res.data : []
        const mapped = list.map((c) => {
          const discountType = Number(c.discountType)
          const discount = Number(c.discount)
          const typeText = discountType === 1 ? '折扣' : '满减'
          const discountText =
            discountType === 1
              ? `${String((discount * 10).toFixed(1)).replace(/\.0$/, '')}折`
              : `￥${discount}`
          return {
            ...c,
            discountType,
            typeText,
            discountText,
          }
        })
        this.setData({ coupons: mapped })
      } else {
        wx.showToast({ title: (res && res.msg) || '加载失败', icon: 'none' })
      }
    } catch (e) {
      wx.showToast({ title: '网络异常', icon: 'none' })
    } finally {
      this.setData({ loading: false })
    }
  },
  async handleClaim(e) {
    const couponId = e.currentTarget.dataset.id
    if (!couponId) return
    wx.showLoading({ title: '领取中...' })
    try {
      const requestId = `${Date.now()}_${Math.random().toString(16).slice(2)}`
      const res = await request({
        url: '/user/coupon/claim',
        method: 'POST',
        data: { couponId, requestId },
      })
      if (String(res && res.code) === '1') {
        wx.showToast({ title: '领取成功', icon: 'success' })
        await this.fetchList()
      } else {
        wx.showToast({ title: (res && res.msg) || '领取失败', icon: 'none' })
      }
    } catch (e2) {
      wx.showToast({ title: '网络异常', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },
})
