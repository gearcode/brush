// pages/console/console.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    client: "",
    pass: "",
    connected: false,
    connectStatus: "连接中...",
    screenWidth: 1920,
    screenHeight: 1080,
    canvasWidth: 0,
    canvasHeight: 0,
    focusX: 0,
    focusY: 0,
    focusRectX: 0,
    focusRectY: 0,
    focusRectWidth: 0,
    focusRectHeight: 0,
    focusRectBorder: 2,
    scale: 0,
    inputFocus: false,
    fpsHidden: true,
    fpsFocus: false,
    fpsDelay: 1000,
    pullDelay: 1000
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that = this;
    wx.showLoading({
      title: "Connecting...",
      mask: true
    });

    this.setData({
      client: options.client,
      pass: options.pass,
      screenWidth: options.screenWidth,
      screenHeight: options.screenHeight
    });

    // 设置canvas大小
    var res = wx.getSystemInfoSync();
    this.setData({
      canvasWidth: res.screenWidth,
      canvasHeight: Math.floor(res.screenWidth * this.data.screenHeight / this.data.screenWidth)
    });

    // 设置focusRect大小
    var scale = this.data.canvasWidth / this.data.screenWidth;
    this.setData({
      scale: scale,
      focusRectWidth: Math.floor(this.data.canvasWidth * scale),
      focusRectHeight: Math.floor(this.data.canvasHeight * scale)
    });

    // 连接服务器WebSocket
    var socket = this.socket = wx.connectSocket({
      url: "wss://www.gearcode.com/brush-server/ws?client=" + this.data.client + "&token=" + this.data.pass + "&width=" + this.data.canvasWidth + "&height=" + this.data.canvasHeight,
      fail: function(e) {
        console.log("ERROR", e);
      }
    });

    socket.onOpen(function (e) {
      wx.hideLoading();
      console.log("WebSocket connection established! ", e);
      that.setData({
        "connected": true
      });
      // WebSocket连接成功, 开始刷新canvas
      that.pull();
    });
    socket.onError(function (e) {
      wx.hideLoading();
      that.closeSocket();
      console.log("WebSocket connection ERROR! ", e);
      that.setData({
        "connectStatus": "连接失败"
      });
    });
    socket.onMessage(function(res){
      console.log("WebSocket receive bytes, length: ", res.data.byteLength);

      that.drawScreen(res.data);
      // 持续更新screen
      that.pullHandler = setTimeout(function() { that.pull(); }, that.data.pullDelay);
    });
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {
    this.closeSocket();
    this.setData({
      "connected": false,
      "connectStatus": "已断开连接"
    });
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {
    this.closeSocket();
    this.setData({
      "connected": false,
      "connectStatus": "已断开连接"
    });
  },

  closeSocket: function() {
    console.log("Close Socket");
    clearTimeout(this.pullHandler);
    if (this.socket) {
      this.socket.close({
        success: function() {
          console.log("WebSocket connection closed! ");
        }
      });
    }
  },
  pull: function() {
    clearTimeout(this.pullHandler);
    console.log("FETCH_SCREEN," + this.data.canvasWidth + "," + this.data.canvasHeight + "," + this.data.focusX + "," + this.data.focusY);
    this.socket.send({
      data: "FETCH_SCREEN," + this.data.canvasWidth + "," + this.data.canvasHeight + "," + this.data.focusX + "," + this.data.focusY
    });
  },

  drawScreen: function (data) {

    var view = new DataView(data);
    var thumbSize = view.getInt32(0);
    var focusSize = view.getInt32(4 + thumbSize);
    console.log("thumbSize:", thumbSize, "focusSize: ", focusSize);

    const thumbBase64 = wx.arrayBufferToBase64(data.slice(4, 4 + thumbSize));
    const focusBase64 = wx.arrayBufferToBase64(data.slice(8 + thumbSize));
    this.setData({
      "thumbBase64": thumbBase64,
      "focusBase64": focusBase64
    });

  },

  focusOn: function(e) {
    console.log(e);
    var x = e.detail.x - e.currentTarget.offsetLeft, 
        y = e.detail.y - e.currentTarget.offsetTop,
        canvas_w = this.data.canvasWidth,
        canvas_h = this.data.canvasHeight,
        border = this.data.focusRectBorder * 2;
    // 计算Client的点击位置
    var client_x = Math.floor( x * this.data.screenWidth / this.data.canvasWidth ),
        client_y = Math.floor( y * this.data.screenHeight / this.data.canvasHeight );
    // 计算Rect的位置
    var rect_w = this.data.focusRectWidth,
        rect_h = this.data.focusRectHeight;
    var rect_x = x - rect_w / 2,
        rect_y = y - rect_h / 2;

    this.setData({
      focusX: client_x,
      focusY: client_y,
      focusRectX: rect_x < 0 ? 0 : (rect_x + rect_w + border > canvas_w ? canvas_w - rect_w - border : rect_x),
      focusRectY: rect_y < 0 ? 0 : (rect_y + rect_h + border > canvas_h ? canvas_h - rect_h - border : rect_y)
    });
    console.log("Click canvas: ", x, y, client_x, client_y);
  },

  consoleClick: function(e) {
    var x = e.detail.x - e.currentTarget.offsetLeft,
        y = e.detail.y - e.currentTarget.offsetTop,
        click = "";
    if(e.type == "tap") {
      click = "CLICK,1";
    }
    if(e.type == "longpress") {
      click = "CLICK,3";
    }
    var socketData = click + "," + this.data.canvasWidth + "," + this.data.canvasHeight + "," + this.data.focusX + "," + this.data.focusY + "," + x + "," + y;
    this.socket.send({
      data: socketData
    });
    console.log("Click: ", socketData);
  },

  pullKeyboard: function (e) {
    console.log("Show keyboard");
    this.setData({
      inputFocus: true
    });
  },

  keyboardInput: function (e) {
    console.log("User input:", e);
    const val = e.detail.value;

    // 防止死循环输入
    if(this.data.prevInputTime) {
      if (e.timeStamp - this.data.prevInputTime < 200 && val[0] == this.data.prevInputValue[this.data.prevInputValue.length-1]) {
        console.log("Omit input: ", val);
        return "";
      }
    }
    this.setData({
      prevInputValue: e.detail.value,
      prevInputTime: e.timeStamp
    });

    const ascii = /^[ -~]+$/;
    // 输入法输入中, 暂时忽略
    if (val.length != e.detail.cursor) {
      return "";
    }

    // 过滤非ASCII字符
    if (!ascii.test(val)) {
      console.log("非ASCII字符: ", val);
      return "";
    }

    // 发送至服务器
    var asciiString = val.toUpperCase().split("").map(function (item, index, array) {
      return item.charCodeAt();
    }).join(",");
    var socketData = "INPUT," + asciiString;
    this.socket.send({
      data: socketData
    });
    console.log("Input: ", socketData);

    return "";
  },

  keyboardButton: function (e) {
    var socketData = "INPUT," + e.target.dataset.code;
    this.socket.send({
      data: socketData
    });
    console.log("Input: ", socketData);
  },

  fpsShow: function (e) {
    this.setData({
      fpsHidden: false,
      fpsFocus: true
    });
  },
  fpsCancel: function (e) {
    this.setData({
      fpsHidden: true
    });
  },
  fpsConfirm: function (e) {
    this.setData({
      pullDelay: this.data.fpsDelay,
      fpsHidden: true
    });
    this.pull();
  },
  fpsInput: function (e) {
    this.setData({
      fpsDelay: e.detail.value
    });
  }
})