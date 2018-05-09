//index.js
//获取应用实例
const app = getApp()

Page({
  data: {
    motto: '点击下面的按钮进行连接',
    userInfo: {},
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo'),
    clientArr: [],
    connectDialogHidden: true,
    connectPassFocus: false,
    connectPass: "",
    connectClientId: "",
    screenWidth: 1920,
    screenHeight: 1080
  },
  onLoad: function () {
    if (app.globalData.userInfo) {
      this.setData({
        userInfo: app.globalData.userInfo,
        hasUserInfo: true
      })
    } else if (this.data.canIUse){
      // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
      // 所以此处加入 callback 以防止这种情况
      app.userInfoReadyCallback = res => {
        this.setData({
          userInfo: res.userInfo,
          hasUserInfo: true
        })
      }
    } else {
      // 在没有 open-type=getUserInfo 版本的兼容处理
      wx.getUserInfo({
        success: res => {
          app.globalData.userInfo = res.userInfo
          this.setData({
            userInfo: res.userInfo,
            hasUserInfo: true
          })
        }
      })
    }

  },

  onShow: function () {
    this.loadClients();
  },

  //事件处理函数
  bindViewTap: function () {
    wx.navigateTo({
      url: '../logs/logs'
    })
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
    console.log(e);
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
