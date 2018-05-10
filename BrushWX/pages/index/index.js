//index.js
//获取应用实例
const app = getApp()

Page({
  data: {
    motto: '点击按钮建立远程桌面连接',
    clientArr: [],
    connectDialogHidden: true,
    connectPassFocus: false,
    connectPass: "",
    connectClientId: "",
    screenWidth: 1920,
    screenHeight: 1080
  },
  onLoad: function () {

  },

  onShow: function () {
    this.loadClients();
  },

  getUserInfo: function(e) {
    console.log(e)
    app.globalData.userInfo = e.detail.userInfo
    this.setData({
      userInfo: e.detail.userInfo,
      hasUserInfo: true
    })
  },

  connectToClient: function(e) {
    this.setData({
      connectDialogHidden: false,
      connectPassFocus: true,
      connectClientId: e.target.dataset.clientId,
      screenWidth: e.target.dataset.clientWidth,
      screenHeight: e.target.dataset.clientHeight
    });
  },

  connectConfirm: function(e) {
    wx.navigateTo({
      url: "../console/console?client=" + this.data.connectClientId + "&pass=" + this.data.connectPass + "&screenWidth=" + this.data.screenWidth + "&screenHeight=" + this.data.screenHeight
    })
    this.setData({
      connectDialogHidden: true,
      connectPass: ""
    });
  },
  connectCancel: function(e) {
    this.setData({
      connectDialogHidden: true,
      connectPass: ""
    });
  },
  connectPassInput: function(e) {
    this.setData({
      connectPass: e.detail.value
    });
  },

  loadClients: function() {
    var that = this;
    wx.showLoading({
      title: "获取Client中",
      mask: true
    });
    wx.request({
      url: 'https://www.gearcode.com/brush-server/clients',
      header: {
        'content-type': 'application/json' // 默认值
      },
      success: function (res) {
        wx.hideLoading();
        if (res.statusCode == 200) {
          that.setData({
            clientArr: res.data
          });
        } else {
          that.loadClientsFail();
        }
      },
      fail: function() {
        that.loadClientsFail();
      }
    });
  },

  loadClientsFail: function() {
    wx.showToast({
      title: "获取Client失败",
      icon: "none",
      mask: true
    })
  }
})
