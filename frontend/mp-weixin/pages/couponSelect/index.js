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
  goCenter() {
    wx.navigateTo({ url: '/pages/couponCenter/index' })
  },
  async fetchList() {
    this.setData({ loading: true })
    try {
      const res = await request({ url: '/user/coupon/list', method: 'GET' })
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
  handlePick(e) {
    const couponId = e.currentTarget.dataset.id
    if (!couponId) return
    wx.setStorageSync('selected_coupon_id', couponId)
    wx.showToast({ title: '已选择', icon: 'success' })
    setTimeout(() => {
      wx.navigateBack({ delta: 1 })
    }, 300)
  },
})
