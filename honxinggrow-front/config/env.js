const env = {
  dev: {
    apiBaseUrl: 'https://onecc.top/prod-api',
    useCloud: false
  },
  trial: {
    apiBaseUrl: 'https://onecc.top/prod-api',
    useCloud: false
  },
  prod: {
    apiBaseUrl: 'https://onecc.top/prod-api',
    useCloud: false
  }
}

const currentEnv = 'trial'  // 默认使用体验版环境

function getEnvConfig() {
  return env[currentEnv] || env.prod
}

function setEnv(envName) {
  if (env[envName]) {
    currentEnv = envName
    return true
  }
  return false
}

module.exports = {
  env,
  currentEnv,
  getEnvConfig,
  setEnv
}
