Component({
  data: {
    selected: 0,
    showRedDot: false, // 是否显示红点
    color: "#999999",
    selectedColor: "#ff6700",
    list: [
      {
        "pagePath": "/pages/index/index",
        "text": "故事",
        "iconPath": "/images/icon_activity.png",
        "selectedIconPath": "/images/icon_activity_selected.png"
      },
      {
        "pagePath": "/pages/activity/activity",
        "text": "活动",
        "iconPath": "/images/icon_around.png",
        "selectedIconPath": "/images/icon_around_selected.png"
      },
      {
        "pagePath": "/pages/qa/qa",
        "text": "问答",
        "iconPath": "/images/icon_story.png",
        "selectedIconPath": "/images/icon_story_selected.png"
      },
      {
        "pagePath": "/pages/mine/mine",
        "text": "我的",
        "iconPath": "/images/icon_mine.png",
        "selectedIconPath": "/images/icon_mine_selected.png"
      }
    ]
  },
  lifetimes: {
    attached() {
      // 检查未读消息红点
      const app = getApp()
      if (app && app.globalData) {
        this.setData({ showRedDot: app.globalData.unreadCount > 0 })
      }

      // 这里的 this.data.list 获取可能为空或者需要从 data 获取
      const list = this.data.list
      const pages = getCurrentPages()
      if (pages.length > 0) {
        const page = pages[pages.length - 1]
        const route = page.route
        // 这里的 route 前面通常不带 /，而 list 里的 pagePath 带 /，需要处理一下
        // 小程序 route: "pages/index/index"
        // list pagePath: "/pages/index/index"
        const index = list.findIndex(item => item.pagePath === `/${route}` || item.pagePath === route)
        
        // 即使 index 是 0，也要确保设置一次，防止组件复用状态残留
        // 如果是首次加载，这里会正确设置高亮
        if (index !== -1) {
          // 使用同步更新，尽可能快地设置状态
          this.setData({
            selected: index
          }, () => {
             // 确保在视图更新后，强制重绘一次（虽然 setData 应该触发重绘，但加上回调更保险）
          })
        }
      }
    }
  },
  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset
      const url = data.path
      // 如果点击的是当前已选中的 tab，不进行任何操作，避免重复动画或刷新
      if (this.data.selected === data.index) return

      getApp().globalData.isTabSwitch = true
      wx.switchTab({url})
      // 不要在这里 setData，会让新页面渲染前先看到旧状态或者造成状态不一致
      // 依赖页面 onShow 或者组件 attached 自动修正
    }
  }
})