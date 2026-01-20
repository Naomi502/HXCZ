import defaultSettings from '@/settings'
import { useDynamicTitle } from '@/utils/dynamicTitle'

const { sideTheme, showSettings, topNav, tagsView, tagsIcon, fixedHeader, sidebarLogo, dynamicTitle, footerVisible, footerContent } = defaultSettings

const storageSetting = JSON.parse(localStorage.getItem('layout-setting')) || ''
const state = {
  title: '',
  theme: storageSetting.theme || '#409EFF',
  isDark: storageSetting.isDark || false,
  sideTheme: storageSetting.sideTheme || sideTheme,
  showSettings: showSettings,
  topNav: storageSetting.topNav === undefined ? topNav : storageSetting.topNav,
  tagsView: storageSetting.tagsView === undefined ? tagsView : storageSetting.tagsView,
  tagsIcon: storageSetting.tagsIcon === undefined ? tagsIcon : storageSetting.tagsIcon,
  fixedHeader: storageSetting.fixedHeader === undefined ? fixedHeader : storageSetting.fixedHeader,
  sidebarLogo: storageSetting.sidebarLogo === undefined ? sidebarLogo : storageSetting.sidebarLogo,
  dynamicTitle: storageSetting.dynamicTitle === undefined ? dynamicTitle : storageSetting.dynamicTitle,
  footerVisible: storageSetting.footerVisible === undefined ? footerVisible : storageSetting.footerVisible,
  footerContent: footerContent
}
const mutations = {
  CHANGE_SETTING: (state, { key, value }) => {
    if (state.hasOwnProperty(key)) {
      state[key] = value
    }
  },
  TOGGLE_DARK: (state) => {
    state.isDark = !state.isDark
    // update localStorage
    let layoutSetting = JSON.parse(localStorage.getItem('layout-setting')) || {}
    layoutSetting.isDark = state.isDark
    
    // 同步切换侧边栏主题
    if (state.isDark) {
        state.sideTheme = 'theme-dark'
        layoutSetting.sideTheme = 'theme-dark'
    } else {
        // 如果原本是浅色模式，切回时可以恢复之前的侧边栏设置，或者默认为 light/dark
        // 这里简单处理，切回浅色模式时，默认侧边栏也用深色（若依默认风格），或者保持用户选择
        // 如果想强制一致，可以在这里设置为 theme-light
    }
    
    localStorage.setItem('layout-setting', JSON.stringify(layoutSetting))
  }
}

const actions = {
  // 修改布局设置
  changeSetting({ commit }, data) {
    commit('CHANGE_SETTING', data)
  },
  toggleDark({ commit }) {
    commit('TOGGLE_DARK')
  },
  // 设置网页标题
  setTitle({ commit }, title) {
    state.title = title
    useDynamicTitle()
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

