<template>
  <div id="app">
    <router-view />
    <theme-picker />
  </div>
</template>

<script>
import ThemePicker from "@/components/ThemePicker"

export default {
  name: "App",
  components: { ThemePicker },
  computed: {
    isDark() {
      return this.$store.state.settings.isDark
    }
  },
  watch: {
    isDark: {
      handler(val) {
        if (val) {
          document.body.classList.add('dark-mode')
        } else {
          document.body.classList.remove('dark-mode')
        }
      },
      immediate: true
    }
  },
  metaInfo() {
    return {
      title: this.$store.state.settings.dynamicTitle && this.$store.state.settings.title,
      titleTemplate: title => {
        return title ? `${title} - ${process.env.VUE_APP_TITLE}` : process.env.VUE_APP_TITLE
      }
    }
  }
}
</script>
<style scoped>
#app .theme-picker {
  display: none;
}
</style>
