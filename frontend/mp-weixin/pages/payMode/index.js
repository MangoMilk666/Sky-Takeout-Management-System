/**
 * 测试支付模式选择页
 *
 * 接收参数（通过 URL query string）：
 *   orderId      - 订单 ID
 *   orderNumber  - 商户订单号（用于调用支付接口）
 *   orderAmount  - 订单金额（展示用）
 *
 * 使用方法：在正式 pay 页面底部点击"测试人员：模拟支付"链接跳转至此页
 */

// 与 utils/env.js 中 baseUrl 保持一致，修改时同步更新
var BASE_URL = 'http://localhost:8080';

/**
 * 获取登录 token
 * 优先从 uni-app Vuex store 读取，读不到时回退到 Storage
 */
function getToken() {
  try {
    var store = getApp().$vm.$store;
    if (store && store.state && store.state.token) {
      return store.state.token;
    }
  } catch (e) { /* ignore */ }
  return wx.getStorageSync('token') || '';
}

/**
 * 统一请求封装
 */
function request(url, method, data, success, fail) {
  wx.request({
    url: BASE_URL + url,
    method: method,
    data: data,
    header: {
      'Content-Type': 'application/json',
      'authentication': getToken()
    },
    success: function (res) {
      success && success(res.data);
    },
    fail: function (err) {
      fail && fail(err);
    }
  });
}

Page({
  data: {
    orderId: null,
    orderNumber: '',
    orderAmount: '',
    loading: false
  },

  onLoad: function (options) {
    this.setData({
      orderId: options.orderId || null,
      orderNumber: options.orderNumber || '',
      orderAmount: options.orderAmount || ''
    });
  },

  /**
   * 正式微信支付
   * 调用 PUT /user/order/payment → 获取预支付参数 → wx.requestPayment
   */
  handleRealPay: function () {
    var self = this;
    if (self.data.loading) return;

    self.setData({ loading: true });

    request(
      '/user/order/payment',
      'PUT',
      { orderNumber: self.data.orderNumber, payMethod: 1 },
      function (res) {
        self.setData({ loading: false });
        if (res.code === 1 && res.data) {
          var data = res.data;
          wx.requestPayment({
            nonceStr: data.nonceStr,
            package: data.packageStr,
            paySign: data.paySign,
            timeStamp: data.timeStamp,
            signType: data.signType || 'RSA',
            success: function () {
              wx.redirectTo({
                url: '/pages/success/index?orderId=' + self.data.orderId
              });
            },
            fail: function () {
              wx.showToast({ title: '支付取消或失败', icon: 'none', duration: 2000 });
            }
          });
        } else {
          wx.showModal({
            title: '下单失败',
            content: res.msg || '请稍后重试',
            showCancel: false
          });
        }
      },
      function () {
        self.setData({ loading: false });
        wx.showToast({ title: '网络错误，请重试', icon: 'none' });
      }
    );
  },

  /**
   * 测试模式：跳过微信支付
   * 调用 PUT /user/order/payment-skip → 直接标记订单已支付
   */
  handleTestPay: function () {
    var self = this;
    if (self.data.loading) return;

    wx.showModal({
      title: '测试模式确认',
      content: '将跳过微信支付，直接标记订单为已支付状态。此操作不产生实际扣款，仅供测试使用。',
      confirmText: '确认跳过',
      cancelText: '取消',
      success: function (modal) {
        if (!modal.confirm) return;

        self.setData({ loading: true });

        request(
          '/user/order/payment-skip',
          'PUT',
          { orderNumber: self.data.orderNumber, payMethod: 1 },
          function (res) {
            self.setData({ loading: false });
            if (res.code === 1) {
              wx.showToast({ title: '测试支付成功', icon: 'success', duration: 1500 });
              setTimeout(function () {
                wx.redirectTo({
                  url: '/pages/success/index?orderId=' + self.data.orderId
                });
              }, 1500);
            } else {
              wx.showModal({
                title: '操作失败',
                content: res.msg || '请稍后重试',
                showCancel: false
              });
            }
          },
          function () {
            self.setData({ loading: false });
            wx.showToast({ title: '网络错误，请重试', icon: 'none' });
          }
        );
      }
    });
  }
});
