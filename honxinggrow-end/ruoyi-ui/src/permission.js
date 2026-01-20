import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          
          // 检查是否为学生角色，如果是则重定向到特殊页面
          const roles = store.getters.roles
          const isStudent = roles.some(role => role === 'student' || role === 'ROLE_STUDENT')
          
          if (isStudent) {
            // 学生角色直接重定向到脚本小子页面
            next({ path: '/hx/script-kiddo', replace: true })
          } else {
            // 非学生角色，如果当前尝试访问特殊页面，则重定向到首页
            if (to.path === '/hx/script-kiddo') {
              next({ path: '/', replace: true })
            } else {
              store.dispatch('GenerateRoutes').then(accessRoutes => {
                // 根据roles权限生成可访问的路由表
                router.addRoutes(accessRoutes) // 动态添加可访问路由表
                next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
              })
            }
          }
        }).catch(err => {
            store.dispatch('LogOut').then(() => {
              Message.error(err)
              next({ path: '/' })
            })
          })
      } else {
        // 已经获取了用户角色，检查是否为学生角色
        const roles = store.getters.roles
        const isStudent = roles.some(role => role === 'student' || role === 'ROLE_STUDENT')
        
        // 如果是学生角色且访问的不是特殊页面，则重定向到特殊页面
        if (isStudent && to.path !== '/hx/script-kiddo') {
          next({ path: '/hx/script-kiddo', replace: true })
        } 
        // 如果非学生角色尝试访问特殊页面，则重定向到首页
        else if (!isStudent && to.path === '/hx/script-kiddo') {
          next({ path: '/', replace: true })
        } 
        // 其他情况正常访问
        else {
          next()
        }
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
